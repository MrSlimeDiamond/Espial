package net.slimediamond.espial.sponge;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.api.EspialService;
import net.slimediamond.espial.api.action.Action;
import net.slimediamond.espial.api.action.BlockAction;
import net.slimediamond.espial.api.action.event.EventTypes;
import net.slimediamond.espial.api.query.Query;
import net.slimediamond.espial.api.query.QueryType;
import net.slimediamond.espial.api.query.Sort;
import net.slimediamond.espial.api.record.BlockRecord;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.submittable.SubmittableResult;
import net.slimediamond.espial.api.transaction.EspialTransaction;
import net.slimediamond.espial.api.transaction.TransactionStatus;
import net.slimediamond.espial.sponge.transaction.EspialTransactionImpl;
import net.slimediamond.espial.util.*;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.service.pagination.PaginationList;

import java.sql.SQLException;
import java.util.*;

public class EspialServiceImpl implements EspialService {
    @Override
    public List<EspialRecord> query(Query query) throws SQLException, JsonProcessingException {
        return Espial.getInstance().getDatabase().query(query);
    }

    @Override
    public SubmittableResult<List<EspialRecord>> submitQuery(Query query) throws Exception {
        List<EspialRecord> result = this.query(query);
        List<EspialRecord> actions = new ArrayList<>(result);
        if (query.getSort() == Sort.REVERSE_CHRONOLOGICAL) {
            actions.sort(Comparator.comparing(EspialRecord::getTimestamp).reversed());
        } else if (query.getSort() == Sort.CHRONOLOGICAL) {
            actions.sort(Comparator.comparing(EspialRecord::getTimestamp));
        }

        // TODO: Asynchronous processing, and probably some queue
        this.process(actions, query);

        return SubmittableResult.of(actions);
    }

    @Override
    public SubmittableResult<? extends EspialRecord> submitAction(Action action) throws Exception {
        Optional<EspialRecord> result =  Espial.getInstance().getDatabase().submit(action);
        return result.<SubmittableResult<? extends EspialRecord>>map(SubmittableResult::new).orElse(null);
    }

    public TransactionStatus rollbackBlock(BlockRecord record) throws Exception {
        if (record.isRolledBack()) return TransactionStatus.ALREADY_DONE;

        BlockAction action = (BlockAction) record.getAction();

        // roll back this specific ID to another state
        if (action.getEventType() == EventTypes.BREAK) {
            // place the block which was broken at that location

            action.getServerLocation().setBlock(action.getState());

            if (BlockUtil.SIGNS.contains(action.getBlockType())) {
                SignUtil.setSignData(action);
            }

            Espial.getInstance().getDatabase().setRolledBack(record.getId(), true);

            return TransactionStatus.SUCCESS;
        } if (action.getEventType() == EventTypes.PLACE) {
            // EDGE CASE: We're always going to rollback places to air. This probably will cause no harm
            // since one must remove a block first before placing a block. But this might cause issues somehow, not sure.
            // (it'll be fine, probably)

            action.getServerLocation().setBlock(BlockTypes.AIR.get().defaultState());
            Espial.getInstance().getDatabase().setRolledBack(record.getId(), true);
            return TransactionStatus.SUCCESS;
        } else if (action.getEventType() == EventTypes.MODIFY) {
            // Rolling back a modification action will entail going to its previous state of modification
            // (if it's present), so let's look for that.

            if (BlockUtil.SIGNS.contains(action.getBlockType())) {

                BlockState state = action.getState();
                action.getServerLocation().setBlock(state);

                List<EspialRecord> records = this.query(Query.builder()
                        .min(action.getServerLocation())
                        .build()).stream().filter(a -> !a.isRolledBack()).toList();
                if (records.size() >= 2) {
                    SignUtil.setSignData((BlockAction)records.get(1).getAction());
                }

                Espial.getInstance().getDatabase().setRolledBack(record.getId(), true);

                return TransactionStatus.SUCCESS;
            }
        }
        return TransactionStatus.UNSUPPORTED;
    }

