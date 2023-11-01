package eu.zhincore.chestnetworks.networks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.bukkit.inventory.ItemStack;
import com.google.common.collect.ArrayListMultimap;
import eu.zhincore.chestnetworks.ChestNetworksPlugin;
import eu.zhincore.chestnetworks.networks.NetworkChest.ChestType;
import eu.zhincore.chestnetworks.util.ChestNetSorter;
import eu.zhincore.chestnetworks.util.SchedulableTask;

public class ChestNetwork {
  public transient String name;
  public transient UUID owner;
  public boolean sort = true;
  public List<NetworkChest> chests = new ArrayList<>();
  private ChestNetworksPlugin plugin;

  private transient ArrayListMultimap<List<String>, NetworkChest> indexByContent = ArrayListMultimap.create();
  private transient SchedulableTask indexingTask = new SchedulableTask(plugin, () -> this.rebuildIndex(), true);

  public ChestNetwork(ChestNetworksPlugin plugin) {
    this.plugin = plugin;
  }

  public void addChest(NetworkChest chest) {
    chests.add(chest);
    indexingTask.schedule();
    plugin.database.scheduleSave();
  }

  public void removeChest(NetworkChest chest) {
    chests.remove(chest);
    indexingTask.schedule();
    plugin.database.scheduleSave();
  }

  public void rebuildIndex() {
    indexByContent.clear();

    // Create index
    for (var chest : chests) {
      indexByContent.get(chest.content).add(chest);
    }
  }

  public void update() {
    for (var chest : chests) {
      update(chest);
    }
  }

  public HashSet<NetworkChest> update(NetworkChest chest) {
    var inventory = chest.getInventory();
    if (inventory == null) {
      // The chest doesn't exist anymore, remove it
      removeChest(chest);
      return null;
    }
    var updatedChests = new HashSet<NetworkChest>();

    switch (chest.type) {
      case INPUT:
        for (var stack : inventory.getStorageContents()) {
          if (stack == null) continue;
          updatedChests.add(storeItemStack(stack, chest));
        }
        break;

      case STORAGE:
        for (var stack : inventory.getStorageContents()) {
          if (stack == null || !chest.content.contains(stack.getType().toString())) continue;
          updatedChests.add(storeItemStack(stack, chest));
        }

        // If a change happened, update input chests in case new spot was made
        if (!updatedChests.isEmpty()) {
          updateInputChests();
        }
        break;
    }

    if (sort && !updatedChests.isEmpty()) {
      updatedChests.add(chest);
      sortChests(updatedChests.toArray(new NetworkChest[0]));
    }

    return updatedChests;
  }

  public void sortChests(NetworkChest... chests) {
    var contentDone = new HashSet<List<String>>();

    // Sort RAID0s
    for (var chest : chests) {
      if (!contentDone.add(chest.content)) continue;

      var raid0 = indexByContent.get(chest.content);
      raid0.sort((a, b) -> a.priority - b.priority);

      ChestNetSorter.sort(raid0.toArray(new NetworkChest[0]));
    }
  }

  private HashSet<NetworkChest> updateInputChests() {
    var updatedInvs = new HashSet<NetworkChest>();

    for (var chest : chests) {
      if (chest.type == ChestType.INPUT) {
        updatedInvs.addAll(update(chest));
      }
    }
    return updatedInvs;
  }

  public NetworkChest storeItemStack(ItemStack stack) {
    return storeItemStack(stack, null);
  }

  private NetworkChest storeItemStack(ItemStack stack, NetworkChest originChest) {
    return storeItemStack(stack, originChest, false);
  }

  private NetworkChest storeItemStack(ItemStack stack, NetworkChest originChest, boolean useMisc) {
    for (var chest : chests) {
      if (chest == originChest || chest.type != ChestType.STORAGE
          || !(chest.content.isEmpty() ? useMisc : chest.content.contains(stack.getType().toString())))
        continue;

      var destination = chest.getInventory();
      if (destination == null) continue;

      var overflow = destination.addItem(stack.clone());

      int remains = 0;
      if (!overflow.isEmpty()) {
        remains = overflow.get(0).getAmount();
      }
      stack.setAmount(remains);

      return chest;
    }

    if (!useMisc) return storeItemStack(stack, originChest, true);
    return null;
  }
}
