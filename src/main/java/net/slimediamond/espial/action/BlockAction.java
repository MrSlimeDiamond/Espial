package net.slimediamond.espial.action;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.slimediamond.espial.nbt.NBTData;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.ListValue;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3d;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public interface BlockAction {
    /**
     * The ID (primary key value) of the record.
     * @return Internal ID
     */
    int getId();

    /**
     * Get the actor's UUID, for players, this is a normal UUID string. Server is "0", and other entities are just the names.
     * @return Actor UUID
     */
    String getUuid();

    /**
     * Get the time this action happened
     * @return Timestamp
     */
    Timestamp getTimestamp();

    /**
     * Get the type of action this is
     * @return Action type
     */
    ActionType getActionType();

    /**
     * Get the block ID (such as minecraft:string)
     * @return Block ID
     */
    String getBlockId();

    /**
     * Get the world string (such as minecraft:overworld)
     * @return World
     */
    String getWorld();

    /**
     * Get the position of the actor
     * @return actor position
     */
    Vector3d getActorPosition();

    /**
     * Get the actor's rotation when this happened
     * @return Actor rotation
     */
    Vector3d getActorRotation();

    /**
     * Get the item the actor had in their hand
     * @return Actor item in hand
     */
    String getActorItem();

    /**
     * Get the X coordinate of the modified block.
     * @return Block X
     */
    int getX();

    /**
     * Get the Y coordinate of the modified block.
     * @return Block Y
     */
    int getY();

    /**
     * Get the Z coordinate of the modified block.
     * @return Block Z
     */
    int getZ();

    /**
     * Whether the block has been rolled back
     * @return Rollback status
     */
    boolean isRolledBack();

    /**
     * Set the NBT data for this action.
     * @param data The {@link NBTData} to set it to.
     */
    void setNBT(NBTData data);

    /**
     * Get the NBT data if it is available.
     * @return A {@link Optional} of the block's {@link NBTData}
     */
    Optional<NBTData> getNBT();

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
        this.getNBT().ifPresent(nbtData -> {
            //blockState.set(blockState.get().with(Keys.IS_WATERLOGGED, nbtData.isWaterlogged()).get());
            if (nbtData.getDirection() != null) {
                blockState.set(blockState.get().with(Keys.DIRECTION, nbtData.getDirection()).get());
            }
        });
        return blockState.get();
    }
}
