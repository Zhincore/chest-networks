package eu.zhincore.chestnetworks;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandChestNet implements CommandExecutor {
  private NetworksController networksController;
  private ChestNetworks plugin;

  public CommandChestNet(ChestNetworks plugin, NetworksController networksController) {
    this.networksController = networksController;
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage(String.format("[%s] This command can be only used by player.", label));
      return true;
    }

    if (args.length < 1)
      return false;

    // Sanitize arguments
    for (int i = args.length - 1; i != 0; i--) {
      args[i] = args[i].replace("\\", "\\\\");
    }

    // Sub-commands
    switch (args[0]) {
      case "h":
      case "help":
        plugin.messenger.send("helpmsg", (Player) sender,
            command.getName(), String.join(", ", command.getAliases()));
        break;

      case "c":
      case "create":
        if (args.length < 2) {
          sender.sendMessage(ChatColor.GOLD + "Use: " + ChatColor.RED + "/" + label + " create <name of network>");
          return true;
        }
        networksController.create((Player) sender, args[1]);
        break;

      case "delete":
        if (args.length < 2) {
          sender.sendMessage(ChatColor.GOLD + "Use: " + ChatColor.RED + "/" + label + " delete <name of network>");
          return true;
        }
        networksController.delete((Player) sender, args[1]);
        break;

      case "setChest":
      case "addChest":
        if (args.length < 3
            || !(args[2].equals("storage") || args[2].equals("input"))) {
          sender.sendMessage(ChatColor.GOLD + "Use: " + ChatColor.RED + "/" + label
              + " addChest <name of network> <input/storage> [contents...]");
          return true;
        }
        networksController.startAddChest((Player) sender, args[1], args[2],
            Arrays.asList(args).subList(3, args.length));
        break;

      case "list":
      case "listNets":
      case "listNetworks":
        networksController.list((Player) sender);
        break;

      case "listChests":
        // TODO
        break;

      case "check":
      case "checkChest":
        networksController.startCheckChest((Player) sender);
        break;

      case "cancel":
      case "cancelSelect":
        networksController.cancelPlayerSelect((Player) sender);
        break;

      default:
        return false;
    }

    return true;
  }

}
