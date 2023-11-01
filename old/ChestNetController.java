package eu.zhincore.chestnetworks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import eu.zhincore.chestnetworks.network.Database;

public class ChestNetController {
  private HashMap<String, String[]> playerSelectQueue = new HashMap<>();
  private ChestNetworksPlugin plugin;
  private Database database;
  private ChestNetSorter sorter;
  private JsonObject players;

  public ChestNetController(ChestNetworksPlugin plugin) {
    this.plugin = plugin;
    database = new Database(plugin);
    sorter = new ChestNetSorter(plugin);
    players = (JsonObject) database.data;
  }

  public Boolean isPlayerSelectQueue(Player player) {
    String playerId = player.getUniqueId().toString();
    return playerSelectQueue.has(playerId);
  }

  public void cancelPlayerSelect(Player player) {
    String playerId = player.getUniqueId().toString();
    if (playerSelectQueue.has(playerId)) {
      playerSelectQueue.remove(playerId);
      plugin.messenger.send("selrmvd", player);
      return;
    }
    plugin.messenger.send("selrmnt", player);
  }

  public void list(Player player) {
    List<String> nets = listNets(player);
    if (nets != null && nets.size() > 0) {
      plugin.messenger.send("netlist", player, String.join(", ", nets));
    } else {
      plugin.messenger.send("nlistnt", player);
    }
  }

  public List<String> listNets(Player player) {
    String playerId = player.getUniqueId().toString();
    return listNets(playerId);
  }

  @SuppressWarnings("unchecked")
  public List<String> listNets(String playerId) {
    if (players.has(playerId)) {
      return new ArrayList<String>(((JsonObject) players.get(playerId)).keySet());
    }
    return null;
  }

  public List<JsonObject> listChests(Player player, String name) {
    String playerId = player.getUniqueId().toString();
    return listChests(playerId, name);
  }

