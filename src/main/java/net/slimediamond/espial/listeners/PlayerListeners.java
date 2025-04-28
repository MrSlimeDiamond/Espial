package net.slimediamond.espial.listeners;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.util.Format;
import org.spongepowered.api.adventure.Audiences;
import org.spongepowered.api.adventure.SpongeComponents;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;

import java.sql.SQLException;

public class PlayerListeners {
    private static final String MESSAGING_PERMISSION = "espial.admin.messaging";
    private static final Audience ADMINS = Audiences.withPermission(MESSAGING_PERMISSION);

    @Listener
    public void onPlayerLeave(ServerSideConnectionEvent.Leave event) {
        if (Espial.getInstance().getBlockOutlines().containsKey(event.player())) {
            Espial.getInstance().getBlockOutlines().get(event.player()).cancel();
            Espial.getInstance().getBlockOutlines().remove(event.player());
        }
    }

    @Listener
    public void onPlayerJoin(ServerSideConnectionEvent.Join event) {
        if (event.player().hasPermission(MESSAGING_PERMISSION)) {
            if (Espial.getInstance().getDatabase().hasLegacyTable()) {
                event.player().sendMessage(Format.error(
                                "You currently have the legacy 'blocklog' database table in your storage.")
                        .appendNewline()
                        .append(Component.text(
                                        "WARNING: Espial v1 --> v2 no longer stores the position of " +
                                                "the player. If you want to keep this data, back up your database.")
                                .color(NamedTextColor.RED)).appendNewline()
                        .append(Component.text("Click ").color(NamedTextColor.RED))
                        .append(Format.chip(
                                Component.text("HERE").color(NamedTextColor.DARK_RED),
                                Component.text("Drop the 'blocklog' table"))
                        .clickEvent(SpongeComponents.executeCallback(cause -> {
                            if (!Espial.getInstance().getDatabase().hasLegacyTable()) {
                                cause.sendMessage(Format.error("The database does not contain the legacy table."));
                                return;
                            }
                            try {
                                Espial.getInstance().getDatabase().dropOldTable();
                                ADMINS.sendMessage(Format.text(event.player().name() +
                                        " dropped the legacy 'blocklog' table."));
                            } catch (SQLException e) {
                                e.printStackTrace();
                                cause.sendMessage(Format.error("Caught a SQLException when dropping the legacy table."));
                            }

                        })))
                        .append(Component.text(" to erase the old table!")
                                .color(NamedTextColor.RED))
                );
            }
        }
    }
}
