package com.cauldrontap;

import java.util.Optional;

/** Where a filled lava bucket is stored after cauldron conversion. */
public enum FilledBucketOutput {

  /** Keep the lava bucket in the dispenser inventory. */
  INVENTORY("inventory"),

  /** Try the chest below the dispenser; fall back to inventory if unavailable or full. */
  CHEST("chest");

  private final String configKey;

  FilledBucketOutput(String configKey) {
    this.configKey = configKey;
  }

  /**
   * Parses a config value.
   *
   * @param value raw config string
   * @return matching output mode, or empty if the value is unknown
   */
  public static Optional<FilledBucketOutput> parse(String value) {
    if (value == null) {
      return Optional.empty();
    }
    String normalized = value.trim().toLowerCase();
    for (FilledBucketOutput output : values()) {
      if (output.configKey.equals(normalized)) {
        return Optional.of(output);
      }
    }
    return Optional.empty();
  }

  /**
   * Parses a config value, defaulting to {@link #INVENTORY} when unknown.
   *
   * @param value raw config string
   * @return matching output mode
   */
  public static FilledBucketOutput fromConfig(String value) {
    return parse(value).orElse(INVENTORY);
  }
}
