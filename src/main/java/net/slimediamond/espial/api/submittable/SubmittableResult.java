package net.slimediamond.espial.api.submittable;

public final class SubmittableResult<T> {
    private T value;

    public SubmittableResult(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }
}
