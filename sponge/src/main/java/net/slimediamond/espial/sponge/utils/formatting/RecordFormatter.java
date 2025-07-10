package net.slimediamond.espial.sponge.utils.formatting;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.slimediamond.espial.api.event.EspialEvents;
import net.slimediamond.espial.api.record.EspialBlockRecord;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.common.utils.formatting.Format;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Keys;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RecordFormatter {

    private static final TextColor NAME_COLOR = Format.THEME_COLOR;
    private static final TextColor EVENT_COLOR = Format.ACCENT_COLOR;
    private static final TextColor STACK_COLOR = Format.THEME_COLOR;
    private static final TextColor SPREAD_TARGET_COLOR = Format.THEME_COLOR;
    private static final TextColor GROUPED_TARGET_COLOR = Format.ACCENT_COLOR;

    public static List<Component> formatRecords(@NotNull final List<EspialRecord> records, boolean spread) {
        if (spread) {
            return records.stream().map(RecordFormatter::format).toList();
        }
        // stack the records
        final Map<StackedRecord, Integer> recordCounts = new LinkedHashMap<>();
        final Map<StackedRecord, Long> recordTimes = new LinkedHashMap<>();
        for (final EspialRecord record : records) {
            final StackedRecord stackedRecord = new StackedRecord(record);
            recordCounts.put(stackedRecord, recordCounts.getOrDefault(stackedRecord, 0) + 1);
            final long time = record.getDate().getTime();
            recordTimes.put(stackedRecord, Math.max(recordTimes.getOrDefault(stackedRecord, 0L), time));
        }

        // TODO: Nicer and more chronological order of display, also show dates
        // this seems to be in the wrong order? So reverse it
        final List<Map.Entry<StackedRecord, Integer>> sortedEntries = new ArrayList<>(recordCounts.entrySet()).reversed();
        return sortedEntries.stream()
                .map(entry -> {
                    final StackedRecord record = entry.getKey();
                    final int count = entry.getValue();

                    Component name = Component.text("#")
                            .append(record.getEntityType().asComponent())
                            .decorate(TextDecoration.ITALIC);

                    if (record.getUser() != null && Sponge.server().userManager().exists(record.getUser())) {
                        // blocking due to .join() doesn't matter here as we should be on another thread regardless
                        name = Component.text(Sponge.server().userManager().loadOrCreate(record.getUser()).join().name());
                    }

                    final TextComponent.Builder builder = Component.text()
                            .append(name.color(NAME_COLOR))
                            .appendSpace()
                            .append(record.getEvent().getVerbComponent().color(EVENT_COLOR))
                            .appendSpace()
                            .append(Component.text(count + "x").color(STACK_COLOR))
                            .appendSpace()
                            .append(record.getTarget().color(GROUPED_TARGET_COLOR));

                    if (record.isRolledBack()) {
                        builder.decorate(TextDecoration.STRIKETHROUGH);
                    }
                    return builder.build().asComponent();
                })
                .toList();
    }

    public static Component format(@NotNull final EspialRecord record) {
        final TextComponent.Builder builder = Component.text();
        if (record.getUser().isPresent()) {
            final UUID uuid = record.getUser().get();
            if (Sponge.server().userManager().exists(uuid)) {
                builder.append(Component.text(Sponge.server().userManager().loadOrCreate(uuid).join().name())
                        .color(NAME_COLOR));
            } else {
                builder.append(Component.text("Unknown User")
                        .hoverEvent(HoverEvent.showText(Format.accent(uuid.toString())))
                        .color(NAME_COLOR));
            }
        } else {
            builder.append(Component.text("?").color(NAME_COLOR));
        }
        builder.appendSpace().append(record.getEvent().getVerbComponent().color(EVENT_COLOR));
        if (record instanceof EspialBlockRecord blockRecord) {
            builder.appendSpace().append(getTarget(blockRecord).state().type().asComponent().color(SPREAD_TARGET_COLOR));

            final List<Component> extraDisplay = new LinkedList<>();
            // also append sign data if it exists (not working)
            blockRecord.getReplacementBlock().get(Keys.SIGN_FRONT_TEXT).ifPresent(signText ->
                    extraDisplay.addAll(formatSignLines("Front Line ", signText.lines().get())));
            blockRecord.getReplacementBlock().get(Keys.SIGN_BACK_TEXT).ifPresent(signText ->
                    extraDisplay.addAll(formatSignLines("Back Line ", signText.lines().get())));


            if (!extraDisplay.isEmpty()) {
                builder.append(Format.accent(" (...)")
                        .hoverEvent(HoverEvent.showText(Component.join(JoinConfiguration.newlines(), extraDisplay))));
            }
        }
        if (record.isRolledBack()) {
            builder.decorate(TextDecoration.STRIKETHROUGH);
        }
        return builder.build();
    }

    private static List<Component> formatSignLines(final String prefix, final List<Component> lines) {
        final List<Component> results = new LinkedList<>();
        for (int i = 0; i < lines.size(); i++) {
            results.add(Format.detail(prefix + i, lines.get(i)));
        }
        return results;
    }

    public static BlockSnapshot getTarget(final EspialBlockRecord record) {
        if (record.getEvent().equals(EspialEvents.PLACE.get())) {
            return record.getReplacementBlock();
        }
        return record.getOriginalBlock();
    }

}
