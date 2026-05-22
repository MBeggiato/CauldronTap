package com.lavatap;

import io.papermc.lib.PaperLib;
import org.bukkit.plugin.java.JavaPlugin;

/** Main plugin class for LavaTap. */
public class LavaTap extends JavaPlugin {

  @Override
  public void onEnable() {
    PaperLib.suggestPaper(this);
    saveDefaultConfig();
  }
}
