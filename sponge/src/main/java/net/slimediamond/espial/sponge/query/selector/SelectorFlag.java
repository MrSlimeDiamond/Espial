package net.slimediamond.espial.sponge.query.selector;

import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.parameter.managed.Flag;

public class SelectorFlag {

    private final Flag flag;
    private final Component description;

    private SelectorFlag(final Flag flag, final Component description) {
        this.flag = flag;
        this.description = description;
    }

    /**
     * The {@link SelectorFlag} factory
     *
     * @param flag The flag
     * @param description Description for the flag
     * @return The created {@link SelectorFlag}
     */
    public static SelectorFlag of(final Flag flag, final Component description) {
        return new SelectorFlag(flag, description);
    }

    /**
     * Get the flag
     *
     * @return Flag
     */
    public Flag getFlag() {
        return flag;
    }

    /**
     * Get the description
     *
     * @return Description
     */
    public Component getDescription() {
        return description;
    }

}
