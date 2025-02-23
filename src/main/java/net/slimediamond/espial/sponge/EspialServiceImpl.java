package net.slimediamond.espial.sponge;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.api.EspialService;
import net.slimediamond.espial.api.action.Action;
import net.slimediamond.espial.api.query.Query;
import net.slimediamond.espial.api.query.QueryType;
import net.slimediamond.espial.api.query.Sort;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.submittable.SubmittableResult;
import net.slimediamond.espial.api.transaction.EspialTransaction;
import net.slimediamond.espial.api.transaction.TransactionStatus;
import net.slimediamond.espial.sponge.transaction.EspialTransactionImpl;
import net.slimediamond.espial.util.*;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.pagination.PaginationList;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class EspialServiceImpl implements EspialService {
    @Override
    public List<EspialRecord> query(Query query) throws Exception {
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

    @Override
    public Optional<User> getBlockOwner(int x, int y, int z) throws SQLException, ExecutionException, InterruptedException {
        return Espial.getInstance().getDatabase().getBlockOwner(x, y, z);
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
