package eu.zhincore.chestnetworks;

import java.io.IOException;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.parser.ParseException;

public class ChestNetworksPlugin extends JavaPlugin {
  public ChestNetMessenger messenger;
  private ChestNetController networksController;

  @Override
  public void onEnable() {
    try {
      messenger = new ChestNetMessenger(getTextResource("messages.json"));
    } catch (IOException | ParseException e) {
      e.printStackTrace();
      getServer().getPluginManager().disablePlugin(this);
      return;
    }
    networksController = new ChestNetController(this);
    getCommand("chestnet").setExecutor(new CommandChestNet(this, networksController));
    getCommand("chestnet").setTabCompleter(new CommandChestNetTabCompleter(networksController));
    getServer().getPluginManager().registerEvents(new ChestNetListener(networksController), this);
  }

  @Override
  public void onDisable() {
    networksController = null;
  }
}
