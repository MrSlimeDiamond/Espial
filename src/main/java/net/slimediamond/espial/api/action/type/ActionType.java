package net.slimediamond.espial.api.action.type;

public interface ActionType {

    /**
     * The name of the action
     * @return Action type name
     */
    String getName();

    /**
     * The verb for the action
     * @return Action type verb
     */
    String getVerb();

    /**
     * Get the description of this action type
     * @return Action type description
     */
    String getDescription();

    /**
     * Get action ID
     * @return Action ID
     */
    int getId();

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        private String name;
        private String verb;
        private String description;
        private int id;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setVerb(String verb) {
            this.verb = verb;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setId(int id) {
            this.id = id;
            return this;
        }

        public ActionType build() {
            return new ActionType() {
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
            };
        }
    }
}