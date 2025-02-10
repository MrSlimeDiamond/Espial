package net.slimediamond.espial.nbt.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.slimediamond.espial.nbt.NBTData;
import net.slimediamond.espial.nbt.SignData;

import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JsonNBTData implements NBTData {
    @JsonProperty("rotation")
    private int rotation = 0;

    @JsonProperty("sign")
    private JsonSignData sign;

    @JsonProperty("waterlogged")
    private boolean waterlogged;

    public JsonNBTData() {}

    @JsonCreator
    public JsonNBTData(
            @JsonProperty("rotation") int rotation,
            @JsonProperty("sign") JsonSignData sign,
            @JsonProperty("waterlogged") boolean waterlogged
    ) {
        this.rotation = rotation;
        this.sign = sign;
        this.waterlogged = waterlogged;
    }

    @Override
    public Optional<SignData> getSignData() {
        return Optional.of(this.sign);
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
        this.sign = sign;
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
        System.out.println("Deserializing!");
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        return mapper.readValue(json, JsonNBTData.class);
    }
}
