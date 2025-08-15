package net.slimediamond.espial.sponge.utils;

import java.util.Date;

public final class DateUtils {

    public static int getYear(final Date date) {
        return date.getYear() + 1900;
    }

    public static int getMonth(final Date date) {
        return date.getMonth() + 1;
    }

}
