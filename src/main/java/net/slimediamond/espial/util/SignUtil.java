package net.slimediamond.espial.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.slimediamond.espial.api.action.BlockAction;
import net.slimediamond.espial.api.nbt.NBTData;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.data.Keys;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SignUtil {
    public static void setSignData(BlockAction action) throws Exception {
        Optional<? extends BlockEntity> blockEntityOptional = action.getServerLocation().blockEntity();
        if (blockEntityOptional.isPresent()) {
            BlockEntity tileEntity = blockEntityOptional.get();
            Optional<NBTData> nbtOptional = action.getNBT();
            if (nbtOptional.isPresent()) {
                NBTData nbtData = nbtOptional.get();
                if (nbtData.getSignData() != null) {
                    List<Component> components = new ArrayList<>();

                    nbtData.getSignData().getFrontText().forEach(line -> components.add(GsonComponentSerializer.gson().deserialize(line)));

                    tileEntity.offer(Keys.SIGN_LINES, components);
                }
            } else {
                throw new Exception("No NBT data found for this entity");
            }
        } else {
            throw new Exception("Could not make a block entity");
        }
    }
}
