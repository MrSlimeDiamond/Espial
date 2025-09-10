package net.slimediamond.espial.sponge.commands;

import net.kyori.adventure.text.Component;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.transaction.Transaction;
import net.slimediamond.espial.api.transaction.TransactionType;
import net.slimediamond.espial.sponge.Espial;
import net.slimediamond.espial.sponge.commands.subsystem.Parameters;
import net.slimediamond.espial.sponge.permission.Permission;
import net.slimediamond.espial.sponge.utils.formatting.Format;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class TransactionCommand extends RecordResultCommand {

    private final TransactionType transactionType;
    private final boolean preview;

    public TransactionCommand(final TransactionType transactionType,
                              final @Nullable Permission permission,
                              final @NotNull Component description,
                              final boolean preview) {
        super(permission, description);

        this.transactionType = transactionType;
        this.preview = preview;
    }

    @Override
    public void apply(final CommandContext context, List<EspialRecord> records) {

        final Instant threeDaysAgo = Instant.now().minus(3, ChronoUnit.DAYS);

        if (!context.hasAny(Parameters.AFTER) && !context.hasAny(Parameters.BEFORE)) {
            records = records.stream()
                    .filter(record -> record.getDate().toInstant().isAfter(threeDaysAgo))
                    .collect(Collectors.toList());
            context.sendMessage(Format.defaults("After: 3 days ago"));
        }

        if (preview) {
            final Optional<ServerPlayer> playerOptional = context.cause().first(ServerPlayer.class);
            if (playerOptional.isEmpty()) {
                context.sendMessage(Format.error("Only players can use this command"));
                return;
            }
            final ServerPlayer player = playerOptional.get();
            final Transaction preview = transactionType.preview(records, player);
            Espial.getInstance().getEspialService().getPreviewManager().submit(player.uniqueId(), preview);
            player.sendMessage(Format.text("Preview applied for ")
                    .append(Format.accent(String.valueOf(records.size())))
                    .append(Component.text(" records "))
                    .append(Format.commandHint("apply", "/espial preview apply"))
                    .appendSpace()
                    .append(Format.commandHint("cancel", "/espial preview cancel")));
        } else {
            final Transaction transaction = transactionType.apply(records, context.cause().audience());
            context.cause().first(Player.class).ifPresent(player ->
                    Espial.getInstance().getEspialService().getTransactionManager().submit(player.uniqueId(), transaction));
        }
    }

}
