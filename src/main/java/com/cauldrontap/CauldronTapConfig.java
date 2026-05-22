package com.cauldrontap;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.logging.Logger;

/** Reads and validates CauldronTap configuration values. */
@SuppressFBWarnings(
    value = "EI_EXPOSE_REP2",
    justification = "Bukkit plugin reference for config access")
public final class CauldronTapConfig {

  private static final String KEY_DEBUG = "debug";
  private static final String KEY_FILLED_BUCKET_OUTPUT = "filled-bucket-output";
  private static final String KEY_CHEST_OUTPUT_POSITION = "chest-output-position";

  private final CauldronTap plugin;
  private final Logger logger;

  /** Creates a config wrapper for the given plugin instance. */
  public CauldronTapConfig(CauldronTap plugin) {
    this.plugin = plugin;
    this.logger = plugin.getLogger();
  }

  /** Returns whether debug logging is enabled. */
  public boolean isDebugEnabled() {
    return plugin.getConfig().getBoolean(KEY_DEBUG, false);
  }

  /** Returns where filled buckets should be routed. */
  public FilledBucketOutput getFilledBucketOutput() {
    String raw = plugin.getConfig().getString(KEY_FILLED_BUCKET_OUTPUT, "inventory");
    return FilledBucketOutput.parse(raw).orElseGet(() -> {
      logger.warning("Unknown filled-bucket-output '" + raw
          + "', using inventory. Valid values: inventory, chest");
      return FilledBucketOutput.INVENTORY;
    });
  }

  /** Returns where a chest should be placed relative to the dispenser when using chest output. */
  public ChestOutputPosition getChestOutputPosition() {
    String raw = plugin.getConfig().getString(KEY_CHEST_OUTPUT_POSITION, "back");
    return ChestOutputPosition.parse(raw).orElseGet(() -> {
      logger.warning("Unknown chest-output-position '" + raw
          + "', using back. Valid values: back, front, down, up, left, right");
      return ChestOutputPosition.BACK;
    });
  }
}
