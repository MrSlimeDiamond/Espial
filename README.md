# Espial
Espial is a SpongeAPI plugin for preventing grief, supporting lookups, rollbacks, restores, and much more.

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
      *  *[--spread | -s]* - Do not group outputs
      * Everything from query command flags (below).
  * **near**
    * Permission: espial.command.lookup
    * Look up within 5 blocks of you. Basically an alias for **/es l -r 5**
    * Flags:
      * Everything from query command flags (below).
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
  * **nearbysigns | signsnear | signs**
    * Permission: espial.command.signs
    * Lookup nearby signs
    * Can also be used as a base command (**/nearbysigns**)
    * Flags: 
      * *[--range | -r \<range\>]* - Lookup a cuboid range
  * **isthisblockmine | isthismyblock | myblock**
    * Permission: espial.command.myblock
    * Check if a block was placed by you
    * Can also be used as a base command (**/isthisblockmine**)
  * **whoplacedthis**
    * Permission: espial.command.whoplacedthis
    * Show the player who placed a block and nothing more.
    * Can also be used as a base command (**/whoplacedthis**)

## Query command flags
| Aliases                            | Description                                 |
|------------------------------------|---------------------------------------------|
| *[--worldedit \| -w]*              | Use your WorldEdit selection                |
| *[--range \| -r \<range\>]*        | Lookup a cuboid around you                  |
| *[--player \| -p \<player name\>]* | Only look at actions from a specific player |
| *[--block \| -b \<block id\>]*     | Only look at a specific block type          |

## Server admin usage
> Espial only supports SpongeAPI 12+

1. Download a jar file from [Ore](https://ore.spongepowered.org/SlimeDiamond/Espial/versions) or [GitHub](https://github.com/MrSlimeDiamond/Espial/releases/)
2. Download a database loader: Tested with [SqliteLoader](https://ore.spongepowered.org/whimxiqal/SqliteLoader) and [MySQLDriver](https://ore.spongepowered.org/Semenkovsky_Ivan/MySQLDriver). (MySQL/MariaDB is recommended)
   1. If you are using MySQL/MariaDB, you will need to set up a database manually.
3. Put both downloaded jar files in your Sponge server's `mods/` directory
4. Have a play around! Make sure the plugin works. Break some blocks and try commands like `/es near`, and also `/es help`

## Development
The following tools are required
* A programming IDE (I recommend: [IntelliJ IDEA](https://www.jetbrains.com/idea/))
* Java 21
* Gradle (though a wrapper is included: `./gradlew` on \*nix and `.\gradlew.bat` on Windows)
* [Git](https://git-scm.com/)

### Compiling
| Directory   | Description                 |
|-------------|-----------------------------|
| src/        | Common source code (Sponge) |
| api/        | API source code             |

#### General compile
Use `gradle build` and find the jars in `build/libs` (not in the submodules)

| Pattern                   | Description                  |
|---------------------------|------------------------------|
| `build/libs/espial-api-*` | API jar files                |
| `build/libs/espial-*.jar` | SpongeAPI jar files          |

Other jar files are not relevant.

#### Compile only a specific submodule
You may use `gradle :submodule:build` to get a specific submodule's file. For example: `gradle :api:build` for an API jar.

### API usage
There is an API available (which currently is not very well documented). With Gradle, it can be imported with:
```groovy
repositories {
  maven {
    name = 'zenoc-repo'
    url = 'https://repo.zenoc.net/repository'
  }
  
  dependencies {
    compileOnly 'net.slimediamond:espial:{version}'
  }
}
```

## Contact
* Discord: `@slimediamond`
* Telegram: `@MrSlimeDiamond`
* IRC channel: `#slimediamond` (irc.esper.net)

Thank you for using Espial! Please report issues