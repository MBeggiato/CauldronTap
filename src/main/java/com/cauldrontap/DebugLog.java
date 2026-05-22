package com.cauldrontap;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Writes debug messages to the plugin logger and an optional log file. */
@SuppressFBWarnings(
    value = "EI_EXPOSE_REP2",
    justification = "Bukkit plugin reference for config and data folder")
public final class DebugLog {

  private final CauldronTap plugin;
  private final Logger logger;
  private final Path logFile;
  private volatile boolean enabled;

  /** Creates a debug logger backed by the plugin data folder. */
  public DebugLog(CauldronTap plugin) {
    this.plugin = plugin;
    this.logger = plugin.getLogger();
    this.logFile = plugin.getDataFolder().toPath().resolve("debug.log");
    reload();
  }

  /** Reloads the debug flag from config.yml. */
  public void reload() {
    enabled = new CauldronTapConfig(plugin).isDebugEnabled();
    if (enabled) {
      logger.info("CauldronTap debug logging enabled — writing to " + logFile);
      writeToFile("--- debug session started ---");
    }
  }

  /** Returns whether debug logging is active. */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Logs a debug message when debug mode is enabled.
   *
   * @param message message to record
   */
  public void log(String message) {
    if (!enabled) {
      return;
    }
    String line = "[" + Instant.now() + "] " + message;
    logger.log(Level.INFO, "[CauldronTap debug] " + message);
    writeToFile(line);
  }

  private void writeToFile(String line) {
    try {
      java.nio.file.Path parent = logFile.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
      Files.writeString(
          logFile,
          line + System.lineSeparator(),
          StandardCharsets.UTF_8,
          StandardOpenOption.CREATE,
          StandardOpenOption.APPEND);
    } catch (IOException exception) {
      logger.log(Level.WARNING, "Failed to write CauldronTap debug log: " + exception.getMessage());
    }
  }
}
