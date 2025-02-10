package net.slimediamond.espial.nbt.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.slimediamond.espial.nbt.SignData;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JsonSignData implements SignData {
    @JsonProperty("front_text")
    private List<String> frontComponents;

    @JsonProperty("back_text")
    private List<String> backComponents;

    @JsonCreator
    public JsonSignData(@JsonProperty("front_text") List<String> frontComponents,
                        @JsonProperty("back_text") List<String> backComponents) {
        this.frontComponents = frontComponents != null ? frontComponents : new ArrayList<>();
        this.backComponents = backComponents != null ? backComponents : new ArrayList<>();
    }

    public List<String> getFrontComponents() {
        return frontComponents;
    }

    public List<String> getBackComponents() {
        return backComponents;
    }
}
