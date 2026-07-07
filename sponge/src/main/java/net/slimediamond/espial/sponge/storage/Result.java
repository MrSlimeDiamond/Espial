package net.slimediamond.espial.sponge.storage;

import net.slimediamond.espial.api.event.EspialEvent;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.Date;
import java.util.UUID;

public record Result(
        String type,
        EspialEvent event,
        int id,
        Date date,
        UUID user,
        EntityType<?> entityType,
        ResourceKey worldKey,
        int x, int y, int z,
        ServerLocation location,
        Date rolledBack) {
}
