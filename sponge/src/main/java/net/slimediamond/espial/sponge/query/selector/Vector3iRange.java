package net.slimediamond.espial.sponge.query.selector;

import org.spongepowered.math.vector.Vector3i;

public class Vector3iRange {

    private final Vector3i minimum;
    private final Vector3i maximum;

    public Vector3iRange(final Vector3i minimum, final Vector3i maximum) {
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public Vector3i getMinimum() {
        return minimum;
    }

    public Vector3i getMaximum() {
        return maximum;
    }

}
