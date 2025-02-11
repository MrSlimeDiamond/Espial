package net.slimediamond.espial.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.slimediamond.espial.action.BlockAction;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class DisplayNameUtil {
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
}
