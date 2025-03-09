package net.slimediamond.espial;

import com.google.inject.Inject;
import net.slimediamond.espial.bridge.SpongeBridgeAPI12;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.reference.ConfigurationReference;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.sql.SQLException;

@Plugin("espial")
public class EspialSpongeAPI12 extends Espial {
    @Inject
    EspialSpongeAPI12(PluginContainer container,
                      Logger logger,
                      @DefaultConfig(sharedRoot = true) ConfigurationReference<CommentedConfigurationNode> reference) {
        super(container, logger, reference);

        instance = this;
        spongeBridge = new SpongeBridgeAPI12();
    }

    @Listener
    public void onConstructPlugin(ConstructPluginEvent event)
            throws SQLException, ConfigurateException {
        super.onConstructPlugin(event);
    }

    @Listener
    public void onServerStarting(final StartingEngineEvent<Server> event) {
        super.onServerStarting(event);
    }

    @Listener
    public void onRegisterCommands(final RegisterCommandEvent<Command.Parameterized> event) {
        super.onRegisterCommands(event);
    }
}
