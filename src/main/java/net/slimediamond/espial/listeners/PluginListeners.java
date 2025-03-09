package net.slimediamond.espial.listeners;

import java.sql.SQLException;
import net.slimediamond.espial.Espial;
import org.spongepowered.api.Server;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.configurate.ConfigurateException;

/**
 * Sponge plugin listener
 * Called from EspialSponge
 *
 * @author SlimeDiamond
 */
public class PluginListeners {
  @Listener
  public void onConstructPlugin(final ConstructPluginEvent event)
      throws ConfigurateException, SQLException {
    Espial.getInstance().onConstructPlugin(event);
  }

  @Listener
  public void onServerStarting(final StartingEngineEvent<Server> event) {
    Espial.getInstance().onServerStarting(event);
  }

  @Listener
  public void onRegisterCommands(final RegisterCommandEvent<Command.Parameterized> event) {
    Espial.getInstance().onRegisterCommands(event);
  }
}
