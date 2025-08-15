package net.slimediamond.espial.sponge.commands;

import net.kyori.adventure.text.Component;
import net.slimediamond.espial.api.aggregate.Aggregator;
import net.slimediamond.espial.api.aggregate.Aggregators;
import net.slimediamond.espial.api.aggregate.ResultAggregate;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.sponge.commands.subsystem.Flags;
import net.slimediamond.espial.sponge.commands.subsystem.Parameters;
import net.slimediamond.espial.sponge.permission.Permissions;
import net.slimediamond.espial.sponge.query.selector.GlobalSelector;
import net.slimediamond.espial.sponge.utils.formatting.Format;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.service.pagination.PaginationList;

import java.util.Comparator;
import java.util.List;

public class GraphCommand extends RecordResultCommand {

    public GraphCommand() {
        super(Permissions.TIMELINE, Component.text("Create a graph of events within a selection"));

        addAlias("graph");
        addAlias("g");

        addSelector(new GlobalSelector());
        addFlag(Flags.AGGREGATE_BY, Component.text("Aggregate results by a certain aggregate"));
    }

    @Override
    public void apply(final CommandContext context, final List<EspialRecord> records) {
        final Aggregator<?> aggregator = context.one(Parameters.AGGREGATOR).orElse(Aggregators.MONTH.get());
        final List<? extends ResultAggregate<?>> results = aggregator.aggregate(records).stream()
                .sorted(Comparator.comparingInt(ResultAggregate::getCount))
                .toList();

        final int max = results.stream()
                .mapToInt(ResultAggregate::getCount)
                .max()
                .orElse(1);

        PaginationList.builder()
                .title(Format.title("Result graph"))
                .contents(results.stream()
                        .map(result -> {
                            final int count = result.getCount();
                            final int scaledLength = (int) Math.round((count / (double) max) * 20); // bar length in chars

                            final String bar = "█".repeat(Math.max(0, scaledLength));

                            return Component.text()
                                    .append(Format.accent(result.getKeyAsString()))
                                    .append(Format.dull(" | "))
                                    .append(Component.text(bar))
                                    .build().asComponent();
                        })
                        .toList())
                .padding(Format.PADDING)
                .sendTo(context.cause().audience());
    }

}
