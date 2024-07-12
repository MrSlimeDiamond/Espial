package net.slimediamond.espial;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class EspialConfiguration {
    @Setting private String jdbc = "jdbc:sqlite:espial.db";

    public String jdbc() {
        return this.jdbc;
    }
}
