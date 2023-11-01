package eu.zhincore.chestnetworks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import co.aikar.commands.MessageType;
import co.aikar.commands.PaperCommandManager;
import co.aikar.locales.MessageKey;
import eu.zhincore.chestnetworks.networks.NetworkChest;

public class ChestNetListener {
  private PaperCommandManager cmdManager;
  private Database database;
  private HashMap<String, SelectionEntry> playerSelect = new HashMap<>();

  public ChestNetListener(ChestNetworksPlugin plugin, Database database) {
    this.cmdManager = plugin.commandManager;
    this.database = database;
  }

  public void startAddChest(UUID playerId, String netName, NetworkChest chest) {
    var entry = new SelectionEntry(SelectionEntry.Type.ADD, netName, chest);
    playerSelect.put(playerId.toString(), entry);
  }

  public void startCheckChest(UUID playerId) {
    var entry = new SelectionEntry(SelectionEntry.Type.CHECK);
    playerSelect.put(playerId.toString(), entry);
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent ev) {
    var player = ev.getPlayer();
    var playerId = player.getUniqueId().toString();
    var entry = playerSelect.get(playerId);
    if (entry == null) return;

    var block = ev.getClickedBlock();
    if (block.getType() != Material.CHEST) {
      cmdManager.sendMessage(player, MessageType.ERROR, MessageKey.of("chestnet.not_chest"));
      return;
    }
    playerSelect.remove(playerId);
    var location = block.getLocation();

    var chestData = database.networkManager.getChestData(location);

    switch (entry.type) {
      case ADD:
        if (chestData != null) {
          cmdManager.sendMessage(player, MessageType.ERROR, MessageKey.of("chestnet.chest_exists"));
          return;
        }

        var network = database.networkManager.get(playerId, entry.netName);
        if (network == null) return;

        entry.chest.location = location;
        network.addChest(entry.chest);
        cmdManager.sendMessage(player, MessageType.INFO, MessageKey.of("chestnet.chest_added"));
        break;

      case CHECK:
        if (chestData == null) {
          cmdManager.sendMessage(player, MessageType.ERROR, MessageKey.of("chestnet.chest_not_exists"));
          return;
        }

        var replacements = new ArrayList<String>();

        replacements.add("network");
        replacements.add(chestData.network.name);

        var owner = Bukkit.getOfflinePlayer(chestData.network.owner);
        replacements.add("owner");
        replacements.add(owner == null ? "unknown" : owner.getName());

        replacements.add("type");
        replacements.add(chestData.type.toString());

        replacements.add("content");
        replacements.add(chestData.content.toString());

        cmdManager.sendMessage(player, MessageType.ERROR, MessageKey.of("chestnet.chest_info"),
            replacements.toArray(new String[0]));
        break;
    }
  }

  private class SelectionEntry {
    public Type type;
    public String netName;
    public NetworkChest chest;

    public SelectionEntry(Type type) {
      this.type = type;
    }

    public SelectionEntry(Type type, String netName, NetworkChest chest) {
      this.type = type;
      this.netName = netName;
      this.chest = chest;
    }

    public enum Type {
      ADD, CHECK
    }
  }
}
