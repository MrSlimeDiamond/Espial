package net.slimediamond.espial.sponge.queue;

import net.slimediamond.espial.api.event.InsertRecordEvent;
import net.slimediamond.espial.sponge.Espial;
import net.slimediamond.espial.sponge.event.SpongeInsertRecordEvent;
import net.slimediamond.espial.sponge.record.SpongeEspialRecord;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKeys;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SpongeRecordingQueue extends Thread {

    private volatile boolean running = true;
    private final BlockingQueue<SpongeEspialRecord> queue = new LinkedBlockingQueue<>();

    @Override
    public void run() {
        while (running) {
            try {
                final SpongeEspialRecord record = queue.take();
                final Cause cause = Cause.of(EventContext.builder()
                        .add(EventContextKeys.PLUGIN, Espial.getInstance().getContainer())
                        .build(), this, record);
                final InsertRecordEvent.Pre event = new SpongeInsertRecordEvent.PreImpl(record, cause);
                Sponge.eventManager().post(event);
                if (!event.isCancelled()) {
                    final int id = Espial.getInstance().getDatabase().submit(record);
                    record.setId(id);
                }
            } catch (final InterruptedException e) {
                // TODO: Send them all to the database immediately
                Thread.currentThread().interrupt();
                break;
            } catch (final Throwable t) {
                Espial.getInstance().getLogger().error("Unable to insert EspialRecord", t);
            }
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public BlockingQueue<SpongeEspialRecord> getQueue() {
        return queue;
    }

}
