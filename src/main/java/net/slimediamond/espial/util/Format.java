package net.slimediamond.espial.util;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.api.action.Action;
import net.slimediamond.espial.api.action.BlockAction;
import net.slimediamond.espial.api.action.HangingDeathAction;
import net.slimediamond.espial.api.action.ItemFrameRemoveAction;
import net.slimediamond.espial.api.action.NBTStorable;
import net.slimediamond.espial.api.action.event.EventType;
import net.slimediamond.espial.api.record.EspialRecord;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
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

public final class Format {
    public static final boolean SHOW_DATE_IN_LOOKUP = false;

    public static final TextColor THEME_COLOR = TextColor.color(79, 235, 52);
    public static final TextColor TEXT_COLOR = NamedTextColor.WHITE;
    public static final TextColor INFO_COLOR = NamedTextColor.WHITE;
    public static final TextColor DEBUG_COLOR = TextColor.color(255, 243, 0);
    public static final TextColor PADDING_COLOR = NamedTextColor.GRAY;
    public static final TextColor ERROR_COLOR = NamedTextColor.RED;
    public static final TextColor HINT_COLOR = TextColor.color(49, 175, 212);
    public static final TextColor COMMAND_HINT_COLOR = NamedTextColor.AQUA;

    public static final TextColor TITLE_COLOR = NamedTextColor.GOLD;
    public static final TextColor NAME_COLOR = INFO_COLOR;
    public static final TextColor STACK_COLOR = INFO_COLOR;
    public static final TextColor DATE_COLOR = NamedTextColor.DARK_GRAY;
    public static final TextColor ITEM_COLOR = THEME_COLOR;
    public static final TextColor SPREAD_ITEM_COLOR = INFO_COLOR;
    public static final TextColor ACTION_COLOR = THEME_COLOR;
    public static final TextColor HOVER_HINT_COLOR = HINT_COLOR;
    public static final TextColor HOVER_TEXT_COLOR = NamedTextColor.GRAY;

    public static final Component PADDING = Component.text("=").color(PADDING_COLOR);
    public static final Component PREFIX = Component.text("Espial › ").color(THEME_COLOR);
    public static final Component DEBUG_PREFIX = PREFIX.append(Component.text("Debug › ")).color(DEBUG_COLOR);

    /* No initialization */
    private Format() {
    }

    public static Component component(Component component) {
        return PREFIX.append(component);
    }

    public static Component component(TextComponent.Builder builder) {
        return component(builder.build());
    }

    public static Component text(String text) {
        return PREFIX.append(Component.text(text).color(TEXT_COLOR));
    }

    public static Component error(String text) {
        return component(Component.text(text).color(ERROR_COLOR));
    }

    public static Component debug(String text) {
        return debug(Component.text(text).color(NamedTextColor.WHITE));
    }

    public static Component debug(Component component) {
        return DEBUG_PREFIX.append(component);
    }

