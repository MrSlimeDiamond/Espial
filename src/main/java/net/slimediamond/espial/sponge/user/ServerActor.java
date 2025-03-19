package net.slimediamond.espial.sponge.user;

import net.slimediamond.espial.api.user.EspialActor;

public class ServerActor implements EspialActor {
    @Override
    public String getUUID() {
        return "0";
    }

    @Override
    public boolean isPlayer() {
        return false;
    }
}
