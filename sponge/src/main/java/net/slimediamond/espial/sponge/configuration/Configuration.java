package net.slimediamond.espial.sponge.configuration;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.LinkedList;
import java.util.List;

@ConfigSerializable
public class Configuration {

    @Setting
    @Comment("The string used to connect to the database")
    private String jdbc = "jdbc:sqlite:espial.db";

    @Setting
    private int nearRange = 5;

    @Setting
    @Comment("A list of events which are ignored, in resource key format " +
            "(for example 'espial:break')")
    private List<ResourceKey> ignoredEvents = new LinkedList<>();

    @Setting
    @Comment("With this enabled, only events done by players are logged")
    private boolean logPlayersOnly = false;

    @Setting
    @Comment("Whether the '/es purge' command is enabled. " +
            "This is very dangerous, hence the config value")
    private boolean purgeCommandEnabled = false;

    public String getJdbc() {
        return jdbc;
    }

    public int getNearRange() {
        return nearRange;
    }

    public List<ResourceKey> getIgnoredEvents() {
        return ignoredEvents;
    }

    public boolean isLogPlayersOnly() {
        return logPlayersOnly;
    }

    public boolean isPurgeCommandEnabled() {
        return purgeCommandEnabled;
    }

}
