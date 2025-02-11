package net.slimediamond.espial.nbt.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.slimediamond.espial.nbt.NBTData;
import net.slimediamond.espial.nbt.SignData;
import org.spongepowered.api.util.Direction;

import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JsonNBTData implements NBTData {
    @JsonProperty("direction")
    private Direction direction;

    @JsonProperty("signData")
    private JsonSignData signData = null; // We can't do Optionals for JSON

    @JsonProperty("waterlogged")
    private boolean waterlogged;

    public JsonNBTData() {}

    @JsonCreator
    public JsonNBTData(
            @JsonProperty("direction") Direction direction,
            @JsonProperty("signData") JsonSignData signData,
            @JsonProperty("waterlogged") boolean waterlogged
    ) {
        this.direction = direction;
        this.signData = signData;
        this.waterlogged = waterlogged;
    }

    @Override
    @Nullable
    public SignData getSignData() {
        return this.signData;
    }

    @Override
    public Direction getDirection() {
        return this.direction;
    }

    @Override
    public boolean isWaterlogged() {
        return this.waterlogged;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void setSignData(JsonSignData sign) {
        this.signData = sign;
    }

    public void setWaterlogged(boolean waterlogged) {
        this.waterlogged = waterlogged;
    }

    // Serialization utility
    public static String serialize(NBTData data) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(data);
    }

    // Deserialization utility
    public static JsonNBTData deserialize(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(json, JsonNBTData.class);
    }
}
