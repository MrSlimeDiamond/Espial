package net.slimediamond.espial.api.aggregate;

import org.spongepowered.api.Sponge;

/**
 * The result of aggregating {@link net.slimediamond.espial.api.record.EspialRecord}s
 */
public interface ResultAggregate<T> {

    /**
     * Get the key used for the result aggregation
     *
     * <p>This might be, for example, a player UUID or name,
     * a month or year, etc.</p>
     *
     * @return Key
     */
    T getKey();

    /**
     * Get the key as a string
     *
     * @return Key as string
     */
    String getKeyAsString();

    /**
     * Get the amount of results which were aggregated
     *
     * @return Record count
     */
    int getCount();

    static <T> ResultAggregate<T> from(final T key, final int count) {
        return Sponge.game().factoryProvider().provide(Factory.class).create(key, count);
    }

    interface Factory {

        <T> ResultAggregate<T> create(final T key, int count);

    }

}
