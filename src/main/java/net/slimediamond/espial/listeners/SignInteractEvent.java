package net.slimediamond.espial.listeners;

import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.StoredBlock;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.entity.ChangeSignEvent;
import org.spongepowered.api.world.server.ServerLocation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.StringJoiner;

public class SignInteractEvent {
    @Listener
    public void onSignChangeEvent(ChangeSignEvent event) throws SQLException {
        // split by "|" when we insert into the database, so we can get each line again later
        // Not the best solution tbh
        StringJoiner stringJoiner = new StringJoiner("|");

        event.text().forEach(line -> stringJoiner.add(GsonComponentSerializer.gson().serialize(line)));

        ServerLocation location = event.sign().serverLocation();
        ArrayList<StoredBlock> blocks = Espial.getInstance().getDatabase().queryBlock(location.world().key().formatted(), location.blockX(), location.blockY(), location.blockZ(), null, null, null);

        StoredBlock block = blocks.get(0); // last index is 0 for some reason
        block.setNBT(stringJoiner.toString());
    }
}
