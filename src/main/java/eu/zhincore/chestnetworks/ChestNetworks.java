package eu.zhincore.chestnetworks;

import java.io.IOException;

import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.parser.ParseException;

public class ChestNetworks extends JavaPlugin {
  public Messenger messenger;
  private NetworksController networksController;

  @Override
  public void onEnable() {
    try {
      messenger = new Messenger(getTextResource("messages.json"));
    } catch (IOException | ParseException e) {
      e.printStackTrace();
      getServer().getPluginManager().disablePlugin(this);
      return;
    }
    networksController = new NetworksController(this);
    getCommand("chestnet").setExecutor(new CommandChestNet(this, networksController));
    getCommand("chestnet").setTabCompleter(new CommandChestNetTabCompleter(networksController));
    getServer().getPluginManager().registerEvents(new EventListener(networksController), this);
  }

  @Override
  public void onDisable() {
    networksController = null;
  }
}
