package eu.zhincore.chestnetworks;

import java.io.IOException;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import com.google.common.collect.Lists;
import co.aikar.commands.MessageType;
import co.aikar.commands.PaperCommandManager;
import eu.zhincore.chestnetworks.networks.ChestNetworksManager;

public class ChestNetworksPlugin extends JavaPlugin {
  public Database database;
  public PaperCommandManager commandManager;

  @Override
  public void onEnable() {
    // Initialize chestnet database
    database = new Database(this);
    database.load();

    // Initialize ACF
    commandManager = new PaperCommandManager(this);
    // commandManager.enableUnstableAPI("help");
    commandManager.usePerIssuerLocale(false);
    commandManager.setFormat(MessageType.INFO, 1, ChatColor.GREEN);

    commandManager.registerDependency(PaperCommandManager.class, commandManager);
    commandManager.registerDependency(ChestNetworksManager.class, database.networkManager);

    // Initialize ACF locales
    var locales = commandManager.getLocales();
    locales.setDefaultLocale(Locale.ENGLISH);
    try {
      loadInternalYamlLanguageFile("lang_en.yml", Locale.ENGLISH);
    } catch (IOException | InvalidConfigurationException e) {
      e.printStackTrace();
      Bukkit.getPluginManager().disablePlugin(this);
      return;
    }

    // Initialize completions
    var completions = commandManager.getCommandCompletions();
    completions.registerCompletion("network", c -> {
      var networks = database.networkManager.listNetworks(c.getPlayer().getUniqueId());
      return Lists.newArrayList(networks);
    });

    // Initialize ACF commands
    commandManager.registerCommand(new ChestNetCommand());
  }

  public boolean loadInternalYamlLanguageFile(String name, Locale locale)
      throws IOException, InvalidConfigurationException {
    var yamlConfiguration = new YamlConfiguration();
    yamlConfiguration.load(getTextResource(name));
    return commandManager.getLocales().loadLanguage(yamlConfiguration, locale);
  }

  @Override
  public void onDisable() {
    database = null;
    commandManager = null;
  }
}
