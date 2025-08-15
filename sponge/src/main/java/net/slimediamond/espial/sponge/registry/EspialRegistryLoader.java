package net.slimediamond.espial.sponge.registry;

import net.slimediamond.espial.api.aggregate.Aggregators;
import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.event.EspialEvents;
import net.slimediamond.espial.api.registry.EspialRegistryTypes;
import net.slimediamond.espial.api.transaction.TransactionType;
import net.slimediamond.espial.api.transaction.TransactionTypes;
import net.slimediamond.espial.api.wand.WandTypes;
import net.slimediamond.espial.sponge.aggregate.DayAggregator;
import net.slimediamond.espial.sponge.aggregate.MonthAggregator;
import net.slimediamond.espial.sponge.aggregate.YearAggregator;
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
        // TODO: More events to fill in missing id entries
        //  {2, 3, 5, 7} are missing
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
                EspialEvents.GROWTH.location(), EspialEvent.builder()
                        .name("Growth")
                        .id(4)
                        .description("Growth of a block")
                        .verb("grew")
                        .build(),
                EspialEvents.MODIFY.location(), EspialEvent.builder()
                        .name("Modify")
                        .id(6)
                        .description("Modify the internal state or data of a block")
                        .verb("modified")
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

        event.register(EspialRegistryTypes.AGGREGATOR.location(), false, () -> Map.of(
                Aggregators.DAY.location(), new DayAggregator(),
                Aggregators.MONTH.location(), new MonthAggregator(),
                Aggregators.YEAR.location(), new YearAggregator()
        ));
    }

}
