package net.slimediamond.espial;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.api.EspialService;
import net.slimediamond.espial.api.transaction.TransactionManager;
import net.slimediamond.espial.sponge.transaction.EspialTransactionImpl;
import net.slimediamond.espial.listeners.ChangeBlockListener;
import net.slimediamond.espial.listeners.InteractListener;
import net.slimediamond.espial.listeners.PlayerLeaveListener;
import net.slimediamond.espial.listeners.SignInteractEvent;
import net.slimediamond.espial.sponge.EspialServiceImpl;
import net.slimediamond.espial.sponge.transaction.TransactionManagerImpl;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
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
import java.util.*;
import java.util.concurrent.TimeUnit;

@Plugin("espial")
public class Espial {
    private static Espial instance;

    public static Component prefix = Component.text("Espial › ").color(NamedTextColor.GREEN);

    private final PluginContainer container;
    private final Logger logger;
    private final ConfigurationReference<CommentedConfigurationNode> reference;
    private final List<UUID> inspectingPlayers = new ArrayList<>();
    private final Map<Player, ScheduledTask> blockOutlines = new HashMap<>();
    private final Map<Object, List<EspialTransactionImpl>> transactions = new HashMap<>();

    private ValueReference<EspialConfiguration, CommentedConfigurationNode> config;
    private Database database;
    private EspialService espialService;
    private TransactionManager transactionManager;

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

        espialService = new EspialServiceImpl();
        transactionManager = new TransactionManagerImpl();

        database = new Database();
        database.open(this.config.get().jdbc());

        Component message = Espial.prefix
                .append(Component.text("Interactive mode is enabled. Disable it with ").color(NamedTextColor.WHITE)
                .append(Component.text("/es i").color(NamedTextColor.YELLOW))
                .append(Component.text(".").color(NamedTextColor.WHITE))
        );
        Task task = Task.builder().execute(() ->
                inspectingPlayers.forEach(uuid -> {
                    Sponge.server().player(uuid).ifPresent(player -> {
                        player.sendActionBar(message);
                    });
                })).plugin(container).interval(1, TimeUnit.SECONDS).build();

        Sponge.asyncScheduler().submit(task, "Espial interactive mode broadcast");
    }

    @Listener
    public void onServerStarting(final StartingEngineEvent<Server> event) {
        Sponge.eventManager().registerListeners(container, new ChangeBlockListener());
        Sponge.eventManager().registerListeners(container, new InteractListener());
        Sponge.eventManager().registerListeners(container, new PlayerLeaveListener());
        Sponge.eventManager().registerListeners(container, new SignInteractEvent());
    }

    @Listener
    public void onRegisterCommands(final RegisterCommandEvent<Command.Parameterized> event) {
        Commands.register(this.container, event);
    }

    public PluginContainer getContainer() {
        return this.container;
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

    public EspialService getEspialService() {
        return espialService;
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public List<UUID> getInspectingPlayers() {
        return inspectingPlayers;
    }

    public Map<Player, ScheduledTask> getBlockOutlines() {
        return blockOutlines;
    }

    public Map<Object, List<EspialTransactionImpl>> getTransactions() {
        return transactions;
    }

    public Logger getLogger() {
        return this.logger;
    }
}
