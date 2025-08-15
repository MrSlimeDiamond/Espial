package net.slimediamond.espial.api.time;

import java.util.Objects;

public class Year {

    private final int year;

    public Year(final int year) {
        this.year = year;
    }

    public static Year from(final int year) {
        return new Year(year);
    }

    public int get() {
        return year;
    }

    @Override
    public String toString() {
        return String.valueOf(year);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final Year year1 = (Year) o;
        return year == year1.year;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(year);
    }

}
