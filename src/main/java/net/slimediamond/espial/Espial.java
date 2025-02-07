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
    public static Component prefix = Component.text("Espial: ").color(NamedTextColor.GREEN);

    public static HashMap<Player, ScheduledTask> blockOutlines = new HashMap<>();

    private final PluginContainer container;
    private final Logger logger;
    private final ConfigurationReference<CommentedConfigurationNode> reference;
    private ValueReference<EspialConfiguration, CommentedConfigurationNode> config;
    private Database database;

    @Inject
    Espial(final PluginContainer container, final Logger logger, final @DefaultConfig(sharedRoot = true) ConfigurationReference<CommentedConfigurationNode> reference) {
        this.container = container;
        this.logger = logger;
        this.reference = reference;
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

        event.register(this.container, Command.builder()
            .permission("espial.command.base")
            .executor(new BaseCommand())
            .addChild(Command.builder()
                    .executor(new BaseCommand())
                    .build(), "info"
            )
            .addChild(Command.builder()
                    .executor(new HelpCommand())
                    .build(), "help"
            )
            .addChild(Command.builder()
                .permission("espial.command.lookup")
                .addFlag(Flag.builder().aliases("single", "s").setParameter(Parameter.bool().key("single").optional().build()).build())
                .addFlag(Flag.builder().aliases("worldedit", "we", "w").setParameter(Parameter.bool().key("use worldedit").optional().build()).build())
                .addFlag(Flag.builder().aliases("range", "r").setParameter(Parameters.LOOKUP_RANGE).build())
                .executor(new LookupCommand(database))
                .build(), "lookup", "l"
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
            .build(), "espial", "es");
    }
}
