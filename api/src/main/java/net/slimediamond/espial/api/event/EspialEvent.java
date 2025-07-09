package net.slimediamond.espial.api.event;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.util.annotation.CatalogedBy;


@CatalogedBy(EspialEvents.class)
public interface EspialEvent {

    /**
     * Get the ID of the Espial event
     *
     * @return Event ID
     */
    int getId();

    /**
     * Get the name of the Espial event
     *
     * @return Event name
     */
    String getName();

    /**
     * Get a description of the event
     *
     * @return Event description
     */
    String getDescription();

    /**
     * Get the verb used to join this event with, when
     * displayed to a user
     * 
     * <p>This might be something like {@code broke}, {@code placed}, etc</p>
     * 
     * @return Event verb
     */
    String getVerb();

    /**
     * Get the event's verb as a component
     *
     * @return Verb component
     */
    Component getVerbComponent();

    static Builder builder() {
        return Sponge.game().builderProvider().provide(Builder.class);
    }
    
    interface Builder extends org.spongepowered.api.util.Builder<EspialEvent, Builder> {

        /**
         * Sets the ID for the event
         *
         *<p><strong>This is required</strong></p>
         *
         * @param id Event ID
         * @return This builder, for chaining
         */
        Builder id(int id);

        /**
         * Sets the name for the event
         *
         *<p><strong>This is required</strong></p>
         *
         * @param name Event name
         * @return This builder, for chaining
         */
        Builder name(@NotNull String name);

        /**
         * Sets the description for the event
         *
         *<p><strong>This is required</strong></p>
         *
         * @param description Event description
         * @return This builder, for chaining
         */
        Builder description(@NotNull String description);

        /**
         * Sets the verb for the event
         *
         *<p><strong>This is required</strong></p>
         *
         * @param verb Event verb
         * @return This builder, for chaining
         */
        Builder verb(@NotNull String verb);

    }

}
