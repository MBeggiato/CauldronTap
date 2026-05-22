package com.cauldrontap;

import io.papermc.lib.PaperLib;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/** Main plugin class for CauldronTap. */
public class CauldronTap extends JavaPlugin {

  private DebugLog debugLog;

  @Override
  public void onEnable() {
    PaperLib.suggestPaper(this);
    saveDefaultConfig();
    debugLog = new DebugLog(this);
    getServer().getPluginManager().registerEvents(
        new DispenserCauldronListener(this, debugLog), this);
  }

  @Override
  public void reloadConfig() {
    super.reloadConfig();
    if (debugLog != null) {
      debugLog.reload();
    }
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!command.getName().equalsIgnoreCase("cauldrontap")) {
      return false;
    }
    if (!sender.hasPermission("cauldrontap.reload")) {
      sender.sendMessage("You do not have permission to reload CauldronTap.");
      return true;
    }
    reloadConfig();
    CauldronTapConfig config = new CauldronTapConfig(this);
    sender.sendMessage("CauldronTap config reloaded. Debug: " + debugLog.isEnabled()
        + ", filled-bucket-output: " + config.getFilledBucketOutput().name().toLowerCase()
        + ", chest-output-position: " + config.getChestOutputPosition().name().toLowerCase());
    return true;
  }
}
