package net.slimediamond.espial.nbt;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.slimediamond.espial.StoredBlock;
import net.slimediamond.espial.util.BlockUtil;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockTypes;

import java.util.List;
import java.util.Optional;

public class NBTDataParser {
    public static Optional<Component> parseNBT(StoredBlock block) {
        // Make a human-readable component of whatever NBT data may want to be displayed
        if (BlockUtil.SIGNS.contains(BlockTypes.registry().value(ResourceKey.of(block.blockId().split(":")[0], block.blockId().split(":")[1])))) {
            // It's a sign, lets parse data accordingly

            if(block.getNBT().isPresent()) {
                var info = Component.text();
                NBTData data = block.getNBT().get();
                if (data.getSignData().isPresent()) {
                    //System.out.println(data.getSignData().get().getFrontComponents());

                    List<String> lines = data.getSignData().get().getFrontComponents();
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
                }

                return Optional.of(info.build());

            } else {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }
}
