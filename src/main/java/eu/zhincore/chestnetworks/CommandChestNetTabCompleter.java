package eu.zhincore.chestnetworks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class CommandChestNetTabCompleter implements TabCompleter {
  private NetworksController networksController;

  public CommandChestNetTabCompleter(NetworksController networksController) {
    this.networksController = networksController;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
    if (networksController.isPlayerSelectQueue((Player) sender)) {
      return Arrays.asList(new String[] { "cancelSelect" });
    } else if (args.length == 1) {
      return Arrays.asList(
          new String[] { "help", "create", "deleteNetwork", "addChest", "setChest", "list", "checkChest", "sorting" });
    } else {
      switch (args[0]) {
        case "create":
        case "deleteNetwork":
          if (args.length == 2) {
            return Arrays.asList(new String[] { "<NetworkName>" });
          }
          return new ArrayList<>();
        case "sorting":
          if (args.length == 2) return getNets((Player) sender);
          if (args.length == 3) return Arrays.asList(new String[] { "on", "off" });
          return new ArrayList<>();
        case "setChest":
        case "addChest":
          if (args.length == 2) {
            return getNets((Player) sender);
          } else if (args.length == 3) {
            return Arrays.asList(new String[] { "input", "storage" });
          } else if (args[2].equals("storage")) {
            return getMaterials(args[3]);
          }
          return new ArrayList<>();
        default:
          return new ArrayList<>();
      }
    }
  }

  private List<String> getNets(Player sender) {
    return networksController.listNets((Player) sender);
  }

  private List<String> getMaterials(String query) {
    List<String> names = new ArrayList<>();
    for (Material material : Material.values()) {
      if (material.name().toUpperCase().startsWith(query.toUpperCase())) {
        names.add(material.name());
      }
    }
    return names;
  }
}
