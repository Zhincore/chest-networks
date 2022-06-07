package eu.zhincore.chestnetworks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Database {
  public JSONObject data;
  private File file;
  private JSONParser parser = new JSONParser();

  public Database(ChestNetworks plugin) {
    file = new File(plugin.getDataFolder(), "data.json");
    reload();
  }

  public void reload() {
    try {
      if (!file.getParentFile().exists()) {
        file.getParentFile().mkdirs();
      }
      if (!file.exists()) {
        PrintWriter pw = new PrintWriter(file, "UTF-8");
        pw.print("{");
        pw.print("}");
        pw.flush();
        pw.close();
      }
      data = (JSONObject) parser.parse(new InputStreamReader(new FileInputStream(file), "UTF-8"));
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public boolean save() {
    try {
      FileWriter fw = new FileWriter(file);
      fw.write(data.toString());
      fw.flush();
      fw.close();

      return true;
    } catch (Exception ex) {
      ex.printStackTrace();
      return false;
    }
  }

  public static Location jsonToLoc(JSONObject obj) {
    return new Location(Bukkit.getWorld(obj.get("world").toString()), Double.parseDouble(obj.get("x").toString()),
        Double.parseDouble(obj.get("y").toString()), Double.parseDouble(obj.get("z").toString()),
        Float.parseFloat(obj.get("yaw").toString()), Float.parseFloat(obj.get("pitch").toString()));
  }

  @SuppressWarnings("unchecked")
  public static JSONObject locToJson(Location loc) {
    JSONObject jso = new JSONObject();
    jso.put("world", loc.getWorld().getName());
    jso.put("x", loc.getX());
    jso.put("y", loc.getY());
    jso.put("z", loc.getZ());
    jso.put("yaw", loc.getYaw());
    jso.put("pitch", loc.getPitch());
    return jso;
  }
}
