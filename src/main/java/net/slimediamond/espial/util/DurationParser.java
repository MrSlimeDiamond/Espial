package net.slimediamond.espial.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DurationParser {
    /**
     * Parse a human-given string containing time units like 3d15h --> 3 days, 15 hours ago
     * and turns it into epoch
     * <p>
     * Supported units:
     * <ul>
     *   <li>y - Years (365 days per year, approximate)</li>
     *   <li>M - Months (30 days per month, approximate)</li>
     *   <li>w - Weeks (7 days per week)</li>
     *   <li>d - Days</li>
     *   <li>h - Hours</li>
     *   <li>m - Minutes</li>
     *   <li>s - Seconds</li>
     * </ul>
     *
     * @param input String to parse
     * @return Epoch long
     * @throws IllegalArgumentException If the input is invalid
     */
    public static long parseDurationAndSubtract(String input) throws IllegalArgumentException {
        Pattern pattern = Pattern.compile("(\\d+)([yMwdhms])");
        Matcher matcher = pattern.matcher(input);

        long durationInMillis = 0;
        while (matcher.find()) {
            long value = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2);

            durationInMillis += switch (unit) {
                case "y" -> value * 365L * 24 * 60 * 60 * 1000L; // Years
                case "M" -> value * 30L * 24 * 60 * 60 * 1000L;  // Months (approx.)
                case "w" -> value * 7L * 24 * 60 * 60 * 1000L;   // Weeks
                case "d" -> value * 24 * 60 * 60 * 1000L;        // Days
                case "h" -> value * 60 * 60 * 1000L;             // Hours
                case "m" -> value * 60 * 1000L;                  // Minutes
                case "s" -> value * 1000L;                       // Seconds
                default -> throw new IllegalArgumentException("Unsupported unit: " + unit);
            };
        }

        if (durationInMillis == 0) {
            throw new IllegalArgumentException("Invalid input format: " + input);
        }

        return System.currentTimeMillis() - durationInMillis;
    }
}
