package net.slimediamond.espial.sponge;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.api.EspialService;
import net.slimediamond.espial.api.action.Action;
import net.slimediamond.espial.api.event.EventManager;
import net.slimediamond.espial.api.query.Query;
import net.slimediamond.espial.api.query.QueryType;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.submittable.SubmittableResult;
import net.slimediamond.espial.api.transaction.EspialTransaction;
import net.slimediamond.espial.api.transaction.TransactionManager;
import net.slimediamond.espial.api.transaction.TransactionStatus;
import net.slimediamond.espial.sponge.event.EventManagerImpl;
import net.slimediamond.espial.api.transaction.EspialTransactionImpl;
import net.slimediamond.espial.sponge.transaction.TransactionManagerImpl;
import net.slimediamond.espial.util.Format;
import net.slimediamond.espial.util.SpongeUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class EspialServiceImpl implements EspialService {
  private TransactionManager transactionManager;
  private EventManager eventManager;

  public EspialServiceImpl() {
    this.transactionManager = new TransactionManagerImpl();
    this.eventManager = new EventManagerImpl();
  }

  @Override
  public TransactionManager getTransactionManager() {
    return transactionManager;
  }

  @Override
  public EventManager getEventManager() {
    return eventManager;
  }

  @Override
  public List<EspialRecord> query(Query query) throws Exception {
    return Espial.getInstance().getDatabase().query(query);
  }

  @Override
  public Optional<EspialRecord> getRecordById(int id) throws Exception {
    return Optional.ofNullable(Espial.getInstance().getDatabase().queryId(id));
  }

  @Override
  public SubmittableResult<List<EspialRecord>> submitQuery(Query query) throws Exception {
    List<EspialRecord> actions = this.query(query);

    StringBuilder argsPreview = new StringBuilder();

    if (query.getPlayerUUIDs() != null && !query.getPlayerUUIDs().isEmpty()) {
      List<String> players = new ArrayList<>();
      for (UUID uuid : query.getPlayerUUIDs()) {
        players.add(Sponge.server().userManager().load(uuid).get().get().name());
      }
      argsPreview
          .append(" Players: ")
          .append("[" + String.join(", ", players) + "]");
    }

    if (query.getBlockIds() != null && !query.getBlockIds().isEmpty()) {
      List<String> blocks = new ArrayList<>();
      for (String block : query.getBlockIds()) {
        blocks.add(block.split(":")[1]);
      }
      argsPreview
              .append(" Blocks: ")
              .append("[" + String.join(", ", blocks) + "]");
    }

    if (!Objects.equals(query.getTimestamp(), Timestamp.from(Instant.ofEpochMilli(0)))
        && query.getTimestamp() != null) {
      if (!argsPreview.isEmpty()) argsPreview.append(" ");
      argsPreview.append(" After: ").append(Format.date(query.getTimestamp()));
    }

    // TODO: Asynchronous processing, and probably some queue
    this.process(actions, query, argsPreview.toString());

    return SubmittableResult.of(actions);
  }

  @Override
  public SubmittableResult<? extends EspialRecord> submitAction(Action action) throws Exception {
    Optional<EspialRecord> result = Espial.getInstance().getDatabase().submit(action);
    return result
        .<SubmittableResult<? extends EspialRecord>>map(SubmittableResult::new)
        .orElse(null);
  }

  @Override
  public Optional<User> getBlockOwner(String world, int x, int y, int z)
      throws SQLException, ExecutionException, InterruptedException {
    Optional<User> userOptional = Espial.getInstance().getDatabase().getBlockOwner(world, x, y, z);

    if (userOptional.isEmpty()) {
      ServerWorld serverWorld = SpongeUtil.getWorld(world).orElseThrow(() ->
              new RuntimeException("getBlockOwner supplied a non-existent " +
                      "world."));
      return ServerLocation.of(serverWorld, x, y, z).createSnapshot().creator().flatMap(uuid -> {
          try {
              return Sponge.server().userManager().load(uuid).get();
          } catch (InterruptedException | ExecutionException e) {
              throw new RuntimeException(e);
          }
      });
    } else {
      return userOptional;
    }
  }

  private void process(List<EspialRecord> records, Query query, String argsPreview)
      throws Exception {
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
        builder
            .append(Component.text(success.size()))
            .append(Component.text(" action(s) were "))
            .append(Component.text(msg))
            .color(NamedTextColor.WHITE);

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
        builder.append(
            Component.text(", with " + skipped + " skipped").color(NamedTextColor.WHITE));
      }

      builder.append(Component.text(".").color(NamedTextColor.WHITE));

      query.getAudience().sendMessage(Format.component(builder.build()));
    } else if (query.getType() == QueryType.LOOKUP) {
      List<Component> contents = Format.generateLookupContents(records, query.isSpread());

      if (contents.isEmpty()) {
        query.getAudience().sendMessage(Format.error("No data was found."));

        return;
      }

      PaginationList.Builder builder =
          PaginationList.builder().title(Format.title("Lookup results")).padding(Format.PADDING);

      if (!argsPreview.isEmpty()) {
        builder.header(
            Format.truncate(
                Component.text("Parameters:")
                    .color(Format.HINT_COLOR)
                    .append(Component.text(argsPreview).color(NamedTextColor.GRAY))));
      }
      builder.contents(contents).sendTo(query.getAudience());

    } else {
      // Some other query type that we don't currently support
      query.getAudience().sendMessage(Format.error("This query type is not supported."));
    }
  }
}
