package net.slimediamond.espial.api.action;

import net.slimediamond.espial.api.EspialProviders;
import net.slimediamond.espial.api.action.event.EventType;
import net.slimediamond.espial.api.record.EntityRecord;
import net.slimediamond.espial.api.submittable.Submittable;
import net.slimediamond.espial.api.submittable.SubmittableResult;
import net.slimediamond.espial.api.user.EspialActor;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.world.server.ServerLocation;

/**
 * An action that removes an item from an item frame
 *
 * @author SlimeDiamond
 */
public interface ItemFrameRemoveAction extends EntityAction, Submittable<EntityRecord> {
  static Builder builder() {
    return new Builder();
  }

  /**
   * Get item type
   *
   * @return Item type
   */
  ItemType getItemType();

  class Builder {
    private ItemType itemType;
    private EspialActor actor;
    private int x, y, z;
    private String world;
    private EventType eventType;

    public Builder itemType(ItemType itemType) {
      this.itemType = itemType;
      return this;
    }

    public Builder actor(EspialActor actor) {
      this.actor = actor;
      return this;
    }

    public Builder x(int x) {
      this.x = x;
      return this;
    }

    public Builder y(int y) {
      this.y = y;
      return this;
    }

    public Builder z(int z) {
      this.z = z;
      return this;
    }

    public Builder location(ServerLocation location) {
      this.x = location.blockX();
      this.y = location.blockY();
      this.z = location.blockZ();
      return this;
    }

    public Builder world(String world) {
      this.world = world;
      return this;
    }

    public Builder event(EventType eventType) {
      this.eventType = eventType;
      return this;
    }

    public ItemFrameRemoveAction build() {
      return new ItemFrameRemoveAction() {

        @Override
        public ItemType getItemType() {
          return itemType;
        }

        @Override
        public EspialActor getActor() {
          return actor;
        }

        @Override
        public int getX() {
          return x;
        }

        @Override
        public int getY() {
          return y;
        }

        @Override
        public int getZ() {
          return z;
        }

        @Override
        public String getWorld() {
          return world;
        }

        @Override
        public EventType getEventType() {
          return eventType;
        }

        @Override
        public SubmittableResult<EntityRecord> submit() throws Exception {
          return (SubmittableResult<EntityRecord>)
              EspialProviders.getEspialService().submitAction(this);
        }
      };
    }
  }
}
