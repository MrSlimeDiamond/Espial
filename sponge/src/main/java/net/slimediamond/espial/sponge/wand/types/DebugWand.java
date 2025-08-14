package net.slimediamond.espial.sponge.wand.types;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.slimediamond.espial.api.query.EspialQuery;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.wand.WandType;
import net.slimediamond.espial.sponge.Espial;
import net.slimediamond.espial.sponge.utils.formatting.Format;
import net.slimediamond.espial.sponge.utils.formatting.RecordFormatter;
import org.spongepowered.api.adventure.SpongeComponents;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.pagination.PaginationList;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class DebugWand implements WandType {

    @Override
    public void apply(final EspialQuery query, final ServerPlayer player, final ItemStack itemStack) {
        Espial.getInstance().getEspialService().query(query).thenAccept(records -> {
            if (records.isEmpty()) {
                player.sendMessage(Format.NO_RECORDS_FOUND);
                return;
            }
            PaginationList.builder()
                    .contents(records.stream().map(record -> RecordFormatter.format(record)
                            .append(Component.text(" [D]").color(NamedTextColor.DARK_AQUA)
                                    .hoverEvent(HoverEvent.showText(
                                            Component.text()
                                                    .append(Format.title("Debug info"))
                                                    .appendNewline()
                                                    .append(basicDebugInfo(record))
                                                    .appendNewline()
                                                    .append(Format.accent("Click for more info"))))
                                    .clickEvent(SpongeComponents.executeCallback(cause -> cause.sendMessage(extraInfo(record))))))
                            .toList())
                    .padding(Format.PADDING)
                    .title(Format.title("Logs + debug info @ ")
                            .append(Format.position(query.getMinimumPosition()).color(Format.ACCENT_COLOR)))
                    .sendTo(player);
        });
    }

    private static Component basicDebugInfo(final EspialRecord record) {
        return Component.join(JoinConfiguration.newlines(),
                Format.detail("Type", record.getClass().getSimpleName()),
                Format.detail("ID", String.valueOf(record.getId())),
                Format.detail("Target", record.getTarget()),
                Format.detail("Rolled back", record.isRolledBack() ? "yes" : "no"));
    }

    private static Component extraInfo(final EspialRecord record) {
        return Component.text()
                .append(basicDebugInfo(record))
                .appendNewline()
                .append(Component.join(JoinConfiguration.newlines(), getFields(record)))
                .build();
    }

    private static List<Component> getFields(final Object instance) {
        final List<Component> results = new LinkedList<>();
        results.add(Format.accent("=== Fields in " + instance.getClass().getSimpleName() + " ==="));

        Class<?> clazz = instance.getClass();
        while (clazz != null && clazz != Object.class) {
            if (clazz != instance.getClass()) {
                results.add(Format.accent("--- Fields inherited from " + clazz.getSimpleName() + " ---"));
            }
            results.addAll(Arrays.stream(clazz.getDeclaredFields()).map(field -> {
                Component value;
                try {
                    field.setAccessible(true);
                    final Object thing = field.get(instance);
                    if (thing == null) {
                        value = Component.text("null").decorate(TextDecoration.ITALIC);
                    } else {
                        value = Component.text(thing.toString())
                                .clickEvent(SpongeComponents.executeCallback(cause -> cause.sendMessage(
                                        Component.join(JoinConfiguration.newlines(), getFields(thing))
                                )));
                    }
                } catch (final Exception e) {
                    if (e instanceof InaccessibleObjectException) {
                        value = Component.text("Access denied")
                                .decorate(TextDecoration.ITALIC)
                                .color(Format.ERROR_COLOR);
                    } else {
                        value = Component.text(e.getClass().getName()).color(Format.ERROR_COLOR);
                        Espial.getInstance().getLogger().error(e);
                    }
                }
                return field(field, value);
            }).toList());
            clazz = clazz.getSuperclass();
        }
        if (results.size() == 1) {
            results.add(Format.warn("No fields seem to be present"));
        }
        return results;
    }

    private static Component field(final Field field, final Component value) {
        return Component.text("  ")
                .append(Component.text(field.getName()).color(Format.THEME_COLOR)
                        .hoverEvent(HoverEvent.showText(Component.join(JoinConfiguration.newlines(),
                                Format.detail("Type", field.getType().getSimpleName()),
                                Format.detail("Modifiers", Modifier.toString(field.getModifiers())),
                                Format.detail("Annotations", Arrays.stream(field.getAnnotations())
                                        .map(a -> a.annotationType().getSimpleName())
                                        .collect(Collectors.joining(", "))),
                                Format.detail("Synthetic", String.valueOf(field.isSynthetic())),
                                Format.detail("Final", String.valueOf(Modifier.isFinal(field.getModifiers())))
                        ))))
                .append(Format.accent(" = "))
                .append(value);
    }

}
