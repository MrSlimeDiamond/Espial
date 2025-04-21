package net.slimediamond.espial.api.action;

import net.slimediamond.espial.api.EspialProviders;
import net.slimediamond.espial.api.action.event.EventType;
import net.slimediamond.espial.api.action.event.EventTypes;
import net.slimediamond.espial.api.nbt.NBTData;
import net.slimediamond.espial.api.record.BlockRecord;
import net.slimediamond.espial.api.submittable.Submittable;
import net.slimediamond.espial.api.submittable.SubmittableResult;
import net.slimediamond.espial.api.user.EspialActor;
import net.slimediamond.espial.util.SpongeUtil;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3i;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An action that affects a block
 *
 * @author SlimeDiamond
 */
public interface BlockAction extends Action, NBTStorable, Submittable<BlockRecord> {
    static Builder builder() {
        return new Builder();
    }

    /**
     * Get the block ID (such as minecraft:string)
     *
     * @return Block ID
     */
    String getBlockId();

    default ServerLocation getServerLocation() {
        return ServerLocation.of(
                SpongeUtil.getWorld(getWorld()).orElseThrow(() ->
                        new RuntimeException("Action stores an invalid world")),
                getX(),
                getY(),
                getZ());
    }

    default BlockType getBlockType() {
        return BlockTypes.registry().value(ResourceKey.of(
                getBlockId().split(String.valueOf(ResourceKey.DEFAULT_SEPARATOR))[0],
                getBlockId().split(String.valueOf(ResourceKey.DEFAULT_SEPARATOR))[1]));
    }

    default BlockState getRollbackBlock() {
        AtomicReference<BlockState> blockState = new AtomicReference<>();
        this.getNBT().ifPresentOrElse(nbtData -> {
            if (nbtData.getRollbackBlock() != null) {
                blockState.set(nbtData.getRollbackBlock());
            }

            // legacy stuff now, maybe make a migration thing for this?
            if (nbtData.getDirection() != null) {
                blockState.set(blockState.get().with(Keys.DIRECTION, nbtData.getDirection()).get());
            }

            if (nbtData.getAxis() != null) {
                blockState.set(blockState.get().with(Keys.AXIS, nbtData.getAxis()).get());
            }

            if (nbtData.getGrowthStage() != null) {
                blockState.set(blockState.get().with(Keys.GROWTH_STAGE, nbtData.getGrowthStage()).get());
            }

            if (nbtData.getHalf() != null) {
                blockState.set(blockState.get().with(Keys.PORTION_TYPE, nbtData.getHalf()).get());
            }
        }, () -> {
            // otherwise simply return the block type if it's a break event
            if (getEventType().equals(EventTypes.BREAK)) {
                blockState.set(getBlockType().defaultState());
            }
        });

        BlockState result = blockState.get();
        if (result == null) {
            result = BlockTypes.AIR.get().defaultState();
        }

        return result;
    }

    default BlockState getRestoreBlock() {
        return this.getNBT().map(NBTData::getRestoreBlock).orElse(BlockTypes.AIR.get().defaultState());
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
            this.world = location.worldKey().formatted();

            return this;
        }

        public Builder position(Vector3i position) {
            this.x = position.x();
            this.y = position.y();
            this.z = position.z();

            return this;
        }

        public Builder world(String world) {
            this.world = world;
            return this;
        }

        public Builder snapshot(BlockSnapshot snapshot) {
            return position(snapshot.position())
                    .world(snapshot.world().formatted())
                    .blockId(SpongeUtil.getBlockId(snapshot.state().type()));
        }

        public Builder event(EventType type) {
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
                    return (SubmittableResult<BlockRecord>)
                            EspialProviders.getEspialService().submitAction(this);
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
                public Optional<NBTData> getNBT() {
                    return Optional.ofNullable(nbtData);
                }

                @Override
                public void setNBT(NBTData data) {
                    nbtData = data;
                }
            };
        }
    }
}
