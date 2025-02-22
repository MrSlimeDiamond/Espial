package net.slimediamond.espial.api.action.event;

import net.slimediamond.espial.api.action.ActionType;

public interface EventType {

    /**
     * The name of the event
     * @return Event type name
     */
    String getName();

    /**
     * The verb for the event
     * @return Event type verb
     */
    String getVerb();

    /**
     * Get the description of this event type
     * @return Event type description
     */
    String getDescription();

    /**
     * Get event ID
     * @return Event ID
     */
    int getId();

    /**
     * Get the event's action type
     * @return Action type
     */
    ActionType getActionType();

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        private String name;
        private String verb;
        private String description;
        private ActionType actionType;
        private int id;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder verb(String verb) {
            this.verb = verb;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder action(ActionType actionType) {
            this.actionType = actionType;
            return this;
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public EventType build() {
            return new EventType() {
                @Override
                public String getName() {
                    return name;
                }

                @Override
                public String getVerb() {
                    return verb;
                }

                @Override
                public String getDescription() {
                    return description;
                }

                @Override
                public int getId() {
                    return id;
                }

                @Override
                public ActionType getActionType() {
                    return actionType;
                }
            };
        }
    }
}