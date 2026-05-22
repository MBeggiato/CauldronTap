package com.cauldrontap;

import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/** Maps a filled cauldron type to the bucket item produced from it. */
public enum CauldronFill {

  LAVA(Material.LAVA_CAULDRON, Material.LAVA_BUCKET),
  WATER(Material.WATER_CAULDRON, Material.WATER_BUCKET);

  private final Material cauldronType;
  private final Material filledBucketType;

  CauldronFill(Material cauldronType, Material filledBucketType) {
    this.cauldronType = cauldronType;
    this.filledBucketType = filledBucketType;
  }

  /**
   * Returns the fill type for a cauldron block, if supported.
   *
   * @param cauldronType block material of the cauldron
   * @return matching fill type, or empty if not a supported filled cauldron
   */
  public static Optional<CauldronFill> fromCauldron(Material cauldronType) {
    for (CauldronFill fill : values()) {
      if (fill.cauldronType == cauldronType) {
        return Optional.of(fill);
      }
    }
    return Optional.empty();
  }

  /** Returns a single filled bucket item stack for this cauldron type. */
  public ItemStack createFilledBucket() {
    return new ItemStack(filledBucketType);
  }

  /** Returns the cauldron block material that triggers this fill. */
  public Material getCauldronType() {
    return cauldronType;
  }
}
