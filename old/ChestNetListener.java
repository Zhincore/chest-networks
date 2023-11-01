package eu.zhincore.chestnetworks;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

public class ChestNetListener implements Listener {
  private ChestNetController networkController;

  public ChestNetListener(ChestNetController networkController) {
    this.networkController = networkController;
  }

  @EventHandler
  public void onInteract(PlayerInteractEvent ev) {
    Block block = ev.getClickedBlock();
    Player player = ev.getPlayer();

    if (ev.getAction() == Action.RIGHT_CLICK_BLOCK) {
      if (block.getType() == Material.CHEST) {
        if (networkController.isPlayerSelectQueue(player)) {
          ev.setUseInteractedBlock(Event.Result.DENY);
          networkController.onPlayerSelected(player, block.getLocation());
        }
      }
    }
  }

  @EventHandler
  public void onBlockBreakEvent(BlockBreakEvent ev) {
    Block block = ev.getBlock();
    Player player = ev instanceof BlockBreakEvent ? ((BlockBreakEvent) ev).getPlayer() : null;
    if (block.getType() == Material.CHEST) {
      networkController.removeChest(block.getLocation(), player);
    }
  }

  @EventHandler
  public void onInventoryCloseEvent(InventoryCloseEvent ev) {
    onInventoryUpdate(ev.getInventory());
  }

  @EventHandler
  public void onInventoryMoveItemEvent(InventoryMoveItemEvent ev) {
    onInventoryUpdate(ev.getDestination());
  }

  private void onInventoryUpdate(Inventory inv) {
    if (inv.getViewers().size() != 1 || inv.getType() != InventoryType.CHEST) {
      return;
    }

    networkController.update(inv);
  }
}
