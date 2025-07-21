package net.slimediamond.espial.api.transaction;

import net.kyori.adventure.audience.Audience;
import net.slimediamond.espial.api.record.EspialRecord;
import org.spongepowered.api.registry.DefaultedRegistryValue;
import org.spongepowered.api.util.annotation.CatalogedBy;

import java.util.List;

/**
 * A type of transaction which does something when applied
 */
@CatalogedBy(TransactionTypes.class)
public interface TransactionType extends DefaultedRegistryValue {

    /**
     * Do a certain action with a provided list of Espial records
     *
     * <p>The list will be sorted for the applied action</p>
     *
     * @param records The records to manipulate or otherwise
     * @return A transaction, which means that it can be undone
     */
    default Transaction apply(final List<EspialRecord> records) {
        return apply(records, Audience.empty());
    }

    /**
     * Do a certain action with a provided list of Espial records
     *
     * <p>The list will be sorted for the applied action</p>
     *
     * @param records The records to manipulate or otherwise
     * @param audience The audience to call back to
     * @return A transaction, which means that it can be undone
     */
    Transaction apply(List<EspialRecord> records, Audience audience);

}
