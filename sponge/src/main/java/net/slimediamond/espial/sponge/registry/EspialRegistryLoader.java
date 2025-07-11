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
                        .build(),
                EspialEvents.DECAY.location(), EspialEvent.builder()
                        .name("Decay")
                        .id(3)
                        .description("Natural block decay")
                        .verb("decayed")
                        .build(),
                EspialEvents.GROWTH.location(), EspialEvent.builder()
                        .name("Growth")
                        .id(4)
                        .description("Growth of a block")
                        .verb("grew")
                        .build(),
                EspialEvents.LIQUID_DECAY.location(), EspialEvent.builder()
                        .name("Liquid Decay")
                        .id(5)
                        .description("Liquid Decay")
                        .verb("decayed")
                        .build(),
                EspialEvents.MODIFY.location(), EspialEvent.builder()
                        .name("Modify")
                        .id(6)
                        .description("Modify the state of a block")
                        .verb("modified")
                        .build(),
                EspialEvents.INTERACT.location(), EspialEvent.builder()
                        .name("Interact")
                        .id(7)
                        .description("Secondarily interact with a block")
                        .verb("used")
                        .build()
        ));

        event.register(EspialRegistryTypes.TRANSACTION_TYPE.location(), false, () -> Map.of(
                TransactionTypes.ROLLBACK.location(), new RollbackTransactionType(),
                TransactionTypes.RESTORE.location(), new RestoreTransactionType()
        ));
    }

}
