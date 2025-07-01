// app/src/main/java/com/example/newtrade/utils/DateTimeUtils.java
package com.example.newtrade.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateTimeUtils {

    // Date formats
    private static final String FORMAT_FULL_DATE = "MMM dd, yyyy";
    private static final String FORMAT_DATE_TIME = "MMM dd, yyyy 'at' h:mm a";
    private static final String FORMAT_TIME_ONLY = "h:mm a";
    private static final String FORMAT_SHORT_DATE = "MM/dd/yy";

    /**
     * Get relative time string (e.g., "2 hours ago", "Yesterday", "Last week")
     */
    public static String getRelativeTimeString(Date date) {
        if (date == null) return "Unknown";

        long now = System.currentTimeMillis();
        long time = date.getTime();
        long diff = now - time;

        if (diff < 0) {
            return "Just now";
        }

        long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        long days = TimeUnit.MILLISECONDS.toDays(diff);

        if (seconds < 60) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes == 1 ? "1 minute ago" : minutes + " minutes ago";
        } else if (hours < 24) {
            return hours == 1 ? "1 hour ago" : hours + " hours ago";
        } else if (days == 1) {
            return "Yesterday";
        } else if (days < 7) {
            return days + " days ago";
        } else if (days < 30) {
            long weeks = days / 7;
            return weeks == 1 ? "1 week ago" : weeks + " weeks ago";
        } else if (days < 365) {
            long months = days / 30;
            return months == 1 ? "1 month ago" : months + " months ago";
        } else {
            long years = days / 365;
            return years == 1 ? "1 year ago" : years + " years ago";
        }
    }

    /**
     * Format date for message timestamps
     */
    public static String formatMessageTime(Date date) {
        if (date == null) return "";

        long now = System.currentTimeMillis();
        long time = date.getTime();
        long diff = now - time;
        long days = TimeUnit.MILLISECONDS.toDays(diff);

        SimpleDateFormat formatter;
        if (days == 0) {
            // Today - show time only
            formatter = new SimpleDateFormat(FORMAT_TIME_ONLY, Locale.getDefault());
        } else if (days == 1) {
            // Yesterday
            return "Yesterday";
        } else if (days < 7) {
            // This week - show day name
            formatter = new SimpleDateFormat("EEEE", Locale.getDefault());
        } else {
            // Older - show date
            formatter = new SimpleDateFormat(FORMAT_SHORT_DATE, Locale.getDefault());
        }

        return formatter.format(date);
    }

    /**
     * Format date for product listings
     */
    public static String formatProductDate(Date date) {
        if (date == null) return "";

        SimpleDateFormat formatter = new SimpleDateFormat(FORMAT_FULL_DATE, Locale.getDefault());
        return formatter.format(date);
    }

    /**
     * Format full date and time
     */
    public static String formatFullDateTime(Date date) {
        if (date == null) return "";

        SimpleDateFormat formatter = new SimpleDateFormat(FORMAT_DATE_TIME, Locale.getDefault());
        return formatter.format(date);
    }

    /**
     * Check if date is today
     */
    public static boolean isToday(Date date) {
        if (date == null) return false;

        long now = System.currentTimeMillis();
        long time = date.getTime();
        long diff = now - time;

        return TimeUnit.MILLISECONDS.toDays(diff) == 0;
    }

    /**
     * Check if date is yesterday
     */
    public static boolean isYesterday(Date date) {
        if (date == null) return false;

        long now = System.currentTimeMillis();
        long time = date.getTime();
        long diff = now - time;

        return TimeUnit.MILLISECONDS.toDays(diff) == 1;
    }

    /**
     * Get chat time display (Today, Yesterday, or date)
     */
    public static String getChatTimeDisplay(Date date) {
        if (date == null) return "";

        if (isToday(date)) {
            return "Today";
        } else if (isYesterday(date)) {
            return "Yesterday";
        } else {
            return formatProductDate(date);
        }
    }

    /**
     * Parse ISO date string to Date object
     */
    public static Date parseISODate(String dateString) {
        if (dateString == null || dateString.isEmpty()) return null;

        try {
            // Handle common ISO formats
            SimpleDateFormat[] formatters = {
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()),
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()),
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
                    new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            };

            for (SimpleDateFormat formatter : formatters) {
                try {
                    return formatter.parse(dateString);
                } catch (Exception e) {
                    // Try next formatter
                }
            }
        } catch (Exception e) {
            // Log error but don't crash
        }

        return null;
    }
}