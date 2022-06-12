package eu.zhincore.chestnetworks.networks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import com.google.common.collect.ArrayListMultimap;
import eu.zhincore.chestnetworks.ChestNetworksPlugin;
import eu.zhincore.chestnetworks.networks.NetworkChest.ChestType;
import eu.zhincore.chestnetworks.util.ChestNetSorter;
import eu.zhincore.chestnetworks.util.ChestNetUtils;
import eu.zhincore.chestnetworks.util.SchedulableTask;

public class ChestNetwork {
  public String name;
  public UUID ownerId;
  public boolean sort = true;
  public List<NetworkChest> chests = new ArrayList<>();
  private ChestNetworksPlugin plugin;

  private transient ArrayListMultimap<List<String>, NetworkChest> indexByContent = ArrayListMultimap.create();
  private transient SchedulableTask indexingTask = new SchedulableTask(plugin, () -> this.rebuildIndex());

  public ChestNetwork(String name, UUID ownerId) {
    this.name = name;
    this.ownerId = ownerId;
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

    for (var chest : chests) {
      indexByContent.get(chest.content).add(chest);
    }
  }

  public void update() {
    for (var chest : chests) {
      update(chest);
    }
  }

  public HashSet<Inventory> update(NetworkChest chest) {
    var chestBlock = ChestNetUtils.getChestByLocation(chest.location);
    if (chestBlock == null) {
      // The chest doesn't exist anymore, remove it
      removeChest(chest);
      return null;
    }

    var inventory = chestBlock.getInventory();
    var updatedInvs = new HashSet<Inventory>();

    switch (chest.type) {
      case INPUT:
        for (var stack : inventory.getStorageContents()) {
          if (stack == null) continue;
          updatedInvs.add(storeItemStack(stack, inventory));
        }
        break;

      case STORAGE:
        for (var stack : inventory.getStorageContents()) {
          if (stack == null || !chest.content.contains(stack.getType().toString())) continue;
          updatedInvs.add(storeItemStack(stack, inventory));
        }

        // If a change happened, update input chests in case new spot was made
        if (!updatedInvs.isEmpty()) {
          updateInputChests();
        }
        break;
    }

    if (!updatedInvs.isEmpty()) {
      ChestNetSorter.sortInventories(inventory);
      for (var updatedInv : updatedInvs) {
        ChestNetSorter.sortInventories(updatedInv);
      }
    }

    return updatedInvs;
  }

  private HashSet<Inventory> updateInputChests() {
    var updatedInvs = new HashSet<Inventory>();

    for (var chest : chests) {
      if (chest.type == ChestType.INPUT) {
        updatedInvs.addAll(update(chest));
      }
    }
    return updatedInvs;
  }

  public Inventory storeItemStack(ItemStack stack) {
    return storeItemStack(stack, null);
  }

  private Inventory storeItemStack(ItemStack stack, Inventory originInventory) {
    return storeItemStack(stack, originInventory, false);
  }

  private Inventory storeItemStack(ItemStack stack, Inventory originInventory, boolean useMisc) {
    for (var chest : chests) {
      if (chest.type != ChestType.STORAGE
          || !(chest.content.isEmpty() ? useMisc : chest.content.contains(stack.getType().toString())))
        continue;

      var targetChest = ChestNetUtils.getChestByLocation(chest.location);
      if (targetChest == null) continue;

      var destination = targetChest.getInventory();

      // Item is already in suitable destination
      if (destination.equals(originInventory)) return destination;

      var overflow = destination.addItem(stack.clone());

      int remains = 0;
      if (!overflow.isEmpty()) {
        remains = overflow.get(0).getAmount();
      }
      stack.setAmount(remains);

      return destination;
    }

    if (!useMisc) return storeItemStack(stack, originInventory, true);
    return null;
  }
}
