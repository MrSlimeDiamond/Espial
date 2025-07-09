package net.slimediamond.espial.sponge.event;

import net.slimediamond.espial.api.event.EspialEvent;
import org.jetbrains.annotations.NotNull;

public class SpongeEspialEventBuilder implements EspialEvent.Builder {

    private int id;
    private String name;
    private String description;
    private String verb;

    @Override
    public EspialEvent.Builder id(final int id) {
        this.id = id;
        return this;
    }

    @Override
    public EspialEvent.Builder name(@NotNull final String name) {
        this.name = name;
        return this;
    }

    @Override
    public EspialEvent.Builder description(@NotNull final String description) {
        this.description = description;
        return this;
    }

    @Override
    public EspialEvent.Builder verb(@NotNull final String verb) {
        this.verb = verb;
        return this;
    }

    @Override
    public @NotNull EspialEvent build() {
        return new SpongeEspialEvent(id, name, description, verb);
    }

}
