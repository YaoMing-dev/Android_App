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

    // Utils
    private SharedPrefsManager prefsManager;
    private RealtimeWebSocketService webSocketService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize utilities
        prefsManager = SharedPrefsManager.getInstance(this);
        fragmentManager = getSupportFragmentManager();

        // Set up window appearance
        setupWindow();

        // Initialize views
        initViews();
        setupNavigation();

        // Load saved fragment or default
        loadInitialFragment(savedInstanceState);

        // Initialize WebSocket service
        initWebSocketService();

        Log.d(TAG, "MainActivity created successfully");
    }

    private void setupWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(Color.TRANSPARENT);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
    }

    private void initViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation);
        fabQuickAdd = findViewById(R.id.fab_quick_add);
    }

    private void setupNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                switchFragment(new HomeFragment(), "home");
                return true;
            } else if (itemId == R.id.nav_search) {
                switchFragment(new SearchFragment(), "search");
                return true;
            } else if (itemId == R.id.nav_messages) {
                switchFragment(new MessagesFragment(), "messages");
                return true;
            } else if (itemId == R.id.nav_profile) {
                switchFragment(new ProfileFragment(), "profile");
                return true;
            }
            return false;
        });

        // FAB click listener
        fabQuickAdd.setOnClickListener(v -> {
            switchFragment(new AddProductFragment(), "add_product");
            // Optionally highlight the add tab or handle UI state
        });
    }

    // ✅ FIX: Use parameter properly instead of global variable
    private void loadInitialFragment(Bundle savedInstanceState) {
        // Restore fragment state or load default
        if (savedInstanceState != null) {
            currentFragmentTag = savedInstanceState.getString(CURRENT_FRAGMENT_TAG, "home");
        }

        // Load appropriate fragment
        switch (currentFragmentTag) {
            case "home":
                currentFragment = new HomeFragment();
                bottomNavigation.setSelectedItemId(R.id.nav_home);
                break;
            case "search":
                currentFragment = new SearchFragment();
                bottomNavigation.setSelectedItemId(R.id.nav_search);
                break;
            case "messages":
                currentFragment = new MessagesFragment();
                bottomNavigation.setSelectedItemId(R.id.nav_messages);
                break;
            case "profile":
                currentFragment = new ProfileFragment();
                bottomNavigation.setSelectedItemId(R.id.nav_profile);
                break;
            case "add_product":
                currentFragment = new AddProductFragment();
                break;
            default:
                currentFragment = new HomeFragment();
                currentFragmentTag = "home";
                bottomNavigation.setSelectedItemId(R.id.nav_home);
                break;
        }

        if (currentFragment != null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container, currentFragment, currentFragmentTag);
            transaction.commit();
        }
    }

    private void switchFragment(Fragment fragment, String tag) {
        if (!tag.equals(currentFragmentTag)) {
            currentFragmentTag = tag;
            currentFragment = fragment;

            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            transaction.replace(R.id.fragment_container, fragment, tag);
            transaction.commit();

            Log.d(TAG, "Switched to fragment: " + tag);
        }
    }

    private void initWebSocketService() {
        Long currentUserId = prefsManager.getUserId();
        if (currentUserId != null && currentUserId > 0) {
            webSocketService = RealtimeWebSocketService.getInstance();
            webSocketService.connect(currentUserId);

            Log.d(TAG, "✅ WebSocket service initialized for user: " + currentUserId);
        } else {
            Log.w(TAG, "⚠️ No valid user ID found, WebSocket not initialized");
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

        // Reconnect WebSocket if needed
        if (webSocketService != null && !webSocketService.isConnected()) {
            Long currentUserId = prefsManager.getUserId();
            if (currentUserId != null && currentUserId > 0) {
                webSocketService.connect(currentUserId);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Disconnect WebSocket
        if (webSocketService != null) {
            webSocketService.disconnect();
        }

        Log.d(TAG, "MainActivity destroyed");
    }

    // Public method for fragments to access WebSocket service
    public RealtimeWebSocketService getWebSocketService() {
        return webSocketService;
    }
}