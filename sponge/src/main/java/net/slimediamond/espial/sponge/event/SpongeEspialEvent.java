package net.slimediamond.espial.sponge.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.common.utils.formatting.Format;
import org.jetbrains.annotations.NotNull;

public class SpongeEspialEvent implements EspialEvent {

    private final int id;
    private final String name;
    private final String description;
    private final String verb;

    public SpongeEspialEvent(final int id,
                             @NotNull final String name,
                             @NotNull final String description,
                             @NotNull final String verb) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.verb = verb;
    }

    @Override
    public int getId() {
        return id;
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
                        .append(Format.detail("ID", String.valueOf(this.id)))
                        .appendNewline()
                        .append(Format.detail("Description", this.description)));
    }

    @Override
    public String toString() {
        return "SpongeEspialEvent{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", verb='" + verb + '\'' +
                '}';
    }

}
