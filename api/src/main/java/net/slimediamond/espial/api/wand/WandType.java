package net.slimediamond.espial.api.wand;

import net.slimediamond.espial.api.query.EspialQuery;
import net.slimediamond.espial.api.transaction.Transaction;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.registry.DefaultedRegistryValue;
import org.spongepowered.api.util.annotation.CatalogedBy;

/**
 * A wand type which is used on Espial wands (obtained
 * with the {@code /espial wand} command)
 */
@CatalogedBy(WandTypes.class)
public interface WandType extends DefaultedRegistryValue {

    /**
     * Apply the wand's usage to a built {@link EspialQuery}
     *
     * @param query The query to use for the wand, containing
     *              relevant information such as the location(s)
     *              of blocks to action
     * @param player The player who used the wand to initiate
     *               the action. In some cases, this is used
     *               to add a {@link Transaction} so that the
     *               action may be undone
     * @param itemStack The item of the wand used, mostly for
     *                  storing extra data in the form of keys
     *                  so that the wand can be applied properly
     */
    void apply(EspialQuery query, ServerPlayer player, ItemStack itemStack);

}
