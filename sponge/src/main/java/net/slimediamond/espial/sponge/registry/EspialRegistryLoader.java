package net.slimediamond.espial.sponge.registry;

import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.event.EspialEvents;
import net.slimediamond.espial.api.registry.EspialRegistryTypes;
import net.slimediamond.espial.api.transaction.TransactionType;
import net.slimediamond.espial.api.transaction.TransactionTypes;
import net.slimediamond.espial.api.wand.WandTypes;
import net.slimediamond.espial.sponge.transaction.RestoreTransactionType;
import net.slimediamond.espial.sponge.transaction.RollbackTransactionType;
import net.slimediamond.espial.sponge.wand.types.DebugWand;
import net.slimediamond.espial.sponge.wand.types.LookupWand;
import net.slimediamond.espial.sponge.wand.types.StageWand;
import net.slimediamond.espial.sponge.wand.types.TransactionWand;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterRegistryEvent;

import java.util.Map;

public class EspialRegistryLoader {

    private final TransactionType rollback = new RollbackTransactionType();
    private final TransactionType restore = new RestoreTransactionType();

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
                        .description("Modify the internal state or data of a block")
                        .verb("modified")
                        .build(),
                EspialEvents.INTERACT.location(), EspialEvent.builder()
                        .name("Interact")
                        .id(7)
                        .description("Secondarily interact with a block")
                        .verb("used")
                        .build(),
                EspialEvents.HANGING_DEATH.location(), EspialEvent.builder()
                        .name("Hanging Death")
                        .id(8)
                        .description("Kill an entity which is 'Hanging' (e.g. paintings or item frames)")
                        .verb("killed")
                        .build(),
                EspialEvents.SIGN_MODIFY.location(), EspialEvent.builder()
                        .name("Sign Modification")
                        .id(9)
                        .description("Modify the contents of a sign")
                        .verb("modified")
                        .build()
        ));

        event.register(EspialRegistryTypes.TRANSACTION_TYPE.location(), false, () -> Map.of(
                TransactionTypes.ROLLBACK.location(), rollback,
                TransactionTypes.RESTORE.location(), restore
        ));

        event.register(EspialRegistryTypes.WAND_TYPE.location(), false, () -> Map.of(
                WandTypes.LOOKUP.location(), new LookupWand(),
                WandTypes.ROLLBACK.location(), new TransactionWand(rollback),
                WandTypes.RESTORE.location(), new TransactionWand(restore),
                WandTypes.STAGE.location(), new StageWand(),
                WandTypes.DEBUG.location(), new DebugWand()
        ));
    }

}
