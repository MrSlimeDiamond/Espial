package net.slimediamond.espial.common.utils.formatting;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class Format {

    public static final TextColor THEME_COLOR = TextColor.color(67, 196, 100);
    public static final TextColor ACCENT_COLOR = TextColor.color(156, 226, 171);
    public static final TextColor TEXT_COLOR = TextColor.color(230, 255, 240);
    public static final TextColor DULL_COLOR = NamedTextColor.DARK_GRAY;
    public static final TextColor WARNING_COLOR = TextColor.color(255, 243, 0);
    public static final TextColor PADDING_COLOR = TextColor.color(224, 186, 215);
    public static final TextColor ERROR_COLOR = NamedTextColor.RED;
    public static final TextColor DETAIL_KEY_COLOR = TextColor.color(49, 175, 212);
    public static final TextColor DETAIL_VALUE_COLOR = TEXT_COLOR;
    public static final TextColor COMMAND_HINT_COLOR = TextColor.color(85, 180, 142);
    public static final TextColor TITLE_COLOR = TextColor.color(224, 141, 121);

    public static final Component PADDING = Component.text("=").color(PADDING_COLOR);
    public static final Component PREFIX = Component.text("Espial › ").color(THEME_COLOR);
    public static final Component NO_RECORDS_FOUND = Format.error("No records were found");

    public static Component component(final Component component) {
        return PREFIX.append(component);
    }

    public static Component component(final TextComponent.Builder builder) {
        return component(builder.build());
    }

    public static Component text(final String text) {
        return PREFIX.append(Component.text(text).color(TEXT_COLOR));
    }

    public static Component error(final String text) {
        return component(Component.text(text).color(ERROR_COLOR));
    }

    public static Component commandHint(final String command) {
        return commandHint(command, command,
                Component.text(command).color(NamedTextColor.GRAY));
    }

    public static Component commandHint(final String display, final String command,
                                        final Component hover) {
        Component component = Component.text("[").color(TITLE_COLOR)
                .append(Component.text(display).color(COMMAND_HINT_COLOR))
                .append(Component.text("]").color(TITLE_COLOR));
        if (hover != null) {
            component = component.hoverEvent(HoverEvent.showText(hover));
        }

        return component.clickEvent(ClickEvent.runCommand(command));
    }

    public static Component defaults(final String defaults) {
        return component(Component.text("Defaults used: " + defaults).color(NamedTextColor.GRAY));
    }

    public static Component title(final String text) {
        return Component.text().append(PREFIX).append(Component.text(text).color(TITLE_COLOR)).build();
    }

    public static Component accent(final String text) {
        return Component.text(text).color(ACCENT_COLOR);
    }

    public static Component dull(final String text) {
        return Component.text(text).color(DULL_COLOR);
    }

    public static Component warn(final String text) {
        return component(Component.text(text).color(Format.WARNING_COLOR));
    }

    public static Component detail(final String key, final String value) {
        return detail(key, Component.text(value));
    }

    public static Component detail(final String key, final Component value) {
        return Component.text()
                .append(Component.text(key).color(DETAIL_KEY_COLOR))
                .append(Component.text(": ").color(DETAIL_VALUE_COLOR))
                .append(value == null ? Component.text("null") : value).color(DETAIL_VALUE_COLOR)
                .build();
    }

    public static Component link(final String display, final String url) {
        return Component.text(display).color(COMMAND_HINT_COLOR)
                .hoverEvent(HoverEvent.showText(Component.text(url).color(NamedTextColor.GRAY)))
                .clickEvent(ClickEvent.openUrl(url));
    }

}
