package net.slimediamond.espial.api.action;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.slimediamond.espial.api.nbt.NBTData;

import java.sql.SQLException;
import java.util.Optional;

public interface NBTStorable {
    /**
     * Set the NBT data for this action.
     * @param data The {@link NBTData} to set it to.
     */
    void setNBT(NBTData data) throws JsonProcessingException, SQLException;

    /**
     * Get the NBT data if it is available.
     * @return A {@link Optional} of the block's {@link NBTData}
     */
    Optional<NBTData> getNBT() throws SQLException, JsonProcessingException;
}
