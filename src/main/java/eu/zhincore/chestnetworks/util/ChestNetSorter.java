package eu.zhincore.chestnetworks.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import eu.zhincore.chestnetworks.networks.NetworkChest;

public class ChestNetSorter {
  private static final Comparator<ItemStack> defaultComparator = new Comparator<>() {
    public int compare(ItemStack stack1, ItemStack stack2) {
      int materialOrder = stack1.getType().compareTo(stack2.getType());
      return materialOrder == 0 ? stack1.getAmount() - stack2.getAmount() : materialOrder;
    }
  };

  public static void sort(NetworkChest... chests) {
    var contents = new ArrayList<ItemStack>();
    var inventories = new ArrayList<Inventory>();

    for (var chest : chests) {
      var inventory = chest.getInventory();
      inventories.add(inventory);

      for (var stack : inventory.getContents()) {
        if (stack != null) contents.add(stack);
      }
      inventory.clear();
    }

    sort(inventories, contents);
  }

  public static void sort(Inventory... inventories) {
    var contents = new ArrayList<ItemStack>();
    for (var inventory : inventories) {
      for (var stack : inventory.getContents()) {
        if (stack != null) contents.add(stack);
      }
      inventory.clear();
    }

    sort(Arrays.asList(inventories), contents);
  }

  private static void sort(List<Inventory> inventories, List<ItemStack> contents) {
    Collections.sort(contents, defaultComparator);
    int i = 0;
    int leftItems = contents.size();

    for (var inventory : inventories) {
      int size = inventory.getSize();
      if (leftItems < size) size = leftItems;

      inventory.addItem(contents.subList(i, size).toArray(new ItemStack[0]));

      leftItems -= size;
      if (leftItems <= 0) break;
    }
  }

  // private void sortInventoriesAsync(Inventory... inventories) {

  // taskChain.newChain().async(() -> Collections.sort(contents,
  // defaultComparator)).sync(() -> {

  // });
  // }
}
