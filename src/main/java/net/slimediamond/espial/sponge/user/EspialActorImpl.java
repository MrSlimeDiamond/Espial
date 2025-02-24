package net.slimediamond.espial.sponge.user;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.slimediamond.espial.api.user.EspialActor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.math.vector.Vector3d;

public class EspialActorImpl implements EspialActor {
  private final Living living;
  private final String uuid;

  public EspialActorImpl(Living living) {
    this.living = living;

    if (living instanceof Player player) {
      this.uuid = player.profile().uuid().toString();
    } else {
      this.uuid = PlainTextComponentSerializer.plainText().serialize(living.displayName().get());
    }
  }

  public String getName() {
    if (living instanceof Player player) {
      return player.name();
    } else {
      return this.uuid;
    }
  }

  @Override
  public String getUUID() {
    return this.uuid;
  }

  @Override
  public @Nullable Vector3d getPosition() {
    return living.position();
  }

  @Override
  public @Nullable Vector3d getRotation() {
    return living.rotation();
  }

  @Override
  public String getItem() {
    return living.activeItem().get().type().key(RegistryTypes.ITEM_TYPE).formatted();
  }
}
