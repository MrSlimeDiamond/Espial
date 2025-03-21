package net.slimediamond.espial.api.nbt.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.type.PortionType;
import org.spongepowered.api.data.type.PortionTypes;

import java.io.IOException;

public class PortionTypeDeserializer extends JsonDeserializer<PortionType> {
    @Override
    public PortionType deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String half = jsonParser.getValueAsString().toLowerCase();
        return PortionTypes.registry().value(ResourceKey.sponge(half));
    }
}
