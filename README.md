# Espial
Espial is a SpongeAPI plugin for preventing grief, supporting lookups, rollbacks, restores, and much more.

## v2 warning
Espial version 2 is **not compatible** with below versions. The entire database structure has changed.

Sorry for this inconvenience.

## Requirements
* Java 21
* SpongeAPI 12 or higher (Minecraft 1.21+)

## Command Usage
* **/espial | /es**
  * Permission: espial.command.base
  * Base command. If no subcommand is specified, defaults to an info screen
  * **lookup | l**
    * Permission: espial.command.lookup
    * Query server logs for grief
    * Flags:
      *  `-s` - Do not group outputs
      * Everything from query command flags (below).
  * **near**
    * Permission: espial.command.lookup
    * Look up within 5 blocks of you. Basically an alias for `/es l -r 5`
    * Flags:
      * `-s` - Do not group outputs
  * **rollback | rb**
    * Permission: espial.command.rollback
    * Roll back a selection to a certain point in time. Default limit is 3 days
    * Flags:
      * Everything from query command flags (below).
  * **restore | rs**
    * Permission: espial.command.restore
    * Restore previously rolled back changes. Default limit is 3 days
    * Flags:
      * Everything from query command flags (below).
  * **reload**
    * Permission: espial.command.reload
    * Reload values from the configuration file
  * **interactive | i**
    * Permission: espial.command.interactive
    * Enter an interactive inspector mode where you can break or place blocks to query.
  * **undo**
    * Permission: espial.command.undo
    * Revert your previous action(s).
  * **redo**
    * Permission: espial.command.redo
    * Revert your previous undoals.
  * **nearbysigns**
    * Permission: espial.command.nearbysigns
    * Lookup nearby signs
    * Can also be used as a base command (**/nearbysigns**)
    * Flags: 
      * Everything from query command flags (below).
  * **wand | w**
    * Permission: espial.command.wand
    * Create a wand item which can be used to lookup, rollback, or restore a block
    * Flags:
      * *`-m [maximum]`* - Specify a maximum amount of uses the wand has. When the flag `-m` is specified,
      defaults to 1, but otherwise the maximum uses is infinite.
  * **purge**
    * Permission: espial.command.purge
    * Remove records permanently. Requires the config value `purge-command-enabled` to work (see below)
    * Flags:
      * `-g` - Use a global selection
      * Everything from query command flags (below)

## Query command flags
| Usage                 | Description                                                             |
|-----------------------|-------------------------------------------------------------------------|
| `-w`                  | Use your WorldEdit selection                                            |
| `-r <range>`          | Lookup a cuboid around you                                              |
| `-p <player>`         | Only look at actions from a specific **player**                         |
| `-b <block type>`     | Only look at a specific **block type**                                  |
| `-e <event>`          | Specify a certain **Espial event** to filter for                        |
| `-t <duration>`       | Query for logs **after** a specific date. Specified in duration format  |
| `--before <duration>` | Query for logs **before** a specific date. Specified in duration format |

## Configuration
| Node                  | Default                 | Description                                                                                                         |
|-----------------------|-------------------------|---------------------------------------------------------------------------------------------------------------------|
| ignored-events        | *(empty)*               | A list of events which are ignored, in resource key format (e.g. `espial:break`)                                    |
| jdbc                  | `jdbc:sqlite:espial.db` | The string used to connect to the database, e.g. `jdbc:mysql://localhost:3306/DATABASE?user=USER&password=PASSWORD` |
| log-players-only      | `false`                 | Whether to log only **player-caused events**, not other causes, such as creepers, TNT, etc                          |
| near-range            | `5`                     | The range that the `/es near` and `/es nearbysigns` commands use                                                    |
| purge-command-enabled | `false`                 | Whether the `/es purge` command is enabled, which deletes records **permanently**                                   |

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
Use `gradle build` and find the jars in `build/libs` (not in the submodules)

| Pattern                          | Description                  |
|----------------------------------|------------------------------|
| `api/build/libs/espial-api-*`    | API jar files                |
| `sponge/build/libs/espial-*.jar` | SpongeAPI jar files          |

Other jar files are not relevant.

#### Compile only a specific submodule
You may use `gradle :submodule:build` to get a specific submodule's file. For example: `gradle :api:build` for an API jar.

### API usage
There is an API available (which currently is not very well documented). With Gradle, it can be imported with:

**Gradle (Groovy)**
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

**Gradle (Kotlin)**
```kotlin
repositories {
    maven("https://repo.zenoc.net/repository")
}

dependencies {
    compileOnly("net.slimediamond:espial:{version}")
}
```

Replace `{version}` with the desired version.

## Contact
* Discord: `@slimediamond`
* Telegram: `@MrSlimeDiamond`
* IRC channel: `#slimediamond` (irc.esper.net)

Thank you for using Espial! Please report issues