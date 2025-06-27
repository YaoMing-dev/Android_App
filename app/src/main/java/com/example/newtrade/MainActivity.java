// app/src/main/java/com/example/newtrade/MainActivity.java
package com.example.newtrade;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.DisplayCutoutCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.newtrade.ui.home.HomeFragment;
import com.example.newtrade.ui.chat.MessagesFragment;
import com.example.newtrade.ui.product.AddProductFragment;
import com.example.newtrade.ui.profile.ProfileFragment;
import com.example.newtrade.ui.search.SearchFragment;
import com.example.newtrade.utils.SharedPrefsManager;
import com.example.newtrade.websocket.RealtimeWebSocketService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String CURRENT_FRAGMENT_TAG = "current_fragment";

    // UI Components
    private BottomNavigationView bottomNavigation;
    private FloatingActionButton fabQuickAdd;

    // Fragment Management
    private FragmentManager fragmentManager;
    private Fragment currentFragment;
    private String currentFragmentTag = "home";

    // Data
    private SharedPrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ FIX: Simplified camera cutout handling
        setupWindowForCameraCutout();

        setContentView(R.layout.activity_main);

        // Initialize components
        initViews();
        initData();
        setupBottomNavigation();
        setupFloatingActionButton();

        // Restore fragment state or load default
        if (savedInstanceState != null) {
            currentFragmentTag = savedInstanceState.getString(CURRENT_FRAGMENT_TAG, "home");
        }

        // Load initial fragment
        navigateToFragment(currentFragmentTag);

        // Start WebSocket service
        startWebSocketService();

        Log.d(TAG, "✅ MainActivity created successfully");
    }

    // ✅ FIX: Proper DisplayCutout API handling
    private void setupWindowForCameraCutout() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Handle display cutout
            getWindow().getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        // Make status bar transparent
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        // ✅ FIX: Handle window insets properly
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content),
                (View v, WindowInsetsCompat insets) -> {
                    // ✅ FIX: Use DisplayCutoutCompat instead of android.view.DisplayCutout
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        DisplayCutoutCompat cutout = insets.getDisplayCutout();
                        if (cutout != null) {
                            int left = cutout.getSafeInsetLeft();
                            int top = cutout.getSafeInsetTop();
                            int right = cutout.getSafeInsetRight();
                            int bottom = cutout.getSafeInsetBottom();

                            v.setPadding(left, top, right, bottom);
                            Log.d(TAG, "✅ Applied cutout padding: " + left + "," + top + "," + right + "," + bottom);
                        }
                    }

                    return WindowInsetsCompat.CONSUMED;
                });

        // Make activity fullscreen
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
    }

    private void initViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation);
        fabQuickAdd = findViewById(R.id.fab_quick_add);
        fragmentManager = getSupportFragmentManager();
    }

    private void initData() {
        prefsManager = SharedPrefsManager.getInstance(this);

        // Check if user is logged in
        if (!prefsManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                navigateToFragment("home");
                return true;
            } else if (itemId == R.id.nav_search) {
                navigateToFragment("search");
                return true;
            } else if (itemId == R.id.nav_add_product) {
                navigateToFragment("add_product");
                return true;
            } else if (itemId == R.id.nav_messages) {
                navigateToFragment("messages");
                return true;
            } else if (itemId == R.id.nav_profile) {
                navigateToFragment("profile");
                return true;
            }

            return false;
        });
    }

    private void setupFloatingActionButton() {
        if (fabQuickAdd != null) {
            fabQuickAdd.setOnClickListener(v -> {
                navigateToFragment("add_product");
                bottomNavigation.setSelectedItemId(R.id.nav_add_product);
            });
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, com.example.newtrade.ui.auth.LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ✅ FIX: Use correct container ID
    private void navigateToFragment(String fragmentTag) {
        Fragment fragment = createFragmentByTag(fragmentTag);
        if (fragment == null) {
            Log.e(TAG, "❌ Failed to create fragment: " + fragmentTag);
            return;
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Add smooth transition
        transaction.setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out
        );

        // ✅ FIX: Use correct container ID
        transaction.replace(R.id.nav_host_fragment, fragment, fragmentTag);
        transaction.commit();

        currentFragment = fragment;
        currentFragmentTag = fragmentTag;

        Log.d(TAG, "✅ Navigated to fragment: " + fragmentTag);
    }

    private Fragment createFragmentByTag(String tag) {
        switch (tag) {
            case "home":
                return new HomeFragment();
            case "search":
                return new SearchFragment();
            case "add_product":
                return new AddProductFragment();
            case "messages":
                return new MessagesFragment();
            case "profile":
                return new ProfileFragment();
            default:
                Log.e(TAG, "❌ Unknown fragment tag: " + tag);
                return null;
        }
    }

    private void startWebSocketService() {
        try {
            Intent serviceIntent = new Intent(this, RealtimeWebSocketService.class);
            startService(serviceIntent);
            Log.d(TAG, "✅ WebSocket service started");
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to start WebSocket service", e);
        }
    }

    // ===== LIFECYCLE METHODS =====

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CURRENT_FRAGMENT_TAG, currentFragmentTag);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check if user is still logged in
        if (!prefsManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        Log.d(TAG, "✅ MainActivity resumed");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "MainActivity paused");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Stop WebSocket service
        try {
            Intent serviceIntent = new Intent(this, RealtimeWebSocketService.class);
            stopService(serviceIntent);
            Log.d(TAG, "✅ WebSocket service stopped");
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to stop WebSocket service", e);
        }
    }
}