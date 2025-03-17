package net.slimediamond.espial.sponge.record;

import net.slimediamond.espial.Espial;
import net.slimediamond.espial.api.action.Action;
import net.slimediamond.espial.api.action.HangingDeathAction;
import net.slimediamond.espial.api.record.EntityRecord;
import net.slimediamond.espial.api.transaction.TransactionStatus;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.Entity;

import java.sql.Timestamp;

public class EntityRecordImpl extends EntityRecord {
    public EntityRecordImpl(int id, Timestamp timestamp, boolean rolledBack,
                            Action action) {
        super(id, timestamp, rolledBack, action);
    }

    @Override
    public TransactionStatus rollback() throws Exception {
        if (isRolledBack()) {
            return TransactionStatus.ALREADY_DONE;
        }
        if (this.getAction() instanceof HangingDeathAction deathAction) {
            try {
                Entity entity = deathAction
                        .getServerLocation()
                        .world()
                        .createEntity(
                                deathAction.getEntityType(), deathAction.getServerLocation().position());

                entity.offer(Keys.DIRECTION, deathAction.getNBT().get().getDirection());

                deathAction.getServerLocation().spawnEntity(entity);

                Espial.getInstance().getDatabase().setRolledBack(getId(), true);

                return TransactionStatus.SUCCESS;
            } catch (IllegalArgumentException e) {
                return TransactionStatus.FAILURE; // skip
            }
        }

        return TransactionStatus.UNSUPPORTED;
    }

    @Override
    public TransactionStatus restore() throws Exception {
        if (!isRolledBack()) {
            return TransactionStatus.ALREADY_DONE;
        }

        // TODO
        //        if (this.action instanceof HangingDeathAction deathAction) {
        //
        // deathAction.getServerLocation().setBlock(BlockState.builder().blockType(BlockTypes.AIR).build());
        //
        //            Espial.getInstance().getDatabase().setRolledBack(getId(), false);
        //
        //            return TransactionStatus.SUCCESS;
        //        }

        return TransactionStatus.UNSUPPORTED;
    }
}
