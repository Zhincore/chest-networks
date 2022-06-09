package eu.zhincore.chestnetworks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ChestNetController {
  private HashMap<String, String[]> playerSelectQueue = new HashMap<>();
  private ChestNetworksPlugin plugin;
  private ChestNetDatabase database;
  private JSONObject players;

  public ChestNetController(ChestNetworksPlugin plugin) {
    this.plugin = plugin;
    database = new ChestNetDatabase(plugin);
    players = (JSONObject) database.data;
  }

  public Boolean isPlayerSelectQueue(Player player) {
    String playerId = player.getUniqueId().toString();
    return playerSelectQueue.containsKey(playerId);
  }

  public void cancelPlayerSelect(Player player) {
    String playerId = player.getUniqueId().toString();
    if (playerSelectQueue.containsKey(playerId)) {
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
    if (players.containsKey(playerId)) {
      return new ArrayList<String>(((JSONObject) players.get(playerId)).keySet());
    }
    return null;
  }

  public List<JSONObject> listChests(Player player, String name) {
    String playerId = player.getUniqueId().toString();
    return listChests(playerId, name);
  }

  @SuppressWarnings("unchecked")
  public List<JSONObject> listChests(String playerId, String netName) {
    if (players.containsKey(playerId)) {
      JSONObject network = (JSONObject) players.get(playerId);
      if (network.containsKey(netName)) {
        var chests = new ArrayList<JSONObject>((JSONArray) network.get(netName));

        // Add info to each chest, used when referencing the network
        for (var chest : chests) {
          chest.put("owner", playerId);
          chest.put("network", netName);
        }
        return chests;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public List<JSONObject> listGlobalChests() {
    List<String> playerIds = new ArrayList<>(players.keySet());
    List<JSONObject> output = new ArrayList<>();
    for (String playerId : playerIds) {
      for (String networkName : listNets(playerId)) {
        output.addAll(listChests(playerId, networkName));
      }
    }
    return output;
  }

  @SuppressWarnings("unchecked")
  public List<Location> listGlobalChestLocations() {
    List<String> playerIds = new ArrayList<>(players.keySet());
    List<Location> output = new ArrayList<>();
    for (String playerId : playerIds) {
      for (String networkName : listNets(playerId)) {
        output.addAll(listChests(playerId, networkName).stream()
            .map(v -> ((Location) ChestNetDatabase.jsonToLoc((JSONObject) v.get("location"))))
            .collect(Collectors.toList()));
      }
    }
    return output;
  }

  public void delete(Player player, String name) {
    String playerId = player.getUniqueId().toString();
    if (players.containsKey(playerId)) {
      JSONObject networks = (JSONObject) players.get(playerId);
      if (networks.containsKey(name)) {
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
    if (!players.containsKey(playerId)) {
      players.put(playerId, new JSONObject());
    }
    JSONObject networks = (JSONObject) players.get(playerId);
    if (networks.containsKey(name)) {
      plugin.messenger.send("cnet409", player, name);
      return;
    }

    networks.put(name, new JSONArray());
    if (database.save()) {
      plugin.messenger.send("created", player, name);
    } else {
      plugin.messenger.send("dberror", player);
    }

  }

  public void startAddChest(Player player, String name, String type, List<String> content) {
    String playerId = player.getUniqueId().toString();
    if (!players.containsKey(playerId)) {
      plugin.messenger.send("cnet404", player, name);
      return;
    }
    JSONObject networks = (JSONObject) players.get(playerId);
    if (!networks.containsKey(name)) {
      plugin.messenger.send("cnet404", player, name);
      return;
    }

    if (playerSelectQueue.containsKey(playerId)) {
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
    playerSelectQueue.put(playerId, data);
    plugin.messenger.send("selectc", player);
  }

  public void onPlayerSelected(Player player, Location loc) {
    String playerId = player.getUniqueId().toString();
    if (!playerSelectQueue.containsKey(playerId)) return;
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
    JSONObject jsonLoc = (JSONObject) ChestNetDatabase.locToJson(loc);
    JSONArray network = (JSONArray) ((JSONObject) players.get(playerId)).get(playerData[1]);

    // Detect is this chest is already registered
    Chest registeredChest = getRegisteredChest(getChestByLoc(loc).getInventory());
    if (registeredChest != null) {
      JSONObject chestData = getChestData(registeredChest.getLocation());
      network.remove(chestData);
    }

    JSONObject chest = new JSONObject();
    chest.put("location", jsonLoc);
    chest.put("type", playerData[2]);
    JSONArray content = new JSONArray();
    content.addAll((List<String>) Arrays.asList(playerData[3].toUpperCase().split(",")));
    content.removeIf(v -> v.equals(""));
    chest.put("content", content);
    network.add(chest);
    database.save();
    plugin.messenger.send("chadded", player, playerData[1]);
  }

  @SuppressWarnings("unchecked")
  private void checkChest(Player player, Location loc) {
    Chest registeredChest = getRegisteredChest(getChestByLoc(loc).getInventory());
    if (registeredChest == null) {
      plugin.messenger.send("ccecknt", player);
      return;
    }
    JSONObject chestData = getChestData(registeredChest.getLocation());
    String type = (String) chestData.get("type");
    JSONArray content = (JSONArray) chestData.get("content");
    plugin.messenger.send("chcheck", player, (String) chestData.get("network"), (String) chestData.get("owner"), type,
        type.equals("input") ? "N/A" : content.size() == 0 ? "§oEverything§r" : (String) String.join(", ", content));
  }

  public JSONObject getChestData(Location loc) {
    for (Object chest : listGlobalChests()) {
      if (ChestNetDatabase.jsonToLoc((JSONObject) ((JSONObject) chest).get("location")).equals(loc)) {
        return (JSONObject) chest;
      }
    }
    return null;
  }

  public void removeChest(Location loc, Player player) {
    JSONObject chest = getChestData(loc);
    if (chest == null) return;
    ((JSONArray) ((JSONObject) players.get(chest.get("owner"))).get(chest.get("network"))).remove(chest);
    if (player != null) {
      plugin.messenger.send("chesrem", (CommandSender) player, (String) chest.get("network"));
    }
    database.save();
  }

  public void setSorting(String playerId, String netName, boolean enabled) {
    JSONArray network = (JSONArray) ((JSONObject) players.get(playerId)).get(netName);
    for (Object chestDatai : network) {
      JSONObject chestData = (JSONObject) chestDatai;
      if (enabled) chestData.put("sort", true);
      else chestData.remove("sort");
    }
    database.save();

    update(playerId, netName);
  }

  public void update(String playerId, String netName) {
    JSONArray network = (JSONArray) ((JSONObject) players.get(playerId)).get(netName);

    for (Object chestData : network) {
      Chest chest = getChestByLoc(ChestNetDatabase.jsonToLoc((JSONObject) ((JSONObject) chestData).get("location")));
      if (chest != null) {
        update(chest.getInventory());
      }
    }
  }

  public void update(Inventory inventory) {
    Chest chest = getRegisteredChest(inventory);
    if (chest == null) return;

    JSONObject chestData = getChestData(chest.getLocation());
    JSONArray network = (JSONArray) ((JSONObject) players.get(chestData.get("owner"))).get(chestData.get("network"));
    var changedChests = new ArrayList<JSONObject>();

    // If it is an input chest
    if (((String) chestData.get("type")).equals("input")) {
      // Try to send all of its items to the storage
      for (ItemStack item : inventory.getStorageContents()) {
        if (item == null) continue;
        changedChests.add(placeItems(network, inventory, item));
      }
      // If it is a storage chest
    } else {
      // Find items that do not belong there
      for (ItemStack item : inventory.getStorageContents()) {
        if (item == null) continue;
        if (!((JSONArray) chestData.get("content")).contains(item.getType().toString())) {
          changedChests.add(placeItems(network, inventory, item));
        }
      }
      // Trigger update for input chests in case new empty spot exists
      // items
      for (Object inputChestData : network) {
        if (((JSONObject) inputChestData).get("type").equals("input")) {
          Chest inputChest = getChestByLoc(
              ChestNetDatabase.jsonToLoc((JSONObject) ((JSONObject) inputChestData).get("location")));
          if (inputChest != null) {
            update(inputChest.getInventory());
          }
        }
      }

      if (chestData.get("sort") != null) sortInventory(inventory);
      for (var _chestData : changedChests) {
        if (_chestData == null || _chestData.get("sort") != null) continue;
        Chest _chest = getChestByLoc(
            ChestNetDatabase.jsonToLoc((JSONObject) ((JSONObject) _chestData).get("location")));
        sortInventory(_chest.getInventory());
      }
    }
  }

  private void sortInventory(Inventory inventory) {
    if (Bukkit.getPluginManager().getPlugin("ChestSort") != null) {
      de.jeff_media.chestsort.api.ChestSortAPI.sortInventory(inventory);
    }
  }

  private JSONObject placeItems(JSONArray network, Inventory inventory, ItemStack item) {
    if (item == null) return null;

    Function<Boolean, JSONObject> findPlace = fallback -> {
      for (Object chest : network) {
        JSONObject chestconf = (JSONObject) chest;
        JSONArray chestcontent = (JSONArray) chestconf.get("content");

        if (!chestconf.get("type").equals("storage")
            || !(chestcontent.isEmpty() ? fallback : chestcontent.contains(item.getType().toString())))
          continue;

        Location loc = ChestNetDatabase.jsonToLoc((JSONObject) chestconf.get("location"));
        Chest target = getChestByLoc(loc);
        if (target == null) continue;

        Inventory destination = target.getInventory();
        if (destination.equals(inventory)) continue;
        HashMap<Integer, ItemStack> overflow = destination.addItem(item);
        Integer remains = 0;
        if (!overflow.isEmpty()) {
          remains = overflow.get(0).getAmount();
        }
        item.setAmount(remains);

        return chestconf;
      }

      return null;
    };

    var chestconf = findPlace.apply(false);
    if (chestconf == null) {
      // Try to put the item into misc chest
      chestconf = findPlace.apply(true);
    }
    return chestconf;
  }

  private Chest getRegisteredChest(Inventory inventory) {
    Chest[] chests = getChests(inventory);
    if (chests == null) return null;

    List<Location> registeredChestLocs = listGlobalChestLocations();
    for (Chest chest : chests) {
      if (registeredChestLocs.contains(chest.getLocation())) {
        return chest;
      }
    }
    return null;
  }

  private Chest getChestByLoc(Location loc) {
    Block target = loc.getBlock();
    if (target.getType() == Material.CHEST) {
      return (Chest) target.getState();
    }
    removeChest(loc, null);
    return null;
  }

  private Chest[] getChests(Inventory inventory) {
    InventoryHolder holder = inventory.getHolder();
    if (holder instanceof DoubleChest) {
      DoubleChest doubleChest = ((DoubleChest) holder);
      return new Chest[] { (Chest) doubleChest.getLeftSide(), (Chest) doubleChest.getRightSide() };
    } else if (holder instanceof Chest) {
      return new Chest[] { (Chest) holder };
    }
    return null;
  }
}
