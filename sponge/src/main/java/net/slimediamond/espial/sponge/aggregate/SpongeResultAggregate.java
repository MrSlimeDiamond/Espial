package net.slimediamond.espial.sponge.aggregate;

import net.slimediamond.espial.api.aggregate.ResultAggregate;

public final class SpongeResultAggregate<T> implements ResultAggregate<T> {

    private final T key;
    private final int count;

    public SpongeResultAggregate(final T key, final int count) {
        this.key = key;
        this.count = count;
    }

    @Override
    public T getKey() {
        return key;
    }

    @Override
    public String getKeyAsString() {
        return key.toString();
    }

    @Override
    public int getCount() {
        return count;
    }

    public static final class FactoryImpl implements ResultAggregate.Factory {

        @Override
        public <T> ResultAggregate<T> create(final T key, final int count) {
            return new SpongeResultAggregate<>(key, count);
        }

    }

}
