package eu.zhincore.chestnetworks.util;

import java.lang.reflect.Type;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import com.google.gson.*;

public class BukkitLocationSerializer implements JsonSerializer<Location>, JsonDeserializer<Location> {
  @Override
  public JsonElement serialize(Location location, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject json = new JsonObject();
    json.addProperty("world", location.getWorld().getName());
    json.addProperty("x", location.getX());
    json.addProperty("y", location.getY());
    json.addProperty("z", location.getZ());
    json.addProperty("yaw", location.getYaw());
    json.addProperty("pitch", location.getPitch());
    return json;
  }

  @Override
  public Location deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
    var obj = json.getAsJsonObject();
    return new Location(Bukkit.getWorld(obj.get("world").getAsString()), obj.get("x").getAsDouble(),
        obj.get("y").getAsDouble(), obj.get("z").getAsDouble(), obj.get("yaw").getAsFloat(),
        obj.get("pitch").getAsFloat());
  }
}
