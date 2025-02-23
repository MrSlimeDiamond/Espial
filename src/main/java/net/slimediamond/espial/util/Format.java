package net.slimediamond.espial.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.slimediamond.espial.api.action.Action;
import net.slimediamond.espial.api.action.BlockAction;
import net.slimediamond.espial.api.action.HangingDeathAction;
import net.slimediamond.espial.api.action.ItemFrameRemoveAction;
import net.slimediamond.espial.api.action.NBTStorable;
import net.slimediamond.espial.api.action.event.EventType;
import net.slimediamond.espial.api.nbt.NBTDataParser;
import net.slimediamond.espial.api.record.EspialRecord;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class Format {
    /* No initialization */
    private Format() {}

    public static boolean SHOW_DATE_IN_LOOKUP = false;
    public static NamedTextColor MAIN_COLOR = NamedTextColor.GREEN;
    public static NamedTextColor TITLE_COLOR = NamedTextColor.GOLD;
    public static NamedTextColor NAME_COLOR = NamedTextColor.WHITE;
    public static NamedTextColor STACK_COLOR = NamedTextColor.WHITE;
    public static NamedTextColor PADDING_COLOR = NamedTextColor.GRAY;
    public static NamedTextColor DATE_COLOR = NamedTextColor.DARK_GRAY;
    public static NamedTextColor ITEM_COLOR = NamedTextColor.GREEN;
    public static NamedTextColor SPREAD_ITEM_COLOR = NamedTextColor.WHITE;
    public static NamedTextColor ACTION_COLOR = MAIN_COLOR;

    private static final int MAX_WIDTH = 320; // Max chat width in pixels

    // Character pixel widths based on Minecraft's font
    private static final int DEFAULT_WIDTH = 6;
    private static final int SPACE_WIDTH = 4;
    private static final int DOT_WIDTH = 3;
    private static final int WIDE_WIDTH = 9;
    private static final java.util.Map<Character, Integer> CHAR_WIDTHS = java.util.Map.of(
            'i', 3, 'l', 3, '.', 3, ' ', 4,
            'M', 9, 'W', 9, '@', 9
    );

    public static Component prefix =
            Component.text("Espial › ").color(MAIN_COLOR);

    public static Component component(Component component) {
        return prefix.append(component);
    }

    public static Component component(TextComponent.Builder builder) {
        return component(builder.build());
    }

    public static Component text(String text) {
        return prefix.append(Component.text(text).color(NamedTextColor.WHITE));
    }

    public static Component error(String message) {
        return component(Component.text(message).color(NamedTextColor.RED));
    }

    public static Component noBlockFound() {
        return error("Could not find a block. Maybe move closer?");
    }

    public static Component playersOnly() {
        return error("Only players can run this command.");
    }

    public static Component defaults(String defaults) {
        return component(Component.text("Defaults used: " + defaults).color(NamedTextColor.GRAY));
    }

    public static Component truncate(Component message) {
        List<Component> components = new ArrayList<>();
        int currentWidth = 0;

        for (Component part : message.children()) {
            String text = PlainTextComponentSerializer.plainText().serialize(part);
            TextColor color = part.color(); // Preserve color
            TextComponent.Builder newComponent = Component.text().color(color);

            for (char c : text.toCharArray()) {
                int charWidth = CHAR_WIDTHS.getOrDefault(c, DEFAULT_WIDTH);
                if (currentWidth + charWidth > MAX_WIDTH - SPACE_WIDTH + 9) {
                    newComponent.append(Component.text(" ...", color));
                    components.add(newComponent.build());
                    return Component.join(JoinConfiguration.noSeparators(), components);
                }
                newComponent.append(Component.text(c, color));
                currentWidth += charWidth;
            }
            components.add(newComponent.build());
        }

        return Component.join(JoinConfiguration.noSeparators(), components);
    }

    public static Component title(String text) {
        return Component.text()
                .append(prefix)
                .append(Component.text(text).color(TITLE_COLOR))
                .build();
    }

    public static Component getDisplayName(Action action) {
        String uuidString = action.getActor().getUUID();

        if (uuidString.equals("0")) {
            return Component.text("(server)").color(NAME_COLOR)
                    .decorate(TextDecoration.ITALIC);
        }

        UUID uuid;
        try {
            uuid = UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            return Component.text()
                    .append(Component.text("("))
                    .append(Component.text(uuidString))
                    .append(Component.text(")"))
                    .build().color(NAME_COLOR)
                    .decorate(TextDecoration.ITALIC);
        }

        Optional<User> user;
        try {
            user = Sponge.server().userManager().load(uuid).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return user.map(value -> Component.text(value.name())
                        .color(NAME_COLOR))
                .orElseGet(() -> Component.text(uuidString)
                        .color(NAME_COLOR));
    }

    public static List<Component> generateLookupContents(
            List<EspialRecord> records, boolean spread) {
        List<Component> contents = new ArrayList<>();

        if (spread) {
            records.forEach(record -> {
                Component displayName = getDisplayName(record.getAction());
                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm");
                String formattedDate = dateFormat.format(
                        new Date(record.getTimestamp().getTime()));
                TextComponent.Builder msg = Component.text();

                if (SHOW_DATE_IN_LOOKUP) {
                    msg.append(Component.text(formattedDate)
                            .color(DATE_COLOR))
                            .append(Component.space());
                }

                msg.append(displayName)
                .append(Component.space())
                .append(makeHoverableAction(
                        record.getAction().getEventType(), true).color(
                        ACTION_COLOR))
                .append(Component.space())
                .append(getItemDisplayName(record).color(SPREAD_ITEM_COLOR))
                .clickEvent(ClickEvent.runCommand(
                        "/espial inspect " + record.getId()))
                .hoverEvent(HoverEvent.showText(component(
                        Component.text()
                            .append(Component.newline())
                            .append(Component.text("Click to teleport!")
                                    .color(NamedTextColor.GRAY))
                            .append(Component.newline())
                            .append(Component.text("Internal ID: ")
                                    .color(NamedTextColor.GRAY))
                            .append(Component.text(record.getId())
                                    .color(NamedTextColor.DARK_GRAY))
                            .append(Component.newline())
                            .append(Component.text("Item in hand: ")
                                    .color(NamedTextColor.GRAY))
                            .append(Component.text(
                                            record.getAction().getActor().getItem())
                                    .color(NamedTextColor.DARK_GRAY))
                            .append(Component.newline())
                            .append(Component.text(formattedDate)
                                    .color(NamedTextColor.DARK_GRAY))
                            .build()
                )));

                if (record.getAction() instanceof NBTStorable nbt) {
                    nbt.getNBT().flatMap(NBTDataParser::parseNBT)
                            .ifPresent(component -> {
                                msg.append(Component.text(" (...)")
                                    .color(NamedTextColor.GRAY)
                                    .hoverEvent(HoverEvent.showText(
                                            component(
                                                Component.text()
                                                        .color(NamedTextColor.WHITE)
                                                        .append(component)))));
                            });
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


                BlockTracker key = new BlockTracker(displayName,
                        record.getAction().getEventType(),
                        getItemDisplayName(record).color(ITEM_COLOR));
                groupedBlocks.put(key, groupedBlocks.getOrDefault(key, 0) + 1);
                long time = record.getTimestamp().getTime();
                latestTimes.put(key,
                        Math.max(latestTimes.getOrDefault(key, 0L), time));
            });

            List<Map.Entry<BlockTracker, Integer>> sortedEntries =
                    new ArrayList<>(groupedBlocks.entrySet());
            sortedEntries.sort((e1, e2) ->
                    Long.compare(latestTimes.get(e2.getKey()),
                            latestTimes.get(e1.getKey()))
            );

            sortedEntries.forEach(entry -> {
                BlockTracker key = entry.getKey();
                int count = entry.getValue();
                contents.add(Component.text()
                        .append(key.name())
                        .append(Component.space())
                        .append(makeHoverableAction(entry.getKey().eventType(),
                                true).color(ACTION_COLOR))
                        .append(Component.space())
                        .append(Component.text((count > 1 ? count + "x " : ""))
                                .color(STACK_COLOR))
                        .append(entry.getKey().block()
                                .color(ITEM_COLOR))
                        .build());
            });
        }
        return contents;
    }

    public static Component makeHoverableAction(EventType eventType,
                                                boolean useVerb) {
        String text = useVerb ? eventType.getVerb() : eventType.getName();
        return Component.text(text).hoverEvent(
                HoverEvent.showText(
                        component(Component.text()
                            .append(Component.text("Event")
                                    .color(TITLE_COLOR))
                            .append(Component.newline())
                            .append(Component.text("ID: ")
                                    .color(NamedTextColor.GRAY))
                            .append(Component.text(eventType.getId())
                                    .color(NamedTextColor.WHITE))
                            .append(Component.newline())
                            .append(Component.text("Name: ")
                                    .color(NamedTextColor.GRAY))
                            .append(Component.text(eventType.getName())
                                    .color(NamedTextColor.WHITE))
                            .append(Component.newline())
                            .append(Component.text("Description: ")
                                    .color(NamedTextColor.GRAY))
                            .append(Component.text(
                                            eventType.getDescription())
                                    .color(NamedTextColor.WHITE))
                )));
    }

    public static Component getItemDisplayName(EspialRecord record) {
        Component displayName = Component.text("(unknown)")
                .color(NamedTextColor.GRAY)
                .decorate(TextDecoration.ITALIC);

        if (record.getAction() instanceof BlockAction blockAction) {
            displayName = blockAction.getBlockType().asComponent();
        } else if (record.getAction() instanceof HangingDeathAction hangingDeathAction) {
            displayName = hangingDeathAction.getEntityType().asComponent();
        } else if (record.getAction() instanceof ItemFrameRemoveAction itemFrameRemoveAction) {
            displayName = itemFrameRemoveAction.getItemType().asComponent();
        }

        return displayName;
    }

    private record BlockTracker(Component name, EventType eventType, Component block) {
    }
}
