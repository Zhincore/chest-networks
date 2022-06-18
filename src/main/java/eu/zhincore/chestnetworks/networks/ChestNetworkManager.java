package eu.zhincore.chestnetworks.networks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.inventory.Inventory;
import com.google.common.collect.HashBasedTable;
import eu.zhincore.chestnetworks.ChestNetworksPlugin;

public class ChestNetworkManager {
  public HashBasedTable<UUID, String, ChestNetwork> networks = HashBasedTable.create();
  private ChestNetworksPlugin plugin;

  public ChestNetworkManager(ChestNetworksPlugin plugin) {
    this.plugin = plugin;
  }

  public boolean create(UUID playerId, String netName) {
    var playerNets = networks.row(playerId);
    if (playerNets.containsKey(netName)) return false;
    playerNets.put(netName, new ChestNetwork(playerId, netName));
    plugin.database.scheduleSave();
    return true;
  }

  public boolean update(UUID playerId, String netName) {
    var network = networks.row(playerId).get(netName);
    if (network == null) return false;
    network.update();
    return true;
  }

  public void update(Inventory inventory) {
    var chest = getChestData(inventory);
    if (chest == null) return;

    chest.network.update(chest);
  }

  private NetworkChest getChestData(Inventory inventory) {
    var location = inventory.getLocation();

    for (var network : networks.values()) {
      for (var chest : network.chests) {
        if (chest.location.equals(location)) return chest;
      }
    }

    return null;
  }

  public List<NetworkChest> getAllChests() {
    var chests = new ArrayList<NetworkChest>();

    for (var network : networks.values()) {
      chests.addAll(network.chests);
    }

    return chests;
  }
}
