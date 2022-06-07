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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class NetworksController {
  private HashMap<String, String[]> playerSelectQueue = new HashMap<>();
  private ChestNetworks plugin;
  private Database database;
  private JSONObject players;

  public NetworksController(ChestNetworks plugin) {
    this.plugin = plugin;
    database = new Database(plugin);
    players = (JSONObject) database.data;
  }

  public Boolean isPlayerSelectQueue(Player player) {
    String playerID = player.getUniqueId().toString();
    return playerSelectQueue.containsKey(playerID);
  }

  public void cancelPlayerSelect(Player player) {
    String playerID = player.getUniqueId().toString();
    if (playerSelectQueue.containsKey(playerID)) {
      playerSelectQueue.remove(playerID);
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
    String playerID = player.getUniqueId().toString();
    return listNets(playerID);
  }

  @SuppressWarnings("unchecked")
  public List<String> listNets(String playerID) {
    if (players.containsKey(playerID)) {
      return new ArrayList<String>(((JSONObject) players.get(playerID)).keySet());
    }
    return null;
  }

  public List<JSONObject> listChests(Player player, String name) {
    String playerID = player.getUniqueId().toString();
    return listChests(playerID, name);
  }

  @SuppressWarnings("unchecked")
  public List<JSONObject> listChests(String playerID, String netName) {
    if (players.containsKey(playerID)) {
      JSONObject network = (JSONObject) players.get(playerID);
      if (network.containsKey(netName)) {
        var chests = new ArrayList<JSONObject>((JSONArray) network.get(netName));

        // Add info to each chest, used when referencing the network
        for (var chest : chests) {
          chest.put("owner", playerID);
          chest.put("network", netName);
        }
        return chests;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public List<JSONObject> listGlobalChests() {
    List<String> playerIDs = new ArrayList<>(players.keySet());
    List<JSONObject> output = new ArrayList<>();
    for (String playerID : playerIDs) {
      for (String networkName : listNets(playerID)) {
        output.addAll(listChests(playerID, networkName));
      }
    }
    return output;
  }

  @SuppressWarnings("unchecked")
  public List<Location> listGlobalChestLocations() {
    List<String> playerIDs = new ArrayList<>(players.keySet());
    List<Location> output = new ArrayList<>();
    for (String playerID : playerIDs) {
      for (String networkName : listNets(playerID)) {
        output.addAll(listChests(playerID, networkName)
            .stream().map(v -> ((Location) Database.jsonToLoc((JSONObject) v.get("location"))))
            .collect(Collectors.toList()));
      }
    }
    return output;
  }

  public void delete(Player player, String name) {
    String playerID = player.getUniqueId().toString();
    if (players.containsKey(playerID)) {
      JSONObject networks = (JSONObject) players.get(playerID);
      if (networks.containsKey(name)) {
        networks.remove(name);
        plugin.messenger.send("cnetdel", player, name);
        database.save();
        return;
      }
    }
    plugin.messenger.send("cnet404", player, name);
  }

  @SuppressWarnings("unchecked")
  public void create(Player player, String name) {
    String playerID = player.getUniqueId().toString();
    if (!players.containsKey(playerID)) {
      players.put(playerID, new JSONObject());
    }
    JSONObject networks = (JSONObject) players.get(playerID);
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
    String playerID = player.getUniqueId().toString();
    if (!players.containsKey(playerID)) {
      plugin.messenger.send("cnet404", player, name);
      return;
    }
    JSONObject networks = (JSONObject) players.get(playerID);
    if (!networks.containsKey(name)) {
      plugin.messenger.send("cnet404", player, name);
      return;
    }

    if (playerSelectQueue.containsKey(playerID)) {
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
    String playerID = player.getUniqueId().toString();
    playerSelectQueue.put(playerID, data);
    plugin.messenger.send("selectc", player);
  }

  public void onPlayerSelected(Player player, Location loc) {
    String playerID = player.getUniqueId().toString();
    if (!playerSelectQueue.containsKey(playerID))
      return;
    String[] playerData = playerSelectQueue.remove(playerID);
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
    String playerID = player.getUniqueId().toString();
    JSONObject jsonLoc = (JSONObject) Database.locToJson(loc);
    JSONArray network = (JSONArray) ((JSONObject) players.get(playerID)).get(playerData[1]);

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
    plugin.messenger.send("chcheck", player,
        (String) chestData.get("network"),
        (String) chestData.get("owner"),
        type,
        type.equals("input") ? "N/A"
            : content.size() == 0 ? "§oEverything§r" : (String) String.join(", ", content));
  }

  public JSONObject getChestData(Location loc) {
    for (Object chest : listGlobalChests()) {
      if (Database.jsonToLoc((JSONObject) ((JSONObject) chest).get("location")).equals(loc)) {
        return (JSONObject) chest;
      }
    }
    return null;
  }

  public void removeChest(Location loc, Player player) {
    JSONObject chest = getChestData(loc);
    if (chest == null)
      return;
    ((JSONArray) ((JSONObject) players.get(chest.get("owner"))).get(chest.get("network"))).remove(chest);
    if (player != null) {
      plugin.messenger.send("chesrem", (CommandSender) player, (String) chest.get("network"));
    }
    database.save();
  }

  public void update(Inventory inventory) {
    Chest chest = getRegisteredChest(inventory);
    if (chest == null)
      return;

    JSONObject chestData = getChestData(chest.getLocation());
    JSONArray network = (JSONArray) ((JSONObject) players.get(chestData.get("owner"))).get(chestData.get("network"));
    // If it is an input chest
    if (((String) chestData.get("type")).equals("input")) {
      // Try to send all of its items to the storage
      for (ItemStack item : inventory.getContents()) {
        if (item == null)
          continue;
        placeItems(network, inventory, item);
      }
      // If it is a storage chest
    } else {
      // Find items that do not belong there
      for (ItemStack item : inventory.getContents()) {
        if (item == null)
          continue;
        if (!((JSONArray) chestData.get("content")).contains(item.getType().toString())) {
          placeItems(network, inventory, item);
        }
      }
      // Trigger update for input chests in case new empty spot exists
      // items
      for (Object inputChestData : network) {
        if (((JSONObject) inputChestData).get("type").equals("input")) {
          Chest inputChest = getChestByLoc(
              Database.jsonToLoc((JSONObject) ((JSONObject) inputChestData).get("location")));
          if (inputChest != null) {
            update(inputChest.getInventory());
          }
        }
      }
    }
  }

  private void placeItems(JSONArray network, Inventory source, ItemStack item) {
    if (item == null)
      return;

    Function<Boolean, Boolean> findPlace = fallback -> {
      for (Object chest : network) {
        JSONObject chestconf = (JSONObject) chest;
        JSONArray chestcontent = (JSONArray) chestconf.get("content");

        if (chestconf.get("type").equals("storage") &&
            chestcontent.isEmpty() ? fallback : chestcontent.contains(item.getType().toString())) {

          Location loc = Database.jsonToLoc((JSONObject) chestconf.get("location"));
          Chest target = getChestByLoc(loc);
          if (target == null)
            continue;

          Inventory destination = target.getInventory();
          HashMap<Integer, ItemStack> overflow = destination.addItem(item);
          Integer remains = 0;
          if (!overflow.isEmpty()) {
            remains = overflow.get(0).getAmount();
          }
          item.setAmount(remains);
          return true;
        }
      }

      return false;
    };

    if (!findPlace.apply(false)) {
      // Try to put the item into misc chest
      findPlace.apply(true);
    }
  }

  private Chest getRegisteredChest(Inventory inventory) {
    Chest[] chests = getChests(inventory);
    if (chests == null)
      return null;

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
