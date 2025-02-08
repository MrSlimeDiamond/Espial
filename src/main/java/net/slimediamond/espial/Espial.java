package net.slimediamond.espial;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.commands.*;
import net.slimediamond.espial.listeners.ChangeBlockListener;
import net.slimediamond.espial.listeners.InteractListener;
import net.slimediamond.espial.listeners.PlayerLeaveListener;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.Flag;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.world.server.ServerLocation;
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

    private Command.Parameterized espialCommand;

    @Inject
    Espial(final PluginContainer container, final Logger logger, final @DefaultConfig(sharedRoot = true) ConfigurationReference<CommentedConfigurationNode> reference) {
        this.container = container;
        this.logger = logger;
        this.reference = reference;

        instance = this;
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
                        .addParameter(Parameters.HELP_COMMAND)
                        .shortDescription(Component.text("Display a help screen"))
                        .build(), "help", "?"
                )
                .addChild(Command.builder()
                        .permission("espial.command.lookup")
                        .shortDescription(Component.text("Lookup a block or region"))
                        .addFlag(Flag.builder().aliases("single", "s").setParameter(Parameter.bool().key("single").optional().build()).build())
                        .addFlag(Flag.builder().aliases("worldedit", "we", "w").setParameter(Parameter.bool().key("use worldedit").optional().build()).build())
                        .addFlag(Flag.builder().aliases("range", "r").setParameter(Parameters.LOOKUP_RANGE).build())
                        .addFlag(Flag.builder().aliases("player", "p").setParameter(Parameters.LOOKUP_PLAYER).build())
                        .addFlag(Flag.builder().aliases("block", "b").setParameter(Parameters.LOOKUP_BLOCK).build())
                        .addFlag(Flag.builder().aliases("time", "t").setParameter(Parameters.TIME).build())
                        .executor(new LookupCommand(database))
                        .build(), "lookup", "l"
                )
                .addChild(Command.builder()
                        .permission("espial.command.rollback")
                        .shortDescription(Component.text("Roll back changes made by players"))
                        .addFlag(Flag.builder().aliases("worldedit", "we", "w").setParameter(Parameter.bool().key("use worldedit").optional().build()).build())
                        .addFlag(Flag.builder().aliases("range", "r").setParameter(Parameters.LOOKUP_RANGE).build())
                        .addFlag(Flag.builder().aliases("player", "p").setParameter(Parameters.LOOKUP_PLAYER).build())
                        .addFlag(Flag.builder().aliases("block", "b").setParameter(Parameters.LOOKUP_BLOCK).build())
                        .addFlag(Flag.builder().aliases("time", "t").setParameter(Parameters.TIME).build())
                        .executor(new RollbackCommand(database))
                        .build(), "rollback", "rb"
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
                        .addParameter(Parameters.ROLLBACK_ID)
                        .build(), "rollbackid", "rbid"
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

    public RollbackStatus rollback(StoredBlock block) throws SQLException {
        if (block.rolledBack()) return RollbackStatus.ALREADY_ROLLEDBACK;

        // roll back this specific ID to another state
        if (block.actionType() == ActionType.BREAK) {
            // place the block which was broken at that location
            BlockType blockType = BlockTypes.registry().value(ResourceKey.of(block.blockId().split(":")[0], block.blockId().split(":")[1]));

            block.sponge().location().get().setBlock(blockType.defaultState());

            database.setRolledBack(block.uid(), true);

            return RollbackStatus.SUCCESS;
        } if (block.actionType() == ActionType.PLACE) {
            // EDGE CASE: We're always going to rollback places to air. This probably will cause no harm
            // since one must remove a block first before placing a block. But this might cause issues somehow, not sure.
            // (it'll be fine, probably)

            block.sponge().location().get().setBlock(BlockTypes.AIR.get().defaultState());
            database.setRolledBack(block.uid(), true);
            return RollbackStatus.SUCCESS;
        } else {
            return RollbackStatus.UNSUPPORTED;
        }
    }
}
