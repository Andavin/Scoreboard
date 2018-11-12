package com.andavin.scoreboard.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TimeUtil {

    public static final long SECOND = 1000L, MINUTE = SECOND * 60, HOUR = MINUTE * 60,
            DAY = HOUR * 24, WEEK = DAY * 7, MONTH = DAY * 30, YEAR = DAY * 365;

    private static final long[] UNITS = { YEAR, MONTH, WEEK, DAY, HOUR, MINUTE, SECOND };
    private static final String[][] SUFFIXES = {
            { "yr ", "mo ", "w ", "d ", "h ", "m ", "s" },
            { " years ", " months ", " weeks ", " days ", " hours ", " minutes ", " seconds" },
            { " year ", " month ", " week ", " day ", " hour ", " minute ", " second" }
    };

    private static final DateFormat FORMAT = new SimpleDateFormat("MMMMM dd, yyyy 'at' h:mm a");
    private static final TimeZone EST = TimeZone.getTimeZone("EST");
    private static final Pattern TIME_PATTERN = Pattern.compile(
            "(?:([0-9]+)\\s*y[a-z]*[,\\s]*)?" +
            "(?:([0-9]+)\\s*mo[a-z]*[,\\s]*)?" +
            "(?:([0-9]+)\\s*w[a-z]*[,\\s]*)?" +
            "(?:([0-9]+)\\s*d[a-z]*[,\\s]*)?" +
            "(?:([0-9]+)\\s*h[a-z]*[,\\s]*)?" +
            "(?:([0-9]+)\\s*(?:mil[a-z]*|ms)[,\\s]*)?" +
            "(?:([0-9]+)\\s*m[a-z]*[,\\s]*)?" +
            "(?:([0-9]+)\\s*(?:s[a-z]*)?)?",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Get the time that is the beginning of today
     * (i.e. 12:00 AM EST) in milliseconds.
     *
     * @return The beginning of today.
     */
    public static long getBeginningDay() {
        Calendar calendar = Calendar.getInstance(EST);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.AM_PM, Calendar.AM);
        return calendar.getTimeInMillis();
    }

    /**
     * Get the time that is the beginning of the Sunday this
     * week (i.e. Sunday at 12:00 AM EST) in milliseconds.
     *
     * @return The beginning of the week.
     */
    public static long getBeginningWeek() {
        Calendar calendar = Calendar.getInstance(EST);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.AM_PM, Calendar.AM);
        return calendar.getTimeInMillis();
    }

    /**
     * Parse a string of time. The string should be in the relative format of
     * {@code [0-9] identifier} where {@code identifier} is the unit of time.
     * There can be multiple formats in the string and an undefined amount or
     * type of spacing.
     * <p>
     * <b>Warning</b>: It is always better for the string to be formatted with the
     * start being the highest unit of time and working it's way down to the
     * lowest (years, then months, then weeks etc.). If it is not in this order
     * the search algorithm may mix up units and some units may end up being incorrect.
     *
     * @param timeString The string of time to parse for time units.
     * @return The amount of time in milliseconds that was parsed from the string.
     * @throws NumberFormatException If the string contained elements other than the numbers and identifiers.
     */
    public static long parse(String timeString) throws NumberFormatException {

        long total = 0;
        Matcher matcher = TIME_PATTERN.matcher(timeString);
        while (matcher.find()) {

            String main = matcher.group();
            if (main == null || main.isEmpty()) {
                continue;
            }

            for (int i = 0; i <= UNITS.length; i++) {

                String group = matcher.group(i + 1);
                if (group == null || group.isEmpty()) {
                    continue;
                }

                if (i < 5) {
                    total += Integer.parseInt(group) * UNITS[i];
                } else if (i == 5) {
                    total += Long.parseLong(group);
                } else {
                    total += Integer.parseInt(group) * UNITS[i - 1];
                }
            }
        }

        return total;
    }

    /**
     * Format the difference between the current time and the
     * given unix time date. This will always return positive
     * values even if the toDate is in the past.
     *
     * @param toDate The date to format the difference to.
     * @return The formatted date string.
     */
    public static String formatDifference(long toDate) {
        return TimeUtil.formatDifference(toDate, false);
    }

    /**
     * Format the difference between the current time and the
     * given unix time date. This will always return positive
     * values even if the toDate is in the past.
     *
     * @param toDate The date to format the difference to.
     * @param detailed Whether or not to include milliseconds.
     * @return The formatted date string.
     */
    public static String formatDifference(long toDate, boolean detailed) {
        return TimeUtil.formatDifference(System.currentTimeMillis(), toDate, detailed, true);
    }

    /**
     * Get the time like {@link #formatDifference(long)}, but
     * instead formatting from 0.
     *
     * @param time The time to format.
     * @return The formatted time string.
     */
    public static String formatTime(long time) {
        return TimeUtil.formatDifference(0, time, false, true);
    }

    /**
     * Get the given date formatted into a friendly
     * calendar date (e.g. November 2, 2017 at 3:05 PM).
     *
     * @param date The unix date to format.
     * @return The formatted date string.
     */
    public static String formatDate(long date) {
        return FORMAT.format(date);
    }

    /**
     * Format the time difference between the two dates. This will always
     * return a positive formatted value even if toDate is after fromDate
     * which case the values will simply be swapped.
     *
     * @param fromDate The date in milliseconds to format from.
     * @param toDate The date in milliseconds to format to.
     * @param detailed Whether to include millisecond calculation in the figures.
     * @param shrink If the suffixes should be shrunk down (e.g. {@code yr} vs {@code years}).
     * @return The formatted date difference between the two times.
     */
    public static String formatDifference(long fromDate, long toDate, boolean detailed, boolean shrink) {

        // Ensure fromDate is always <= toDate
        if (fromDate > toDate) {
            long temp = fromDate;
            fromDate = toDate;
            toDate = temp;
        }

        long diff = toDate - fromDate;
        if (diff < SECOND) {
            // If the time is less than a second then return 0 seconds or
            // if it's detailed then how many milliseconds are left
            return detailed ? diff + TimeUtil.getUnitSuffix(SUFFIXES[0].length, shrink, diff > 1) :
                    "0" + TimeUtil.getUnitSuffix(SUFFIXES[0].length - 1, shrink, diff > 1);
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < UNITS.length; i++) {

            long unit = UNITS[i];
            int unitsFound = TimeUtil.getDiff(unit, fromDate, toDate);
            if (unitsFound > 0) {
                toDate -= unitsFound * unit;
                sb.append(unitsFound).append(TimeUtil.getUnitSuffix(i, shrink, unitsFound > 1));
            }
        }

        long left = toDate - fromDate;
        if (detailed && left > 0) {

            // Don't add a space unless the last one didn't have a space on it
            if (sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ') {
                sb.append(' ');
            }

            // toDate now is however many milliseconds are left
            // since all other units have been subtracted from it
            sb.append(left).append(TimeUtil.getUnitSuffix(SUFFIXES[0].length, shrink, left > 1));
        }

        // Cut off the extra space at the end if it exists
        if (sb.charAt(sb.length() - 1) == ' ') {
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
    }

    /**
     * Get the suffix of the given index based on the UNITS field.
     * These units account for a space in front of all but seconds
     * since seconds are all but guaranteed to be included.
     *
     * @param index The index of the unit to get the suffix for.
     * @param shrink If the suffixes should be shrunk down (e.g. {@code yr} vs {@code years}).
     * @param plural If the suffix should be for multiples.
     * @return The suffix for the unit with the given index.
     */
    private static String getUnitSuffix(int index, boolean shrink, boolean plural) {
        return 0 <= index && index < SUFFIXES[0].length ? SUFFIXES[shrink ? 0 : plural ? 1 : 2][index] :
                shrink ? "ms" : plural ? " milliseconds" : " millisecond";
    }

    private static int getDiff(long unit, long fromDate, long toDate) {

        // Should always be positive
        long diff = toDate - fromDate;
        if (diff == 0 || diff < unit) {
            return 0;
        }

        int amount = 0;
        while (diff >= unit) {
            amount++;
            diff -= unit;
        }

        return amount;
    }
}
