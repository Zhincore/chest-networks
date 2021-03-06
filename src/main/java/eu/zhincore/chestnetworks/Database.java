package eu.zhincore.chestnetworks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import org.bukkit.Location;
import com.google.common.collect.HashBasedTable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.zhincore.chestnetworks.networks.ChestNetworkManager;
import eu.zhincore.chestnetworks.util.BukkitLocationSerializer;
import eu.zhincore.chestnetworks.util.ChestNetworksPluginSerializer;
import eu.zhincore.chestnetworks.util.HashBasedTableSerializer;
import eu.zhincore.chestnetworks.util.SchedulableTask;

public class Database {
  private File file;
  private Gson gson;
  public ChestNetworksPlugin plugin;
  public ChestNetworkManager networkManager;
  private SchedulableTask saveTask;

  public Database(ChestNetworksPlugin plugin) {
    this.plugin = plugin;
    saveTask = new SchedulableTask(plugin, () -> this.save());
    file = new File(plugin.getDataFolder(), "data.json");
    gson = new GsonBuilder().enableComplexMapKeySerialization()
        .registerTypeAdapter(ChestNetworksPlugin.class, new ChestNetworksPluginSerializer(plugin))
        .registerTypeAdapter(HashBasedTable.class, new HashBasedTableSerializer())
        .registerTypeAdapter(Location.class, new BukkitLocationSerializer()).create();
  }

  public ChestNetworkManager load() {
    try {
      if (!file.getParentFile().exists()) {
        file.getParentFile().mkdirs();
      }
      if (file.exists()) {
        Reader reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
        return networkManager = gson.fromJson(reader, ChestNetworkManager.class);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return networkManager = new ChestNetworkManager(plugin);
  }

  public void scheduleSave() {
    saveTask.schedule();
  }

  public boolean save() {
    try {
      FileWriter fw = new FileWriter(file);
      fw.write(gson.toJson(networkManager));
      fw.flush();
      fw.close();

      return true;
    } catch (Exception ex) {
      ex.printStackTrace();
      return false;
    }
  }
}
