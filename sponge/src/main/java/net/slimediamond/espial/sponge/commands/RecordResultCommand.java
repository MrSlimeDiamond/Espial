package net.slimediamond.espial.sponge.commands;

import net.kyori.adventure.text.Component;
import net.slimediamond.espial.api.query.EspialQuery;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.sponge.Espial;
import net.slimediamond.espial.sponge.commands.subsystem.AbstractCommand;
import net.slimediamond.espial.sponge.commands.subsystem.Flags;
import net.slimediamond.espial.sponge.commands.subsystem.HelpCommand;
import net.slimediamond.espial.sponge.permission.Permission;
import net.slimediamond.espial.sponge.query.selector.RangeSelector;
import net.slimediamond.espial.sponge.query.selector.Selector;
import net.slimediamond.espial.sponge.query.selector.Vector3iRange;
import net.slimediamond.espial.sponge.query.selector.WorldEditSelector;
import net.slimediamond.espial.sponge.utils.CommandUtils;
import net.slimediamond.espial.sponge.utils.formatting.Format;
import net.slimediamond.espial.sponge.utils.formatting.RecordFormatter;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class RecordResultCommand extends AbstractCommand {

    protected final List<Predicate<EspialRecord>> predicates = new LinkedList<>();
    protected final Set<Selector> selectors = new HashSet<>();

    public RecordResultCommand(final @Nullable Permission permission,
                               final @NotNull Component description) {
        super(permission, description);

        selectors.add(new RangeSelector());
        Sponge.pluginManager().plugin("worldedit")
                .ifPresent(worldedit -> selectors.add(new WorldEditSelector()));

        selectors.stream().flatMap(selector -> selector.getFlag().stream())
                        .forEach(entry -> addFlag(entry.getFlag(), entry.getDescription()));

        addFlags(Flags.QUERY_FLAGS);
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException {
        // This can throw a CommandException, so if we don't meet some preconditions
        // then we can exit
        prerequisites();

        final ServerLocation location = context.cause().location()
                .orElseThrow(() -> new CommandException(Component.text("You must have a location to use this command")));
        Vector3iRange selection = null;
        for (final Selector selector : selectors) {
            if (selector.getFlag().isPresent()) {
                if (context.hasFlag(selector.getFlag().get().getFlag())) {
                    selection = selector.select(context);
                }
            } else {
                selection = selector.select(context);
            }
        }

        if (selection == null) {
            // show help
            context.sendMessage(Format.error("You need to provide a selector"));
            return new HelpCommand(this).execute(context);
        }

        final EspialQuery.Builder builder = CommandUtils.getQueryBuilder(context);

        builder.minimum(selection.getMinimum());
        builder.maximum(selection.getMaximum());
        builder.worldKey(location.worldKey());

        final EspialQuery query = builder.build();

        context.sendMessage(Format.text("Querying records..."));
        Espial.getInstance().getEspialService().query(query).thenAccept(records -> {
            Stream<EspialRecord> stream = records.stream();
            for (final Predicate<EspialRecord> predicate : predicates) {
                stream = stream.filter(predicate);
            }
            final List<EspialRecord> results = stream.collect(Collectors.toList()); // mutable list
            if (results.isEmpty()) {
                context.sendMessage(Format.NO_RECORDS_FOUND);
            } else {
                this.apply(context, results);
            }
        });
        return CommandResult.success();
    }

    public abstract void apply(final CommandContext context, final List<EspialRecord> records);

    public void prerequisites() throws CommandException {

    }

    protected void addPredicate(@NotNull final Predicate<EspialRecord> predicate) {
        predicates.add(predicate);
    }

    protected void addSelector(@NotNull final Selector selector) {
        selectors.add(selector);
        selector.getFlag().ifPresent(flag -> addFlag(flag.getFlag(), flag.getDescription()));
    }

    public void displayRecords(final CommandContext context, final List<EspialRecord> records, final boolean spread) {
        PaginationList.builder()
                .title(Format.title("Lookup results"))
                .contents(RecordFormatter.formatRecords(records, spread))
                .padding(Format.PADDING)
                .sendTo(context.cause().audience());
    }

}
