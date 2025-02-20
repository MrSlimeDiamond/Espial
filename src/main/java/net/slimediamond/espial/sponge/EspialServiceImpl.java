package net.slimediamond.espial.sponge;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.slimediamond.espial.CommandParameters;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.api.EspialService;
import net.slimediamond.espial.api.action.ActionType;
import net.slimediamond.espial.api.action.BlockAction;
import net.slimediamond.espial.api.nbt.NBTDataParser;
import net.slimediamond.espial.api.query.Query;
import net.slimediamond.espial.api.query.QueryType;
import net.slimediamond.espial.api.transaction.EspialTransaction;
import net.slimediamond.espial.api.transaction.TransactionStatus;
import net.slimediamond.espial.sponge.transaction.EspialTransactionImpl;
import net.slimediamond.espial.util.*;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.server.ServerLocation;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class EspialServiceImpl implements EspialService {
    @Override
    public void setSignData(BlockAction action) {
        action.getServerLocation().blockEntity().ifPresent(tileEntity -> {
            action.getNBT().ifPresent(nbtData -> {
                if (nbtData.getSignData() != null) {
                    List<Component> components = new ArrayList<>();

                    nbtData.getSignData().getFrontText().forEach(line -> components.add(GsonComponentSerializer.gson().deserialize(line)));

                    tileEntity.offer(Keys.SIGN_LINES, components);
                }
            });
        });
    }

    @Override
    public List<BlockAction> query(Query query) throws SQLException {
        return Espial.getInstance().getDatabase().query(query);
    }

    public TransactionStatus rollback(BlockAction action) throws SQLException {
        if (action.isRolledBack()) return TransactionStatus.ALREADY_DONE;

        // roll back this specific ID to another state
        if (action.getActionType() == ActionType.BREAK) {
            // place the block which was broken at that location

            action.getServerLocation().setBlock(action.getState());

            if (BlockUtil.SIGNS.contains(action.getBlockType())) {
                setSignData(action);
            }

            Espial.getInstance().getDatabase().setRolledBack(action.getId(), true);

            return TransactionStatus.SUCCESS;
        } if (action.getActionType() == ActionType.PLACE) {
            // EDGE CASE: We're always going to rollback places to air. This probably will cause no harm
            // since one must remove a block first before placing a block. But this might cause issues somehow, not sure.
            // (it'll be fine, probably)

            action.getServerLocation().setBlock(BlockTypes.AIR.get().defaultState());
            Espial.getInstance().getDatabase().setRolledBack(action.getId(), true);
            return TransactionStatus.SUCCESS;
        } else if (action.getActionType() == ActionType.MODIFY) {
            // Rolling back a modification action will entail going to its previous state of modification
            // (if it's present), so let's look for that.

            if (BlockUtil.SIGNS.contains(action.getBlockType())) {

                BlockState state = action.getState();
                action.getServerLocation().setBlock(state);

                List<BlockAction> actions = this.query(Query.builder()
                        .setMin(action.getServerLocation())
                        .build()).stream().filter(a -> !a.isRolledBack()).toList();
                if (actions.size() >= 2) {
                    setSignData(actions.get(1));
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
        if (action.getActionType() == ActionType.BREAK) {
            // place the block which was broken at that location

            action.getServerLocation().setBlock(BlockTypes.AIR.get().defaultState());

            Espial.getInstance().getDatabase().setRolledBack(action.getId(), false);

            return TransactionStatus.SUCCESS;
        } if (action.getActionType() == ActionType.PLACE) {
            action.getServerLocation().setBlock(action.getState());

            if (BlockUtil.SIGNS.contains(action.getBlockType())) {
                setSignData(action);
            }

            Espial.getInstance().getDatabase().setRolledBack(action.getId(), false);
            return TransactionStatus.SUCCESS;
        } if (action.getActionType() == ActionType.MODIFY) {
            // Because this is a restore, let's get the one after this which is rolled back

            if (BlockUtil.SIGNS.contains(action.getBlockType())) {

                BlockState state = action.getState();
                action.getServerLocation().setBlock(state);

                List<BlockAction> actions = this.query(Query.builder().setMin(action.getServerLocation()).build()).stream().filter(a -> a.isRolledBack()).toList();

                if (actions.size() >= 2) {
                    setSignData(actions.get(1));
                }

                Espial.getInstance().getDatabase().setRolledBack(action.getId(), false);

                return TransactionStatus.SUCCESS;
            }

        }

        return TransactionStatus.UNSUPPORTED;
    }

    @Override
    public void submit(Query query) throws Exception {
        List<BlockAction> actions = this.query(query);
        List<Integer> ids = actions.stream().map(BlockAction::getId).toList();
        EspialTransaction transaction = new EspialTransactionImpl(ids, query);

        // Commit a transaction but only if we will use it later
        // for example: lookups won't be added here, but rollbacks will
        if (query.getType().isReversible()) {
            Espial.getInstance().getTransactionManager().add(transaction.getUser(), transaction);
        }

        // TODO: Asynchronous processing, and probably some queue
        this.process(actions, query.getType(), query.getAudience(), query.isSpread());
    }

    private void process(List<BlockAction> actions, QueryType type, Audience audience, boolean spread) throws Exception {
        if (type == QueryType.ROLLBACK || type == QueryType.RESTORE) {
            String msg = "processed";

            switch (type) {
                case ROLLBACK -> msg = "rolled back";
                case RESTORE -> msg = "restored";
            }

            List<Integer> success = new ArrayList<>();
            int skipped = 0;

            for (BlockAction action : actions) {
                TransactionStatus status;
                switch (type) {
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
            } else {
                builder.append(Component.text("Nothing was " + msg).color(NamedTextColor.WHITE));
            }

            if (skipped != 0) {
                builder.append(Component.text(", with " + skipped + " skipped").color(NamedTextColor.WHITE));
            }

            builder.append(Component.text(".").color(NamedTextColor.WHITE));

            audience.sendMessage(Espial.prefix.append(builder.build()));
        } else if (type == QueryType.LOOKUP) {
            List<Component> contents = MessageUtil.generateLookupContents(actions, spread);

            if (contents.isEmpty()) {
                audience.sendMessage(Espial.prefix.append(Component.text("No data was found.").color(NamedTextColor.RED)));
                return;
            }

            PaginationList.builder().title(Espial.prefix.append(Component.text("Lookup results").color(NamedTextColor.WHITE)))
                    .contents(contents)
                    .sendTo(audience);
        } else {
            // Some other query type that we don't currently support
            audience.sendMessage(Espial.prefix.append(Component.text("This query type is not currently supported.").color(NamedTextColor.RED)));
        }
    }
}
