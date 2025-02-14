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
    private final List<String> frontText;

    @JsonProperty("back_text")
    private final List<String> backText;

    @JsonCreator
    public JsonSignData(@JsonProperty("front_text") List<String> frontText,
                        @JsonProperty("back_text") List<String> backText) {
        this.frontText = frontText != null ? frontText : new ArrayList<>();
        this.backText = backText != null ? backText : new ArrayList<>();
    }

    public List<String> getFrontText() {
        return frontText;
    }

    public List<String> getBackText() {
        return backText;
    }
}
