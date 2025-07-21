package net.slimediamond.espial.sponge.data;

import net.slimediamond.espial.api.query.EspialQuery;
import net.slimediamond.espial.api.transaction.TransactionType;
import net.slimediamond.espial.sponge.Espial;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;

/**
 * Data keys for Espial
 */
public class EspialKeys {

    /**
     * The filters on a wand.
     *
     * <p>The stored integer is an ID which points to an
     * {@link EspialQuery.Builder}</p>
     *
     * @see net.slimediamond.espial.sponge.wand.QueryBuilderCache#get(int)
     */
    public static final Key<Value<Integer>> WAND_FILTERS = EspialKeys.key("wand_filters", Integer.class);

    /**
     * The {@link TransactionType} that a wand applies
     *
     * <p>Referenced via {@link ResourceKey}</p>
     */
    public static final Key<Value<ResourceKey>> WAND_TRANSACTION_TYPE = EspialKeys.key("wand_transaction_type", ResourceKey.class);

    /**
     * Whether an {@link org.spongepowered.api.item.inventory.ItemStack} is an Espial wand
     */
    public static final Key<Value<Boolean>> WAND = EspialKeys.key("is_wand", Boolean.class);

    /**
     * Whether an Espial wand does lookups instead of applying a transaction
     */
    public static final Key<Value<Boolean>> WAND_DOES_LOOKUPS = EspialKeys.key("wand_lookup", Boolean.class);

    /**
     * Whether an Espial wand is single use, which means that it will be
     * removed from the inventory of the acting player once it is used once
     */
    public static final Key<Value<Integer>> WAND_MAX_USES = EspialKeys.key("wand_max_uses", Integer.class);

    /**
     * The amount of uses that a wand has had
     */
    public static final Key<Value<Integer>> WAND_USES = EspialKeys.key("wand_uses", Integer.class);

    private static <T> Key<Value<T>> key(final String key, final Class<T> clazz) {
        return Key.from(Espial.getInstance().getContainer(), key, clazz);
    }

}
