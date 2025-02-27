package net.slimediamond.espial.api.event;

import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.user.EspialActor;

public class EspialPostInsertRecordEvent extends AbstractEvent {
  private EspialRecord record;

  public EspialPostInsertRecordEvent(EspialActor actor, EspialRecord record) {
    super(actor);

    this.record = record;
  }

  public EspialRecord getRecord() {
    return this.record;
  }
}
