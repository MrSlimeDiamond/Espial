package net.slimediamond.espial.sponge.wand.types;

import net.slimediamond.espial.api.query.EspialQuery;
import net.slimediamond.espial.api.transaction.Transaction;
import net.slimediamond.espial.api.transaction.TransactionType;
import net.slimediamond.espial.api.wand.WandType;
import net.slimediamond.espial.sponge.Espial;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.ItemStack;

public class TransactionWand implements WandType {

    private final TransactionType transactionType;

    public TransactionWand(final TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    @Override
    public void apply(final EspialQuery query, final ServerPlayer player, final ItemStack itemStack) {
        Espial.getInstance().getEspialService().query(query).thenAccept(records -> {
            final Transaction transaction = transactionType.apply(records, player);
            Espial.getInstance().getEspialService().getTransactionManager().submit(player.uniqueId(), transaction);
        });
    }
}
