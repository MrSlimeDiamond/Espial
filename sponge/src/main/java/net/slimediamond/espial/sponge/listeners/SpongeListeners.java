package net.slimediamond.espial.sponge.listeners;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.slimediamond.espial.api.SignText;
import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.event.EspialEvents;
import net.slimediamond.espial.api.query.EspialQuery;
import net.slimediamond.espial.api.record.BlockRecord;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.record.HangingDeathRecord;
import net.slimediamond.espial.api.record.SignModifyRecord;
import net.slimediamond.espial.api.registry.EspialRegistryTypes;
import net.slimediamond.espial.api.transaction.Transaction;
import net.slimediamond.espial.api.transaction.TransactionType;
import net.slimediamond.espial.api.transaction.TransactionTypes;
import net.slimediamond.espial.sponge.utils.formatting.Format;
import net.slimediamond.espial.sponge.Espial;
import net.slimediamond.espial.sponge.data.EspialKeys;
import net.slimediamond.espial.sponge.query.EspialQueries;
import net.slimediamond.espial.sponge.wand.QueryBuilderCache;
import net.slimediamond.espial.sponge.wand.WandLoreBuilder;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.transaction.BlockTransaction;
import org.spongepowered.api.block.transaction.Operation;
import org.spongepowered.api.block.transaction.Operations;
import org.spongepowered.api.command.manager.CommandMapping;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.hanging.Hanging;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.entity.ChangeSignEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.plugin.PluginContainer;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class SpongeListeners {

    private static final Predicate<Slot> WAND_PREDICATE = slot -> slot.peek().get(EspialKeys.WAND).orElse(false);
    // need to store resource key because Sponge registry isn't initialized yet
    private static final Set<ResourceKey> IGNORED_MODIFICATION_BLOCK_TYPES = Set.of(
            BlockTypes.REDSTONE_ORE.location(),
            BlockTypes.DEEPSLATE_REDSTONE_ORE.location()
    );

    @Listener(order = Order.EARLY)
    public void onBlockChange(final InteractBlockEvent.Primary.Start event, @First final ServerPlayer player) {
        event.block().location().ifPresent(location -> {
            if (Espial.getInstance().getEspialService().getInspectingUsers().contains(player.uniqueId())) {
                event.setCancelled(true);
                EspialQueries.queryRecords(location, player);
            } else {
                handleWand(event, location, player);
            }
        });
    }

    @Listener(order = Order.EARLY)
    public void onBlockChange(final InteractBlockEvent.Secondary event, @First final ServerPlayer player) {
        if (!event.context().get(EventContextKeys.USED_HAND).map(ht -> ht.equals(HandTypes.MAIN_HAND.get())).orElse(false)) {
            return;
        }
        event.block().location().ifPresent(location -> {
            final ServerLocation actualLocation = location.relativeTo(event.targetSide());
            if (Espial.getInstance().getEspialService().getInspectingUsers().contains(player.uniqueId())) {
                EspialQueries.queryRecords(actualLocation, player);
            } else {
                handleWand(event, actualLocation, player);
            }
        });
    }

    @Listener
    public void onBlockChange(final ChangeBlockEvent.All event) {
        if (event.cause().containsType(CommandMapping.class)
                || event.cause().containsType(PluginContainer.class)) {
            return;
        }
        final Optional<Entity> optionalCause = event.cause().first(Entity.class);
        if (optionalCause.isEmpty()) {
            return;
        }
        final Entity cause = optionalCause.get();
        for (final BlockTransaction transaction : event.transactions()) {
            final BlockSnapshot original = transaction.original();
            final BlockSnapshot replacement = transaction.finalReplacement();
            if (original.location().isEmpty() || replacement.location().isEmpty()) {
                continue; // somehow no location
            }
            // some blocks, such as redstone ore, are very spammy in the logs when "modified"
            // so hard-ignore some of them
            if (IGNORED_MODIFICATION_BLOCK_TYPES.contains(original.state().type().key(RegistryTypes.BLOCK_TYPE))
                    && transaction.operation().equals(Operations.MODIFY.get())) {
                return;
            }
            final Optional<ServerPlayer> playerOptional = event.cause().first(ServerPlayer.class);

            if (playerOptional.isEmpty() && Espial.getInstance().getConfig().isLogPlayersOnly()) {
                return;
            }

            if (playerOptional.isPresent()
                    && Espial.getInstance().getEspialService().getInspectingUsers().contains(playerOptional.get().uniqueId())) {
                event.setCancelled(true);
                return;
            }

            final Optional<EspialEvent> eventOptional = getEspialEvent(transaction.operation());
            if (eventOptional.isEmpty()
                    || Espial.getInstance().getConfig().getIgnoredEvents()
                    .contains(eventOptional.get().key(EspialRegistryTypes.EVENT))) {
                continue; // unknown operation
            }
            final EspialEvent espialEvent = eventOptional.get();

            final BlockRecord.Builder builder = BlockRecord.builder()
                    .original(original)
                    .replacement(replacement)
                    .entityType(cause.type())
                    .location(original.location().get())
                    .event(espialEvent);

            playerOptional.ifPresent(player -> builder.user(player.uniqueId()));

            final EspialRecord record = builder.build();
            if (record.getTarget().equals(BlockTypes.AIR.location().formatted())) {
                return;
            }

            Espial.getInstance().getEspialService().submit(record);
        }
    }

    @Listener
    public void onEntityDestruct(final DestructEntityEvent event, @First final Entity cause) {
        final Entity entity = event.entity();
        if (entity instanceof final Hanging hanging) {
            final HangingDeathRecord.Builder builder = HangingDeathRecord.builder()
                    .entityType(cause.type())
                    .targetEntityType(hanging.type())
                    .event(EspialEvents.HANGING_DEATH.get())
                    .location(entity.serverLocation())
                    .extraData(hanging.toContainer());
            if (cause instanceof final ServerPlayer player) {
                builder.user(player.uniqueId());
            }
            Espial.getInstance().getEspialService().submit(builder.build());
        }
    }

    @Listener
    public void onSignChange(final ChangeSignEvent event, @First final Player player) {
        final List<Component> front;
        final List<Component> back;

        if (event.isFrontSide()) {
            front = event.text().all();
            back = event.sign().backText().lines().all();
        } else {
            front = event.sign().frontText().lines().all();
            back = event.text().all();
        }

        Espial.getInstance().getEspialService().submit(SignModifyRecord.builder()
                .originalContents(SignText.from(event.sign().frontText(), event.sign().backText()))
                .replacementContents(SignText.from(front, back))
                .entityType(player.type())
                .user(player.uniqueId())
                .event(EspialEvents.SIGN_MODIFY.get())
                .location(event.sign().serverLocation())
                .blockState(event.sign().block())
                .build());
    }

    private static Optional<EspialEvent> getEspialEvent(final Operation operation) {
        // see if a Sponge operation matches the value of the ResourceKey
        return EspialEvents.registry().streamEntries()
                .filter(entry -> entry.key().value().equals(operation.key(RegistryTypes.OPERATION).value()))
                .map(RegistryEntry::value)
                .findFirst();
    }

    private static void handleWand(final Event event, final ServerLocation location, final ServerPlayer player) {
        final ItemStack item = player.itemInHand(HandTypes.MAIN_HAND.get());
        if (item.get(EspialKeys.WAND).orElse(false)) {
            if (event instanceof Cancellable cancellable) {
                cancellable.setCancelled(true);
            }
            final Optional<EspialQuery.Builder> queryOptional = item.get(EspialKeys.WAND_FILTERS)
                    .map(QueryBuilderCache::get);
            if (queryOptional.isEmpty()) {
                player.sendMessage(Format.error("This wand does not store a valid query"));
                removeItem(player, WAND_PREDICATE);
                return;
            }
            final EspialQuery.Builder query = queryOptional.get();
            query.location(location);

            if (item.get(EspialKeys.WAND_MAX_USES).isPresent()) {
                // remove 1 durability, or kill it entirely
                final int max = item.get(EspialKeys.WAND_MAX_USES).get();
                final int used = item.get(EspialKeys.WAND_USES).orElse(max);
                final int result = used - 1;
                if (result <= 0) {
                    // durability is used up
                    removeItem(player, slot -> slot.peek().get(EspialKeys.WAND).orElse(false)
                            && slot.peek().get(EspialKeys.WAND_USES).orElse(0) == used);
                    player.playSound(Sound.sound(SoundTypes.ENTITY_ITEM_BREAK, Sound.Source.PLAYER, 1, 1));
                    player.sendActionBar(Format.error("Wand durability used up!"));

                    // ...but still continue
                } else {
                    // apply new shit
                    item.offer(EspialKeys.WAND_USES, result);
                    item.offer(Keys.LORE, WandLoreBuilder.getLore(item, query));
                }
            }

            if (item.get(EspialKeys.WAND_DOES_LOOKUPS).orElse(false)) {
                EspialQueries.showRecords(query.build(), player);
            } else {
                // get applier transaction
                final Optional<TransactionType> transactionOptional = item.get(EspialKeys.WAND_TRANSACTION_TYPE)
                        .map(typeKey -> TransactionTypes.registry().value(typeKey));
                if (transactionOptional.isEmpty()) {
                    player.sendMessage(Format.error("This wand does not have a valid transaction type"));
                    return;
                }
                Espial.getInstance().getEspialService().query(query.build()).thenAccept(records -> {
                    final Transaction result = transactionOptional.get().apply(records, player);
                    Espial.getInstance().getEspialService().getTransactionManager().submit(player.uniqueId(), result);
                    // TODO: messaging
                });
            }
        }
    }

    @Listener(order = Order.FIRST)
    public void onDispenseItem(final DropItemEvent.Dispense event) {
        clearTools(event.entities());
    }

    @Listener(order = Order.FIRST)
    public void onDestructItem(final DropItemEvent.Destruct event) {
        clearTools(event.entities());
    }

    private static void clearTools(final List<Entity> entities) {
        entities.forEach(entity -> {
            if (entity instanceof Item item) {
                if (item.item().get().get(EspialKeys.WAND).orElse(false)) {
                    entity.remove();
                }
            }
        });
    }

    private static void removeItem(final Carrier carrier, final Predicate<Slot> predicate) {
        carrier.inventory().slots().stream()
                .filter(predicate)
                .forEach(Slot::poll);
    }

}
