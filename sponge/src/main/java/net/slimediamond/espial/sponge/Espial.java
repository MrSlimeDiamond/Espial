package net.slimediamond.espial.sponge;

import com.google.inject.Inject;
import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.query.EspialQuery;
import net.slimediamond.espial.api.record.EspialBlockRecord;
import net.slimediamond.espial.api.record.EspialHangingDeathRecord;
import net.slimediamond.espial.api.record.EspialSignModifyRecord;
import net.slimediamond.espial.api.services.EspialService;
import net.slimediamond.espial.api.transaction.Transaction;
import net.slimediamond.espial.common.utils.formatting.Format;
import net.slimediamond.espial.sponge.commands.RootCommand;
import net.slimediamond.espial.sponge.configuration.Configuration;
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
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.RegisterBuilderEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.reference.ConfigurationReference;
import org.spongepowered.configurate.serialize.SerializationException;
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

    // TODO no shared root
    @DefaultConfig(sharedRoot = true)
    @Inject
    private ConfigurationReference<CommentedConfigurationNode> reference;

    public Espial() {
        if (instance != null) {
            throw new IllegalStateException("Espial is a singleton");
        }
        instance = this;
    }

    @Listener
    public void onConstructPlugin(final ConstructPluginEvent event) throws SerializationException, SQLException {
        this.logger.info("Starting Espial version {} by SlimeDiamond",
                this.container.metadata().version().toString());

        this.espialService = new SpongeEspialService();
        Sponge.eventManager().registerListeners(this.container, new SpongeListeners());
        Sponge.eventManager().registerListeners(this.container, new EspialRegistryLoader());
        this.config = this.reference.referenceTo(Configuration.class).get();

        this.recordingQueue = new SpongeRecordingQueue();
        this.recordingQueue.start();

        this.logger.info("Starting database");
        this.database = new EspialDatabase(this.config.getJdbc());
        this.database.open();
        this.logger.info("Database opened");
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
        RootCommand rootCommand = new RootCommand();
        event.register(this.container, rootCommand.build(), rootCommand.getAliases().getFirst(),
                rootCommand.getAliases().toArray(new String[0]));
    }

    @Listener
    public void onRegisterBuilders(final RegisterBuilderEvent event) {
        event.register(EspialEvent.Builder.class, SpongeEspialEventBuilder::new);
        event.register(EspialBlockRecord.Builder.class, SpongeBlockRecordBuilder::new);
        event.register(EspialHangingDeathRecord.Builder.class,SpongeHangingDeathRecordBuilder::new);
        event.register(EspialSignModifyRecord.Builder.class, SpongeSignModifyRecordBuilder::new);
        event.register(EspialQuery.Builder.class, SpongeQueryBuilder::new);
        event.register(Transaction.Builder.class, TransactionBuilder::new);
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
