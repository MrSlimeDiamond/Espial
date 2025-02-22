package net.slimediamond.espial.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.api.action.BlockAction;
import net.slimediamond.espial.api.action.type.ActionType;
import net.slimediamond.espial.api.nbt.NBTDataParser;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class MessageUtil {
    public static Component getDisplayName(BlockAction action) {
        String uuidString = action.getUuid();

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

    public static List<Component> generateLookupContents(List<BlockAction> actions, boolean spread) {
        List<Component> contents = new ArrayList<>();

        if (spread) {
            // Clone it in case it's immutable
            List<BlockAction> actionsCloned = new ArrayList<>(actions);

            actionsCloned.sort(Comparator.comparing(BlockAction::getTimestamp).reversed());
            actionsCloned.forEach(action -> {
                Component displayName = getDisplayName(action);
                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm");
                String formattedDate = dateFormat.format(new Date(action.getTimestamp().getTime()));
                TextComponent.Builder msg = Component.text()
                        .append(Component.text(formattedDate).color(NamedTextColor.GRAY))
                        .append(Component.space())
                        .append(displayName)
                        .append(Component.space())
                        .append(makeHoverableAction(action.getType(), true).color(NamedTextColor.GREEN))
                        .append(Component.space())
                        .append(action.getBlockType().asComponent().color(NamedTextColor.GREEN))
                        .clickEvent(ClickEvent.runCommand("/espial inspect " + action.getId()))
                        .hoverEvent(HoverEvent.showText(Espial.prefix
                                .append(Component.newline())
                                .append(Component.text("Click to teleport!").color(NamedTextColor.GRAY))
                                .append(Component.newline())
                                .append(Component.text("Internal ID: ").color(NamedTextColor.GRAY))
                                .append(Component.text(action.getId()).color(NamedTextColor.DARK_GRAY))
                                .append(Component.newline())
                                .append(Component.text("Item in hand: ").color(NamedTextColor.GRAY))
                                .append(Component.text(action.getActorItem()).color(NamedTextColor.DARK_GRAY))
                                .append(Component.newline())
                                .append(Component.text(formattedDate).color(NamedTextColor.DARK_GRAY))
                        ));

                action.getNBT().flatMap(NBTDataParser::parseNBT).ifPresent(component -> {
                    msg.append(Component.text(" (...)")
                            .color(NamedTextColor.GRAY)
                            .hoverEvent(HoverEvent.showText(Espial.prefix.append(
                                    Component.text().color(NamedTextColor.WHITE).append(component)))));
                });

                if (action.isRolledBack()) {
                    msg.decorate(TextDecoration.STRIKETHROUGH);
                }
                contents.add(msg.build());
            });
        } else {
            // Grouped output in reverse chronological order
            Map<BlockTracker, Integer> groupedBlocks = new HashMap<>();
            Map<BlockTracker, Long> latestTimes = new HashMap<>();

            actions.forEach(block -> {
                Component displayName = getDisplayName(block);
                BlockTracker key = new BlockTracker(displayName, block.getType(), block.getBlockType().asComponent());
                groupedBlocks.put(key, groupedBlocks.getOrDefault(key, 0) + 1);
                long time = block.getTimestamp().getTime();
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
                        .append(makeHoverableAction(entry.getKey().actionType(), true).color(NamedTextColor.GREEN))
                        .append(Component.space())
                        .append(Component.text((count > 1 ? count + "x " : "")).color(NamedTextColor.WHITE))
                        .append(entry.getKey().block().color(NamedTextColor.GREEN))
                        .build());
            });
        }
        return contents;
    }

    public static Component makeHoverableAction(ActionType actionType, boolean useVerb) {
        String text = useVerb ? actionType.getVerb() : actionType.getName();
        return Component.text(text).hoverEvent(
                HoverEvent.showText(
                        Espial.prefix
                                .append(Component.newline())
                                .append(Component.text("Name: ").color(NamedTextColor.GRAY))
                                .append(Component.text(actionType.getName()).color(NamedTextColor.WHITE))
                                .append(Component.newline())
                                .append(Component.text("Description: ").color(NamedTextColor.GRAY))
                                .append(Component.text(actionType.getDescription()).color(NamedTextColor.WHITE))
                ));
    }

    private record BlockTracker(Component name, ActionType actionType, Component block) {}
}
