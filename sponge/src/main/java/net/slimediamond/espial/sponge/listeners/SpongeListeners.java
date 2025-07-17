package net.slimediamond.espial.sponge.listeners;

import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.event.EspialEvents;
import net.slimediamond.espial.api.query.EspialQuery;
import net.slimediamond.espial.api.record.EspialBlockRecord;
import net.slimediamond.espial.api.record.EspialHangingDeathRecord;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.record.EspialSignModifyRecord;
import net.slimediamond.espial.common.utils.formatting.Format;
import net.slimediamond.espial.sponge.Espial;
import net.slimediamond.espial.sponge.utils.formatting.RecordFormatter;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.transaction.BlockTransaction;
import org.spongepowered.api.block.transaction.Operation;
import org.spongepowered.api.command.manager.CommandMapping;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.hanging.Hanging;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.entity.ChangeSignEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.plugin.PluginContainer;

import java.util.Optional;
import java.util.UUID;

public class SpongeListeners {

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
        for (BlockTransaction transaction : event.transactions()) {
            final BlockSnapshot original = transaction.original();
            final BlockSnapshot replacement = transaction.finalReplacement();
            if (original.location().isEmpty() || replacement.location().isEmpty()) {
                continue; // somehow no location
            }
            final Optional<ServerPlayer> playerOptional = event.cause().first(ServerPlayer.class);
            if (playerOptional.isPresent()) {
                final UUID uuid = playerOptional.get().uniqueId();
                if (Espial.getInstance().getEspialService().getInspectingUsers().contains(uuid)) {
                    event.setCancelled(true);
                    // lookup at those coordinates
                    final ServerLocation location = transaction.original().location().get();
                    Espial.getInstance().getEspialService().query(EspialQuery.builder()
                                    .location(location)
                                    .audience(playerOptional.get())
                                    .build())
                            .thenAccept(records -> {
                                if (records.isEmpty()) {
                                    playerOptional.get().sendMessage(Format.NO_RECORDS_FOUND);
                                } else {
                                    PaginationList.builder()
                                            .title(Format.title("Lookup results"))
                                            .padding(Format.PADDING)
                                            .contents(RecordFormatter.formatRecords(records, true))
                                            .sendTo(playerOptional.get());
                                }
                            });
                    return;
                }
            }

            final Optional<EspialEvent> eventOptional = getEspialEvent(transaction.operation());
            if (eventOptional.isEmpty()) {
                continue; // unknown operation
            }
            final EspialEvent espialEvent = eventOptional.get();

            final EspialBlockRecord.Builder builder = EspialBlockRecord.builder()
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
            final EspialHangingDeathRecord.Builder builder = EspialHangingDeathRecord.builder()
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
        Espial.getInstance().getEspialService().submit(EspialSignModifyRecord.builder()
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

}
