package net.slimediamond.espial;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.api.query.Query;
import net.slimediamond.espial.api.query.QueryType;
import net.slimediamond.espial.commands.*;
import net.slimediamond.espial.util.PlayerSelectionUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.Flag;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.plugin.PluginContainer;

import java.util.ArrayList;
import java.util.List;

public class Commands {
    public static final List<Command.Parameterized> commands = new ArrayList<>();
    private static Command.Parameterized nearbysigns = Command.builder()
            .permission("espial.command.signs")
            .shortDescription(Component.text("Lookup signs nearby"))
            .executor(new NearbySignsCommand())
            .addFlag(Flag.builder().aliases("range", "r").setParameter(CommandParameters.LOOKUP_RANGE).build())
            .build();
    private static Command.Parameterized isthisblockmine = Command.builder()
            .permission("espial.command.myblock")
            .shortDescription(Component.text("Check if a block was placed by you"))
            .executor(new IsThisBlockMineCommand())
            .build();

    static {
        commands.add(Command.builder()
                .permission("espial.command.base")
                .executor(new BaseCommand())
                .shortDescription(Component.text("Base command for Espial"))
                .addChild(Command.builder()
                        .executor(new BaseCommand())
                        .shortDescription(Component.text("Show information about the plugin"))
                        .build(), "info"
                )
                .addChild(Command.builder()
                        .executor(new HelpCommand())
                        .addParameter(CommandParameters.HELP_COMMAND)
                        .shortDescription(Component.text("Display a help screen"))
                        .build(), "help", "?"
                )
                .addChild(Command.builder()
                        .permission("espial.command.lookup")
                        .shortDescription(Component.text("Lookup a block or region"))
                        .addFlag(Flag.builder().aliases("spread", "single", "s").setParameter(Parameter.bool().key("single").optional().build()).build())
                        .addFlag(Flag.builder().aliases("worldedit", "we", "w").setParameter(Parameter.bool().key("use worldedit").optional().build()).build())
                        .addFlag(Flag.builder().aliases("range", "r").setParameter(CommandParameters.LOOKUP_RANGE).build())
                        .addFlag(Flag.builder().aliases("player", "p").setParameter(CommandParameters.LOOKUP_PLAYER).build())
                        .addFlag(Flag.builder().aliases("block", "b").setParameter(CommandParameters.LOOKUP_BLOCK).build())
                        .addFlag(Flag.builder().aliases("time", "t").setParameter(CommandParameters.TIME).build())
                        .executor(context -> Espial.getInstance().getEspialService().execute(context, QueryType.LOOKUP))
                        .build(), "lookup", "l"
                )
                .addChild(Command.builder()
                        .permission("espial.command.lookup")
                        .shortDescription(Component.text("Look up blocks in a range of 5 blocks"))
                        .executor(context -> {
                            if (context.cause().root() instanceof Player player) {
                                Pair<ServerLocation, ServerLocation> locations = PlayerSelectionUtil.getCuboidAroundPlayer(player, 5);
                                Query query = Query.builder()
                                                .setType(QueryType.LOOKUP)
                                                .setMin(locations.getLeft())
                                                .setMax(locations.getRight())
                                                .build();
                                try {
                                    Espial.getInstance().getEspialService().process(query, player, false);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                context.sendMessage(Component.text("You must be a player to use this.").color(NamedTextColor.RED));
                            }
                            return CommandResult.success();
                        }).build(), "near"
                )
                .addChild(nearbysigns, "nearbysigns", "signs", "signsnear")
                .addChild(isthisblockmine, "isthisblockmine", "isthismyblock", "myblock")
                .addChild(Command.builder()
                        .permission("espial.command.rollback")
                        .shortDescription(Component.text("Roll back changes made by players"))
                        .addFlag(Flag.builder().aliases("worldedit", "we", "w").setParameter(Parameter.bool().key("use worldedit").optional().build()).build())
                        .addFlag(Flag.builder().aliases("range", "r").setParameter(CommandParameters.LOOKUP_RANGE).build())
                        .addFlag(Flag.builder().aliases("player", "p").setParameter(CommandParameters.LOOKUP_PLAYER).build())
                        .addFlag(Flag.builder().aliases("block", "b").setParameter(CommandParameters.LOOKUP_BLOCK).build())
                        .addFlag(Flag.builder().aliases("time", "t").setParameter(CommandParameters.TIME).build())
                        .executor(context -> Espial.getInstance().getEspialService().execute(context, QueryType.ROLLBACK))
                        .build(), "rollback", "rb"
                )
                .addChild(Command.builder()
                        .permission("espial.command.restore")
                        .shortDescription(Component.text("Restore changes which were rolled back"))
                        .addFlag(Flag.builder().aliases("worldedit", "we", "w").setParameter(Parameter.bool().key("use worldedit").optional().build()).build())
                        .addFlag(Flag.builder().aliases("range", "r").setParameter(CommandParameters.LOOKUP_RANGE).build())
                        .addFlag(Flag.builder().aliases("player", "p").setParameter(CommandParameters.LOOKUP_PLAYER).build())
                        .addFlag(Flag.builder().aliases("block", "b").setParameter(CommandParameters.LOOKUP_BLOCK).build())
                        .addFlag(Flag.builder().aliases("time", "t").setParameter(CommandParameters.TIME).build())
                        .executor(context -> Espial.getInstance().getEspialService().execute(context, QueryType.RESTORE))
                        .build(), "restore", "rs"
                )
                .addChild(Command.builder()
                        .permission("espial.command.undo")
                        .shortDescription(Component.text("Undo what you just did"))
                        .executor(new TransactionCommands.Undo())
                        .build(), "undo"
                )
                .addChild(Command.builder()
                        .permission("espial.command.redo")
                        .shortDescription(Component.text("Redo what you just undid"))
                        .executor(new TransactionCommands.Redo())
                        .build(), "redo"
                )
                .addChild(Command.builder()
                        .permission("espial.command.inspect")
                        .executor(new InspectCommand())
                        .addParameter(CommandParameters.GENERIC_ID)
                        .addChild(Command.builder()
                                .executor(new InspectCommand())
                                .build(), "stop", "s"
                        )
                        .build(), "inspect"
                )
                .addChild(Command.builder()
                        .permission("espial.command.interactive")
                        .executor(new InteractiveToggleCommand())
                        .shortDescription(Component.text("Enable interactive inspector mode"))
                        .build(), "interactive", "i"
                )
                .addChild(Command.builder()
                        .permission("espial.command.rollbackid")
                        .executor(new TransactionCommands.RollbackId())
                        .addParameter(CommandParameters.GENERIC_ID)
                        .build(), "rollbackid", "rbid"
                )
                .addChild(Command.builder()
                        .permission("espial.command.restoreid")
                        .executor(new TransactionCommands.RestoreId())
                        .addParameter(CommandParameters.GENERIC_ID)
                        .build(), "restoreid", "rsid"
                )
                .build());

        commands.add(Command.builder()
                .permission("espial.command.whoplacedthis")
                .executor(new WhoPlacedThisCommand())
                .shortDescription(Component.text("Show who placed a block"))
                .build());

        commands.add(Command.builder()
                .permission("espial.command.signinfo")
                .executor(new SignInfoCommand())
                .build());
    }

    public static void register(PluginContainer container, RegisterCommandEvent<Command.Parameterized> event) {
        event.register(container, commands.get(0), "espial", "es");
        event.register(container, commands.get(1), "whoplacedthis");
        event.register(container, commands.get(2), "signinfo");
        event.register(container, nearbysigns, "nearbysigns", "signs", "signsnear", "signsnearby");
        event.register(container, isthisblockmine, "isthisblockmine", "isthismyblock", "myblock");
    }
}
