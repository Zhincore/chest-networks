# Chest Networks

ChestNetworks allows you to create networks of chests; chests that sort items between themselves.

This is useful for automatic warehouses.

## Usage

There are two kinds of chests in a chest network:

- **input** - don't store items, but only send them into storage chests if possible
- **storage** - store items
  - Can have defined list of items to store, other items will be sent elsewhere if possible.
  - If it has no list of items, it's a "misc" chest and will contain any items that don't fit in other chests

_Storage_ chests will try to send "foreign" items to the correct chest, meaning you can just dump your whole inventory into any chest in the network and let the network handle it.

Sorting is triggered when closing the chest's inventory and by hoppers (if the event isn't disabled\*).

### ChestNetwork command:

/%s <subcommand> - Main command (Aliases: %s)

#### Subcommands:

help - Displays this help (Alias: h)
create <new_network_name> - Creates new chest network with the given name (Alias: c)
delete <network_name> - Deletes the whole chest network (doesn't ask, double check the name before deleting)
ist - Shows a list of your chest networks (Aliases: listNets, listNetworks)
addChest <network_name> <chest_type ('storage' or 'input')> [content...] - Adds new chest into the named chest network of given type or changes the setting for already registered chest; if the type is 'storage', you need to specify what items the chest should contain, you can put multiple names of items separated by spaces; If you don't specify any contents, the network will put items that can't go anywhere else into this chest (a 'misc.' chest). After entering this command, you will be asked to right-click the desired chest. (Alias: setChest)
checkChest - Shows info about the chest. After entering this command, you will be asked to right-click the desired chest. (Alias: check)
cancelSelect - Cancels selecting a chest and cancels previously typed ChestNetworks command if you're required to select a chest. (Aliases: cancel)

## TODO

[ ] Proper file names
[ ] Use ACF?
[ ] ACF localization?
[X] Own sorting
[ ] RAID0 sorting on chests with same content
[ ] Better separation of concerns
[ ] Proper (de)serialization + data manipulation
[ ] Storage chest priority
[ ] YAML translation file
[ ] Proper help
[ ] Online manual
