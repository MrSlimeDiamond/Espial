package net.slimediamond.espial.util;

import com.google.gson.stream.MalformedJsonException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.StoredBlock;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockTypes;

import java.util.Optional;

public class NBTDataParser {
    public static Optional<Component> parseNBT(StoredBlock block) {
        // Make a human-readable component of whatever NBT data may want to be displayed
        if (BlockUtil.SIGNS.contains(BlockTypes.registry().value(ResourceKey.of(block.blockId().split(":")[0], block.blockId().split(":")[1])))) {
            // It's a sign, lets parse data accordingly

            if(block.getNBT().isPresent()) {
                var info = Component.text();
                String data = block.getNBT().get();
                String[] lines = data.split("\\|");
                for (int i = 0; i < lines.length; i++) {
                    Component line;
                    try {
                        line = GsonComponentSerializer.gson().deserialize(lines[i]);
                    } catch (Exception ignored) {
                        continue;
                    }
                    info.append(Component.newline());
                    info.append(Component.text("Line ").append(net.kyori.adventure.text.Component.text((i + 1) + ": ")).color(NamedTextColor.DARK_AQUA));
                    info.append(line);
                }

                return Optional.of(info.build());

            } else {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }
}
