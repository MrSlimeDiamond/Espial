package net.slimediamond.espial.sponge.utils;

import java.util.Date;

public final class TimeUtils {

    /**
     * Returns a user-friendly relative "time since" value for two dates.
     *
     * @param start Starting time. Must be prior to present.
     * @return String relative "time since" value.
     */
    public static String getTimeSince(final Date start) {
        String result = "";

        long diffInSeconds = (new Date().getTime() - start.getTime()) / 1000;
        long[] diff = new long[] {0, 0, 0, 0};

        diff[3] = (diffInSeconds >= 60 ? diffInSeconds % 60 : diffInSeconds);
        diff[2] = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60 : diffInSeconds;
        diff[1] = (diffInSeconds = (diffInSeconds / 60)) >= 24 ? diffInSeconds % 24 : diffInSeconds;
        diff[0] = (diffInSeconds / 24);

        // Only show days if more than 1
        if (diff[0] >= 1) {
            result += diff[0] + "d";
        }
        // Only show hours if > 1
        if (diff[1] >= 1) {
            result += diff[1] + "h";
        }
        // Only show minutes if > 1 and less than 60
        if (diff[2] > 1) {
            result += diff[2] + "m";
        }
        if (!result.isEmpty()) {
            result += " ago";
        }

        if (diff[0] == 0 && diff[1] == 0 && diff[2] <= 1) {
            result = "just now";
        }

        return result;
    }

}
