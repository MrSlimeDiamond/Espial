package net.slimediamond.espial;

import com.google.inject.Inject;
import net.slimediamond.espial.bridge.SpongeBridgeAPI14;
import net.slimediamond.espial.util.BlockUtil;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterDataEvent;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.reference.ConfigurationReference;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

@Plugin("espial")
public class EspialSpongeAPI14 extends Espial {
    @Inject
    EspialSpongeAPI14(
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
}
