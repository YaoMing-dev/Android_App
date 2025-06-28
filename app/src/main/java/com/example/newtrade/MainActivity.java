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
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.newtrade.api.ApiClient;
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

        // ✅ FIX 1: Initialize ApiClient FIRST THING
        Log.d(TAG, "🔄 Initializing ApiClient...");
        try {
            ApiClient.init(this);
            Log.d(TAG, "✅ ApiClient initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to initialize ApiClient", e);
            // Continue anyway, fragments will handle this gracefully
        }

        // ✅ FIX 2: Initialize SharedPrefsManager
        prefsManager = SharedPrefsManager.getInstance(this);

        // ✅ FIX 3: Check authentication and redirect if needed
        if (!prefsManager.isLoggedIn()) {
            Log.d(TAG, "User not logged in, redirecting to LoginActivity");
            Intent intent = new Intent(this, com.example.newtrade.ui.auth.LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // ✅ FIX 4: Setup window for camera cutout
        setupWindowForCameraCutout();

        setContentView(R.layout.activity_main);

        // Initialize components
        initViews();
        setupBottomNavigation();
        setupFloatingActionButton();

        // Restore fragment state or load default
        if (savedInstanceState != null) {
            currentFragmentTag = savedInstanceState.getString(CURRENT_FRAGMENT_TAG, "home");
        }

        // Load default fragment
        switchToFragment(currentFragmentTag);

        // Start WebSocket service for real-time features
        startWebSocketService();

        Log.d(TAG, "✅ MainActivity created successfully for user: " + prefsManager.getUserEmail());
    }

    private void setupWindowForCameraCutout() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                getWindow().getAttributes().layoutInDisplayCutoutMode =
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            }

            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().setNavigationBarColor(Color.TRANSPARENT);

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content),
                    (v, insets) -> {
                        return insets;
                    });

            Log.d(TAG, "✅ Window setup completed");
        } catch (Exception e) {
            Log.w(TAG, "⚠️ Window setup failed: " + e.getMessage());
        }
    }

    private void initViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation);
        fabQuickAdd = findViewById(R.id.fab_quick_add);
        fragmentManager = getSupportFragmentManager();

        // Check if views are found
        if (bottomNavigation == null) {
            Log.e(TAG, "❌ BottomNavigation not found in layout");
            return;
        }

        Log.d(TAG, "✅ Views initialized successfully");
    }

    private void setupBottomNavigation() {
        if (bottomNavigation == null) return;

        bottomNavigation.setOnItemSelectedListener(item -> {
            String tag = getFragmentTagFromMenuItem(item.getItemId());
            if (tag != null && !tag.equals(currentFragmentTag)) {
                switchToFragment(tag);
                return true;
            }
            return false;
        });

        Log.d(TAG, "✅ Bottom navigation setup completed");
    }

    private void setupFloatingActionButton() {
        if (fabQuickAdd != null) {
            fabQuickAdd.setOnClickListener(v -> {
                // Quick add product action
                switchToFragment("add_product");
                bottomNavigation.setSelectedItemId(R.id.nav_sell);
            });
        }
    }

    private String getFragmentTagFromMenuItem(int itemId) {
        if (itemId == R.id.nav_home) return "home";
        else if (itemId == R.id.nav_search) return "search";
        else if (itemId == R.id.nav_sell) return "add_product";
        else if (itemId == R.id.nav_messages) return "messages";
        else if (itemId == R.id.nav_profile) return "profile";
        return null;
    }

    private void switchToFragment(String tag) {
        try {
            Fragment newFragment = getFragmentByTag(tag);
            if (newFragment == null) {
                Log.e(TAG, "❌ Failed to create fragment for tag: " + tag);
                return;
            }

            FragmentTransaction transaction = fragmentManager.beginTransaction();

            if (currentFragment != null) {
                transaction.hide(currentFragment);
            }

            Fragment existingFragment = fragmentManager.findFragmentByTag(tag);
            if (existingFragment != null) {
                transaction.show(existingFragment);
                currentFragment = existingFragment;
            } else {
                transaction.add(R.id.nav_host_fragment, newFragment, tag);
                currentFragment = newFragment;
            }

            transaction.commit();
            currentFragmentTag = tag;

            Log.d(TAG, "✅ Switched to fragment: " + tag);

        } catch (Exception e) {
            Log.e(TAG, "❌ Error switching fragment: " + tag, e);
        }
    }

    private Fragment getFragmentByTag(String tag) {
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
                return new HomeFragment();
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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CURRENT_FRAGMENT_TAG, currentFragmentTag);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // ✅ FIX: Re-check authentication on resume
        if (!prefsManager.isLoggedIn()) {
            Log.d(TAG, "User logged out, redirecting to LoginActivity");
            Intent intent = new Intent(this, com.example.newtrade.ui.auth.LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "🧹 MainActivity destroyed");
    }

    @Override
    public void onBackPressed() {
        // ✅ FIX: Handle back button properly
        if (currentFragmentTag.equals("home")) {
            super.onBackPressed(); // Exit app
        } else {
            // Navigate to home
            switchToFragment("home");
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        }
    }
}