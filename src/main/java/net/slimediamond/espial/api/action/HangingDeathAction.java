package net.slimediamond.espial.api.action;

import net.slimediamond.espial.Espial;
import net.slimediamond.espial.api.action.event.EventType;
import net.slimediamond.espial.api.nbt.NBTData;
import net.slimediamond.espial.api.record.EntityRecord;
import net.slimediamond.espial.api.submittable.Submittable;
import net.slimediamond.espial.api.submittable.SubmittableResult;
import net.slimediamond.espial.api.user.EspialActor;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.hanging.Hanging;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.Optional;

public interface HangingDeathAction extends EntityDeathAction, NBTStorable, Submittable<EntityRecord> {

    EntityType<?> getEntityType();

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        private EspialActor actor;
        private EntityType<?> entityType;
        private int x, y, z;
        private String world;
        private EventType eventType;
        private NBTData nbtData;

        public Builder actor(EspialActor actor) {
            this.actor = actor;
            return this;
        }

        public Builder entity(EntityType<?> entityType) {
            this.entityType = entityType;
            return this;
        }

        public Builder x(int x) {
            this.x = x;
            return this;
        }

        public Builder y(int y) {
            this.y = y;
            return this;
        }

        public Builder z(int z) {
            this.z = z;
            return this;
        }

        public Builder location(ServerLocation location) {
            this.x = location.blockX();
            this.y = location.blockY();
            this.z = location.blockZ();
            return this;
        }

        public Builder withNBTData(NBTData nbtData) {
            this.nbtData = nbtData;
            return this;
        }

        public Builder world(String world) {
            this.world = world;
            return this;
        }

        public Builder event(EventType eventType) {
            this.eventType = eventType;
            return this;
        }

        public HangingDeathAction build() {
            return new HangingDeathAction() {

                @Override
                public EntityType<?> getEntityType() {
                    return entityType;
                }

                @Override
                public EspialActor getActor() {
                    return actor;
                }

                @Override
                public int getX() {
                    return x;
                }

                @Override
                public int getY() {
                    return y;
                }

                @Override
                public int getZ() {
                    return z;
                }

                @Override
                public String getWorld() {
                    return world;
                }

                @Override
                public EventType getEventType() {
                    return eventType;
                }

                @Override
                public void setNBT(NBTData data) {
                    nbtData = data;
                }

                @Override
                public Optional<NBTData> getNBT() {
                    return Optional.ofNullable(nbtData);
                }

                @Override
                public SubmittableResult<EntityRecord> submit() throws Exception {
                    return SubmittableResult.of((EntityRecord) Espial.getInstance().getDatabase().submit(this).orElse(null));
                }
            };
        }
    }
}
