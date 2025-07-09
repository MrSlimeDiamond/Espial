package net.slimediamond.espial.api.event;

import net.slimediamond.espial.api.EspialResourceKey;
import net.slimediamond.espial.api.registry.EspialRegistryTypes;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.DefaultedRegistryReference;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryKey;
import org.spongepowered.api.registry.RegistryScope;
import org.spongepowered.api.registry.RegistryScopes;

@RegistryScopes(scopes = RegistryScope.GAME)
public final class EspialEvents {

    public static final DefaultedRegistryReference<EspialEvent> BREAK = EspialEvents.key(EspialResourceKey.espial("break"));

    public static final DefaultedRegistryReference<EspialEvent> PLACE = EspialEvents.key(EspialResourceKey.espial("place"));

    public static final DefaultedRegistryReference<EspialEvent> DECAY = EspialEvents.key(EspialResourceKey.espial("decay"));

    public static final DefaultedRegistryReference<EspialEvent> GROWTH = EspialEvents.key(EspialResourceKey.espial("growth"));

    public static final DefaultedRegistryReference<EspialEvent> LIQUID_DECAY = EspialEvents.key(EspialResourceKey.espial("liquid_decay"));

    public static final DefaultedRegistryReference<EspialEvent> MODIFY = EspialEvents.key(EspialResourceKey.espial("modify"));

    public static final DefaultedRegistryReference<EspialEvent> INTERACT = EspialEvents.key(EspialResourceKey.espial("interact"));

    public static final DefaultedRegistryReference<EspialEvent> HANGING_DEATH = EspialEvents.key(EspialResourceKey.espial("hanging_death"));

    public static final DefaultedRegistryReference<EspialEvent> ITEM_FRAME_REMOVE = EspialEvents.key(EspialResourceKey.espial("item_frame_remove"));

    public static Registry<EspialEvent> registry() {
        return Sponge.game().registry(EspialRegistryTypes.EVENT);
    }

    private static DefaultedRegistryReference<EspialEvent> key(final ResourceKey location) {
        return RegistryKey.of(EspialRegistryTypes.EVENT, location).asDefaultedReference(Sponge::game);
    }

}
