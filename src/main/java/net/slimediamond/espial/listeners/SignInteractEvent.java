package net.slimediamond.espial.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.slimediamond.espial.action.ActionType;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.StoredBlock;
import net.slimediamond.espial.nbt.json.JsonNBTData;
import net.slimediamond.espial.nbt.json.JsonSignData;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.entity.ChangeSignEvent;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class SignInteractEvent {
    @Listener
    public void onSignChangeEvent(ChangeSignEvent event) throws SQLException {
        if (event.cause().root() instanceof Living source) {
            //ArrayList<StoredBlock> blocks = Espial.getInstance().getDatabase().queryBlock(location.world().key().formatted(), location.blockX(), location.blockY(), location.blockZ(), null, null, null);

            Optional<StoredBlock> block = Espial.getInstance().getDatabase().insertAction(ActionType.MODIFY, source, event.sign().serverLocation().world().key().formatted(), null, event.sign().serverLocation().createSnapshot());

            if (block.isPresent()) {
                JsonNBTData nbtData = new JsonNBTData();
                List<Component> front = event.text().get();
                List<Component> back = event.sign().backText().lines().get();

                List<String> frontSerialized = front.stream().map(component -> GsonComponentSerializer.gson().serialize(component)).toList();
                List<String> backSerialized = back.stream().map(component -> GsonComponentSerializer.gson().serialize(component)).toList();

                nbtData.setSignData(new JsonSignData(frontSerialized, backSerialized));
                block.get().setNBT(nbtData);
            } else {
                Espial.getInstance().getLogger().error("Could not insert a sign modification event because Database#insertAction returned Optional.empty(). (is the database down?)");
            }
        }
    }
}
