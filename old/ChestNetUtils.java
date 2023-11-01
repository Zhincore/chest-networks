package eu.zhincore.chestnetworks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class ChestNetUtils {
  public static Chest getChestByLocation(Location location) {
    Block target = location.getBlock();
    if (target.getType() == Material.CHEST) {
      return (Chest) target.getState();
    }
    return null;
  }

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
