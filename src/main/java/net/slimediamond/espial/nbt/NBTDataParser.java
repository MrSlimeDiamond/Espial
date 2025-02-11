package net.slimediamond.espial.nbt;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.slimediamond.espial.util.BlockUtil;
import org.spongepowered.api.block.BlockType;

import java.util.List;
import java.util.Optional;

public class NBTDataParser {
    public static Optional<Component> parseNBT(NBTData data, BlockType blockId) {
        // Make a human-readable component of whatever NBT data may want to be displayed
        if (BlockUtil.SIGNS.contains(blockId)) {
            // It's a sign, lets parse data accordingly

            if (data.getSignData() != null ) {
                List<String> lines = data.getSignData().getFrontComponents();
                List<Component> components = lines.stream().map(line -> GsonComponentSerializer.gson().deserialize(line)).toList();

                var builder = Component.empty().toBuilder();
                for (int i = 0; i < components.size(); i++) {
                    // Append a newline
                    builder.append(Component.newline());
                    builder.append(
                            Component.text("Line ")
                                    .append(Component.text((i + 1) + ": "))
                                    .color(NamedTextColor.GRAY)
                    );
                    // Append the actual line content
                    builder.append(components.get(i));
                }

                return Optional.of(builder.build());
            } else {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }
}
