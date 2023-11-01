package eu.zhincore.chestnetworks;

import org.bukkit.entity.Player;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.MessageType;
import co.aikar.commands.PaperCommandManager;
import co.aikar.commands.annotation.*;
import co.aikar.locales.MessageKey;
import eu.zhincore.chestnetworks.networks.ChestNetworksManager;

@CommandAlias("cnet|chestnet|chestnetworks")
@CommandPermission("chestnetworks.command")
public class ChestNetCommand extends BaseCommand {
  @Dependency
  private PaperCommandManager cmdManager;

  @Dependency
  private ChestNetworksManager networkManager;

  @Subcommand("list")
  public void list(Player player) {
    var networks = networkManager.listNetworks(player.getUniqueId());

    if (networks.isEmpty()) {
      cmdManager.sendMessage(player, MessageType.INFO, MessageKey.of("chestnet.no_networks"));
    } else {
      var replacements = new String[] { "{networks}", String.join(", ", networks) };
      cmdManager.sendMessage(player, MessageType.INFO, MessageKey.of("chestnet.list"), replacements);
    }
  }

  @Subcommand("create")
  public void create(Player player, String name) {
    if (networkManager.create(player.getUniqueId(), name)) {
      cmdManager.sendMessage(player, MessageType.INFO, MessageKey.of("chestnet.created"), "{name}", name);
    } else {
      cmdManager.sendMessage(player, MessageType.ERROR, MessageKey.of("chestnet.exists"), "{name}", name);
    }
  }

  @Subcommand("deleteNetwork")
  public void deleteNetwork(Player player, @Values("@network") String name) {
    if (networkManager.delete(player.getUniqueId(), name)) {
      cmdManager.sendMessage(player, MessageType.INFO, MessageKey.of("chestnet.deleted"), "{name}", name);
    } else {
      cmdManager.sendMessage(player, MessageType.ERROR, MessageKey.of("chestnet.not_exists"), "{name}", name);
    }
  }

  @Subcommand("addChest|setChest")
  public void setChest(Player player, String name) {
    if (networkManager.create(player.getUniqueId(), name)) {
      cmdManager.sendMessage(player, MessageType.INFO, MessageKey.of("chestnet.created"), "{name}", name);
    } else {
      cmdManager.sendMessage(player, MessageType.ERROR, MessageKey.of("chestnet.exists"), "{name}", name);
    }
  }
}
