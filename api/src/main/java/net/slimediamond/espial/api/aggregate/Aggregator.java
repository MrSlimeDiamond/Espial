package net.slimediamond.espial.api.aggregate;

import net.slimediamond.espial.api.record.EspialRecord;
import org.spongepowered.api.registry.DefaultedRegistryValue;
import org.spongepowered.api.util.annotation.CatalogedBy;

import java.util.List;

/**
 * A display of what records can be aggregated by
 */
@CatalogedBy(Aggregators.class)
public interface Aggregator<T> extends DefaultedRegistryValue {

    /**
     * Aggregate the records into a {@link ResultAggregate} list
     *
     * @param records The records to aggregate accordingly
     * @return The results
     */
    List<ResultAggregate<T>> aggregate(List<EspialRecord> records);

}
