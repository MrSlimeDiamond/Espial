package net.slimediamond.espial.sponge.utils.formatting;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.slimediamond.espial.api.event.EspialEvents;
import net.slimediamond.espial.api.record.BlockRecord;
import net.slimediamond.espial.api.record.HangingDeathRecord;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.record.SignModifyRecord;
import net.slimediamond.espial.sponge.utils.TimeUtils;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.adventure.SpongeComponents;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RecordFormatter {

    private static final TextColor NAME_COLOR = Format.THEME_COLOR;
    private static final TextColor EVENT_COLOR = Format.ACCENT_COLOR;
    private static final TextColor STACK_COLOR = Format.THEME_COLOR;
    private static final TextColor SPREAD_TARGET_COLOR = Format.THEME_COLOR;
    private static final TextColor GROUPED_TARGET_COLOR = Format.ACCENT_COLOR;
    private static final String FRONT_LINES_PREFIX = "Front line ";
    private static final String BACK_LINES_PREFIX = "Back line ";

    public static List<Component> formatRecords(@NotNull final List<EspialRecord> records, final boolean spread) {
        if (spread) {
            // make it mutable before rearranging just in case
            final List<EspialRecord> mutable = new LinkedList<>(records);
            mutable.sort(Comparator.comparingInt(EspialRecord::getId).reversed());
            return mutable.stream().map(RecordFormatter::format).toList();
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
        Component name = Component.text("#")
                .append(record.getEntityType().asComponent())
                .decorate(TextDecoration.ITALIC);

        if (record.getUser().isPresent() && Sponge.server().userManager().exists(record.getUser().get())) {
            // blocking due to .join() doesn't matter here as we should be on another thread regardless
            name = Component.text(Sponge.server().userManager().loadOrCreate(record.getUser().get()).join().name());
        }
        builder.append(name.color(NAME_COLOR));

        builder.appendSpace().append(record.getEvent().getVerbComponent().color(EVENT_COLOR));
        builder.appendSpace().append(getTarget(record).color(SPREAD_TARGET_COLOR));

        final List<Component> extraDisplay = new LinkedList<>();

        if (record instanceof final BlockRecord blockRecord) {
            // also append sign data if it exists (not working)
            blockRecord.getOriginalBlock().createArchetype().ifPresent(blockEntity -> {
                blockEntity.get(Keys.SIGN_FRONT_TEXT).ifPresent(signText ->
                        extraDisplay.addAll(formatSignLines(FRONT_LINES_PREFIX, signText.lines().get())));
                blockEntity.get(Keys.SIGN_BACK_TEXT).ifPresent(signText ->
                        extraDisplay.addAll(formatSignLines(BACK_LINES_PREFIX, signText.lines().get())));
            });
        } else if (record instanceof final SignModifyRecord signModifyRecord) {
            extraDisplay.addAll(formatSignLines(FRONT_LINES_PREFIX,
                    signModifyRecord.getReplacementContents().getFront()));
            extraDisplay.addAll(formatSignLines(BACK_LINES_PREFIX,
                    signModifyRecord.getReplacementContents().getBack()));
        }

        builder.appendSpace()
                .append(Component.text(TimeUtils.getTimeSince(record.getDate())).color(Format.TEXT_COLOR)
                        .hoverEvent(HoverEvent.showText(Component.text(record.getDate().toString()))));

        if (!extraDisplay.isEmpty()) {
            builder.append(Component.text(" (...)").color(Format.PADDING_COLOR)
                    .hoverEvent(HoverEvent.showText(Component.join(JoinConfiguration.newlines(), extraDisplay))));
        }

        if (record.isRolledBack()) {
            builder.decorate(TextDecoration.STRIKETHROUGH);
        }

        builder.clickEvent(SpongeComponents.executeCallback(cause ->
                cause.first(ServerPlayer.class).ifPresent(player ->
                        player.setPosition(record.getLocation().position().add(0.5, 0, 0.5)))));
        return builder.build();
    }

    private static List<Component> formatSignLines(final String prefix, final List<Component> lines) {
        final List<Component> results = new LinkedList<>();
        for (int i = 0; i < lines.size(); i++) {
            results.add(Format.detail(prefix + (i + 1), lines.get(i)));
        }
        return results;
    }

    public static Component getTarget(final EspialRecord record) {
        if (record instanceof final BlockRecord blockRecord) {
            if (blockRecord.getEvent().equals(EspialEvents.PLACE.get())
                    || blockRecord.getEvent().equals(EspialEvents.GROWTH.get())) {
                return blockRecord.getReplacementBlock().state().type().asComponent();
            }
            return blockRecord.getOriginalBlock().state().type().asComponent();
        } else if (record instanceof final HangingDeathRecord hangingDeathRecord) {
            return hangingDeathRecord.getTargetEntityType().asComponent();
        } else if (record instanceof final SignModifyRecord signModifyRecord) {
            return signModifyRecord.getBlockState().type().asComponent();
        }
        final ResourceKey resourceKey = ResourceKey.resolve(record.getTarget());
        return Component.text(resourceKey.value().replace("_", " "));
    }

}
