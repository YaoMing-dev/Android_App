// app/src/main/java/com/example/newtrade/utils/NavigationUtils.java
package com.example.newtrade.utils;

import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;

public class NavigationUtils {

    /**
     * Standard back button handling for Activities
     * Call this in onOptionsItemSelected of every Activity
     */
    public static boolean handleBackButton(AppCompatActivity activity, MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            activity.finish();
            return true;
        }
        return false;
    }

    /**
     * Setup toolbar with back button
     */
    public static void setupToolbarWithBackButton(AppCompatActivity activity,
                                                  androidx.appcompat.widget.Toolbar toolbar,
                                                  String title) {
        activity.setSupportActionBar(toolbar);
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (title != null) {
                activity.getSupportActionBar().setTitle(title);
            }
        }
    }
}