package net.slimediamond.espial.api.transaction;

import net.slimediamond.espial.api.record.EspialRecord;
import org.spongepowered.api.util.annotation.CatalogedBy;

import java.util.List;

/**
 * A type of transaction which does something when applied
 */
@CatalogedBy(TransactionTypes.class)
public interface TransactionType {

    /**
     * Get whether this type of transaction can be undone
     *
     * @return Whether this transaction type can be undone
     */
    boolean canBeUndone();

    /**
     * Do a certain action with a provided list of Espial records
     *
     * @param records The records to manipulate or otherwise
     * @return A transaction, which means that it can be undone
     */
    Transaction apply(List<EspialRecord> records);

}