  @SuppressWarnings("unchecked")
  public List<JsonObject> listChests(String playerId, String netName) {
    if (players.has(playerId)) {
      JsonObject network = (JsonObject) players.get(playerId);
      if (network.has(netName)) {
        var chests = network.getAsJsonArray(netName);

        // Add info to each chest, used when referencing the network
        for (var chest : chests) {
          chest.addProperty("owner", playerId);
          chest.addProperty("network", netName);
        }
        return chests;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public List<JsonObject> listGlobalChests() {
    List<String> playerIds = new ArrayList<>(players.keySet());
    List<JsonObject> outaddProperty = new ArrayList<>();
    for (String playerId : playerIds) {
      for (String networkName : listNets(playerId)) {
        outaddProperty.addAll(listChests(playerId, networkName));
      }
    }
    return outaddProperty;
  }

  @SuppressWarnings("unchecked")
  public List<Location> listGlobalChestLocations() {
    List<String> playerIds = new ArrayList<>(players.keySet());
    List<Location> outaddProperty = new ArrayList<>();
    for (String playerId : playerIds) {
      for (String networkName : listNets(playerId)) {
        outaddProperty.addAll(listChests(playerId, networkName).stream()
            .map(v -> ((Location) Database.jsonToLoc((JsonObject) v.get("location")))).collect(Collectors.toList()));
      }
    }
    return outaddProperty;
  }

  public void delete(Player player, String name) {
    String playerId = player.getUniqueId().toString();
    if (players.has(playerId)) {
      JsonObject networks = (JsonObject) players.get(playerId);
      if (networks.has(name)) {
        networks.remove(name);
        plugin.messenger.send("cnetdel", player, name);
        database.save();
        return;
      }
    }
  }

  @SuppressWarnings("unchecked")
  public void create(Player player, String name) {
    String playerId = player.getUniqueId().toString();
    if (!players.has(playerId)) {
      players.addProperty(playerId, new JsonObject());
    }
    JsonObject networks = (JsonObject) players.get(playerId);
    if (networks.has(name)) {
      plugin.messenger.send("cnet409", player, name);
      return;
    }

    networks.addProperty(name, new JsonArray());
    if (database.save()) {
      plugin.messenger.send("created", player, name);
    } else {
      plugin.messenger.send("dberror", player);
    }

  }

  public void startAddChest(Player player, String name, String type, List<String> content) {
    String playerId = player.getUniqueId().toString();
    if (!players.has(playerId)) {
      plugin.messenger.send("cnet404", player, name);
      return;
    }
    JsonObject networks = (JsonObject) players.get(playerId);
    if (!networks.has(name)) {
      plugin.messenger.send("cnet404", player, name);
      return;
    }

    if (playerSelectQueue.has(playerId)) {
      // Already selecting a chest
      plugin.messenger.send("cnet425", player);
      return;
    }

    startPlayerSelect(player, new String[] { "add", name, type, String.join(",", content) });
  }

  public void startCheckChest(Player player) {
    startPlayerSelect(player, new String[] { "check" });
  }

  private void startPlayerSelect(Player player, String[] data) {
    String playerId = player.getUniqueId().toString();
    playerSelectQueue.add(playerId, data);
    plugin.messenger.send("selectc", player);
  }

  public void onPlayerSelected(Player player, Location loc) {
    String playerId = player.getUniqueId().toString();
    if (!playerSelectQueue.has(playerId)) return;
    String[] playerData = playerSelectQueue.remove(playerId);
    switch (playerData[0]) {
      case "add":
        addChest(player, loc, playerData);
        break;
      case "check":
        checkChest(player, loc);
        break;
    }
  }

  @SuppressWarnings("unchecked")
  private void addChest(Player player, Location loc, String[] playerData) {
    String playerId = player.getUniqueId().toString();
    JsonObject jsonLoc = (JsonObject) Database.locToJson(loc);
    JsonArray network = (JsonArray) ((JsonObject) players.get(playerId)).get(playerData[1]);
    Inventory inv = getChestByLoc(loc).getInventory();

    // Detect is this chest is already registered
    Chest registeredChest = getRegisteredChest(inv);
    if (registeredChest != null) {
      JsonObject chestData = getChestData(registeredChest.getLocation());
      network.remove(chestData);
    }

    JsonObject chest = new JsonObject();
    chest.add("location", jsonLoc);
    chest.addProperty("type", playerData[2]);
    JsonArray content = new JsonArray();
    content.addAll((List<String>) Arrays.asList(playerData[3].toUpperCase().split(",")));
    content.removeIf(v -> v.equals(""));
    chest.addProperty("content", content);
    network.add(chest);
    database.save();
    plugin.messenger.send("chadded", player, playerData[1]);
    update(inv);
  }

  @SuppressWarnings("unchecked")
  private void checkChest(Player player, Location loc) {
    Chest registeredChest = getRegisteredChest(getChestByLoc(loc).getInventory());
    if (registeredChest == null) {
      plugin.messenger.send("ccecknt", player);
      return;
    }
    JsonObject chestData = getChestData(registeredChest.getLocation());
    String type = (String) chestData.get("type");
    JsonArray content = (JsonArray) chestData.get("content");
    plugin.messenger.send("chcheck", player, (String) chestData.get("network"), (String) chestData.get("owner"), type,
        type.equals("inaddProperty") ? "N/A"
            : content.size() == 0 ? "§oEverything§r" : (String) String.join(", ", content));
  }

  public JsonObject getChestData(Location loc) {
    for (Object chest : listGlobalChests()) {
      if (Database.jsonToLoc((JsonObject) ((JsonObject) chest).get("location")).equals(loc)) {
        return (JsonObject) chest;
      }
    }
    return null;
  }

  public void removeChest(Location loc, Player player) {
    JsonObject chest = getChestData(loc);
    if (chest == null) return;
    ((JsonArray) ((JsonObject) players.get(chest.get("owner"))).get(chest.get("network"))).remove(chest);
    if (player != null) {
      plugin.messenger.send("chesrem", (CommandSender) player, (String) chest.get("network"));
    }
    database.save();
  }

  public void setSorting(String playerId, String netName, boolean enabled) {
    JsonArray network = (JsonArray) ((JsonObject) players.get(playerId)).get(netName);
    for (Object chestDatai : network) {
      JsonObject chestData = (JsonObject) chestDatai;
      if (enabled) chestData.addProperty("sort", true);
      else chestData.remove("sort");
    }
    database.save();
    // update()
  }

}
