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
      *  *[--worldedit | -w]* - Use a WorldEdit range
      *  *[--range | -r \<range\>]* - Lookup a cuboid range
      *  *[--player | -p \<player name\>]* - Only look at actions from a specific player
  * **inspect | i \<id\>**
    * Looks up a specific ID.
  * **inspect stop**
    * Stop block particles for the block you are inspecting.

[Video demonstration](https://youtu.be/gn4QvT5-5Oc)

## Features
- [x] Looking up data at block coordinates
- [x] Looking up data in a region
- [x] Looking up block modification (i.e broken and placed blocks)
- [x] Looking up interactions
- [ ] Rollbacks
- [ ] Interactive inspector (like /co i)
