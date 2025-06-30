// app/src/main/java/com/example/newtrade/utils/DateTimeUtils.java
package com.example.newtrade.utils;

import android.text.format.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {

    private static final SimpleDateFormat API_DATE_FORMAT =
            new SimpleDateFormat(Constants.DATE_FORMAT_API, Locale.getDefault());

    private static final SimpleDateFormat DISPLAY_DATE_FORMAT =
            new SimpleDateFormat(Constants.DATE_FORMAT_DISPLAY, Locale.getDefault());

    private static final SimpleDateFormat TIME_FORMAT =
            new SimpleDateFormat(Constants.TIME_FORMAT_DISPLAY, Locale.getDefault());

    public static String formatMessageTime(String apiDateString) {
        try {
            Date date = API_DATE_FORMAT.parse(apiDateString);
            if (date == null) return "";

            long now = System.currentTimeMillis();
            long messageTime = date.getTime();

            if (DateUtils.isToday(messageTime)) {
                return TIME_FORMAT.format(date);
            } else if (now - messageTime < DateUtils.WEEK_IN_MILLIS) {
                return DateUtils.getRelativeTimeSpanString(messageTime, now, DateUtils.DAY_IN_MILLIS).toString();
            } else {
                return DISPLAY_DATE_FORMAT.format(date);
            }
        } catch (Exception e) {
            return apiDateString;
        }
    }

    public static String formatProductDate(String apiDateString) {
        try {
            Date date = API_DATE_FORMAT.parse(apiDateString);
            if (date == null) return "";

            long now = System.currentTimeMillis();
            long productTime = date.getTime();

            if (now - productTime < DateUtils.DAY_IN_MILLIS) {
                return DateUtils.getRelativeTimeSpanString(productTime, now, DateUtils.MINUTE_IN_MILLIS).toString();
            } else {
                return DISPLAY_DATE_FORMAT.format(date);
            }
        } catch (Exception e) {
            return apiDateString;
        }
    }
}