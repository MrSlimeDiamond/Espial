package net.slimediamond.espial.sponge.registry;

import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.event.EspialEvents;
import net.slimediamond.espial.api.registry.EspialRegistryTypes;
import net.slimediamond.espial.api.transaction.TransactionTypes;
import net.slimediamond.espial.sponge.transaction.RestoreTransactionType;
import net.slimediamond.espial.sponge.transaction.RollbackTransactionType;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterRegistryEvent;

import java.util.Map;

public class EspialRegistryLoader {

    @Listener
    public void onRegisterRegistries(final RegisterRegistryEvent.GameScoped event) {
        event.register(EspialRegistryTypes.EVENT.location(), false, () -> Map.of(
                EspialEvents.BREAK.location(), EspialEvent.builder()
                        .name("Break")
                        .id(0)
                        .description("Break a block")
                        .verb("broke")
                        .build(),
                EspialEvents.PLACE.location(), EspialEvent.builder()
                        .name("Place")
                        .id(1)
                        .description("Place a block")
                        .verb("placed")
                        .build()
        ));
        event.register(EspialRegistryTypes.TRANSACTION_TYPE.location(), false, () -> Map.of(
                TransactionTypes.ROLLBACK.location(), new RollbackTransactionType(),
                TransactionTypes.RESTORE.location(), new RestoreTransactionType()
        ));
    }

}
