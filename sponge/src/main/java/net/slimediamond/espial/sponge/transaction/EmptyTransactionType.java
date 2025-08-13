package net.slimediamond.espial.sponge.transaction;

import net.kyori.adventure.audience.Audience;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.transaction.Transaction;
import net.slimediamond.espial.api.transaction.TransactionType;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.List;

public class EmptyTransactionType implements TransactionType {

    @Override
    public Transaction apply(final List<EspialRecord> records, final Audience audience) {
        return Transaction.empty();
    }

    @Override
    public Transaction preview(final List<EspialRecord> records, final ServerPlayer viewer) {
        return Transaction.empty();
    }

}
