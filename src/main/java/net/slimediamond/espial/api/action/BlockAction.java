package net.slimediamond.espial.api.action;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.api.action.event.EventType;
import net.slimediamond.espial.api.nbt.NBTData;
import net.slimediamond.espial.api.record.BlockRecord;
import net.slimediamond.espial.api.submittable.Submittable;
import net.slimediamond.espial.api.submittable.SubmittableResult;
import net.slimediamond.espial.api.user.EspialActor;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.world.server.ServerLocation;

import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public interface BlockAction extends Action, NBTStorable, Submittable<BlockRecord> {
    /**
     * Get the block ID (such as minecraft:string)
     * @return Block ID
     */
    String getBlockId();

    default ServerLocation getServerLocation() {
        return ServerLocation.of(ResourceKey.of(getWorld().split(":")[0], getWorld().split(":")[1]), getX(), getY(), getZ());
    }

    default BlockType getBlockType() {
        return BlockTypes.registry().value(ResourceKey.of(
                getBlockId().split(String.valueOf(ResourceKey.DEFAULT_SEPARATOR))[0],
                getBlockId().split(String.valueOf(ResourceKey.DEFAULT_SEPARATOR))[1]));
    }

    default BlockState getState() {
        AtomicReference<BlockState> blockState = new AtomicReference<>(getBlockType().defaultState());
        try {
            this.getNBT().ifPresent(nbtData -> {
                if (nbtData.getDirection() != null) {
                    blockState.set(blockState.get().with(Keys.DIRECTION, nbtData.getDirection()).get());
                }

                if (nbtData.getAxis() != null) {
                    blockState.set(blockState.get().with(Keys.AXIS, nbtData.getAxis()).get());
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return blockState.get();
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        private String blockId;
        private EspialActor actor;
        private int x, y, z;
        private String world;
        private EventType type;
        private NBTData nbtData;

        public Builder blockId(String blockId) {
            this.blockId = blockId;
            return this;
        }

        public Builder actor(EspialActor actor) {
            this.actor = actor;
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

        public Builder world(String world) {
            this.world = world;
            return this;
        }

        public Builder type(EventType type) {
            this.type = type;
            return this;
        }

        public Builder withNBTData(NBTData nbtData) {
            this.nbtData = nbtData;
            return this;
        }

        public BlockAction build() {
            return new BlockAction() {
                @Override
                public SubmittableResult<BlockRecord> submit() throws Exception {
                    return (SubmittableResult<BlockRecord>) Espial.getInstance().getEspialService().submitAction(this);
                }

                @Override
                public String getBlockId() {
                    return blockId;
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
                    return type;
                }

                @Override
                public void setNBT(NBTData data) {
                    nbtData = data;
                }

                @Override
                public Optional<NBTData> getNBT() {
                    return Optional.ofNullable(nbtData);
                }
            };
        }
    }
}
