// app/src/main/java/com/example/newtrade/MainActivity.java
package com.example.newtrade;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.newtrade.ui.home.HomeFragment;
import com.example.newtrade.ui.messages.MessagesFragment;
import com.example.newtrade.ui.product.AddProductFragment;
import com.example.newtrade.ui.profile.ProfileFragment;
import com.example.newtrade.ui.search.SearchFragment;
import com.example.newtrade.utils.Constants;
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

        // ✅ CRITICAL: Fix camera cutout/notch issues
        setupWindowForCameraCutout();

        setContentView(R.layout.activity_main);

        // Initialize components
        initViews();
        initUtils();
        setupBottomNavigation();
        setupWindowInsets();
        startWebSocketService();

        // Restore fragment state or set default
        if (savedInstanceState != null) {
            currentFragmentTag = savedInstanceState.getString(CURRENT_FRAGMENT_TAG, "home");
        }

        // Navigate to initial fragment
        navigateToFragment(currentFragmentTag);

        Log.d(TAG, "✅ MainActivity created successfully");
    }

    // ✅ CRITICAL: Camera cutout and display cutout handling
    private void setupWindowForCameraCutout() {
        // Handle display cutout (camera notch/punch hole)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        // Make status bar transparent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().setNavigationBarColor(Color.TRANSPARENT);

            // Full screen flags
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            );
        }

        Log.d(TAG, "✅ Window setup for camera cutout completed");
    }

    private void initViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation);
        fabQuickAdd = findViewById(R.id.fab_quick_add);

        // Validate views
        if (bottomNavigation == null) {
            Log.e(TAG, "❌ Bottom navigation not found in layout");
            Toast.makeText(this, "Layout error - bottom navigation missing", Toast.LENGTH_LONG).show();
            return;
        }

        fragmentManager = getSupportFragmentManager();

        Log.d(TAG, "✅ Views initialized successfully");
    }

    private void initUtils() {
        prefsManager = SharedPrefsManager.getInstance(this);

        // Check if user is logged in
        if (!prefsManager.isLoggedIn()) {
            Log.w(TAG, "⚠️ User not logged in, redirecting to login");
            redirectToLogin();
            return;
        }

        // Test backend connectivity
        Constants.testBackendConnectivity();

        Log.d(TAG, "✅ Utils initialized - User ID: " + prefsManager.getUserId());
    }

    private void setupBottomNavigation() {
        if (bottomNavigation == null) return;

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

        // Setup FAB
        if (fabQuickAdd != null) {
            fabQuickAdd.setOnClickListener(v -> {
                navigateToFragment("add_product");
                bottomNavigation.setSelectedItemId(R.id.nav_add_product);
            });
        }

        Log.d(TAG, "✅ Bottom navigation setup completed");
    }

    // ✅ IMPROVED: Window insets handling for modern Android
    private void setupWindowInsets() {
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {

                // Handle system bars
                WindowInsetsCompat systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                WindowInsetsCompat displayCutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout());

                // Apply padding to avoid overlap with system UI
                int topPadding = Math.max(systemBars.top, displayCutout.top);
                int bottomPadding = systemBars.bottom;

                // Apply padding to main content, not bottom navigation
                View mainContent = findViewById(R.id.fragment_container);
                if (mainContent != null) {
                    mainContent.setPadding(
                            systemBars.left + displayCutout.left,
                            topPadding,
                            systemBars.right + displayCutout.right,
                            0 // Don't add bottom padding to main content
                    );
                }

                // Apply bottom padding to bottom navigation if needed
                if (bottomNavigation != null) {
                    bottomNavigation.setPadding(0, 0, 0, bottomPadding);
                }

                Log.d(TAG, "✅ Window insets applied - Top: " + topPadding + ", Bottom: " + bottomPadding);

                return insets;
            });
        }
    }

    // ✅ IMPROVED: Fragment navigation with proper lifecycle
    private void navigateToFragment(String fragmentTag) {
        if (fragmentTag.equals(currentFragmentTag) && currentFragment != null) {
            return; // Already showing this fragment
        }

        Fragment fragment = createFragmentByTag(fragmentTag);
        if (fragment == null) {
            Log.e(TAG, "❌ Failed to create fragment for tag: " + fragmentTag);
            return;
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Add animation
        transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
        );

        // Replace fragment
        transaction.replace(R.id.fragment_container, fragment, fragmentTag);

        // Don't add to back stack for main navigation
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

    // ✅ IMPROVED: WebSocket service management
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
            Log.e(TAG, "❌ Error stopping WebSocket service", e);
        }

        Log.d(TAG, "MainActivity destroyed");
    }

    // ===== BACK BUTTON HANDLING =====

    @Override
    public void onBackPressed() {
        // Handle back button based on current fragment
        if (currentFragment instanceof HomeFragment) {
            // Exit app if on home fragment
            finishAffinity();
        } else {
            // Navigate to home fragment
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        }
    }

    // ===== NAVIGATION HELPERS =====

    private void redirectToLogin() {
        Intent intent = new Intent(this, com.example.newtrade.ui.auth.LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ===== PUBLIC METHODS FOR FRAGMENTS =====

    /**
     * Method for fragments to navigate to other fragments
     */
    public void navigateToFragmentFromChild(String fragmentTag) {
        navigateToFragment(fragmentTag);

        // Update bottom navigation selection
        switch (fragmentTag) {
            case "home":
                bottomNavigation.setSelectedItemId(R.id.nav_home);
                break;
            case "search":
                bottomNavigation.setSelectedItemId(R.id.nav_search);
                break;
            case "add_product":
                bottomNavigation.setSelectedItemId(R.id.nav_add_product);
                break;
            case "messages":
                bottomNavigation.setSelectedItemId(R.id.nav_messages);
                break;
            case "profile":
                bottomNavigation.setSelectedItemId(R.id.nav_profile);
                break;
        }
    }

    /**
     * Get current fragment tag
     */
    public String getCurrentFragmentTag() {
        return currentFragmentTag;
    }

    /**
     * Check if specific fragment is currently showing
     */
    public boolean isFragmentShowing(String fragmentTag) {
        return fragmentTag.equals(currentFragmentTag);
    }
}