package net.slimediamond.espial.nbt;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
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

            TextComponent.Builder builder = Component.empty().toBuilder();

            if (data.getSignData() != null ) {
                List<String> frontLines = data.getSignData().getFrontComponents();
                List<String> backLines = data.getSignData().getBackComponents();
                if (frontLines.isEmpty() && frontLines.stream().noneMatch(line -> line.equals("\"\""))) {
                    List<Component> components = frontLines.stream().map(line -> GsonComponentSerializer.gson().deserialize(line)).toList();

                    builder.append(Component.newline());
                    builder.append(Component.text("Front:").color(NamedTextColor.DARK_AQUA));
                    builder.append(signLines(components));
                }

                if (!backLines.isEmpty() && backLines.stream().noneMatch(line -> line.equals("\"\""))) {
                    List<Component> components = backLines.stream().map(line -> GsonComponentSerializer.gson().deserialize(line)).toList();

                    builder.append(Component.newline());
                    builder.append(Component.text("Back:").color(NamedTextColor.DARK_AQUA));
                    builder.append(signLines(components));
                }

                return Optional.of(builder.build());
            } else {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    public static Component signLines(List<Component> components) {
        var builder = Component.empty().toBuilder();
        for (int i = 0; i < components.size(); i++) {
            builder.append(Component.newline());
            builder.append(
                    Component.text("Line ")
                            .append(Component.text((i + 1) + ": "))
                            .color(NamedTextColor.DARK_AQUA)
            );
            // Append the actual line content
            builder.append(components.get(i));
        }

        return builder.build();
    }
}
