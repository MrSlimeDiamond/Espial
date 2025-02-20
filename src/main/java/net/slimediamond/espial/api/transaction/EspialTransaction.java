package net.slimediamond.espial.api.transaction;

import net.kyori.adventure.audience.Audience;
import net.slimediamond.espial.api.query.Query;
import net.slimediamond.espial.api.query.QueryType;

import java.util.List;

public interface EspialTransaction {
    /**
     * The type of transaction this is
     * @return Transaction type
     */
    QueryType getType();

    /**
     * Get the query used to make the transaction
     * @return Query
     */
    Query getQuery();

    /**
     * Get the user
     * @return User
     */
    Object getUser();

    /**
     * Get audience
     * @return Audience
     */
    Audience getAudience();

    /**
     * Return a list of all the IDs that were affected
     * @return IDs
     */
    List<Integer> getAffectedIds();

    /**
     * Whether this transaction has been undone
     * @return Undone
     */
    boolean isUndone();

    /**
     * Undo this transaction
     */
    int undo() throws Exception;

    /**
     * Redo this transaction
     */
    int redo() throws Exception;
}
