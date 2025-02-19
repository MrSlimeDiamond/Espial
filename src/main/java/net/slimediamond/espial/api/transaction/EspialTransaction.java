package net.slimediamond.espial.api.transaction;

import net.slimediamond.espial.Espial;
import net.slimediamond.espial.api.query.QueryType;

import java.util.List;

public class EspialTransaction {
    private List<Integer> ids;
    private QueryType type;
    private boolean undone;

    public EspialTransaction(List<Integer> ids, QueryType type, boolean undone) {
        this.ids = ids;
        this.type = type;
        this.undone = undone;
    }

    public List<Integer> getIds() {
        return ids;
    }

    public QueryType getType() {
        return type;
    }

    public boolean isUndone() {
        return undone;
    }

    public void undo() throws Exception {
        if (type == QueryType.ROLLBACK) {
            // Restore all IDs
            for (int id : ids) {
                Espial.getInstance().getEspialService().restore(Espial.getInstance().getDatabase().queryId(id));
            }
        } else if (type == QueryType.RESTORE) {
            for (int id : ids) {
                Espial.getInstance().getEspialService().rollback(Espial.getInstance().getDatabase().queryId(id));
            }
        }

        this.undone = true;
    }

    public void redo() throws Exception {
        if (type == QueryType.RESTORE) {
            // Restore all IDs
            for (int id : ids) {
                Espial.getInstance().getEspialService().restore(Espial.getInstance().getDatabase().queryId(id));
            }
        } else if (type == QueryType.ROLLBACK) {
            for (int id : ids) {
                Espial.getInstance().getEspialService().rollback(Espial.getInstance().getDatabase().queryId(id));
            }
        }

        this.undone = false;
    }
}
