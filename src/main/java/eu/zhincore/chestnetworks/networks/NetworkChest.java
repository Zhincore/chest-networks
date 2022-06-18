package eu.zhincore.chestnetworks.networks;

import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;

public class NetworkChest {
  public ChestType type;
  public Location location;
  public ChestNetwork network;
  public List<String> content;
  public int priority = 0;

  public NetworkChest(ChestType type, Location location, ChestNetwork network, List<String> content) {
    this.type = type;
    this.location = location;
    this.network = network;
    this.content = content;
  }

  public Inventory getInventory() {
    var block = location.getBlock();
    if (block.getType() != Material.CHEST) return null;

    var chest = (Chest) block.getState();
    return chest.getInventory();
  }

  static enum ChestType {
    STORAGE, INPUT
  }
}
