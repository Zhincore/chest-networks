package eu.zhincore.chestnetworks;

import org.bukkit.plugin.java.JavaPlugin;

public class ChestNetworksPlugin extends JavaPlugin {
  public Database database = new Database(this);

  @Override
  public void onEnable() {
    database.load();
  }

  @Override
  public void onDisable() {
  }
}
