# Espial
Block logging plugin for SpongeAPI for viewing player interactions with blocks (i.e who broke or placed a block)

Inspired by the likes of [CoreProtect](https://www.spigotmc.org/resources/coreprotect.8631/) and [Prism](https://github.com/Libter/SpongePrism).

## Requirements
* Java 21
* SpongeAPI 12 (Minecraft 1.21)

## Command Usage
* **/espial | /es**
  * Base command. If no subcommand is specified, defaults to an info screen (**/espial info**)
  * **lookup | l**
    * Look up a block. Defaults to the block you are looking at.
    * Flags:
      *  *[--single | -s]* - Do not group outputs
      *  Everything from query command flags (below).
  * **rollback | rb**
    * Roll back a block or a range. Defaults to the block you are looking at
    * Flags:
      * Everything from query command flags (below).
  * **restore | rs**
    * Restore a block or a range. Defaults to the block you are looking at
    * Flags:
      * Everything from query command flags (below).

  * **interactive | i**
    * Enter an interactive inspector mode where you can break or place blocks to query.
  * **undo**
    * Revert what you just did.
  * **redo**
    * Revert what you just undid. 

## Query command flags
*  *[--worldedit | -w]* - Use a WorldEdit range
*  *[--range | -r \<range\>]* - Lookup a cuboid range
*  *[--player | -p \<player name\>]* - Only look at actions from a specific player
*  *[--block | -b \<block id\>]* - Only look at a specific block type
