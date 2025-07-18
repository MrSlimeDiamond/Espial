package net.slimediamond.espial.api.event;

import net.slimediamond.espial.api.record.EspialRecord;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;

public interface InsertRecordEvent extends Event {

    /**
     * Get the affected record
     *
     * @return The record
     */
    EspialRecord getRecord();

    interface Pre extends InsertRecordEvent, Cancellable {

    }

    interface Post extends InsertRecordEvent {

    }

}
