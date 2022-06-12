package eu.zhincore.chestnetworks.networks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.inventory.Inventory;

public class ChestNetworkManager {
  public Map<UUID, ChestNetwork> networks = new HashMap<>();

  public boolean update(UUID playerId, String netName) {
    var network = networks.get(playerId);
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
