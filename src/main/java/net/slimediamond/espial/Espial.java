package net.slimediamond.espial;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.api.EspialService;
import net.slimediamond.espial.api.EspialProviders;
import net.slimediamond.espial.api.transaction.TransactionManager;
import net.slimediamond.espial.bridge.SpongeBridge;
import net.slimediamond.espial.commands.BaseCommand;
import net.slimediamond.espial.commands.IsThisBlockMineCommand;
import net.slimediamond.espial.commands.NearbySignsCommand;
import net.slimediamond.espial.commands.WhoPlacedThisCommand;
import net.slimediamond.espial.commands.subsystem.AbstractCommand;
import net.slimediamond.espial.listeners.BlockListeners;
import net.slimediamond.espial.listeners.EntityListeners;
import net.slimediamond.espial.listeners.InteractListener;
import net.slimediamond.espial.listeners.PlayerLeaveListener;
import net.slimediamond.espial.listeners.PluginListeners;
import net.slimediamond.espial.listeners.SignInteractEvent;
import net.slimediamond.espial.sponge.EspialServiceImpl;
import net.slimediamond.espial.util.Format;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.reference.ConfigurationReference;
import org.spongepowered.configurate.reference.ValueReference;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Main Espial plugin class
 *
 * @author SlimeDiamond
 */
@Plugin("espial")
public class Espial {
  protected static Espial instance;
  protected SpongeBridge spongeBridge;
  private final PluginContainer container;
  private final Logger logger;
  private final ConfigurationReference<CommentedConfigurationNode> reference;
  private final List<UUID> inspectingPlayers = new ArrayList<>();
  private final Map<Player, ScheduledTask> blockOutlines = new HashMap<>();

  private ValueReference<EspialConfiguration, CommentedConfigurationNode> config;
  private Database database;

  @Inject
  protected Espial(
      final PluginContainer container,
      final Logger logger,
      final @DefaultConfig(sharedRoot = true) ConfigurationReference<CommentedConfigurationNode>
              reference) {
    this.container = container;
    this.logger = logger;
    this.reference = reference;

    // for plugin construction, server starting, etc
    Sponge.eventManager().registerListeners(container, new PluginListeners());
  }

  public static Espial getInstance() {
    return instance;
  }

  public SpongeBridge getSpongeBridge() {
    return spongeBridge;
  }

  public void onConstructPlugin(final ConstructPluginEvent event) throws ConfigurateException, SQLException {
    showPluginSplash();

    this.config = this.reference.referenceTo(EspialConfiguration.class);
    this.reference.save();

    EspialProviders.setEspialService(new EspialServiceImpl());

    database = new Database();
    database.open(this.config.get().jdbc());

    Component message = Format.component(Component.text()
                .append(Component.text("Interactive mode is enabled. Disable it with ")
                        .color(NamedTextColor.WHITE)
                        .append(Component.text("/es i").color(NamedTextColor.YELLOW))
                        .append(Component.text(".").color(NamedTextColor.WHITE))));
    Task task = Task.builder().execute(() ->
                    inspectingPlayers.forEach(uuid -> {
                          Sponge.server()
                              .player(uuid)
                              .ifPresent(
                                  player -> {
                                    player.sendActionBar(message);
                                  });
                        }))
            .plugin(container)
            .interval(1, TimeUnit.SECONDS)
            .build();

    Sponge.asyncScheduler().submit(task, "Espial interactive mode broadcast");
  }

  public void onServerStarting(final StartingEngineEvent<Server> event) {
    Sponge.eventManager().registerListeners(container, new BlockListeners());
    Sponge.eventManager().registerListeners(container, new InteractListener());
    Sponge.eventManager().registerListeners(container, new PlayerLeaveListener());
    Sponge.eventManager().registerListeners(container, new SignInteractEvent());
    Sponge.eventManager().registerListeners(container, new EntityListeners());
  }

  public void onRegisterCommands(final RegisterCommandEvent<Command.Parameterized> event) {
    // Root command (/espial)
    register(event, new BaseCommand());

    // Separate commands
    register(event, new WhoPlacedThisCommand());
    register(event, new IsThisBlockMineCommand());
    register(event, new NearbySignsCommand());
  }

  private void register(RegisterCommandEvent<Command.Parameterized> event,
                        AbstractCommand command) {
    event.register(container, command.build(),
            command.getAliases().get(0),
            command.getAliases().toArray(new String[0]));
  }

  public void showPluginSplash() {
    logger.info("Espial - Version {}", container.metadata().version().toString());
  }

  public PluginContainer getContainer() {
    return this.container;
  }

  public ValueReference<EspialConfiguration, CommentedConfigurationNode> getConfig() {
    return config;
  }

  public Database getDatabase() {
    return this.database;
  }

  public EspialService getEspialService() {
    return EspialProviders.getEspialService();
  }

  public TransactionManager getTransactionManager() {
    return getEspialService().getTransactionManager();
  }

  public List<UUID> getInspectingPlayers() {
    return inspectingPlayers;
  }

  public Map<Player, ScheduledTask> getBlockOutlines() {
    return blockOutlines;
  }

  public Logger getLogger() {
    return this.logger;
  }
}
