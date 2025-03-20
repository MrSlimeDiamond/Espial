package net.slimediamond.espial.sponge.record;

import net.slimediamond.espial.Espial;
import net.slimediamond.espial.api.action.Action;
import net.slimediamond.espial.api.action.BlockAction;
import net.slimediamond.espial.api.action.event.EventType;
import net.slimediamond.espial.api.action.event.EventTypes;
import net.slimediamond.espial.api.nbt.NBTData;
import net.slimediamond.espial.api.query.Query;
import net.slimediamond.espial.api.record.BlockRecord;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.transaction.TransactionStatus;
import net.slimediamond.espial.util.BlockUtil;
import net.slimediamond.espial.util.SignUtil;
import net.slimediamond.espial.util.SpongeUtil;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;

import java.sql.Timestamp;
import java.util.List;

public class BlockRecordImpl extends BlockRecord {
    public BlockRecordImpl(int id, Timestamp timestamp, boolean rolledBack, Action action) {
        super(id, timestamp, rolledBack, action);
    }

    private TransactionStatus doRollback(BlockAction action) throws Exception {
        EventType eventType = action.getEventType();

        if (eventType.equals(EventTypes.BREAK)) {
            action.getServerLocation().setBlock(action.getState());
            doSignRollback(action);
            return TransactionStatus.SUCCESS;
        } else if (eventType.equals(EventTypes.PLACE)) {
            action.getServerLocation().setBlock(getRollbackBlockType(action).defaultState());
            return TransactionStatus.SUCCESS;
        } else if (eventType.equals(EventTypes.MODIFY)) {
            return rollbackModification(action);
        }

        return TransactionStatus.UNSUPPORTED;
    }

    private TransactionStatus doRestore(BlockAction action) throws Exception {
        EventType eventType = action.getEventType();

        if (eventType.equals(EventTypes.PLACE)) {
            action.getServerLocation().setBlock(action.getState());
            doSignRollback(action);
            return TransactionStatus.SUCCESS;
        } else if (eventType.equals(EventTypes.BREAK)) {
            action.getServerLocation().setBlock(BlockTypes.AIR.get().defaultState());
            return TransactionStatus.SUCCESS;
        } else if (eventType.equals(EventTypes.MODIFY)) {
            return restoreModification(action);
        }

        return TransactionStatus.UNSUPPORTED;
    }

    private TransactionStatus rollbackModification(BlockAction action) throws Exception {
        if (BlockUtil.SIGNS.contains(action.getBlockType())) {
            action.getServerLocation().setBlock(action.getState());
            List<EspialRecord> records = getRelevantRecords(action, false);
            if (records.size() >= 2) {
                SignUtil.setSignData((BlockAction) records.get(1).getAction());
            }
            return TransactionStatus.SUCCESS;
        }
        return TransactionStatus.UNSUPPORTED;
    }

    private TransactionStatus restoreModification(BlockAction action) throws Exception {
        if (BlockUtil.SIGNS.contains(action.getBlockType())) {
            action.getServerLocation().setBlock(action.getState());
            List<EspialRecord> records = getRelevantRecords(action, true);
            if (records.size() >= 2) {
                SignUtil.setSignData((BlockAction) records.get(1).getAction());
            }
            return TransactionStatus.SUCCESS;
        }
        return TransactionStatus.UNSUPPORTED;
    }

    private void doSignRollback(BlockAction action) throws Exception {
        if (BlockUtil.SIGNS.contains(action.getBlockType())) {
            SignUtil.setSignData(action);
        }
    }

    private BlockType getRollbackBlockType(BlockAction action) {
        return action
                .getNBT()
                .map(NBTData::getRollbackBlock)
                .map(key -> (BlockType) BlockTypes.registry().value(SpongeUtil.getResourceKey(key)))
                .orElse(BlockTypes.AIR.get());
    }

    private List<EspialRecord> getRelevantRecords(BlockAction action, boolean isRolledBack) throws Exception {
        return Espial.getInstance()
                .getEspialService()
                .query(Query.builder().min(action.getServerLocation()).build()).get()
                .stream()
                .filter(a -> a.isRolledBack() == isRolledBack)
                .toList();
    }

    @Override
    public TransactionStatus rollback() throws Exception {
        if (isRolledBack()) {
            return TransactionStatus.ALREADY_DONE;
        }

        BlockAction action = (BlockAction) getAction();
        TransactionStatus status = doRollback(action);
        if (status.equals(TransactionStatus.SUCCESS)) {
            Espial.getInstance().getDatabase().setRolledBack(getId(), true);
        }
        return status;
    }

    @Override
    public TransactionStatus restore() throws Exception {
        if (!isRolledBack()) {
            return TransactionStatus.ALREADY_DONE;
        }

        BlockAction action = (BlockAction) getAction();
        TransactionStatus status = doRestore(action);
        if (status.equals(TransactionStatus.SUCCESS)) {
            Espial.getInstance().getDatabase().setRolledBack(getId(), false);
        }
        return status;
    }
}
