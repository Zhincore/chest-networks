package eu.zhincore.chestnetworks.util;

import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class ChestNetUtils {
  public static Chest[] getChests(Inventory inventory) {
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
