package net.slimediamond.espial.api.user;

import net.kyori.adventure.audience.Audience;
import net.slimediamond.espial.api.transaction.EspialTransaction;

import java.util.List;
import java.util.UUID;

/**
 * An Espial user
 *
 * @author Findlay Richardson (SlimeDiamond)
 */
public interface User {
    /**
     * The user's name
     * @return Name
     */
    String getName();

    /**
     * Get the user's UUID
     * @return UUID
     */
    UUID getUUID();

    /**
     * Get audience to send messages to the user
     * @return User audience
     */
    Audience getAudience();

    /**
     * The transactions (rollbacks and restores) this user has done
     * @return Transactions
     */
    List<EspialTransaction> getTransactions();
}
