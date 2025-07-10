package net.slimediamond.espial.sponge.utils.formatting;

import net.kyori.adventure.text.Component;
import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.record.EspialBlockRecord;
import net.slimediamond.espial.api.record.EspialRecord;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.EntityType;

import java.util.Objects;
import java.util.UUID;

public class StackedRecord {

    private final UUID user;
    private final EntityType entityType;
    private final EspialEvent event;
    private final Component target;
    private final boolean rolledBack;

    public StackedRecord(@NotNull final EspialRecord record) {
        this.user = record.getUser().orElse(null);
        this.entityType = record.getEntityType();
        this.event = record.getEvent();
        this.rolledBack = record.isRolledBack();
        if (record instanceof EspialBlockRecord blockRecord) {
            this.target = blockRecord.getBlockState().type().asComponent();
        } else {
            this.target = Component.empty();
        }
    }

    public UUID getUser() {
        return user;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public EspialEvent getEvent() {
        return event;
    }

    public Component getTarget() {
        return target;
    }

    public boolean isRolledBack() {
        return rolledBack;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final StackedRecord that = (StackedRecord) o;
        return rolledBack == that.rolledBack && Objects.equals(user, that.user) && Objects.equals(event, that.event) &&
                Objects.equals(target, that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, event, target, rolledBack);
    }

}
