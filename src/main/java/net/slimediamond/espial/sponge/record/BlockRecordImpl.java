package net.slimediamond.espial.sponge.record;

import net.slimediamond.espial.Espial;
import net.slimediamond.espial.api.action.Action;
import net.slimediamond.espial.api.action.BlockAction;
import net.slimediamond.espial.api.action.event.EventTypes;
import net.slimediamond.espial.api.nbt.NBTData;
import net.slimediamond.espial.api.query.Query;
import net.slimediamond.espial.api.record.BlockRecord;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.transaction.TransactionStatus;
import net.slimediamond.espial.util.BlockUtil;
import net.slimediamond.espial.util.SignUtil;
import net.slimediamond.espial.util.SpongeUtil;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public class BlockRecordImpl extends BlockRecord {
    public BlockRecordImpl(
            int id,
            Timestamp timestamp,
            boolean rolledBack,
            Action action
    ) {
        super(id, timestamp, rolledBack, action);
    }

    @Override
    public TransactionStatus rollback() throws Exception {
        if (isRolledBack()) {
            return TransactionStatus.ALREADY_DONE;
        }

        BlockAction action = (BlockAction) getAction();

        // roll back this specific ID to another state
        if (action.getEventType() == EventTypes.BREAK) {
            // place the block which was broken at that location

            action.getServerLocation().setBlock(action.getState());

            if (BlockUtil.SIGNS.contains(action.getBlockType())) {
                SignUtil.setSignData(action);
            }

            Espial.getInstance().getDatabase().setRolledBack(getId(), true);

            return TransactionStatus.SUCCESS;
        }
        if (action.getEventType() == EventTypes.PLACE) {
            BlockType blockType = BlockTypes.AIR.get();

            Optional<NBTData> nbtDataOptional = action.getNBT();
            if (nbtDataOptional.isPresent()) {
                if (nbtDataOptional.get().getRollbackBlock() != null) {
                    blockType = BlockTypes.registry().value(SpongeUtil.getResourceKey(nbtDataOptional.get().getRollbackBlock()));
                }
            }

            action.getServerLocation().setBlock(blockType.defaultState());
            Espial.getInstance().getDatabase().setRolledBack(getId(), true);
            return TransactionStatus.SUCCESS;
        } else if (action.getEventType() == EventTypes.MODIFY) {
            // Rolling back a modification action will entail going to its previous state of modification
            // (if it's present), so let's look for that.

            if (BlockUtil.SIGNS.contains(action.getBlockType())) {

                BlockState state = action.getState();
                action.getServerLocation().setBlock(state);

                List<EspialRecord> records =
                        Espial.getInstance()
                                .getEspialService()
                                .query(Query.builder().min(action.getServerLocation()).build())
                                .stream()
                                .filter(a -> !a.isRolledBack())
                                .toList();
                if (records.size() >= 2) {
                    SignUtil.setSignData((BlockAction) records.get(1).getAction());
                }

                Espial.getInstance().getDatabase().setRolledBack(getId(), true);

                return TransactionStatus.SUCCESS;
            }
        }
        return TransactionStatus.UNSUPPORTED;
    }

    @Override
    public TransactionStatus restore() throws Exception {
        if (!isRolledBack()) {
            return TransactionStatus.ALREADY_DONE;
        }
        BlockAction action = (BlockAction) getAction();

        // roll back this specific ID to another state
        if (action.getEventType() == EventTypes.PLACE) {
            // place the block which was broken at that location

            action.getServerLocation().setBlock(action.getState());

            if (BlockUtil.SIGNS.contains(action.getBlockType())) {
                SignUtil.setSignData(action);
            }

            Espial.getInstance().getDatabase().setRolledBack(getId(), false);

            return TransactionStatus.SUCCESS;
        }
        if (action.getEventType() == EventTypes.BREAK) {
            // EDGE CASE: We're always going to rollback places to air. This probably will cause no harm
            // since one must remove a block first before placing a block. But this might cause issues
            // somehow, not sure.
            // (it'll be fine, probably)

            action.getServerLocation().setBlock(BlockTypes.AIR.get().defaultState());
            Espial.getInstance().getDatabase().setRolledBack(getId(), false);
            return TransactionStatus.SUCCESS;
        } else if (action.getEventType() == EventTypes.MODIFY) {
            // Rolling back a modification action will entail going to its previous state of modification
            // (if it's present), so let's look for that.

            if (BlockUtil.SIGNS.contains(action.getBlockType())) {

                BlockState state = action.getState();
                action.getServerLocation().setBlock(state);

                List<EspialRecord> actions =
                        Espial.getInstance()
                                .getEspialService()
                                .query(Query.builder().min(action.getServerLocation()).build())
                                .stream()
                                .filter(a -> a.isRolledBack())
                                .toList();

                if (actions.size() >= 2) {
                    SignUtil.setSignData((BlockAction) actions.get(1).getAction());
                }

                Espial.getInstance().getDatabase().setRolledBack(getId(), false);

                return TransactionStatus.SUCCESS;
            }
        }
        return TransactionStatus.UNSUPPORTED;
    }
}
