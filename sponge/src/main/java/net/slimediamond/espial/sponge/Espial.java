package net.slimediamond.espial.sponge;

import com.google.inject.Inject;
import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.query.EspialQuery;
import net.slimediamond.espial.api.record.BlockRecord;
import net.slimediamond.espial.api.record.HangingDeathRecord;
import net.slimediamond.espial.api.record.SignModifyRecord;
import net.slimediamond.espial.api.services.EspialService;
import net.slimediamond.espial.api.services.EspialServiceProvider;
import net.slimediamond.espial.api.transaction.Transaction;
import net.slimediamond.espial.sponge.commands.NearbySignsCommand;
import net.slimediamond.espial.sponge.commands.RootCommand;
import net.slimediamond.espial.sponge.commands.subsystem.AbstractCommand;
import net.slimediamond.espial.sponge.configuration.Configuration;
import net.slimediamond.espial.sponge.data.EspialKeys;
import net.slimediamond.espial.sponge.event.SpongeEspialEventBuilder;
import net.slimediamond.espial.sponge.listeners.SpongeListeners;
import net.slimediamond.espial.sponge.query.SpongeQueryBuilder;
import net.slimediamond.espial.sponge.queue.SpongeRecordingQueue;
import net.slimediamond.espial.sponge.record.SpongeBlockRecordBuilder;
import net.slimediamond.espial.sponge.record.SpongeHangingDeathRecordBuilder;
import net.slimediamond.espial.sponge.record.SpongeSignModifyRecordBuilder;
import net.slimediamond.espial.sponge.registry.EspialRegistryLoader;
import net.slimediamond.espial.sponge.services.SpongeEspialService;
import net.slimediamond.espial.sponge.storage.EspialDatabase;
import net.slimediamond.espial.sponge.transaction.TransactionBuilder;
import net.slimediamond.espial.sponge.utils.formatting.Format;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.RefreshGameEvent;
import org.spongepowered.api.event.lifecycle.RegisterBuilderEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.RegisterDataEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.reference.ConfigurationReference;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

@Plugin("espial")
public class Espial {

    private static Espial instance;
    private EspialService espialService;
    private Configuration config;
    private EspialDatabase database;
    private SpongeRecordingQueue recordingQueue;

    @Inject
    private PluginContainer container;

    @Inject
    private Logger logger;

    @DefaultConfig(sharedRoot = false)
    @Inject
    private ConfigurationReference<@NotNull CommentedConfigurationNode> reference;

    public Espial() {
        if (instance != null) {
            throw new IllegalStateException("Espial is a singleton");
        }
        instance = this;
    }

    @Listener
    public void onConstructPlugin(final ConstructPluginEvent event) throws ConfigurateException {
        this.logger.info("Starting Espial version {} by SlimeDiamond",
                this.container.metadata().version().toString());

        this.espialService = new SpongeEspialService();
        EspialServiceProvider.offer(espialService);
        Sponge.eventManager().registerListeners(this.container, new SpongeListeners());
        Sponge.eventManager().registerListeners(this.container, new EspialRegistryLoader());
        this.config = this.reference.referenceTo(Configuration.class).get();
        this.reference.save();

        this.logger.info("Starting database");
        this.database = new EspialDatabase(this.config.getJdbc());
        try {
            this.database.open();
            this.recordingQueue = new SpongeRecordingQueue();
            this.recordingQueue.start();
            this.logger.info("Database opened");
        } catch (final SQLException e) {
            this.logger.error("Could not open database connection. Espial will not do anything", e);
        }
    }

    @Listener
    public void onStartingServer(final StartingEngineEvent<Server> event) {
        Sponge.asyncScheduler().submit(Task.builder()
                .execute(() -> espialService.getInspectingUsers().forEach(uuid ->
                        Sponge.server().player(uuid).ifPresent(player ->
                                player.sendActionBar(Format.text("You have interactive mode enabled.")))))
                .plugin(container)
                .interval(1, TimeUnit.SECONDS)
                .build());
    }

    @Listener
    public void onStoppingServer(final StoppingEngineEvent<Server> event) {
        this.logger.info("Espial is stopping!");
        this.recordingQueue.setRunning(false);
        this.recordingQueue.interrupt();
    }

    @Listener
    public void onRegisterCommands(final RegisterCommandEvent<Command.Parameterized> event) {
        registerCommand(event, new RootCommand());
        registerCommand(event, new NearbySignsCommand());
    }

    private void registerCommand(final RegisterCommandEvent<Command.Parameterized> event,
                                 final AbstractCommand command) {
        event.register(this.container, command.build(), command.getAliases().getFirst(),
                command.getAliases().toArray(new String[0]));
    }

    @Listener
    public void onRefresh(final RefreshGameEvent event) throws ConfigurateException {
        reload();
    }

    public void reload() throws ConfigurateException {
        logger.info("Reloading");
        this.reference.load();
        this.config = this.reference.referenceTo(Configuration.class).get();
        logger.info("Finished reloading");
    }

    public void saveConfig() throws ConfigurateException {
        logger.info("Saving config!");
        this.reference.save();
    }

    @Listener
    public void onRegisterBuilders(final RegisterBuilderEvent event) {
        event.register(EspialEvent.Builder.class, SpongeEspialEventBuilder::new);
        event.register(BlockRecord.Builder.class, SpongeBlockRecordBuilder::new);
        event.register(HangingDeathRecord.Builder.class, SpongeHangingDeathRecordBuilder::new);
        event.register(SignModifyRecord.Builder.class, SpongeSignModifyRecordBuilder::new);
        event.register(EspialQuery.Builder.class, SpongeQueryBuilder::new);
        event.register(Transaction.Builder.class, TransactionBuilder::new);
    }

    @Listener
    public void onRegisterData(final RegisterDataEvent event) {
        event.register(DataRegistration.of(EspialKeys.WAND, ItemStack.class));
        event.register(DataRegistration.of(EspialKeys.WAND_FILTERS, ItemStack.class));
        event.register(DataRegistration.of(EspialKeys.WAND_TYPE, ItemStack.class));
        event.register(DataRegistration.of(EspialKeys.WAND_MAX_USES, ItemStack.class));
        event.register(DataRegistration.of(EspialKeys.WAND_USES, ItemStack.class));
        event.register(DataRegistration.of(EspialKeys.STAGE_ROLLS_BACK, ItemStack.class));
    }

    public static Espial getInstance() {
        return instance;
    }

    public PluginContainer getContainer() {
        return container;
    }

    public Logger getLogger() {
        return logger;
    }

    public EspialService getEspialService() {
        return espialService;
    }

    public Configuration getConfig() {
        return config;
    }

    public EspialDatabase getDatabase() {
        return database;
    }

    public SpongeRecordingQueue getRecordingQueue() {
        return recordingQueue;
    }

}
