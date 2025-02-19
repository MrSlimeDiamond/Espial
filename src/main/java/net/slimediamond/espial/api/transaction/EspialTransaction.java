package net.slimediamond.espial.api.transaction;

import net.slimediamond.espial.Espial;

import java.sql.SQLException;
import java.util.ArrayList;

public class EspialTransaction {
    private ArrayList<Integer> ids;
    private EspialTransactionType type;
    private boolean undone;

    public EspialTransaction(ArrayList<Integer> ids, EspialTransactionType type, boolean undone) {
        this.ids = ids;
        this.type = type;
        this.undone = undone;
    }

    public ArrayList<Integer> getIds() {
        return ids;
    }

    public EspialTransactionType getType() {
        return type;
    }

    public boolean isUndone() {
        return undone;
    }

    public void undo() throws Exception {
        if (type == EspialTransactionType.ROLLBACK) {
            // Restore all IDs
            for (int id : ids) {
                Espial.getInstance().getEspialService().restore(Espial.getInstance().getDatabase().queryId(id));
            }
        } else if (type == EspialTransactionType.RESTORE) {
            for (int id : ids) {
                Espial.getInstance().getEspialService().rollback(Espial.getInstance().getDatabase().queryId(id));
            }
        }

        this.undone = true;
    }

    public void redo() throws Exception {
        if (type == EspialTransactionType.RESTORE) {
            // Restore all IDs
            for (int id : ids) {
                Espial.getInstance().getEspialService().restore(Espial.getInstance().getDatabase().queryId(id));
            }
        } else if (type == EspialTransactionType.ROLLBACK) {
            for (int id : ids) {
                Espial.getInstance().getEspialService().rollback(Espial.getInstance().getDatabase().queryId(id));
            }
        }

        this.undone = false;
    }
}
