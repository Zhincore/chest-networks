package eu.zhincore.chestnetworks;

import java.io.IOException;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;
import co.aikar.commands.PaperCommandManager;
import eu.zhincore.chestnetworks.util.ChestNetMessages;

public class ChestNetworksPlugin extends JavaPlugin {
  public Database database = new Database(this);
  public PaperCommandManager manager = new PaperCommandManager(this);
  public ChestNetMessages messages;

  @Override
  public void onEnable() {
    database.load();

    var locales = manager.getLocales();
    try {
      locales.loadYamlLanguageFile("lang_en.yml", Locale.ENGLISH);
    } catch (IOException | InvalidConfigurationException e) {
      e.printStackTrace();
      Bukkit.getPluginManager().disablePlugin(this);
      return;
    }
    messages = new ChestNetMessages(locales);

    manager.registerCommand(new ChestNetCommand());
  }

  @Override
  public void onDisable() {
  }
}
