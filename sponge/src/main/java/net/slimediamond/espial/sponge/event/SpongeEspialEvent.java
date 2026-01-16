package net.slimediamond.espial.sponge.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.sponge.utils.formatting.Format;
import org.jetbrains.annotations.NotNull;

public class SpongeEspialEvent implements EspialEvent {

    private final String name;
    private final String description;
    private final String verb;

    public SpongeEspialEvent(@NotNull final String name,
                             @NotNull final String description,
                             @NotNull final String verb) {
        this.name = name;
        this.description = description;
        this.verb = verb;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getVerb() {
        return verb;
    }

    @Override
    public HoverEvent<Component> getHoverEvent() {
                return HoverEvent.showText(Format.title("Event")
                        .appendNewline()
                        .append(Format.detail("Name", this.name))
                        .appendNewline()
                        .append(Format.detail("Description", this.description)));
    }

    @Override
    public String toString() {
        return "SpongeEspialEvent{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", verb='" + verb + '\'' +
                '}';
    }

}
