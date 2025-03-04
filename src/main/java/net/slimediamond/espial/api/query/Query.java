package net.slimediamond.espial.api.query;

import net.kyori.adventure.audience.Audience;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.submittable.Submittable;
import net.slimediamond.espial.api.submittable.SubmittableResult;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.world.server.ServerLocation;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/**
 * A query.
 *
 * @author SlimeDiamond
 */
public interface Query extends Submittable<List<EspialRecord>> {
  static Builder builder() {
    return new Builder();
  }

  /**
   * Get query type
   *
   * @return Query type
   */
  QueryType getType();

  /**
   * The minimum location of the query
   *
   * @return Query minimum location
   */
  ServerLocation getMin();

  /**
   * The maximum location of the query
   *
   * @return Query maximum location
   */
  ServerLocation getMax();

  /**
   * The earliest date to lookup
   *
   * @return Query minimum timestamp
   */
  @Nullable
  Timestamp getTimestamp();

  /**
   * The player UUID to target
   *
   * @return Target player UUID
   */
  @Nullable
  List<UUID> getPlayerUUIDs();

  /**
   * The block type we are targeting
   *
   * @return Block type
   */
  @Nullable
  List<String> getBlockIds();

  /**
   * Get sorting order of this query
   *
   * @return Sort order
   */
  Sort getSort();

  /**
   * The user which called this Identified for
   *
   * @return
   */
  Object getUser();

  /**
   * An audience to call back to
   *
   * @return Audience
   */
  Audience getAudience();

  /**
   * Whether to spread the results. Only looks on {@link QueryType} LOOKUP
   *
   * @return Spread
   */
  boolean isSpread();

  class Builder {
    boolean spread;
    private QueryType type;
    private ServerLocation min;
    private ServerLocation max;
    private Timestamp timestamp;
    private List<UUID> playerUUIDs;
    private List<String> blockIds;
    private Sort sort;
    private Object user;
    private Audience audience;

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

    public Builder players(@Nullable List<UUID> playerUUIDs) {
      this.playerUUIDs = playerUUIDs;
      return this;
    }

    public Builder blocks(@Nullable List<String> blockIds) {
      this.blockIds = blockIds;
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
        public SubmittableResult<List<EspialRecord>> submit() throws Exception {
          return Espial.getInstance().getEspialService().submitQuery(this);
        }

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
        public @Nullable List<UUID> getPlayerUUIDs() {
          return playerUUIDs;
        }

        @Override
        public @Nullable List<String> getBlockIds() {
          return blockIds;
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
