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
            Fragment fragment;
            String tag;

            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                fragment = new HomeFragment();
                tag = "home";
            } else if (itemId == R.id.nav_search) {
                fragment = new SearchFragment();
                tag = "search";
            } else if (itemId == R.id.nav_add_product) {
                fragment = new AddProductFragment();
                tag = "add_product";
            } else if (itemId == R.id.nav_messages) {
                fragment = new MessagesFragment();
                tag = "messages";
            } else if (itemId == R.id.nav_profile) {
                fragment = new ProfileFragment();
                tag = "profile";
            } else {
                return false;
            }

            return loadFragment(fragment, tag);
        });

        // FAB click listener
        fabQuickAdd.setOnClickListener(v -> {
            loadFragment(new AddProductFragment(), "add_product");
            bottomNavigation.setSelectedItemId(R.id.nav_add_product);
        });

        // Set default fragment
        if (currentFragment == null) {
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        }
    }

    // ✅ FIX: Use parameter properly instead of global variable
    private void loadInitialFragment(Bundle savedInstanceState) {
        // Restore fragment state or load default
        if (savedInstanceState != null) {
            currentFragment = fragmentManager.findFragmentByTag(savedInstanceState.getString(CURRENT_FRAGMENT_TAG));
        }

        // Load appropriate fragment
        if (currentFragment == null) {
            currentFragment = new HomeFragment();
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, currentFragment);
        transaction.commit();
    }

    private boolean loadFragment(Fragment fragment, String tag) {
        if (currentFragment != null && currentFragment.getClass() == fragment.getClass()) {
            return false;
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
        );
        transaction.replace(R.id.fragment_container, fragment, tag);
        transaction.commit();

        currentFragment = fragment;
        return true;
    }

    private void initWebSocketService() {
        try {
            webSocketService = new RealtimeWebSocketService();
            // Connect if user is logged in
            if (prefsManager.isLoggedIn()) {
                String token = prefsManager.getAuthToken();
                if (token != null && !token.isEmpty()) {
                    webSocketService.connect(token);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize WebSocket service", e);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (currentFragment != null) {
            outState.putString(CURRENT_FRAGMENT_TAG, currentFragment.getTag());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webSocketService != null && prefsManager.isLoggedIn()) {
            webSocketService.reconnect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webSocketService != null) {
            webSocketService.disconnect();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocketService != null) {
            webSocketService.cleanup();
        }
    }

    @Override
    public void onBackPressed() {
        if (currentFragment instanceof HomeFragment) {
            super.onBackPressed();
        } else {
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        }
    }
}