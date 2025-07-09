package net.slimediamond.espial.api.transaction;

import net.slimediamond.espial.api.record.EspialRecord;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;

import java.util.List;

/**
 * A transaction, which is the result of manipulation records
 * via {@link TransactionType#apply(List)}
 */
public interface Transaction {

    /**
     * Get the type of transaction
     *
     * @return Transaction type
     */
    TransactionType getTransactionType();

    /**
     * Get a list of records which this transaction
     * affects
     *
     * @return Affected records
     */
    List<EspialRecord> getRecords();

    /**
     * Apply the transaction
     *
     * @return The generated {@link Transaction} (not this one!)
     */
    default Transaction apply() {
        return getTransactionType().apply(getRecords());
    }

    /**
     * Undo the transaction
     *
     * @return {@code true} if successful, {@code false} if not
     */
    boolean undo();

    static Builder builder() {
        return Sponge.game().builderProvider().provide(Builder.class);
    }

    interface Builder extends org.spongepowered.api.util.Builder<Transaction, Builder> {

        /**
         * Specify the {@link TransactionType}
         *
         * <p><strong>This is required</strong></p>
         *
         * @param type The type
         * @return This builder, for chaining
         */
        Builder type(@NotNull TransactionType type);

        /**
         * Specify the records which were manipulated to form
         * the transaction
         *
         * <p><strong>This is required</strong></p>
         *
         * @param records The records
         * @return This builder, for chaining
         */
        Builder records(@NotNull List<EspialRecord> records);

    }

}
