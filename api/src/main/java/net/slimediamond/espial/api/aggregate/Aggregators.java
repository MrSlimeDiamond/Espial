package net.slimediamond.espial.api.aggregate;

import net.slimediamond.espial.api.EspialResourceKey;
import net.slimediamond.espial.api.registry.EspialRegistryTypes;
import net.slimediamond.espial.api.time.Day;
import net.slimediamond.espial.api.time.Month;
import net.slimediamond.espial.api.time.Year;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.*;

import java.util.UUID;

@RegistryScopes(scopes = RegistryScope.GAME)
public final class Aggregators {

    public static final DefaultedRegistryReference<Aggregator<Day>> DAY = Aggregators.key(EspialResourceKey.espial("day"));

    public static final DefaultedRegistryReference<Aggregator<Month>> MONTH = Aggregators.key(EspialResourceKey.espial("month"));

    public static final DefaultedRegistryReference<Aggregator<Year>> YEAR = Aggregators.key(EspialResourceKey.espial("year"));

    public static final DefaultedRegistryReference<Aggregator<UUID>> USER = Aggregators.key(EspialResourceKey.espial("user"));

    private static <T> DefaultedRegistryReference<Aggregator<T>> key(final ResourceKey key) {
        return RegistryKey.of(EspialRegistryTypes.AGGREGATOR, key).asDefaultedReference(Sponge::game);
    }

    public static Registry<Aggregator<?>> registry() {
        return Sponge.game().registry(EspialRegistryTypes.AGGREGATOR);
    }

}
