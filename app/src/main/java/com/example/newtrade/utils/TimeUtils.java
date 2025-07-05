// File: app/src/main/java/com/example/newtrade/utils/TimeUtils.java
package com.example.newtrade.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {

    private static final SimpleDateFormat ISO_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

    public static String getTimeAgo(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "Just now";
        }

        try {
            Date date = ISO_FORMAT.parse(dateString);
            if (date == null) return "Just now";

            long now = System.currentTimeMillis();
            long time = date.getTime();
            long diff = now - time;

            if (diff < 60000) { // Less than 1 minute
                return "Just now";
            } else if (diff < 3600000) { // Less than 1 hour
                int minutes = (int) (diff / 60000);
                return minutes + "m ago";
            } else if (diff < 86400000) { // Less than 24 hours
                int hours = (int) (diff / 3600000);
                return hours + "h ago";
            } else if (diff < 2592000000L) { // Less than 30 days
                int days = (int) (diff / 86400000);
                return days + "d ago";
            } else {
                return new SimpleDateFormat("MMM dd", Locale.getDefault()).format(date);
            }
        } catch (ParseException e) {
            return "Just now";
        }
    }
}