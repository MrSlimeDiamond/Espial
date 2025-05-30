package net.slimediamond.espial;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class EspialConfiguration {
    @Setting
    private String jdbc = "jdbc:sqlite:espial.db";
    @Setting
    private boolean logInteractions = true;
    @Setting
    private int defaultLookupRange = 5;
    @Setting
    private String defaultTime = "3d";
    @Setting
    private boolean debug = false;

    public String getConnectionString() {
        return this.jdbc;
    }

    public boolean logInteractions() {
        return logInteractions;
    }

    public int getDefaultLookupRange() {
        return defaultLookupRange;
    }

    public String getDefaultTime() {
        return defaultTime;
    }

    public boolean isDebugModeEnabled() {
        return debug;
    }
}
