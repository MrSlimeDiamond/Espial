package net.slimediamond.espial;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class EspialConfiguration {
  @Setting private String jdbc = "jdbc:sqlite:espial.db";
  @Setting private boolean logPlayerPosition = true;
  @Setting private boolean logServerChanges = false;
  @Setting private boolean logInteractions = true;

  public String jdbc() {
    return this.jdbc;
  }

  public boolean logPlayerPosition() {
    return logPlayerPosition;
  }

  public boolean logServerChanges() {
    return logServerChanges;
  }

  public boolean logInteractions() {
    return logInteractions;
  }
}
