package eu.zhincore.chestnetworks.networks;

import java.util.List;
import org.bukkit.Location;

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

  static enum ChestType {
    STORAGE, INPUT
  }
}
