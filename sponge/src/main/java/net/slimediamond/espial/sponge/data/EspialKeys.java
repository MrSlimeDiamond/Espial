package net.slimediamond.espial.sponge.data;

import net.slimediamond.espial.api.query.EspialQuery;
import net.slimediamond.espial.api.transaction.TransactionType;
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
     */
    public static Key<Value<Integer>> WAND_FILTERS;

    /**
     * The {@link TransactionType} that a wand applies
     *
     * <p>Referenced via {@link ResourceKey}</p>
     */
    public static Key<Value<ResourceKey>> WAND_TRANSACTION_TYPE;

    /**
     * Whether an {@link org.spongepowered.api.item.inventory.ItemStack} is an Espial wand
     */
    public static Key<Value<Boolean>> WAND;

    /**
     * Whether an Espial wand does lookups instead of applying a transaction
     */
    public static Key<Value<Boolean>> WAND_DOES_LOOKUPS;

    /**
     * Whether an Espial wand is single use, which means that it will be
     * removed from the inventory of the acting player once it is used once
     */
    public static Key<Value<Integer>> WAND_MAX_USES;

    /**
     * The amount of uses that a wand has had
     */
    public static Key<Value<Integer>> WAND_USES;

}
