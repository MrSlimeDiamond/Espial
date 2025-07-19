package net.slimediamond.espial.sponge.wand;

import net.slimediamond.espial.api.query.EspialQuery;

import java.util.HashMap;
import java.util.Map;

public class QueryBuilderCache {

    private static final Map<Integer, EspialQuery.Builder> map = new HashMap<>();

    public static int add(final EspialQuery.Builder builder) {
        final int id = builder.hashCode();
        map.put(id, builder);
        return id;
    }

    public static EspialQuery.Builder get(final int id) {
        return map.get(id);
    }

}
