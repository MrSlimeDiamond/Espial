package net.slimediamond.espial.sponge.user;

import net.slimediamond.espial.api.user.EspialActor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.math.vector.Vector3d;

public class ServerActor implements EspialActor {
    @Override
    public String getUUID() {
        return "0";
    }

    @Override
    public boolean isPlayer() {
        return false;
    }

    @Override
    public @Nullable Vector3d getPosition() {
        return null;
    }

    @Override
    public @Nullable Vector3d getRotation() {
        return null;
    }

    @Override
    public String getItem() {
        return "";
    }
}
