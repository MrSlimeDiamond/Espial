package net.slimediamond.espial.nbt.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.slimediamond.espial.nbt.NBTData;
import net.slimediamond.espial.nbt.SignData;

import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JsonNBTData implements NBTData {
    @JsonProperty("rotation")
    private int rotation = 0;

    @JsonProperty("signData")
    private JsonSignData signData = null; // We can't do Optionals for JSON

    @JsonProperty("waterlogged")
    private boolean waterlogged;

    public JsonNBTData() {}

    @JsonCreator
    public JsonNBTData(
            @JsonProperty("rotation") int rotation,
            @JsonProperty("signData") JsonSignData signData,
            @JsonProperty("waterlogged") boolean waterlogged
    ) {
        this.rotation = rotation;
        this.signData = signData;
        this.waterlogged = waterlogged;
    }

    @Override
    @Nullable
    public SignData getSignData() {
        return this.signData;
    }

    @Override
    public int getRotation() {
        return this.rotation;
    }

    @Override
    public boolean isWaterlogged() {
        return this.waterlogged;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
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
