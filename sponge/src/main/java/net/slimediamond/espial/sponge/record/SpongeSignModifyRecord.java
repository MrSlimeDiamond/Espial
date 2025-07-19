package net.slimediamond.espial.sponge.record;

import net.slimediamond.espial.api.SignText;
import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.record.SignModifyRecord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.Date;
import java.util.UUID;

public class SpongeSignModifyRecord extends SpongeEspialRecord implements SignModifyRecord {

    private final SignText originalContents;
    private final SignText replacementContents;
    private final boolean frontSide;
    private final BlockState blockState;

    public SpongeSignModifyRecord(final @NotNull Date date, final @Nullable UUID user, final @NotNull EntityType<?> entityType,
                                  final @NotNull ServerLocation location, final @NotNull EspialEvent event, final boolean rolledBack,
                                  final SignText originalContents, final SignText replacementContents, final boolean frontSide,
                                  final BlockState blockState) {
        super(date, user, entityType, location, event, rolledBack);
        this.originalContents = originalContents;
        this.replacementContents = replacementContents;
        this.frontSide = frontSide;
        this.blockState = blockState;
    }

    @Override
    public SignText getOriginalContents() {
        return originalContents;
    }

    @Override
    public SignText getReplacementContents() {
        return replacementContents;
    }

    @Override
    public boolean isFrontSide() {
        return frontSide;
    }

    @Override
    public BlockState getBlockState() {
        return blockState;
    }

    @Override
    public String getTarget() {
        return blockState.type().key(RegistryTypes.BLOCK_TYPE).formatted();
    }

    @Override
    public void rollback() {
        getLocation().blockEntity().ifPresent(blockEntity -> {
            // TODO: Back sign lines!
            blockEntity.offer(Keys.SIGN_LINES, originalContents.getFront());
        });
    }

    @Override
    public void restore() {
        getLocation().blockEntity().ifPresent(blockEntity -> {
            // TODO: Back sign lines!
            blockEntity.offer(Keys.SIGN_LINES, replacementContents.getFront());
        });
    }

}
