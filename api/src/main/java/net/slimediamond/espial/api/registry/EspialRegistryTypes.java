package net.slimediamond.espial.api.registry;

import net.slimediamond.espial.api.EspialResourceKey;
import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.transaction.TransactionType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.DefaultedRegistryType;
import org.spongepowered.api.registry.RegistryRoots;
import org.spongepowered.api.registry.RegistryType;

public class EspialRegistryTypes {

    public static final DefaultedRegistryType<EspialEvent> EVENT = EspialRegistryTypes.espialKeyInGame("event");
    public static final DefaultedRegistryType<TransactionType> TRANSACTION_TYPE = EspialRegistryTypes.espialKeyInGame("transaction_type");

    private static <V>DefaultedRegistryType<V> espialKeyInGame(final String key) {
        return RegistryType.of(RegistryRoots.SPONGE, EspialResourceKey.espial(key)).asDefaultedType(Sponge::game);
    }

}
