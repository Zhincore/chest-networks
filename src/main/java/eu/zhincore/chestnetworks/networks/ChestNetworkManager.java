package eu.zhincore.chestnetworks.networks;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableSet;
import eu.zhincore.chestnetworks.ChestNetworksPlugin;

public class ChestNetworkManager {
  private HashBasedTable<String, String, ChestNetwork> networks = HashBasedTable.create();
  private ChestNetworksPlugin plugin;

  public ChestNetworkManager(ChestNetworksPlugin plugin) {
    this.plugin = plugin;
  }

  public Set<String> listNetworks(UUID playerId) {
    try {
      if (networks.containsRow(playerId.toString())) return networks.row(playerId.toString()).keySet();
    } catch (java.lang.NullPointerException e) {
      // Ignored
    }
    return ImmutableSet.of();
  }

  public boolean create(UUID playerId, String netName) {
    var playerNets = networks.row(playerId.toString());
    if (playerNets.containsKey(netName)) return false;
    playerNets.put(netName, new ChestNetwork());
    plugin.database.scheduleSave();
    return true;
  }

  public ChestNetwork get(UUID playerId, String netName) {
    return get(playerId.toString(), netName);
  }

  public ChestNetwork get(String playerId, String netName) {
    return networks.get(playerId, netName);
  }

  public boolean delete(UUID playerId, String netName) {
    return networks.remove(playerId.toString(), netName) != null;
  }

  public boolean update(UUID playerId, String netName) {
    var network = networks.row(playerId.toString()).get(netName);
    if (network == null) return false;
    network.update();
    return true;
  }

  public void update(Inventory inventory) {
    var chest = getChestData(inventory);
    if (chest == null) return;

    chest.network.update(chest);
  }

  public NetworkChest getChestData(Inventory inventory) {
    return getChestData(inventory.getLocation());
  }

  public NetworkChest getChestData(Location location) {
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
