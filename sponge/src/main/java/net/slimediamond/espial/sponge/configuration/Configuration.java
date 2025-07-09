package net.slimediamond.espial.sponge.configuration;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class Configuration {

    @Setting
    @Comment("The string used to connect to the database")
    private String jdbc = "jdbc:sqlite:espial.db";

    @Setting
    private int nearRange = 5;

    public String getJdbc() {
        return jdbc;
    }

    public int getNearRange() {
        return nearRange;
    }

}
