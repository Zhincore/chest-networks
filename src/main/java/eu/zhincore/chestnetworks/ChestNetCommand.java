package eu.zhincore.chestnetworks;

import org.bukkit.entity.Player;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.MessageType;
import co.aikar.commands.annotation.*;

@CommandAlias("cnet|chestnetworks")
@CommandPermission("chestnetworks.command")
public class ChestNetCommand extends BaseCommand {
  @Dependency
  private ChestNetworksPlugin plugin;

  @Subcommand("list")
  public void listNets(Player player) {
    var networks = plugin.database.networkManager.networks.row(player.getUniqueId());
    ;
  }
}
