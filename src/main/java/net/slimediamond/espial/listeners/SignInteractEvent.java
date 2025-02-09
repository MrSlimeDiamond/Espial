package net.slimediamond.espial.listeners;

import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.slimediamond.espial.ActionType;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.StoredBlock;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.entity.ChangeSignEvent;
import org.spongepowered.api.world.server.ServerLocation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.StringJoiner;

public class SignInteractEvent {
    @Listener
    public void onSignChangeEvent(ChangeSignEvent event) throws SQLException {
        if (event.cause().root() instanceof Living source) {
            // split by "|" when we insert into the database, so we can get each line again later
            // Not the best solution tbh
            StringJoiner stringJoiner = new StringJoiner("|");

            event.text().forEach(line -> stringJoiner.add(GsonComponentSerializer.gson().serialize(line)));

            //ArrayList<StoredBlock> blocks = Espial.getInstance().getDatabase().queryBlock(location.world().key().formatted(), location.blockX(), location.blockY(), location.blockZ(), null, null, null);

            Optional<StoredBlock> block = Espial.getInstance().getDatabase().insertAction(ActionType.MODIFY, source, event.sign().serverLocation().world().key().formatted(), null, event.sign().serverLocation().createSnapshot());

            if (block.isPresent()) {
                block.get().setNBT(stringJoiner.toString());
            } else {
                Espial.getInstance().getLogger().error("Could not insert a sign modification event because Database#insertAction returned Optional.empty(). (is the database down?)");
            }
        }
    }
}
