package net.slimediamond.espial.sponge.utils.formatting;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.slimediamond.espial.api.event.EspialEvents;
import net.slimediamond.espial.api.record.*;
import net.slimediamond.espial.sponge.utils.TimeUtils;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.adventure.SpongeComponents;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.registry.RegistryTypes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class RecordFormatter {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
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
            // HACKHACK: Display container change records properly
            if (record instanceof final ContainerChangeRecord containerChangeRecord) {
                // do it (count) times
                for (int i = 0; i < containerChangeRecord.getAffectedItem().quantity(); i++) {
                    recordCounts.put(stackedRecord, recordCounts.getOrDefault(stackedRecord, 0) + 1);
                }
            } else {
                recordCounts.put(stackedRecord, recordCounts.getOrDefault(stackedRecord, 0) + 1);
            }
            final long time = record.getDate().getTime();
            recordTimes.put(stackedRecord, Math.min(recordTimes.getOrDefault(stackedRecord, time), time));
        }

        // TODO: Nicer and more chronological order of display, also show dates
        // this seems to be in the wrong order? So reverse it
        final List<Map.Entry<StackedRecord, Integer>> sortedEntries = new ArrayList<>(recordCounts.entrySet());
        sortedEntries.sort(Comparator.comparing(e -> recordTimes.get(e.getKey())));
        Collections.reverse(sortedEntries);
        return sortedEntries.stream()
                .map(entry -> {
                    final StackedRecord record = entry.getKey();
                    final int count = entry.getValue();

                    final TextComponent.Builder builder = Component.text()
                            .append(formatCause(record).color(NAME_COLOR))
                            .appendSpace()
                            .append(record.getEvent().getVerbComponent().color(EVENT_COLOR))
                            .appendSpace()
                            .append(Component.text(count + "x").color(STACK_COLOR))
                            .appendSpace()
                            .append(record.getTarget().color(GROUPED_TARGET_COLOR))
                            .appendSpace()
                            .append(formatDate(new Date(recordTimes.get(record))));

                    if (record.isRolledBack()) {
                        builder.decorate(TextDecoration.STRIKETHROUGH);
                    }
                    return builder.build().asComponent();
                })
                .toList();
    }

    public static Component format(@NotNull final EspialRecord record) {
        final TextComponent.Builder builder = Component.text()
                .append(formatCause(record))
                .appendSpace()
                .append(record.getEvent().getVerbComponent().color(EVENT_COLOR))
                .appendSpace();

        if (record instanceof ContainerChangeRecord containerChangeRecord) {
            builder.append(Component.text(containerChangeRecord.getAffectedItem().quantity() + "x").color(STACK_COLOR))
                    .appendSpace();
        }

        builder.append(getTarget(record).color(SPREAD_TARGET_COLOR))
                .appendSpace()
                .append(formatDate(record.getDate()));

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
        final TextComponent.Builder builder = Component.text();
        final ResourceKey resourceKey = ResourceKey.resolve(record.getTarget());
        switch (record) {
            case final BlockRecord blockRecord -> {
                if (blockRecord.getEvent().equals(EspialEvents.PLACE.get())
                        || blockRecord.getEvent().equals(EspialEvents.GROWTH.get())) {
                    builder.append(blockRecord.getReplacementBlock().state().type().asComponent());
                } else {
                    builder.append(blockRecord.getOriginalBlock().state().type().asComponent());
                }
            }
            case final HangingDeathRecord hangingDeathRecord ->
                    builder.append(hangingDeathRecord.getTargetEntityType().asComponent());
            case final SignModifyRecord signModifyRecord ->
                    builder.append(signModifyRecord.getBlockState().type().asComponent());
            case final ContainerChangeRecord containerChangeRecord ->
                    builder.append(containerChangeRecord.getAffectedItem().type().asComponent());
            case final ItemFrameChangeRecord itemFrameChangeRecord ->
                    builder.append(itemFrameChangeRecord.getAffectedItem().type().asComponent());
            default -> builder.append(Component.text(resourceKey.value().replace("_", " ")));
        }
        builder.hoverEvent(HoverEvent.showText(Component.text(resourceKey.formatted()).color(NamedTextColor.GRAY)));
        return builder.build();
    }

    public static Component formatCause(final EntityDataHeld entityData) {
        Component name = Component.text("#")
                .append(entityData.getEntityType().asComponent())
                .decorate(TextDecoration.ITALIC);

        if (entityData.getUser().isPresent() && Sponge.server().userManager().exists(entityData.getUser().get())) {
            // blocking due to .join() doesn't matter here as we should be on another thread regardless
            name = Component.text(Sponge.server().userManager().loadOrCreate(entityData.getUser().get()).join().name());
        }
        return name.color(NAME_COLOR)
                .hoverEvent(HoverEvent.showText(
                        Component.text(entityData.getEntityType().key(RegistryTypes.ENTITY_TYPE).formatted())
                                .color(NamedTextColor.GRAY)));
    }

    public static Component formatDate(final Date date) {
        return Component.text(TimeUtils.getTimeSince(date))
                .hoverEvent(HoverEvent.showText(Component.text(DATE_FORMAT.format(date)).color(NamedTextColor.GRAY)));
    }

}
