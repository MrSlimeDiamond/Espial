package net.slimediamond.espial.sponge.commands;

import net.kyori.adventure.text.Component;
import net.slimediamond.espial.api.query.EspialQuery;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.common.permission.Permission;
import net.slimediamond.espial.sponge.Espial;
import net.slimediamond.espial.sponge.commands.subsystem.AbstractCommand;
import net.slimediamond.espial.sponge.commands.subsystem.Flags;
import net.slimediamond.espial.sponge.commands.subsystem.Parameters;
import net.slimediamond.espial.sponge.query.RangeSelector;
import net.slimediamond.espial.sponge.query.Selector;
import net.slimediamond.espial.sponge.query.Vector3iRange;
import net.slimediamond.espial.common.utils.formatting.Format;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.world.server.ServerLocation;

import java.time.Instant;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class RecordResultCommand extends AbstractCommand {

    private List<Predicate<EspialRecord>> predicates = new LinkedList<>();

    private static final Set<Selector> SELECTORS = Set.of(
            new RangeSelector()
    );

    public RecordResultCommand(final @Nullable Permission permission,
                               final @NotNull Component description) {
        super(permission, description);

        SELECTORS.forEach(selector -> addFlag(selector.getFlag(), selector.getDescription()));
        addFlag(Flags.BEFORE, Component.text("Query for logs before a certain time"));
        addFlag(Flags.AFTER, Component.text("Query for logs after a specific time"));
        addFlag(Flags.PLAYER, Component.text("Filter for a specific player"));
        addFlag(Flags.BLOCK, Component.text("Filter for a specific block type"));
        addFlag(Flags.EVENT, Component.text("Filter by a specific event"));
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException {
        final ServerLocation location = context.cause().location()
                .orElseThrow(() -> new CommandException(Component.text("You must have a location to use this command")));
        Vector3iRange selection = null;
        for (final Selector selector : SELECTORS) {
            if (context.hasFlag(selector.getFlag())) {
                if (selection != null) {
                    return CommandResult.error(Format.error("Provide only one selector!"));
                }
                selection = selector.select(context);
            }
        }

        if (selection == null) {
            return CommandResult.error(Format.error("You need to provide a selector!"));
        }

        final EspialQuery.Builder builder = EspialQuery.builder()
                .minimum(selection.getMinimum())
                .maximum(selection.getMaximum())
                .worldKey(location.worldKey())
                .audience(context.cause().audience());

        context.one(Parameters.AFTER).ifPresent(duration -> {
            final Date date = Date.from(Instant.now().minus(duration));
            builder.after(date);
        });

        context.one(Parameters.BEFORE).ifPresent(duration -> {
            final Date date = Date.from(Instant.now().minus(duration));
            builder.before(date);
        });

        context.all(Parameters.USER).forEach(builder::addUser);
        context.all(Parameters.BLOCK_TYPE).forEach(builder::addBlockType);
        context.all(Parameters.EVENT).forEach(builder::addEvent);

        final EspialQuery query = builder.build();

        context.sendMessage(Format.text("Querying records..."));
        Espial.getInstance().getEspialService().query(query).thenAccept(records -> {
            Stream<EspialRecord> stream = records.stream();
            for (final Predicate<EspialRecord> predicate : predicates) {
                stream = stream.filter(predicate);
            }
            final List<EspialRecord> results = stream.collect(Collectors.toList()); // mutable list
            if (results.isEmpty()) {
                context.sendMessage(Format.error("No records found"));
            } else {
                this.apply(context, results);
            }
        });
        return CommandResult.success();
    }

    public abstract void apply(final CommandContext context, final List<EspialRecord> records);

    protected void addPredicate(@NotNull final Predicate<EspialRecord> predicate) {
        predicates.add(predicate);
    }

}
