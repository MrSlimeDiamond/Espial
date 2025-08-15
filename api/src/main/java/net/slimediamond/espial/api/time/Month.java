package net.slimediamond.espial.api.time;

import java.util.Objects;

public class Month {

    private final Year year;
    private final int month;

    public Month(final Year year, final int month) {
        this.year = year;
        this.month = month;
    }

    public static Month from(final int year, final int month) {
        return new Month(Year.from(year), month);
    }

    public Year getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    @Override
    public String toString() {
        return year.get() + "-" + month;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final Month month1 = (Month) o;
        return month == month1.month && Objects.equals(year, month1.year);
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, month);
    }

}
