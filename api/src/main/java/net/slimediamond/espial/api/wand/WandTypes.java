package net.slimediamond.espial.api.wand;

import net.slimediamond.espial.api.EspialResourceKey;
import net.slimediamond.espial.api.registry.EspialRegistryTypes;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.*;

@RegistryScopes(scopes = RegistryScope.GAME)
public final class WandTypes {

    public static final DefaultedRegistryReference<WandType> LOOKUP = WandTypes.key(EspialResourceKey.espial("lookup"));

    public static final DefaultedRegistryReference<WandType> ROLLBACK = WandTypes.key(EspialResourceKey.espial("rollback"));

    public static final DefaultedRegistryReference<WandType> RESTORE = WandTypes.key(EspialResourceKey.espial("restore"));

    public static final DefaultedRegistryReference<WandType> STAGE = WandTypes.key(EspialResourceKey.espial("stage"));

    public static final DefaultedRegistryReference<WandType> DEBUG = WandTypes.key(EspialResourceKey.espial("debug"));

    private static DefaultedRegistryReference<WandType> key(final ResourceKey key) {
        return RegistryKey.of(EspialRegistryTypes.WAND_TYPE, key).asDefaultedReference(Sponge::game);
    }

    public static Registry<WandType> registry() {
        return Sponge.game().registry(EspialRegistryTypes.WAND_TYPE);
    }

}
