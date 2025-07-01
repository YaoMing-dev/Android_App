// app/src/main/java/com/example/newtrade/utils/DateTimeUtils.java
package com.example.newtrade.utils;

import android.text.format.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {

    public static String formatMessageTime(Date date) {
        if (date == null) return "";

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return timeFormat.format(date);
    }

    public static String formatDate(Date date) {
        if (date == null) return "";

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return dateFormat.format(date);
    }

    public static String formatDateTime(Date date) {
        if (date == null) return "";

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return dateTimeFormat.format(date);
    }
}