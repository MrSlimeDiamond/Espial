package net.slimediamond.espial;

import com.google.inject.Inject;
import net.slimediamond.espial.bridge.SpongeBridgeAPI12;
import net.slimediamond.espial.listeners.PluginListeners;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.reference.ConfigurationReference;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

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
}
