package net.slimediamond.espial.api.nbt.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.slimediamond.espial.api.nbt.NBTData;
import net.slimediamond.espial.api.nbt.SignData;
import net.slimediamond.espial.api.nbt.json.deserializer.PortionTypeDeserializer;
import org.spongepowered.api.data.type.PortionType;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.util.Direction;

import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JsonNBTData implements NBTData {
    @JsonProperty("direction")
    private Direction direction;

    @JsonProperty("axis")
    private Axis axis;

    @JsonProperty("growth_stage")
    private Integer growthStage;

    @JsonProperty("rollback_block")
    private String rollbackBlock;

    @JsonProperty("signData")
    private JsonSignData signData;

    @JsonProperty("half")
    @JsonDeserialize(using = PortionTypeDeserializer.class)
    private PortionType half;

    @JsonProperty("waterlogged")
    private Boolean waterlogged;

    public JsonNBTData() {
    }

    @JsonCreator
    public JsonNBTData(
            @JsonProperty("direction") Direction direction,
            @JsonProperty("axis") Axis axis,
            @JsonProperty("growth_stage") Integer growthStage,
            @JsonProperty("rollback_block") String rollbackBlock,
            @JsonProperty("signData") JsonSignData signData,
            @JsonProperty("half") PortionType half,
            @JsonProperty("waterlogged") Boolean waterlogged) {
        this.direction = direction;
        this.axis = axis;
        this.growthStage = growthStage;
        this.rollbackBlock = rollbackBlock;
        this.signData = signData;
        this.half = half;
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

    @Override
    @Nullable
    public SignData getSignData() {
        return this.signData;
    }

    public void setSignData(JsonSignData sign) {
        this.signData = sign;
    }

    @Override
    public Direction getDirection() {
        return this.direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    @Nullable
    @Override
    public Integer getGrowthStage() {
        return growthStage;
    }

    public void setGrowthStage(Integer growthStage) {
        this.growthStage = growthStage;
    }

    @Nullable
    @Override
    public String getRollbackBlock() {
        return rollbackBlock;
    }

    public void setRollbackBlock(String rollbackBlock) {
        this.rollbackBlock = rollbackBlock;
    }

    @Override
    public Axis getAxis() {
        return this.axis;
    }

    @Override
    public PortionType getHalf() {
        return half;
    }

    public void setHalf(PortionType half) {
        this.half = half;
    }

    public void setAxis(Axis axis) {
        this.axis = axis;
    }

    @Override
    public Boolean isWaterlogged() {
        return this.waterlogged;
    }

    public void setWaterlogged(boolean waterlogged) {
        this.waterlogged = waterlogged;
    }
}
