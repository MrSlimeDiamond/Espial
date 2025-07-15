package net.slimediamond.espial.sponge.queue;

import net.slimediamond.espial.sponge.Espial;
import net.slimediamond.espial.sponge.record.SpongeEspialRecord;

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
                final int id = Espial.getInstance().getDatabase().submit(record);
                record.setId(id);
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
