package net.slimediamond.espial.sponge.commands;

import net.kyori.adventure.text.Component;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.transaction.Transaction;
import net.slimediamond.espial.api.transaction.TransactionType;
import net.slimediamond.espial.common.permission.Permission;
import net.slimediamond.espial.common.utils.formatting.Format;
import net.slimediamond.espial.sponge.Espial;
import net.slimediamond.espial.sponge.commands.subsystem.Parameters;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.Player;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public abstract class TransactionCommand extends RecordResultCommand {

    private final TransactionType transactionType;

    public TransactionCommand(final TransactionType transactionType,
                              final @Nullable Permission permission,
                              final @NotNull Component description) {
        super(permission, description);

        this.transactionType = transactionType;
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

        final Transaction transaction = transactionType.apply(records, context.cause().audience());
        context.cause().first(Player.class).ifPresent(player ->
                Espial.getInstance().getEspialService().getTransactionManager().submit(player.uniqueId(), transaction));
    }

}
