package net.slimediamond.espial.api.nbt;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import java.util.List;
import java.util.Optional;

public class NBTDataParser {
    public static Optional<Component> parseNBT(NBTData data) {
        // Make a human-readable component of whatever NBT data may want to be displayed
        TextComponent.Builder builder = Component.empty().toBuilder();
        boolean shouldReturn = false;

        if (data.getSignData() != null) {
            // It's a sign, lets parse data accordingly

            List<String> frontLines = data.getSignData().getFrontText();
            List<String> backLines = data.getSignData().getBackText();
            List<Component> frontComponents = frontLines.stream().map(line -> GsonComponentSerializer.gson().deserialize(line)).toList();

            builder.append(Component.newline());
            builder.append(Component.text("Front:").color(NamedTextColor.DARK_AQUA));
            builder.append(signLines(frontComponents));

            List<Component> backComponents = backLines.stream().map(line -> GsonComponentSerializer.gson().deserialize(line)).toList();

            builder.append(Component.newline());
            builder.append(Component.text("Back:").color(NamedTextColor.DARK_AQUA));
            builder.append(signLines(backComponents));

            shouldReturn = true;
        }

        if (data.getDirection() != null) {
            builder.append(Component.newline());
            builder.append(Component.text("Direction: ").color(NamedTextColor.DARK_AQUA));
            builder.append(Component.text(data.getDirection().toString()).color(NamedTextColor.WHITE));

            shouldReturn = true;
        }

        if (data.getAxis() != null) {
            builder.append(Component.newline());
            builder.append(Component.text("Axis: ").color(NamedTextColor.DARK_AQUA));
            builder.append(Component.text(data.getAxis().toString()).color(NamedTextColor.WHITE));

            shouldReturn = true;
        }

        if (data.isWaterlogged()) {
            builder.append(Component.newline());
            builder.append(Component.text("Waterlogged: ").color(NamedTextColor.DARK_AQUA));
            builder.append(Component.text(data.isWaterlogged()).color(NamedTextColor.WHITE));

            shouldReturn = true;
        }

        if (shouldReturn) {
            return Optional.of(builder.build());
        } else {
            return Optional.empty();
        }
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
