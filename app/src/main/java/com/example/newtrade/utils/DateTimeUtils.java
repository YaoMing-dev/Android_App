// app/src/main/java/com/example/newtrade/utils/DateTimeUtils.java
package com.example.newtrade.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateTimeUtils {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("hh:mm a", Locale.getDefault());
    private static final SimpleDateFormat FULL_FORMAT = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());

    public static String formatDate(Date date) {
        if (date == null) return "";
        return DATE_FORMAT.format(date);
    }

    public static String formatTime(Date date) {
        if (date == null) return "";
        return TIME_FORMAT.format(date);
    }

    public static String formatDateTime(Date date) {
        if (date == null) return "";
        return FULL_FORMAT.format(date);
    }

    public static String getTimeAgo(Date date) {
        if (date == null) return "";

        long timeAgo = System.currentTimeMillis() - date.getTime();

        if (timeAgo < TimeUnit.MINUTES.toMillis(1)) {
            return "Just now";
        } else if (timeAgo < TimeUnit.HOURS.toMillis(1)) {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(timeAgo);
            return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";
        } else if (timeAgo < TimeUnit.DAYS.toMillis(1)) {
            long hours = TimeUnit.MILLISECONDS.toHours(timeAgo);
            return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
        } else if (timeAgo < TimeUnit.DAYS.toMillis(7)) {
            long days = TimeUnit.MILLISECONDS.toDays(timeAgo);
            return days + " day" + (days == 1 ? "" : "s") + " ago";
        } else {
            return formatDate(date);
        }
    }

    public static String getMessageTimeFormat(Date date) {
        if (date == null) return "";

        long timeAgo = System.currentTimeMillis() - date.getTime();

        if (timeAgo < TimeUnit.DAYS.toMillis(1)) {
            return formatTime(date);
        } else if (timeAgo < TimeUnit.DAYS.toMillis(7)) {
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
            return dayFormat.format(date);
        } else {
            SimpleDateFormat shortFormat = new SimpleDateFormat("MM/dd", Locale.getDefault());
            return shortFormat.format(date);
        }
    }
}