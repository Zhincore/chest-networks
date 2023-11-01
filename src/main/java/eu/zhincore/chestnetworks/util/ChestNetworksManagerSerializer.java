package eu.zhincore.chestnetworks.util;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.UUID;
import com.google.common.collect.HashBasedTable;
import com.google.gson.*;
import eu.zhincore.chestnetworks.ChestNetworksPlugin;
import eu.zhincore.chestnetworks.networks.ChestNetwork;
import eu.zhincore.chestnetworks.networks.ChestNetworksManager;

public class ChestNetworksManagerSerializer
    implements JsonSerializer<ChestNetworksManager>, JsonDeserializer<ChestNetworksManager> {
  ChestNetworksPlugin plugin;

  public ChestNetworksManagerSerializer(ChestNetworksPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public JsonElement serialize(ChestNetworksManager manager, Type typeOfSrc, JsonSerializationContext context) {
    return context.serialize(manager.networks.rowMap());
  }

  @Override
  public ChestNetworksManager deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
    HashMap<String, HashMap<String, JsonObject>> map = context.deserialize(json, HashMap.class);
    HashBasedTable<String, String, ChestNetwork> networks = HashBasedTable.create();

    for (var playerRow : map.entrySet()) {
      for (var networkCell : playerRow.getValue().entrySet()) {
        var owner = playerRow.getKey();
        var name = networkCell.getKey();

        var net = networks.put(owner, name, context.deserialize(networkCell.getValue(), ChestNetwork.class));
        net.name = name;
        net.owner = UUID.fromString(owner);
      }
    }

    var manager = new ChestNetworksManager(plugin);
    manager.networks = networks;
    return manager;
  }
}
