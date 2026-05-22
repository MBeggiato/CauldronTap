package com.cauldrontap;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.papermc.paper.event.block.BlockPreDispenseEvent;
import java.util.Map;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Lets dispensers fill empty buckets from lava or water cauldrons they face.
 *
 * <p>Inventory changes run on the next server tick. Use {@link Container#getSnapshotInventory()}
 * before {@link Dispenser#update(boolean)}. Calling {@code update()} after editing
 * {@link Container#getInventory()} restores the old snapshot and discards changes.
 */
@SuppressFBWarnings(
    value = "EI_EXPOSE_REP2",
    justification = "Bukkit plugin reference for scheduled tasks")
public final class DispenserCauldronListener implements Listener {

  private final CauldronTap plugin;
  private final DebugLog debug;
  private final CauldronTapConfig config;

  /** Creates a listener tied to the plugin instance and debug logger. */
  public DispenserCauldronListener(CauldronTap plugin, DebugLog debug) {
    this.plugin = plugin;
    this.debug = debug;
    this.config = new CauldronTapConfig(plugin);
  }

  /** Handles dispenser activation when an empty bucket targets a filled cauldron. */
  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPreDispense(BlockPreDispenseEvent event) {
    ItemStack toDispense = event.getItemStack();
    if (toDispense.getType() != Material.BUCKET) {
      return;
    }

    Block dispenserBlock = event.getBlock();
    if (dispenserBlock.getType() != Material.DISPENSER) {
      return;
    }

    BlockFace facing = ((Directional) dispenserBlock.getBlockData()).getFacing();
    Block targetBlock = dispenserBlock.getRelative(facing);
    Optional<CauldronFill> fillType = CauldronFill.fromCauldron(targetBlock.getType());
    if (fillType.isEmpty()) {
      return;
    }

    event.setCancelled(true);

    Location dispenserLocation = dispenserBlock.getLocation();
    Location cauldronLocation = targetBlock.getLocation();
    int slot = event.getSlot();
    CauldronFill cauldronFill = fillType.get();

    debug.log("Intercepted bucket dispense at " + dispenserLocation
        + " slot " + slot + " facing " + cauldronFill.name().toLowerCase()
        + " cauldron at " + cauldronLocation);

    new BukkitRunnable() {
      @Override
      public void run() {
        applyBucketFill(dispenserLocation, cauldronLocation, slot, cauldronFill);
      }
    }.runTask(plugin);
  }

  private void applyBucketFill(
      Location dispenserLocation, Location cauldronLocation, int slot, CauldronFill cauldronFill) {
    Block dispenserBlock = dispenserLocation.getBlock();
    if (dispenserBlock.getType() != Material.DISPENSER) {
      debug.log("Abort: block at " + dispenserLocation + " is no longer a dispenser");
      return;
    }

    Block cauldronBlock = cauldronLocation.getBlock();
    if (cauldronBlock.getType() != cauldronFill.getCauldronType()) {
      debug.log("Abort: block at " + cauldronLocation + " is no longer a "
          + cauldronFill.name().toLowerCase() + " cauldron (" + cauldronBlock.getType() + ")");
      return;
    }

    Dispenser dispenser = (Dispenser) dispenserBlock.getState();
    Inventory snapshot = dispenser.getSnapshotInventory();
    ItemStack bucketStack = snapshot.getItem(slot);

    if (bucketStack == null || bucketStack.getType() != Material.BUCKET) {
      debug.log("Abort: slot " + slot + " no longer contains an empty bucket ("
          + describeStack(bucketStack) + ")");
      return;
    }

    int amountBefore = bucketStack.getAmount();
    consumeOneEmptyBucket(snapshot, slot, bucketStack);

    ItemStack filledBucket = cauldronFill.createFilledBucket();
    FilledBucketOutput outputMode = config.getFilledBucketOutput();
    FillResult fillResult =
        routeFilledBucket(dispenserBlock, snapshot, slot, outputMode, filledBucket);

    cauldronBlock.setType(Material.CAULDRON);
    boolean updated = dispenser.update(true);

    ItemStack worldSlot = ((Dispenser) dispenserBlock.getState()).getInventory().getItem(slot);
    debug.log("Applied " + cauldronFill.name().toLowerCase() + " fill at " + dispenserLocation
        + ": slot=" + slot
        + ", bucketsBefore=" + amountBefore
        + ", outputMode=" + outputMode
        + ", fillResult=" + fillResult
        + ", snapshotSlotAfter=" + describeStack(snapshot.getItem(slot))
        + ", worldSlotAfter=" + describeStack(worldSlot)
        + ", dispenserUpdate=" + updated
        + ", cauldron emptied at " + cauldronLocation);
  }

  private FillResult routeFilledBucket(
      Block dispenserBlock,
      Inventory dispenserSnapshot,
      int slot,
      FilledBucketOutput outputMode,
      ItemStack filledBucket) {
    if (outputMode == FilledBucketOutput.CHEST) {
      ChestOutputPosition chestPosition = config.getChestOutputPosition();
      FillResult chestResult = tryPlaceInChestAt(dispenserBlock, filledBucket, chestPosition);
      if (chestResult != null) {
        return chestResult;
      }
      BlockFace offset = chestPosition.toBlockFace(
          (Directional) dispenserBlock.getBlockData());
      debug.log("Chest output unavailable or full at "
          + dispenserBlock.getRelative(offset).getLocation()
          + " (" + chestPosition + "), falling back to dispenser inventory");
    }

    return placeInDispenserInventory(
        dispenserSnapshot, slot, filledBucket, dispenserBlock.getLocation());
  }

  /**
   * Tries to add the filled bucket to a chest at the configured offset from the dispenser.
   *
   * @return result when placement succeeded, or {@code null} to fall back to inventory output
   */
  private FillResult tryPlaceInChestAt(
      Block dispenserBlock, ItemStack filledBucket, ChestOutputPosition position) {
    BlockFace offset = position.toBlockFace((Directional) dispenserBlock.getBlockData());
    Block chestBlock = dispenserBlock.getRelative(offset);
    if (!isChestBlock(chestBlock)) {
      return null;
    }

    if (!(chestBlock.getState() instanceof Chest chest)) {
      return null;
    }

    Inventory chestSnapshot = chest.getSnapshotInventory();
    Map<Integer, ItemStack> overflow = chestSnapshot.addItem(filledBucket.clone());
    if (!overflow.isEmpty()) {
      return null;
    }

    chest.update(true);
    debug.log("Placed " + filledBucket.getType().name().toLowerCase()
        + " in chest at " + chestBlock.getLocation()
        + " (" + position + " of dispenser)");
    return FillResult.CHEST;
  }

  private static boolean isChestBlock(Block block) {
    Material type = block.getType();
    return type == Material.CHEST || type == Material.TRAPPED_CHEST;
  }

  private FillResult placeInDispenserInventory(
      Inventory snapshot, int slot, ItemStack filledBucket, Location dispenserLocation) {
    if (snapshot.getItem(slot) == null) {
      snapshot.setItem(slot, filledBucket);
      return FillResult.REPLACED_IN_SLOT;
    }

    Map<Integer, ItemStack> overflow = snapshot.addItem(filledBucket);
    if (!overflow.isEmpty()) {
      dispenserLocation.getWorld().dropItemNaturally(
          dispenserLocation.clone().add(0.5, 0.5, 0.5),
          overflow.values().iterator().next());
      debug.log("Dispenser full; dropped " + filledBucket.getType().name().toLowerCase()
          + " at " + dispenserLocation);
      return FillResult.DROPPED_OVERFLOW;
    }
    return FillResult.ADDED_TO_OTHER_SLOT;
  }

  private static void consumeOneEmptyBucket(Inventory snapshot, int slot, ItemStack bucketStack) {
    if (bucketStack.getAmount() == 1) {
      snapshot.setItem(slot, null);
      return;
    }

    ItemStack remaining = bucketStack.clone();
    remaining.setAmount(remaining.getAmount() - 1);
    snapshot.setItem(slot, remaining);
  }

  private enum FillResult {
    REPLACED_IN_SLOT,
    ADDED_TO_OTHER_SLOT,
    DROPPED_OVERFLOW,
    CHEST
  }

  private static String describeStack(ItemStack stack) {
    if (stack == null) {
      return "empty";
    }
    return stack.getType().name() + " x" + stack.getAmount();
  }
}
