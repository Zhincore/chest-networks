package eu.zhincore.chestnetworks.util;

import java.lang.reflect.Type;
import com.google.gson.*;
import eu.zhincore.chestnetworks.ChestNetworksPlugin;

public class ChestNetworksPluginSerializer
    implements JsonSerializer<ChestNetworksPlugin>, JsonDeserializer<ChestNetworksPlugin> {
  ChestNetworksPlugin plugin;

  public ChestNetworksPluginSerializer(ChestNetworksPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public JsonElement serialize(ChestNetworksPlugin plugin, Type typeOfSrc, JsonSerializationContext context) {
    return new JsonPrimitive("ChestNetworks");
  }

  @Override
  public ChestNetworksPlugin deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
    return plugin;
  }
}
