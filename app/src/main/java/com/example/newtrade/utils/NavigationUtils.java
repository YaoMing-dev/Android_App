// app/src/main/java/com/example/newtrade/utils/NavigationUtils.java
package com.example.newtrade.utils;

import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class NavigationUtils {

    /**
     * ✅ IMPROVED: Standard back button handling for Activities
     */
    public static boolean handleBackButton(@NonNull AppCompatActivity activity, @NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Use onBackPressed() instead of finish() for better navigation
            activity.onBackPressed();
            return true;
        }
        return false;
    }

    /**
     * ✅ IMPROVED: Setup toolbar with back button
     */
    public static void setupToolbarWithBackButton(@NonNull AppCompatActivity activity,
                                                  @NonNull Toolbar toolbar,
                                                  String title) {
        activity.setSupportActionBar(toolbar);
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
            if (title != null && !title.isEmpty()) {
                activity.getSupportActionBar().setTitle(title);
            }
        }
    }

    /**
     * ✅ NEW: Setup toolbar without back button
     */
    public static void setupToolbar(@NonNull AppCompatActivity activity,
                                    @NonNull Toolbar toolbar,
                                    String title) {
        activity.setSupportActionBar(toolbar);
        if (activity.getSupportActionBar() != null) {
            if (title != null && !title.isEmpty()) {
                activity.getSupportActionBar().setTitle(title);
            }
        }
    }
}