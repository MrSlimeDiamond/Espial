package net.slimediamond.espial.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.slimediamond.espial.api.action.BlockAction;
import org.spongepowered.api.data.Keys;

import java.util.ArrayList;
import java.util.List;

public class SignUtil {
    public static void setSignData(BlockAction action) {
        action.getServerLocation().blockEntity().ifPresent(tileEntity -> {
            action.getNBT().ifPresent(nbtData -> {
                if (nbtData.getSignData() != null) {
                    List<Component> components = new ArrayList<>();

                    nbtData.getSignData().getFrontText().forEach(line -> components.add(GsonComponentSerializer.gson().deserialize(line)));

                    tileEntity.offer(Keys.SIGN_LINES, components);
                }
            });
        });
    }
}
