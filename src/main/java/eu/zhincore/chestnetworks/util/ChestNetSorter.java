package eu.zhincore.chestnetworks.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ChestNetSorter {
  private static final Comparator<ItemStack> defaultComparator = new Comparator<>() {
    public int compare(ItemStack stack1, ItemStack stack2) {
      int materialOrder = stack1.getType().compareTo(stack2.getType());
      return materialOrder == 0 ? stack1.getAmount() - stack2.getAmount() : materialOrder;
    }
  };

  public static void sortInventories(Inventory... inventories) {
    var contents = new ArrayList<ItemStack>();
    for (var inventory : inventories) {
      for (var stack : inventory.getContents()) {
        if (stack != null) contents.add(stack);
      }
      inventory.clear();
    }

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
