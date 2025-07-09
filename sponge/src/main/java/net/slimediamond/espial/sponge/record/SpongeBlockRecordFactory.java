package net.slimediamond.espial.sponge.record;

import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.record.EspialBlockRecord;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3i;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

public final class SpongeBlockRecordFactory implements RecordFactory<EspialBlockRecord> {

    @Override
    public EspialBlockRecord create(@NotNull final EspialEvent event, @NotNull final ResultSet rs) throws SQLException {
        final int id = rs.getInt("id");
        final Date date = Date.from(rs.getTimestamp("time").toInstant());
        final UUID user = UUID.fromString(rs.getString("player_uuid"));
        final BlockState blockState = BlockState.fromString(rs.getString("target"));
        final ResourceKey worldKey = ResourceKey.resolve(rs.getString("world"));
        final int x = rs.getInt("x");
        final int y = rs.getInt("y");
        final int z = rs.getInt("z");
        final boolean rolledBack = rs.getBoolean("rolled_back");

        final ServerLocation serverLocation = ServerLocation.of(worldKey, Vector3i.from(x, y, z));

        return new SpongeBlockRecord(id, date, user, serverLocation, event, blockState, rolledBack);
    }

}
