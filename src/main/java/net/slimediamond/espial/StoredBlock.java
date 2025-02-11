package net.slimediamond.espial;

import net.slimediamond.espial.action.ActionType;
import net.slimediamond.espial.nbt.NBTData;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3d;

import java.sql.Timestamp;
import java.util.Optional;

public interface StoredBlock {
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

    // Not scuffed at all.

    /**
     * Get this block as a Sponge block (as it currently stands, not when the block was modified)
     * @return Sponge block
     */
    default BlockSnapshot asSpongeBlock() {
        return BlockSnapshot.builder().from(ServerLocation.of(ResourceKey.of(getWorld().split(":")[0], getWorld().split(":")[1]), getX(), getY(), getZ())).build();
    }
}