    public static void sendDebug(Audience audience, Component component) {
        if (Espial.getInstance().getConfig().get().isDebugModeEnabled()) {
            audience.sendMessage(component);
        }
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

    public static Component title(String text) {
        return Component.text().append(PREFIX).append(Component.text(text).color(TITLE_COLOR)).build();
    }

    public static Component commandHint(String command) {
        return commandHint(command, command, null);
    }

    public static Component commandHint(String display, String command,
                                        Component hover) {
        Component component = Component.text("[").color(NamedTextColor.GOLD)
                .append(Component.text(display).color(COMMAND_HINT_COLOR))
                .append(Component.text("]").color(NamedTextColor.GOLD));
        if (hover != null) {
            component = component.hoverEvent(HoverEvent.showText(hover));
        }

        return component.clickEvent(ClickEvent.runCommand(command));
    }

    public static Component chip(@NonNull Component text) {
        return chip(text, null);
    }

    public static Component chip(@NonNull Component text, @Nullable Component hover) {
        TextComponent.Builder builder = Component.text()
                .append(Component.text("[", PADDING_COLOR))
                .append(text)
                .append(Component.text("]", PADDING_COLOR));
        if (hover != null) {
            builder.hoverEvent(HoverEvent.showText(hover));
        }
        return builder.build();
    }

    @Deprecated
    public static Component chip(String text, NamedTextColor color) {
        return Component.text()
                .append(Component.text("[").color(PADDING_COLOR))
                .append(Component.text(text).color(color))
                .append(Component.text("] ").color(PADDING_COLOR))
                .build();
    }

    public static String date(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm");
        return dateFormat.format(new Date(date.getTime()));
    }

    public static Component truncate(Component component) {
        return truncate(component, 60);
    }

    /**
     * Truncate a string to make it shorter, and append a '...' if we can't fit it
     *
     * @param component Component to truncate
     * @param max       Maximum number of characters
     * @return Truncated component
     */
    public static Component truncate(Component component, int max) {
        String ellipsis = " ...";
        List<Component> result = new ArrayList<>();
        int count = 0;

        for (Segment seg : flatten(component)) {
            String text = seg.text;
            Style style = seg.style;

            if (count + text.length() > max) {
                result.add(Component.text(text.substring(0, max - count) + ellipsis, style));
                return Component.join(JoinConfiguration.noSeparators(), result)
                        .hoverEvent(HoverEvent.showText(component));
            }

            result.add(Component.text(text, style));
            count += text.length();
        }
        return Component.join(JoinConfiguration.noSeparators(), result);
    }

    private static List<Segment> flatten(Component comp) {
        List<Segment> segments = new ArrayList<>();
        if (comp instanceof TextComponent tc && !tc.content().isEmpty()) {
            segments.add(new Segment(tc.content(), tc.style()));
        }
        comp.children().forEach(child -> segments.addAll(flatten(child)));
        return segments;
    }

    /**
     * Get the display name of an action's actor.
     *
     * @param action The action
     * @return Display name component
     */
    public static Component getDisplayName(Action action) {
        String uuidString = action.getActor().getUUID();

        if (uuidString.equals("0")) {
            return Component.text("(server)").color(NAME_COLOR).decorate(TextDecoration.ITALIC);
        }

        UUID uuid;
        try {
            uuid = UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            return Component.text()
                    .append(Component.text("("))
                    .append(Component.text(uuidString))
                    .append(Component.text(")"))
                    .build()
                    .color(NAME_COLOR)
                    .decorate(TextDecoration.ITALIC);
        }

        try {
            return Component.text(Sponge.server().userManager().loadOrCreate(uuid).get().name()).color(NAME_COLOR);
        } catch (InterruptedException | ExecutionException e) {
            //throw new RuntimeException(e);
            return Component.text("Unknown User").color(NAME_COLOR)
                    .hoverEvent(HoverEvent.showText(Component.text(uuidString)));
        }
    }

    /**
     * Generate the lookup contents based on a set of {@link EspialRecord}s
     *
     * @param records The records to generate the content based on
     * @param spread  Whether to group the records
     * @return List of {@link Component}s to display
     */
    public static List<Component> generateLookupContents(List<EspialRecord> records, boolean spread) {
        List<Component> contents = new ArrayList<>();

        if (spread) {
            records.forEach(record -> {
                Component displayName = getDisplayName(record.getAction());
                TextComponent.Builder msg = Component.text();

                String formattedDate = Format.date(record.getTimestamp());

                if (SHOW_DATE_IN_LOOKUP) {
                    msg.append(Component.text(formattedDate).color(DATE_COLOR)).append(Component.space());
                }

                msg.append(displayName)
                        .append(Component.space())
                        .append(makeHoverableAction(record.getAction().getEventType(), true)
                                .color(ACTION_COLOR))
                        .append(Component.space())
                        .append(getItemDisplayName(record).color(SPREAD_ITEM_COLOR))
                        .clickEvent(ClickEvent.runCommand("/espial inspect " + record.getId()))
                        .hoverEvent(HoverEvent.showText(component(Component.text()
                                .appendNewline()
                                .append(Component.text("Click to teleport!").color(HOVER_HINT_COLOR))
                                .appendNewline()
                                .append(Component.text("Internal ID: ").color(HOVER_HINT_COLOR))
                                .append(Component.text(record.getId()).color(HOVER_TEXT_COLOR))
                                .appendNewline()
                                .append(Component.text(formattedDate).color(HOVER_TEXT_COLOR))
                                .build())));

                if (record.getAction() instanceof NBTStorable nbt) {
                    nbt.getNBT().flatMap(NBTDataParser::parseNBT)
                            .ifPresent(component ->
                                    msg.append(Component.text(" (...)")
                                            .color(NamedTextColor.GRAY)
                                            .hoverEvent(HoverEvent.showText(title("NBT Data")
                                                    .append(Component.text()
                                                            .color(NamedTextColor.WHITE)
                                                            .append(component))))));
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

                BlockTracker key = new BlockTracker(
                        displayName,
                        record.getAction().getEventType(),
                        getItemDisplayName(record).color(ITEM_COLOR),
                        record.isRolledBack());
                groupedBlocks.put(key, groupedBlocks.getOrDefault(key, 0) + 1);
                long time = record.getTimestamp().getTime();
                latestTimes.put(key, Math.max(latestTimes.getOrDefault(key, 0L), time));
            });

            List<Map.Entry<BlockTracker, Integer>> sortedEntries =
                    new ArrayList<>(groupedBlocks.entrySet());
            sortedEntries.sort((e1, e2) ->
                    Long.compare(latestTimes.get(e2.getKey()), latestTimes.get(e1.getKey())));

            sortedEntries.forEach(entry -> {
                BlockTracker key = entry.getKey();
                int count = entry.getValue();
                TextComponent.Builder builder = Component.text()
                        .append(key.name())
                        .append(Component.space())
                        .append(makeHoverableAction(entry.getKey().eventType(), true).color(ACTION_COLOR))
                        .append(Component.space())
                        .append(Component.text((count > 1 ? count + "x " : "")).color(STACK_COLOR))
                        .append(entry.getKey().block().color(ITEM_COLOR));

                if (key.rolledBack()) {
                    builder.decorate(TextDecoration.STRIKETHROUGH);
                }
                contents.add(builder.build());
            });
        }
        return contents;
    }

    public static Component makeHoverableAction(EventType eventType, boolean useVerb) {
        String text = useVerb ? eventType.getVerb() : eventType.getName();
        return Component.text(text)
                .hoverEvent(HoverEvent.showText(component(Component.text()
                        .append(Component.text("Event").color(TITLE_COLOR))
                        .append(Component.newline())
                        .append(Component.text("ID: ").color(HOVER_HINT_COLOR))
                        .append(Component.text(eventType.getId()).color(HOVER_TEXT_COLOR))
                        .append(Component.newline())
                        .append(Component.text("Name: ").color(HOVER_HINT_COLOR))
                        .append(Component.text(eventType.getName()).color(HOVER_TEXT_COLOR))
                        .append(Component.newline())
                        .append(Component.text("Description: ").color(HOVER_HINT_COLOR))
                        .append(Component.text(eventType.getDescription()).color(HOVER_TEXT_COLOR)))));
    }

    public static Component getItemDisplayName(EspialRecord record) {
        Component displayName =
                Component.text("(unknown)").color(NamedTextColor.GRAY).decorate(TextDecoration.ITALIC);

        if (record.getAction() instanceof BlockAction blockAction) {
            displayName = blockAction.getBlockType().asComponent();
        } else if (record.getAction() instanceof HangingDeathAction hangingDeathAction) {
            displayName = hangingDeathAction.getEntityType().asComponent();
        } else if (record.getAction() instanceof ItemFrameRemoveAction itemFrameRemoveAction) {
            displayName = itemFrameRemoveAction.getItemType().asComponent();
        }

        return displayName;
    }

    private record BlockTracker(Component name, EventType eventType, Component block, boolean rolledBack) {
    }

    private record Segment(String text, Style style) {
    }
}
