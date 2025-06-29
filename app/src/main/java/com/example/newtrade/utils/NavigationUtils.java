package com.example.newtrade.utils;

import android.app.Activity;
import android.view.MenuItem;

public class NavigationUtils {

    public static boolean handleBackButton(Activity activity, MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            activity.onBackPressed();
            return true;
        }
        return false;
    }

    public static void navigateBack(Activity activity) {
        activity.onBackPressed();
    }
}
