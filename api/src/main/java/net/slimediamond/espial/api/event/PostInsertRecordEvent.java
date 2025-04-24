package net.slimediamond.espial.api.event;

import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.user.EspialActor;
import org.spongepowered.api.event.Cause;

public class PostInsertRecordEvent extends EspialEvent {
    private final EspialRecord record;

    public PostInsertRecordEvent(EspialActor actor, Cause cause, EspialRecord record) {
        super(actor, cause);
        this.record = record;
    }

    public EspialRecord getRecord() {
        return record;
    }
}
