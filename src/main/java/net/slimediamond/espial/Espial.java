package net.slimediamond.espial;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.commands.*;
import net.slimediamond.espial.listeners.ChangeBlockListener;
import net.slimediamond.espial.listeners.InteractListener;
import net.slimediamond.espial.listeners.PlayerLeaveListener;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.Flag;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.reference.ConfigurationReference;
import org.spongepowered.configurate.reference.ValueReference;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.sql.SQLException;
import java.util.HashMap;

@Plugin("espial")
public class Espial {
    private static Espial instance;

    public static Component prefix = Component.text("Espial: ").color(NamedTextColor.GREEN);

    public static HashMap<Player, ScheduledTask> blockOutlines = new HashMap<>();

    private final PluginContainer container;
    private final Logger logger;
    private final ConfigurationReference<CommentedConfigurationNode> reference;

    private ValueReference<EspialConfiguration, CommentedConfigurationNode> config;
    private Database database;
    private BlockLogService blockLogService;

    private Command.Parameterized espialCommand;

    @Inject
    Espial(final PluginContainer container, final Logger logger, final @DefaultConfig(sharedRoot = true) ConfigurationReference<CommentedConfigurationNode> reference) {
        this.container = container;
        this.logger = logger;
        this.reference = reference;

        instance = this;

        blockLogService = new BlockLogService();
    }

    @Listener
    public void onConstructPlugin(final ConstructPluginEvent event) throws ConfigurateException, SQLException {
        this.config = this.reference.referenceTo(EspialConfiguration.class);
        this.reference.save();

        database = new Database(this.config.get().logPlayerPosition());
        database.open(this.config.get().jdbc());
    }

    @Listener
    public void onServerStarting(final StartingEngineEvent<Server> event) {
        Sponge.eventManager().registerListeners(container, new ChangeBlockListener(database));
        Sponge.eventManager().registerListeners(container, new InteractListener(database));
        Sponge.eventManager().registerListeners(container, new PlayerLeaveListener());
    }

    @Listener
    public void onRegisterCommands(final RegisterCommandEvent<Command.Parameterized> event) {
        Parameter.Value<Integer> idParameter = Parameter.integerNumber().key("id").optional().build();
        this.espialCommand = Command.builder()
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
                        .addFlag(Flag.builder().aliases("single", "s").setParameter(Parameter.bool().key("single").optional().build()).build())
                        .addFlag(Flag.builder().aliases("worldedit", "we", "w").setParameter(Parameter.bool().key("use worldedit").optional().build()).build())
                        .addFlag(Flag.builder().aliases("range", "r").setParameter(CommandParameters.LOOKUP_RANGE).build())
                        .addFlag(Flag.builder().aliases("player", "p").setParameter(CommandParameters.LOOKUP_PLAYER).build())
                        .addFlag(Flag.builder().aliases("block", "b").setParameter(CommandParameters.LOOKUP_BLOCK).build())
                        .addFlag(Flag.builder().aliases("time", "t").setParameter(CommandParameters.TIME).build())
                        .executor(context -> Espial.getInstance().getBlockLogService().doSelectiveCommand(context, EspialTransactionType.LOOKUP))
                        .build(), "lookup", "l"
                )
                .addChild(Command.builder()
                        .permission("espial.command.rollback")
                        .shortDescription(Component.text("Roll back changes made by players"))
                        .addFlag(Flag.builder().aliases("worldedit", "we", "w").setParameter(Parameter.bool().key("use worldedit").optional().build()).build())
                        .addFlag(Flag.builder().aliases("range", "r").setParameter(CommandParameters.LOOKUP_RANGE).build())
                        .addFlag(Flag.builder().aliases("player", "p").setParameter(CommandParameters.LOOKUP_PLAYER).build())
                        .addFlag(Flag.builder().aliases("block", "b").setParameter(CommandParameters.LOOKUP_BLOCK).build())
                        .addFlag(Flag.builder().aliases("time", "t").setParameter(CommandParameters.TIME).build())
                        .executor(context -> Espial.getInstance().getBlockLogService().doSelectiveCommand(context, EspialTransactionType.ROLLBACK))
                        .build(), "rollback", "rb"
                )
                .addChild(Command.builder()
                        .permission("espial.command.restore")
                        .shortDescription(Component.text("Restore changes which have been rolled back"))
                        .addFlag(Flag.builder().aliases("worldedit", "we", "w").setParameter(Parameter.bool().key("use worldedit").optional().build()).build())
                        .addFlag(Flag.builder().aliases("range", "r").setParameter(CommandParameters.LOOKUP_RANGE).build())
                        .addFlag(Flag.builder().aliases("player", "p").setParameter(CommandParameters.LOOKUP_PLAYER).build())
                        .addFlag(Flag.builder().aliases("block", "b").setParameter(CommandParameters.LOOKUP_BLOCK).build())
                        .addFlag(Flag.builder().aliases("time", "t").setParameter(CommandParameters.TIME).build())
                        .executor(context -> Espial.getInstance().getBlockLogService().doSelectiveCommand(context, EspialTransactionType.RESTORE))
                        .build(), "restore", "rs"
                )
                .addChild(Command.builder()
                        .permission("espial.command.undo")
                        .shortDescription(Component.text("Undo what you just did"))
                        .executor(new UndoCommand())
                        .build(), "undo"
                )
                .addChild(Command.builder()
                        .permission("espial.command.redo")
                        .shortDescription(Component.text("Redo what you just undid"))
                        .executor(new RedoCommand())
                        .build(), "redo"
                )
                .addChild(Command.builder()
                        .permission("espial.command.inspect")
                        .executor(new InspectCommand(idParameter, database, container))
                        .addParameter(idParameter)
                        .addChild(Command.builder()
                                .executor(new InspectCommand(idParameter, database, container))
                                .build(), "stop", "s"
                        )
                        .build(), "inspect", "i"
                )
                .addChild(Command.builder()
                        .permission("espial.command.rollbackid")
                        .executor(new RollbackIdCommand(database))
                        .addParameter(CommandParameters.ROLLBACK_ID)
                        .build(), "rollbackid", "rbid"
                )
                .addChild(Command.builder()
                        .permission("espial.command.restoreid")
                        .executor(new RestoreIdCommand(database))
                        .addParameter(CommandParameters.ROLLBACK_ID)
                        .build(), "restoreid", "rsid"
                )
                .build();

        event.register(this.container, espialCommand, "espial", "es");

        event.register(this.container, Command.builder()
                .permission("espial.whoplacedthis")
                .executor(new WhoPlacedThisCommand(database))
                .build(), "whoplacedthis"
        );
    }

    public PluginContainer getContainer() {
        return this.container;
    }

    public Command.Parameterized getEspialCommand() {
        return this.espialCommand;
    }

    public ValueReference<EspialConfiguration, CommentedConfigurationNode> getConfig() {
        return config;
    }

    public static Espial getInstance() {
        return instance;
    }

    public Database getDatabase() {
        return this.database;
    }

    public BlockLogService getBlockLogService() {
        return blockLogService;
    }
}
