package net.slimediamond.espial.sponge.aggregate;

import net.slimediamond.espial.api.aggregate.Aggregator;
import net.slimediamond.espial.api.aggregate.ResultAggregate;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.time.Day;
import net.slimediamond.espial.sponge.utils.DateUtils;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DayAggregator implements Aggregator<Day> {

    @Override
    public List<ResultAggregate<Day>> aggregate(final List<EspialRecord> records) {
        final Map<Day, List<EspialRecord>> counts = records.stream()
                .sorted(Comparator.comparingLong(record -> record.getDate().getTime()))
                .collect(Collectors.groupingBy(
                        record -> Day.from(DateUtils.getYear(record.getDate()),
                                DateUtils.getMonth(record.getDate()),
                                record.getDate().getDate()),
                        LinkedHashMap::new,
                        Collectors.toList()));

        return counts.entrySet().stream()
                .map(entry -> ResultAggregate.from(entry.getKey(), entry.getValue().size()))
                .toList();
    }

}
