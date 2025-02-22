package net.slimediamond.espial.sponge;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.api.EspialService;
import net.slimediamond.espial.api.action.BlockAction;
import net.slimediamond.espial.api.action.type.ActionTypes;
import net.slimediamond.espial.api.query.Query;
import net.slimediamond.espial.api.query.QueryType;
import net.slimediamond.espial.api.query.Sort;
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
    public List<BlockAction> query(Query query) throws SQLException {
        return Espial.getInstance().getDatabase().query(query);
    }

    public TransactionStatus rollback(BlockAction action) throws SQLException {
        if (action.isRolledBack()) return TransactionStatus.ALREADY_DONE;

        // roll back this specific ID to another state
        if (action.getType() == ActionTypes.BREAK) {
            // place the block which was broken at that location

            action.getServerLocation().setBlock(action.getState());

            if (BlockUtil.SIGNS.contains(action.getBlockType())) {
                SignUtil.setSignData(action);
            }

            Espial.getInstance().getDatabase().setRolledBack(action.getId(), true);

            return TransactionStatus.SUCCESS;
        } if (action.getType() == ActionTypes.PLACE) {
            // EDGE CASE: We're always going to rollback places to air. This probably will cause no harm
            // since one must remove a block first before placing a block. But this might cause issues somehow, not sure.
            // (it'll be fine, probably)

            action.getServerLocation().setBlock(BlockTypes.AIR.get().defaultState());
            Espial.getInstance().getDatabase().setRolledBack(action.getId(), true);
            return TransactionStatus.SUCCESS;
        } else if (action.getType() == ActionTypes.MODIFY) {
            // Rolling back a modification action will entail going to its previous state of modification
            // (if it's present), so let's look for that.

            if (BlockUtil.SIGNS.contains(action.getBlockType())) {

                BlockState state = action.getState();
                action.getServerLocation().setBlock(state);

                List<BlockAction> actions = this.query(Query.builder()
                        .setMin(action.getServerLocation())
                        .build()).stream().filter(a -> !a.isRolledBack()).toList();
                if (actions.size() >= 2) {
                    SignUtil.setSignData(actions.get(1));
                }

                Espial.getInstance().getDatabase().setRolledBack(action.getId(), true);

                return TransactionStatus.SUCCESS;
            }
        }
        return TransactionStatus.UNSUPPORTED;
    }

    public TransactionStatus restore(BlockAction action) throws SQLException {
        if (!action.isRolledBack()) return TransactionStatus.ALREADY_DONE;

        // roll forwards this specific ID to another state
        if (action.getType() == ActionTypes.BREAK) {
            // place the block which was broken at that location

            action.getServerLocation().setBlock(BlockTypes.AIR.get().defaultState());

            Espial.getInstance().getDatabase().setRolledBack(action.getId(), false);

            return TransactionStatus.SUCCESS;
        } if (action.getType() == ActionTypes.PLACE) {
            action.getServerLocation().setBlock(action.getState());

            if (BlockUtil.SIGNS.contains(action.getBlockType())) {
                SignUtil.setSignData(action);
            }

            Espial.getInstance().getDatabase().setRolledBack(action.getId(), false);
            return TransactionStatus.SUCCESS;
        } if (action.getType() == ActionTypes.MODIFY) {
            // Because this is a restore, let's get the one after this which is rolled back

            if (BlockUtil.SIGNS.contains(action.getBlockType())) {

                BlockState state = action.getState();
                action.getServerLocation().setBlock(state);

                List<BlockAction> actions = this.query(Query.builder().setMin(action.getServerLocation()).build()).stream().filter(a -> a.isRolledBack()).toList();

                if (actions.size() >= 2) {
                    SignUtil.setSignData(actions.get(1));
                }

                Espial.getInstance().getDatabase().setRolledBack(action.getId(), false);

                return TransactionStatus.SUCCESS;
            }

        }

        return TransactionStatus.UNSUPPORTED;
    }

    @Override
    public void submit(Query query) throws Exception {
        List<BlockAction> result = this.query(query);
        List<BlockAction> actions = new ArrayList<>(result);
        if (query.getSort() == Sort.REVERSE_CHRONOLOGICAL) {
            actions.sort(Comparator.comparing(BlockAction::getTimestamp).reversed());
        } else if (query.getSort() == Sort.CHRONOLOGICAL) {
            actions.sort(Comparator.comparing(BlockAction::getTimestamp));
        }

        // TODO: Asynchronous processing, and probably some queue
        this.process(actions, query);
    }

    private void process(List<BlockAction> actions, Query query) throws Exception {
        if (query.getType() == QueryType.ROLLBACK || query.getType() == QueryType.RESTORE) {
            String msg = "processed";

            switch (query.getType()) {
                case ROLLBACK -> msg = "rolled back";
                case RESTORE -> msg = "restored";
            }

            List<Integer> success = new ArrayList<>();
            int skipped = 0;

            for (BlockAction action : actions) {
                TransactionStatus status;
                switch (query.getType()) {
                    case ROLLBACK -> status = this.rollback(action);
                    case RESTORE -> status = this.restore(action);
                    default -> status = TransactionStatus.UNSUPPORTED;
                }

                if (status == TransactionStatus.SUCCESS) {
                    success.add(action.getId());
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
            List<Component> contents = MessageUtil.generateLookupContents(actions, query.isSpread());

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
