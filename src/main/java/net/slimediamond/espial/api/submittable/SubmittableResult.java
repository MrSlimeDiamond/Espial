package net.slimediamond.espial.api.submittable;

/**
 * The result of a {@link Submittable} action.
 *
 * @author Findlay Richardson (SlimeDiamond)
 */
public final class SubmittableResult<T> {
    private final T value;

    public SubmittableResult(T value) {
        this.value = value;
    }

    public static <T> SubmittableResult<T> of(T value) {
        return new SubmittableResult<>(value);
    }

    public T get() {
        return value;
    }
}
