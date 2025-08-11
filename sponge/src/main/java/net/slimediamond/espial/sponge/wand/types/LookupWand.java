package net.slimediamond.espial.sponge.wand.types;

import net.slimediamond.espial.api.query.EspialQuery;
import net.slimediamond.espial.api.wand.WandType;
import net.slimediamond.espial.sponge.query.EspialQueries;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.ItemStack;

public class LookupWand implements WandType {

    @Override
    public void apply(final EspialQuery query, final ServerPlayer player, final ItemStack itemStack) {
        EspialQueries.showRecords(query, player);
    }

}
