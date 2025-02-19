package net.slimediamond.espial.api.query;

import org.spongepowered.api.block.BlockState;
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

        public Builder setType(QueryType type) {
            this.type = type;
            return this;
        }

        public Builder setMin(ServerLocation min) {
            this.min = min;
            return this;
        }

        public Builder setMax(ServerLocation max) {
            this.max = max;
            return this;
        }

        public Builder setTimestamp(@Nullable Timestamp timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder setPlayerUUID(@Nullable UUID playerUUID) {
            this.playerUUID = playerUUID;
            return this;
        }

        public Builder setBlockId(@Nullable String blockId) {
            this.blockId = blockId;
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
            };
        }
    }
}
