package net.slimediamond.espial.api.query;

import net.kyori.adventure.audience.Audience;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.world.server.ServerLocation;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.UUID;

public interface Query {
    /**
     * Get query type
     * @return Query type
     */
    QueryType getType();
    /**
     * The minimum location of the query
     * @return Query minimum location
     */
    ServerLocation getMin();

    /**
     * The maximum location of the query
     * @return Query maximum location
     */
    ServerLocation getMax();

    /**
     * The earliest date to lookup
     * @return Query minimum timestamp
     */
    @Nullable
    Timestamp getTimestamp();

    /**
     * The player UUID to target
     * @return Target player UUID
     */
    @Nullable
    UUID getPlayerUUID();

    /**
     * The block type we are targeting
     * @return Block type
     */
    @Nullable
    String getBlockId();

    /**
     * Get sorting order of this query
     * @return Sort order
     */
    Sort getSort();

    /**
     * The user which called this
     * Identified for 
     * @return
     */
    Object getUser();

    /**
     * An audience to call back to
     * @return Audience
     */
    Audience getAudience();

    /**
     * Whether to spread the results. Only looks on {@link QueryType} LOOKUP
     * @return Spread
     */
    boolean isSpread();

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        private QueryType type;
        private ServerLocation min;
        private ServerLocation max;
        private Timestamp timestamp;
        private UUID playerUUID;
        private String blockId;
        private Sort sort;
        private Object user;
        private Audience audience;
        boolean spread;

        public Builder type(QueryType type) {
            this.type = type;
            return this;
        }

        public Builder min(ServerLocation min) {
            this.min = min;
            return this;
        }

        public Builder max(ServerLocation max) {
            this.max = max;
            return this;
        }

        public Builder after(@Nullable Timestamp timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder player(@Nullable UUID playerUUID) {
            this.playerUUID = playerUUID;
            return this;
        }

        public Builder block(@Nullable String blockId) {
            this.blockId = blockId;
            return this;
        }

        public Builder sort(@NonNull Sort sort) {
            this.sort = sort;
            return this;
        }

        public Builder caller(@NonNull Object user) {
            this.user = user;
            return this;
        }

        public Builder audience(@NonNull Audience audience) {
            this.audience = audience;
            return this;
        }

        public Builder spread(boolean spread) {
            this.spread = spread;
            return this;
        }

        public Query build() {
            return new Query() {
                @Override
                public QueryType getType() {
                    return type;
                }

                @Override
                public ServerLocation getMin() {
                    return min;
                }

                @Override
                public ServerLocation getMax() {
                    return max;
                }

                @Override
                public @Nullable Timestamp getTimestamp() {
                    return timestamp;
                }

                @Override
                public @Nullable UUID getPlayerUUID() {
                    return playerUUID;
                }

                @Override
                public @Nullable String getBlockId() {
                    return blockId;
                }

                @Override
                public Sort getSort() {
                    return sort == null ? Sort.DEFAULT : sort;
                }

                @Override
                public Object getUser() {
                    return user;
                }

                @Override
                public Audience getAudience() {
                    return audience;
                }

                @Override
                public boolean isSpread() {
                    return spread;
                }
            };
        }
    }
}
