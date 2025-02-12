# Espial
Block logging plugin for SpongeAPI for viewing player interactions with blocks (i.e who broke or placed a block)

Inspired by the likes of [CoreProtect](https://www.spigotmc.org/resources/coreprotect.8631/) and [Prism](https://github.com/darkhelmet-gaming/Prism).

## Requirements
* Java 21
* SpongeAPI 12 or higher (Minecraft 1.21+)

## Command Usage
* **/espial | /es**
  * Permission: espial.command.base
  * Base command. If no subcommand is specified, defaults to an info screen (**/espial info**)
  * **lookup | l**
    * Permission: espial.command.lookup
    * Look up a block. Defaults to the block you are looking at.
    * Flags:
      *  *[--single | -s]* - Do not group outputs
      *  Everything from query command flags (below).
  * **rollback | rb**
    * Permission: espial.command.rollback
    * Roll back a block or a range. Defaults to the block you are looking at
    * Flags:
      * Everything from query command flags (below).
  * **restore | rs**
    * Permission: espial.command.restore
    * Restore a block or a range. Defaults to the block you are looking at
    * Flags:
      * Everything from query command flags (below).
  * **interactive | i**
    * Permission: espial.command.interactive
    * Enter an interactive inspector mode where you can break or place blocks to query.
  * **undo**
    * Permission: espial.command.undo
    * Revert you previous action(s).
  * **redo**
    * Permission: espial.command.redo
    * Revert your previous undoals.
* **/whoplacedthis**
  * Permission: espial.command.whoplacedthis
  * Show the player who placed a block and nothing more.

## Query command flags
*  *[--worldedit | -w]* - Use a WorldEdit range
*  *[--range | -r \<range\>]* - Lookup a cuboid range
*  *[--player | -p \<player name\>]* - Only look at actions from a specific player
*  *[--block | -b \<block id\>]* - Only look at a specific block type
