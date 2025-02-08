package net.slimediamond.espial.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DurationParser {
    // ChatGPT wrote this, don't ask me questions

    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)([a-zA-Z]+)");

    public static long parseDurationAndSubtract(String input) {
        Pattern pattern = Pattern.compile("(\\d+)([dhms])"); // Match the pattern for numbers followed by units (d, h, m, s)
        Matcher matcher = pattern.matcher(input);

        if (matcher.matches()) {
            // Extract the numeric value and the unit
            long value = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2).toLowerCase();

            // Calculate the duration in milliseconds
            long durationInMillis;
            switch (unit) {
                case "d": // Days
                    durationInMillis = value * 24 * 60 * 60 * 1000L; // Convert to milliseconds
                    break;
                case "h": // Hours
                    durationInMillis = value * 60 * 60 * 1000L; // Convert to milliseconds
                    break;
                case "m": // Minutes
                    durationInMillis = value * 60 * 1000L; // Convert to milliseconds
                    break;
                case "s": // Seconds
                    durationInMillis = value * 1000L; // Convert to milliseconds
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported unit: " + unit);
            }

            // Subtract the duration from the current time (current time in milliseconds)
            long currentTimeInMillis = System.currentTimeMillis();
            return currentTimeInMillis - durationInMillis; // Return the past time in milliseconds
        } else {
            throw new IllegalArgumentException("Invalid input format: " + input);
        }
    }

}
