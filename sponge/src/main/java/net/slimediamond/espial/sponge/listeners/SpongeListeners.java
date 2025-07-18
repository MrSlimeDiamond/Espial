package net.slimediamond.espial.sponge.listeners;

import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.event.EspialEvents;
import net.slimediamond.espial.api.query.EspialQuery;
import net.slimediamond.espial.api.record.BlockRecord;
import net.slimediamond.espial.api.record.HangingDeathRecord;
import net.slimediamond.espial.api.record.SignModifyRecord;
import net.slimediamond.espial.common.utils.formatting.Format;
import net.slimediamond.espial.sponge.Espial;
import net.slimediamond.espial.sponge.utils.formatting.RecordFormatter;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.transaction.BlockTransaction;
import org.spongepowered.api.block.transaction.Operation;
import org.spongepowered.api.command.manager.CommandMapping;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.hanging.Hanging;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.entity.ChangeSignEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.plugin.PluginContainer;

import java.util.Optional;

public class SpongeListeners {

    @Listener(order = Order.EARLY)
    public void onBlockChange(final InteractBlockEvent.Primary.Start event, @First ServerPlayer player) {
        if (Espial.getInstance().getEspialService().getInspectingUsers().contains(player.uniqueId())) {
            event.block().location().ifPresent(location -> {
                event.setCancelled(true);
                queryRecords(location, player);
            });
        }
    }

    @Listener(order = Order.EARLY)
    public void onBlockChange(final InteractBlockEvent.Secondary event, @First ServerPlayer player) {
        if (Espial.getInstance().getEspialService().getInspectingUsers().contains(player.uniqueId())
                && event.context().get(EventContextKeys.USED_HAND).map(h -> h.equals(HandTypes.MAIN_HAND.get())).orElse(false)) {
            event.block().location().ifPresent(location -> queryRecords(location.relativeTo(event.targetSide()), player));
        }
    }

    @Listener
    public void onBlockChange(final ChangeBlockEvent.All event) {
        if (event.cause().containsType(CommandMapping.class)
                || event.cause().containsType(PluginContainer.class)) {
            return;
        }
        final Optional<Entity> optionalCause = event.cause().first(Entity.class);
        if (optionalCause.isEmpty()) {
            return;
        }
        final Entity cause = optionalCause.get();
        for (final BlockTransaction transaction : event.transactions()) {
            final BlockSnapshot original = transaction.original();
            final BlockSnapshot replacement = transaction.finalReplacement();
            if (original.location().isEmpty() || replacement.location().isEmpty()) {
                continue; // somehow no location
            }
            final Optional<ServerPlayer> playerOptional = event.cause().first(ServerPlayer.class);

            if (playerOptional.isPresent()
                    && Espial.getInstance().getEspialService().getInspectingUsers().contains(playerOptional.get().uniqueId())) {
                event.setCancelled(true);
                queryRecords(replacement.location().get(), playerOptional.get());
                return;
            }

            final Optional<EspialEvent> eventOptional = getEspialEvent(transaction.operation());
            if (eventOptional.isEmpty()) {
                continue; // unknown operation
            }
            final EspialEvent espialEvent = eventOptional.get();

            final BlockRecord.Builder builder = BlockRecord.builder()
                    .original(original)
                    .replacement(replacement)
                    .entityType(cause.type())
                    .location(original.location().get())
                    .event(espialEvent);

            playerOptional.ifPresent(player -> builder.user(player.uniqueId()));

            Espial.getInstance().getEspialService().submit(builder.build());
        }
    }

    @Listener
    public void onEntityDestruct(final DestructEntityEvent event, @First final Entity cause) {
        final Entity entity = event.entity();
        if (entity instanceof final Hanging hanging) {
            final HangingDeathRecord.Builder builder = HangingDeathRecord.builder()
                    .entityType(cause.type())
                    .targetEntityType(hanging.type())
                    .event(EspialEvents.HANGING_DEATH.get())
                    .location(entity.serverLocation())
                    .extraData(hanging.toContainer());
            if (cause instanceof final ServerPlayer player) {
                builder.user(player.uniqueId());
            }
            Espial.getInstance().getEspialService().submit(builder.build());
        }
    }

    @Listener
    public void onSignChange(final ChangeSignEvent event, @First final Player player) {
        Espial.getInstance().getEspialService().submit(SignModifyRecord.builder()
                .originalContents(event.originalText().all())
                .replacementContents(event.text().all())
                .entityType(player.type())
                .user(player.uniqueId())
                .event(EspialEvents.SIGN_MODIFY.get())
                .location(event.sign().serverLocation())
                .blockState(event.sign().block())
                .build());
    }

    private static Optional<EspialEvent> getEspialEvent(final Operation operation) {
        // see if a Sponge operation matches the value of the ResourceKey
        return EspialEvents.registry().streamEntries()
                .filter(entry -> entry.key().value().equals(operation.key(RegistryTypes.OPERATION).value()))
                .map(RegistryEntry::value)
                .findFirst();
    }

    private static void queryRecords(final ServerLocation location, final Player player) {
        Espial.getInstance().getEspialService().query(EspialQuery.builder()
                        .location(location)
                        .audience(player)
                        .build())
                .thenAccept(records -> {
                    if (records.isEmpty()) {
                        player.sendMessage(Format.NO_RECORDS_FOUND);
                    } else {
                        PaginationList.builder()
                                .title(Format.title("Lookup results"))
                                .padding(Format.PADDING)
                                .contents(RecordFormatter.formatRecords(records, true))
                                .sendTo(player);
                    }
                });
    }

}