    public TransactionStatus restoreBlock(BlockRecord record) throws Exception {
        if (!record.isRolledBack()) return TransactionStatus.ALREADY_DONE;
        BlockAction action = (BlockAction) record.getAction();

        // roll back this specific ID to another state
        if (action.getEventType() == EventTypes.PLACE) {
            // place the block which was broken at that location

            action.getServerLocation().setBlock(action.getState());

            if (BlockUtil.SIGNS.contains(action.getBlockType())) {
                SignUtil.setSignData(action);
            }

            Espial.getInstance().getDatabase().setRolledBack(record.getId(), false);

            return TransactionStatus.SUCCESS;
        } if (action.getEventType() == EventTypes.BREAK) {
            // EDGE CASE: We're always going to rollback places to air. This probably will cause no harm
            // since one must remove a block first before placing a block. But this might cause issues somehow, not sure.
            // (it'll be fine, probably)

            action.getServerLocation().setBlock(BlockTypes.AIR.get().defaultState());
            Espial.getInstance().getDatabase().setRolledBack(record.getId(), false);
            return TransactionStatus.SUCCESS;
        } else if (action.getEventType() == EventTypes.MODIFY) {
            // Rolling back a modification action will entail going to its previous state of modification
            // (if it's present), so let's look for that.

            if (BlockUtil.SIGNS.contains(action.getBlockType())) {

                BlockState state = action.getState();
                action.getServerLocation().setBlock(state);

                List<EspialRecord> actions = this.query(Query.builder().min(action.getServerLocation()).build()).stream().filter(a -> a.isRolledBack()).toList();

                if (actions.size() >= 2) {
                    SignUtil.setSignData((BlockAction)actions.get(1).getAction());
                }

                Espial.getInstance().getDatabase().setRolledBack(record.getId(), false);

                return TransactionStatus.SUCCESS;
            }

        }
        return TransactionStatus.UNSUPPORTED;
    }


    private void process(List<EspialRecord> records, Query query) throws Exception {
        if (query.getType() == QueryType.ROLLBACK || query.getType() == QueryType.RESTORE) {
            String msg = "processed";

            switch (query.getType()) {
                case ROLLBACK -> msg = "rolled back";
                case RESTORE -> msg = "restored";
            }

            List<Integer> success = new ArrayList<>();
            int skipped = 0;

            for (EspialRecord record : records) {
                TransactionStatus status;
                switch (query.getType()) {
                    case ROLLBACK -> status = record.rollback();
                    case RESTORE -> status = record.restore();
                    default -> status = TransactionStatus.UNSUPPORTED;
                }

                if (status == TransactionStatus.SUCCESS) {
                    success.add(record.getId());
                } else {
                    skipped++;
                }
            }

            TextComponent.Builder builder = Component.text();

            if (!success.isEmpty()) {
                builder.append(Component.text(success.size()))
                       .append(Component.text(" action(s) were "))
                       .append(Component.text(msg)).color(NamedTextColor.WHITE);

                // Commit a transaction but only if we will use it later
                // for example: lookups won't be added here, but rollbacks will
                if (query.getType().isReversible()) {
                    EspialTransaction transaction = new EspialTransactionImpl(success, query);
                    Espial.getInstance().getTransactionManager().add(transaction.getUser(), transaction);
                }

            } else {
                builder.append(Component.text("Nothing was " + msg).color(NamedTextColor.WHITE));
            }

            if (skipped != 0) {
                builder.append(Component.text(", with " + skipped + " skipped").color(NamedTextColor.WHITE));
            }

            builder.append(Component.text(".").color(NamedTextColor.WHITE));

            query.getAudience().sendMessage(Espial.prefix.append(builder.build()));
        } else if (query.getType() == QueryType.LOOKUP) {
            List<Component> contents = MessageUtil.generateLookupContents(records, query.isSpread());

            if (contents.isEmpty()) {
                query.getAudience().sendMessage(Espial.prefix.append(Component.text("No data was found.").color(NamedTextColor.RED)));
                return;
            }

            PaginationList.builder().title(Espial.prefix.append(Component.text("Lookup results").color(NamedTextColor.WHITE)))
                    .contents(contents)
                    .sendTo(query.getAudience());
        } else {
            // Some other query type that we don't currently support
            query.getAudience().sendMessage(Espial.prefix.append(Component.text("This query type is not currently supported.").color(NamedTextColor.RED)));
        }
    }
}
