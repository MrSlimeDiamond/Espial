package net.slimediamond.espial.sponge.listeners;

import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.event.EspialEvents;
import net.slimediamond.espial.api.query.EspialQuery;
import net.slimediamond.espial.api.record.EspialBlockRecord;
import net.slimediamond.espial.common.utils.formatting.Format;
import net.slimediamond.espial.sponge.Espial;
import net.slimediamond.espial.sponge.utils.formatting.RecordFormatter;
import org.spongepowered.api.Server;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.transaction.BlockTransaction;
import org.spongepowered.api.block.transaction.Operation;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.Optional;
import java.util.UUID;

public class SpongeListeners {

    @Listener
    public void onBlockChange(final ChangeBlockEvent.All event) {
        for (BlockTransaction transaction : event.transactions()) {
            if (transaction.original().location().isEmpty()) {
                continue; // somehow no location
            }
            final Optional<ServerPlayer> playerOptional = event.cause().first(ServerPlayer.class);
            if (playerOptional.isEmpty()) {
                continue;
            }

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
                                playerOptional.get().sendMessage(Format.error("No records were found"));
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

            final Optional<EspialEvent> eventOptional = getEspialEvent(transaction.operation());
            if (eventOptional.isEmpty()) {
                continue; // unknown operation
            }
            final EspialEvent espialEvent = eventOptional.get();

            final BlockSnapshot blockSnapshot;
            if (espialEvent.equals(EspialEvents.PLACE.get())) {
                // it is the block after
                blockSnapshot = transaction.finalReplacement();
            } else {
                blockSnapshot = transaction.original();
            }

            Espial.getInstance().getEspialService().submit(
                    EspialBlockRecord.builder()
                        .blockState(blockSnapshot.state())
                        .location(blockSnapshot.location().get())
                        .user(uuid)
                        .event(espialEvent)
                        .build());
        }
    }

    private static Optional<EspialEvent> getEspialEvent(Operation operation) {
        // see if a Sponge operation matches the value of the ResourceKey
        return EspialEvents.registry().streamEntries()
                .filter(entry -> entry.key().value().equals(operation.key(RegistryTypes.OPERATION).value()))
                .map(RegistryEntry::value)
                .findFirst();
    }

}
