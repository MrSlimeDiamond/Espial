package net.slimediamond.espial.sponge.user;

import net.kyori.adventure.audience.Audience;
import net.slimediamond.espial.api.transaction.EspialTransaction;
import net.slimediamond.espial.api.user.User;
import org.spongepowered.api.entity.living.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserImpl implements User {
    private String name;
    private UUID uuid;
    private Audience audience;

    private List<EspialTransaction> transactions = new ArrayList<>();

    public UserImpl(Player player) {
        this.name = player.name();
        this.uuid = player.uniqueId();
        this.audience = player;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public UUID getUUID() {
        return this.uuid;
    }

    @Override
    public Audience getAudience() {
        return this.audience;
    }

    @Override
    public List<EspialTransaction> getTransactions() {
        return this.transactions;
    }
}
