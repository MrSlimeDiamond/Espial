package net.slimediamond.espial.sponge.wand;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.TextDecoration;
import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.query.EspialQuery;
import net.slimediamond.espial.sponge.utils.formatting.Format;
import net.slimediamond.espial.sponge.data.EspialKeys;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.inventory.ItemStackLike;

import java.util.LinkedList;
import java.util.List;

public final class WandLoreBuilder {

    public static List<Component> getLore(final ItemStackLike item, final EspialQuery.Builder builder) {
        final List<Component> lore = new LinkedList<>();

        final EspialQuery query = builder.build();
        if (!query.getUsers().isEmpty()) {
            // AAAAAAA join
            lore.add(Format.detail("Users", Component.text()
                    .append(Component.text("{"))
                    .append(Component.join(JoinConfiguration.commas(true), query.getUsers().stream()
                            .map(uuid -> Component.text(Sponge.server().userManager().loadOrCreate(uuid).join().name()))
                            .toList()))
                    .append(Component.text("}"))
                    .build()));
        }
        if (!query.getEvents().isEmpty()) {
            lore.add(Format.detail("Events", "{" + String.join(", ",
                    query.getEvents().stream().map(EspialEvent::getName).toList()) + "}"));
        }
        if (!query.getBlockTypes().isEmpty()) {
            lore.add(Format.detail("Block types", Component.text()
                    .append(Component.text("{"))
                    .append(Component.join(JoinConfiguration.commas(true),
                            query.getBlockTypes().stream().map(ComponentLike::asComponent).toList()))
                    .append(Component.text("}"))
                    .build()));
        }
        query.getBefore().ifPresent(before -> lore.add(Format.detail("Before", before.toString())));
        query.getAfter().ifPresent(after -> lore.add(Format.detail("After", after.toString())));

        item.get(EspialKeys.WAND_MAX_USES).ifPresent(max -> {
            final int used = item.get(EspialKeys.WAND_USES).orElse(max);
            lore.add(Format.detail("Uses", used + "/" + max));
        });

        return lore.stream()
                .map(component -> component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                .toList();
    }

}
