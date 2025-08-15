package net.slimediamond.espial.api.time;

import java.util.Objects;

public class Day {

    private final Year year;
    private final Month month;
    private final int day;

    public Day(final Month month, final int day) {
        this.year = month.getYear();
        this.month = month;
        this.day = day;
    }

    public static Day from(final int year, final int month, final int day) {
        return new Day(new Month(Year.from(year), month), day);
    }

    public Year getYear() {
        return year;
    }

    public int getDay() {
        return day;
    }

    @Override
    public String toString() {
        return year.get() + "-" + month.getMonth() + "-" + day;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final Day day1 = (Day) o;
        return day == day1.day && Objects.equals(year, day1.year) && Objects.equals(month, day1.month);
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, month, day);
    }

}
