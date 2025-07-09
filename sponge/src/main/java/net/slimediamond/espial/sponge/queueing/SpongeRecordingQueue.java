package net.slimediamond.espial.sponge.queueing;

import net.slimediamond.espial.sponge.Espial;
import net.slimediamond.espial.sponge.record.SpongeBlockRecord;
import net.slimediamond.espial.sponge.record.SpongeEspialRecord;

import java.sql.SQLException;
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
                if (record instanceof SpongeBlockRecord blockRecord) {
                    final int id = Espial.getInstance().getDatabase().submit(blockRecord);
                    blockRecord.setId(id);
                }
                // we should have handled invalid records via EspialService, so no need
                // to do anything at this stage
            } catch (final InterruptedException e) {
                // TODO: Send them all to the database immediately
                Thread.currentThread().interrupt();
                break;
            } catch (final SQLException e) {
                Espial.getInstance().getLogger().error("Unable to insert EspialRecord", e);
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
