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

        // Initialize ApiClient FIRST
        ApiClient.init(this);

        setContentView(R.layout.activity_main);

        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize components
        initViews();
        initData();
        setupWindowInsets();
        setupBottomNavigation();
        setupListeners();

        // Load initial fragment
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment(), "home", R.id.nav_home);
        } else {
            currentFragmentTag = savedInstanceState.getString(CURRENT_FRAGMENT_TAG, "home");
        }

        // Test backend connection
        testBackendConnection();

        Log.d(TAG, "✅ MainActivity created successfully");
    }

    private void initViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation);
        fabQuickAdd = findViewById(R.id.fab_quick_add);
    }

    private void initData() {
        fragmentManager = getSupportFragmentManager();
        prefsManager = new SharedPrefsManager(this);
    }

    private void setupWindowInsets() {
        View rootView = findViewById(android.R.id.content);

        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            );
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                loadFragment(new HomeFragment(), "home", R.id.nav_home);
                return true;
            } else if (itemId == R.id.nav_search) {
                loadFragment(new SearchFragment(), "search", R.id.nav_search);
                return true;
            } else if (itemId == R.id.nav_sell) {
                loadFragment(new AddProductFragment(), "sell", R.id.nav_sell);
                return true;
            } else if (itemId == R.id.nav_messages) {
                loadFragment(new MessagesFragment(), "messages", R.id.nav_messages);
                return true;
            } else if (itemId == R.id.nav_profile) {
                loadFragment(new ProfileFragment(), "profile", R.id.nav_profile);
                return true;
            }

            return false;
        });
    }

    private void setupListeners() {
        if (fabQuickAdd != null) {
            fabQuickAdd.setOnClickListener(v -> {
                bottomNavigation.setSelectedItemId(R.id.nav_sell);
            });
        }
    }

    private void loadFragment(Fragment fragment, String tag, int menuItemId) {
        try {
            if (currentFragment != null && currentFragment.getClass().equals(fragment.getClass())) {
                Log.d(TAG, "Same fragment, skipping: " + tag);
                return;
            }

            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
            );
            // ✅ FIX: Sử dụng container ID có sẵn trong layout
            transaction.replace(R.id.nav_host_fragment, fragment, tag); // Hoặc R.id.container
            transaction.commit();

            currentFragment = fragment;
            currentFragmentTag = tag;

            Log.d(TAG, "✅ Fragment loaded: " + tag);

        } catch (Exception e) {
            Log.e(TAG, "❌ Error loading fragment: " + tag, e);
        }
    }

    private void testBackendConnection() {
        if (ApiClient.isInitialized()) {
            ApiClient.getApiService().healthCheck()
                    .enqueue(new retrofit2.Callback<com.example.newtrade.models.StandardResponse<String>>() {
                        @Override
                        public void onResponse(retrofit2.Call<com.example.newtrade.models.StandardResponse<String>> call,
                                               retrofit2.Response<com.example.newtrade.models.StandardResponse<String>> response) {
                            if (response.isSuccessful()) {
                                Log.d(TAG, "✅ Backend connection successful!");
                            } else {
                                Log.e(TAG, "❌ Backend connection failed: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<com.example.newtrade.models.StandardResponse<String>> call, Throwable t) {
                            Log.e(TAG, "❌ Backend connection error: " + t.getMessage());
                        }
                    });
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

        Long userId = prefsManager.getUserId();
        if (userId != null && userId > 0) {
            // ✅ FIX: Sử dụng constructor thay vì getInstance()
            RealtimeWebSocketService.getInstance().connect(userId, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RealtimeWebSocketService.getInstance().disconnect();
    }
}