package net.slimediamond.espial.sponge.transaction;

import net.kyori.adventure.audience.Audience;
import net.slimediamond.espial.api.query.QueryType;
import net.slimediamond.espial.api.transaction.EspialTransaction;

import java.util.List;

public class BasicEspialTransaction implements EspialTransaction {
  private final QueryType type;
  private final Object user;
  private final Audience audience;
  private final List<Integer> ids;
  private boolean undone;

  public BasicEspialTransaction(QueryType type, Object user, Audience audience, List<Integer> ids) {
    this.type = type;
    this.user = user;
    this.audience = audience;
    this.ids = ids;
  }

  @Override
  public QueryType getType() {
    return type;
  }

  @Override
  public Object getUser() {
    return user;
  }

  @Override
  public Audience getAudience() {
    return audience;
  }

  @Override
  public List<Integer> getAffectedIds() {
    return ids;
  }

  @Override
  public boolean isUndone() {
    return undone;
  }

  @Override
  public int undo() throws Exception {
    this.undone = true;
    return EspialTransactionImpl.undo(ids, type);
  }

  @Override
  public int redo() throws Exception {
    this.undone = false;
    return EspialTransactionImpl.redo(ids, type);
  }
}
