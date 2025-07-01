// app/src/main/java/com/example/newtrade/utils/DateTimeUtils.java
package com.example.newtrade.utils;

import android.text.format.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateTimeUtils {

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd", Locale.getDefault());
    private static final SimpleDateFormat FULL_DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private static final SimpleDateFormat PRODUCT_DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    // ✅ FIX: Add missing isToday method
    public static boolean isToday(Date date) {
        if (date == null) return false;

        Calendar today = Calendar.getInstance();
        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.setTime(date);

        return today.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == dateCalendar.get(Calendar.DAY_OF_YEAR);
    }

    // ✅ FIX: Add missing formatMessageTime method
    public static String formatMessageTime(Date date) {
        if (date == null) return "";

        if (isToday(date)) {
            return TIME_FORMAT.format(date);
        } else {
            return DATE_FORMAT.format(date);
        }
    }

    // ✅ FIX: Add missing getRelativeTimeString method
    public static String getRelativeTimeString(Date date) {
        if (date == null) return "";

        long now = System.currentTimeMillis();
        long time = date.getTime();

        return DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS).toString();
    }

    // ✅ FIX: Add missing formatProductDate method
    public static String formatProductDate(Date date) {
        if (date == null) return "";
        return PRODUCT_DATE_FORMAT.format(date);
    }

    public static String formatTimeAgo(Date date) {
        if (date == null) return "";

        long now = System.currentTimeMillis();
        long time = date.getTime();
        long diff = now - time;

        if (diff < TimeUnit.MINUTES.toMillis(1)) {
            return "Just now";
        } else if (diff < TimeUnit.HOURS.toMillis(1)) {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            return minutes + "m";
        } else if (diff < TimeUnit.DAYS.toMillis(1)) {
            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            return hours + "h";
        } else if (diff < TimeUnit.DAYS.toMillis(7)) {
            long days = TimeUnit.MILLISECONDS.toDays(diff);
            return days + "d";
        } else {
            return DATE_FORMAT.format(date);
        }
    }

    public static String formatChatTime(Date date) {
        if (date == null) return "";

        if (isToday(date)) {
            return TIME_FORMAT.format(date);
        } else if (isYesterday(date)) {
            return "Yesterday";
        } else {
            return DATE_FORMAT.format(date);
        }
    }

    private static boolean isYesterday(Date date) {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);

        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.setTime(date);

        return yesterday.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR) &&
                yesterday.get(Calendar.DAY_OF_YEAR) == dateCalendar.get(Calendar.DAY_OF_YEAR);
    }

    public static String formatFullDate(Date date) {
        if (date == null) return "";
        return FULL_DATE_FORMAT.format(date);
    }

    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) return false;

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}