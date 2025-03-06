package net.slimediamond.espial.api.transaction;

import net.kyori.adventure.audience.Audience;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.api.query.Query;
import net.slimediamond.espial.api.query.QueryType;
import net.slimediamond.espial.api.record.BlockRecord;
import net.slimediamond.espial.api.record.EspialRecord;

import java.util.List;

public class EspialTransactionImpl implements EspialTransaction {
  private final List<Integer> ids;
  private final QueryType type;
  private final Query query;
  private final Object user;
  private final Audience audience;
  private boolean undone;

  public EspialTransactionImpl(List<Integer> ids, Query query) {
    this.ids = ids;
    this.type = query.getType();
    this.query = query;
    this.user = query.getUser();
    this.audience = query.getAudience();
    this.undone = false;
  }

  public static int undo(List<Integer> ids, QueryType type) throws Exception {
    if (type == QueryType.ROLLBACK) {
      // Restore all IDs
      for (int id : ids) {
        EspialRecord record = Espial.getInstance().getDatabase().queryId(id);
        if (record instanceof BlockRecord) {
          record.restore();
        }
      }
    } else if (type == QueryType.RESTORE) {
      for (int id : ids) {
        EspialRecord record = Espial.getInstance().getDatabase().queryId(id);
        if (record instanceof BlockRecord) {
          record.rollback();
        }
      }
    }

    return ids.size();
  }

  public static int redo(List<Integer> ids, QueryType type) throws Exception {
    if (type == QueryType.ROLLBACK) {
      // Rollback all IDs
      for (int id : ids) {
        EspialRecord record = Espial.getInstance().getDatabase().queryId(id);
        if (record instanceof BlockRecord) {
          record.rollback();
        }
      }
    } else if (type == QueryType.RESTORE) {
      for (int id : ids) {
        EspialRecord record = Espial.getInstance().getDatabase().queryId(id);
        if (record instanceof BlockRecord) {
          record.restore();
        }
      }
    }

    return ids.size();
  }

  @Override
  public List<Integer> getAffectedIds() {
    return this.ids;
  }

  @Override
  public QueryType getType() {
    return this.type;
  }

  @Override
  public Object getUser() {
    return this.user;
  }

  @Override
  public Audience getAudience() {
    return audience;
  }

  @Override
  public boolean isUndone() {
    return this.undone;
  }

  @Override
  public int undo() throws Exception {
    this.undone = true;

    return undo(ids, type);
  }

  @Override
  public int redo() throws Exception {
    this.undone = false;

    return redo(ids, type);
  }
}
