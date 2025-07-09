package net.slimediamond.espial.api.transaction;

import net.slimediamond.espial.api.EspialResourceKey;
import net.slimediamond.espial.api.registry.EspialRegistryTypes;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.DefaultedRegistryReference;
import org.spongepowered.api.registry.RegistryKey;

public class TransactionTypes {

    public static final DefaultedRegistryReference<TransactionType> ROLLBACK = TransactionTypes .key(EspialResourceKey.espial("rollback"));

    public static final DefaultedRegistryReference<TransactionType> RESTORE = TransactionTypes .key(EspialResourceKey.espial("restore"));

    private static DefaultedRegistryReference<TransactionType> key(final ResourceKey location) {
        return RegistryKey.of(EspialRegistryTypes.TRANSACTION_TYPE, location).asDefaultedReference(Sponge::game);
    }

}
