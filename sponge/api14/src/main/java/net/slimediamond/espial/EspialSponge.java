package net.slimediamond.espial;

import com.google.inject.Inject;
import net.slimediamond.espial.bridge.SpongeBridgeAPI14;
import net.slimediamond.espial.util.BlockUtil;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.RegisterDataEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.reference.ConfigurationReference;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.sql.SQLException;

// API 14
@Plugin("espial")
public class EspialSponge extends Espial {
    @Inject
    EspialSponge(
            final PluginContainer container,
            final Logger logger,
            final @DefaultConfig(sharedRoot = true) ConfigurationReference<CommentedConfigurationNode>
                    reference) {
        super(container, logger, reference);

        instance = this;
        spongeBridge = new SpongeBridgeAPI14();
    }

    // Wait for Sponge's registry to initialize before adding any blocks
    @Listener
    public void onDataRegister(RegisterDataEvent event) {
        BlockUtil.SIGNS.add(BlockTypes.PALE_OAK_WALL_HANGING_SIGN.get());
        BlockUtil.SIGNS.add(BlockTypes.PALE_OAK_HANGING_SIGN.get());
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
