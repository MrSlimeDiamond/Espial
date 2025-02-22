package net.slimediamond.espial.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.api.action.Action;
import net.slimediamond.espial.api.action.BlockAction;
import net.slimediamond.espial.api.action.NBTStorable;
import net.slimediamond.espial.api.action.event.EventType;
import net.slimediamond.espial.api.nbt.NBTDataParser;
import net.slimediamond.espial.api.record.EspialRecord;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class MessageUtil {
    public static Component getDisplayName(Action action) {
        String uuidString = action.getActor().getUUID();

        if (uuidString.equals("0")) {
            return Component.text("(server)").color(NamedTextColor.YELLOW).decorate(TextDecoration.ITALIC);
        }

        UUID uuid;
        try {
            uuid = UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            return Component.text()
                    .append(Component.text("("))
                    .append(Component.text(uuidString))
                    .append(Component.text(")"))
                    .build().color(NamedTextColor.YELLOW).decorate(TextDecoration.ITALIC);
        }

        Optional<User> user;
        try {
            user = Sponge.server().userManager().load(uuid).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return user.map(value -> Component.text(value.name()).color(NamedTextColor.YELLOW))
                .orElseGet(() -> Component.text(uuidString).color(NamedTextColor.YELLOW));
    }

    public static List<Component> generateLookupContents(List<EspialRecord> records, boolean spread) {
        List<Component> contents = new ArrayList<>();

        if (spread) {
            records.forEach(record -> {
                Component displayName = getDisplayName(record.getAction());
                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm");
                String formattedDate = dateFormat.format(new Date(record.getTimestamp().getTime()));
                TextComponent.Builder msg = Component.text()
                        .append(Component.text(formattedDate).color(NamedTextColor.GRAY))
                        .append(Component.space())
                        .append(displayName)
                        .append(Component.space())
                        .append(makeHoverableAction(record.getAction().getEventType(), true).color(NamedTextColor.GREEN))
                        .append(Component.space());

                if (record.getAction() instanceof BlockAction blockAction) {
                    msg.append(blockAction.getBlockType().asComponent().color(NamedTextColor.GREEN));
                }

                msg.clickEvent(ClickEvent.runCommand("/espial inspect " + record.getId()))
                .hoverEvent(HoverEvent.showText(Espial.prefix
                        .append(Component.newline())
                        .append(Component.text("Click to teleport!").color(NamedTextColor.GRAY))
                        .append(Component.newline())
                        .append(Component.text("Internal ID: ").color(NamedTextColor.GRAY))
                        .append(Component.text(record.getId()).color(NamedTextColor.DARK_GRAY))
                        .append(Component.newline())
                        .append(Component.text("Item in hand: ").color(NamedTextColor.GRAY))
                        .append(Component.text(record.getAction().getActor().getItem()).color(NamedTextColor.DARK_GRAY))
                        .append(Component.newline())
                        .append(Component.text(formattedDate).color(NamedTextColor.DARK_GRAY))
                ));

                try {
                    if (record.getAction() instanceof NBTStorable nbt) {
                        nbt.getNBT().flatMap(NBTDataParser::parseNBT).ifPresent(component -> {
                            msg.append(Component.text(" (...)")
                                    .color(NamedTextColor.GRAY)
                                    .hoverEvent(HoverEvent.showText(Espial.prefix.append(
                                            Component.text().color(NamedTextColor.WHITE).append(component)))));
                        });
                    }
                } catch (Exception e) {
                    msg.append(Component.text(" (!!!)")
                            .color(NamedTextColor.RED)
                            .hoverEvent(HoverEvent.showText(Espial.prefix.append(
                                    Component.newline()
                                            .append(Component.text("An error occurred while processing NBT data!").color(NamedTextColor.RED))
                            )))
                    );
                    e.printStackTrace();
                }

                if (record.isRolledBack()) {
                    msg.decorate(TextDecoration.STRIKETHROUGH);
                }
                contents.add(msg.build());
            });
        } else {
            // Grouped output in reverse chronological order
            Map<BlockTracker, Integer> groupedBlocks = new HashMap<>();
            Map<BlockTracker, Long> latestTimes = new HashMap<>();

            records.forEach(record -> {
                Component displayName = getDisplayName(record.getAction());
                Component blockType = Component.text("(unknown)")
                        .color(NamedTextColor.GRAY)
                        .decorate(TextDecoration.ITALIC);

                if (record.getAction() instanceof BlockAction blockAction) {
                    blockType = blockAction.getBlockType().asComponent();
                }

                BlockTracker key = new BlockTracker(displayName, record.getAction().getEventType(), blockType);
                groupedBlocks.put(key, groupedBlocks.getOrDefault(key, 0) + 1);
                long time = record.getTimestamp().getTime();
                latestTimes.put(key, Math.max(latestTimes.getOrDefault(key, 0L), time));
            });

            List<Map.Entry<BlockTracker, Integer>> sortedEntries = new ArrayList<>(groupedBlocks.entrySet());
            sortedEntries.sort((e1, e2) ->
                    Long.compare(latestTimes.get(e2.getKey()), latestTimes.get(e1.getKey()))
            );

            sortedEntries.forEach(entry -> {
                BlockTracker key = entry.getKey();
                int count = entry.getValue();
                contents.add(Component.text()
                        .append(key.name())
                        .append(Component.space())
                        .append(makeHoverableAction(entry.getKey().eventType(), true).color(NamedTextColor.GREEN))
                        .append(Component.space())
                        .append(Component.text((count > 1 ? count + "x " : "")).color(NamedTextColor.WHITE))
                        .append(entry.getKey().block().color(NamedTextColor.GREEN))
                        .build());
            });
        }
        return contents;
    }

    public static Component makeHoverableAction(EventType eventType, boolean useVerb) {
        String text = useVerb ? eventType.getVerb() : eventType.getName();
        return Component.text(text).hoverEvent(
                HoverEvent.showText(
                        Espial.prefix
                                .append(Component.newline())
                                .append(Component.text("Name: ").color(NamedTextColor.GRAY))
                                .append(Component.text(eventType.getName()).color(NamedTextColor.WHITE))
                                .append(Component.newline())
                                .append(Component.text("Description: ").color(NamedTextColor.GRAY))
                                .append(Component.text(eventType.getDescription()).color(NamedTextColor.WHITE))
                ));
    }

    private record BlockTracker(Component name, EventType eventType, Component block) {}
}
