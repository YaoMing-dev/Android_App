// app/src/main/java/com/example/newtrade/MainActivity.java
package com.example.newtrade;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.newtrade.ui.auth.LoginActivity;
import com.example.newtrade.ui.home.HomeFragment;
import com.example.newtrade.ui.search.SearchFragment;
import com.example.newtrade.ui.product.AddProductFragment;
import com.example.newtrade.ui.chat.ChatListFragment;
import com.example.newtrade.ui.profile.ProfileFragment;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // UI Components
    private BottomNavigationView bottomNavigation;
    private FloatingActionButton fabQuickAdd;

    // Utils
    private SharedPrefsManager prefsManager;

    // Fragment management
    private Fragment currentFragment;
    private final FragmentManager fragmentManager = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize SharedPrefs
        prefsManager = new SharedPrefsManager(this);

        // Check authentication
        if (!prefsManager.isLoggedIn()) {
            Log.d(TAG, "User not logged in, redirecting to login");
            navigateToLogin();
            return;
        }

        Log.d(TAG, "User logged in - UserId: " + prefsManager.getUserId());

        // Set layout and initialize
        setContentView(R.layout.activity_main);
        initViews();
        setupBottomNavigation();
        setupFAB();

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment(), R.id.nav_home);
        }

        Log.d(TAG, "MainActivity initialized successfully");
    }

    private void initViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation);
        fabQuickAdd = findViewById(R.id.fab_quick_add);

        if (bottomNavigation == null || fabQuickAdd == null) {
            Log.e(TAG, "❌ Required views not found in layout");
            return;
        }

        Log.d(TAG, "✅ All views initialized successfully");
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                fragment = new HomeFragment();
                showFAB();
            } else if (itemId == R.id.nav_search) {
                fragment = new SearchFragment();
                showFAB();
            } else if (itemId == R.id.nav_add_product) {
                fragment = new AddProductFragment();
                hideFAB();
            } else if (itemId == R.id.nav_chat) {
                fragment = new ChatListFragment();
                showFAB();
            } else if (itemId == R.id.nav_profile) {
                fragment = new ProfileFragment();
                showFAB();
            }

            if (fragment != null) {
                return loadFragment(fragment, itemId);
            }

            return false;
        });
    }

    private void setupFAB() {
        fabQuickAdd.setOnClickListener(v -> {
            // Navigate to Add Product
            bottomNavigation.setSelectedItemId(R.id.nav_add_product);
        });
    }

    private boolean loadFragment(Fragment fragment, int menuItemId) {
        try {
            // Don't reload the same fragment
            if (currentFragment != null &&
                    currentFragment.getClass().equals(fragment.getClass())) {
                return true;
            }

            FragmentTransaction transaction = fragmentManager.beginTransaction();

            // Replace fragment with animation
            transaction.setCustomAnimations(
                    R.anim.fade_in,
                    R.anim.fade_out,
                    R.anim.fade_in,
                    R.anim.fade_out
            );

            transaction.replace(R.id.fragment_container, fragment);
            transaction.commit();

            currentFragment = fragment;

            Log.d(TAG, "✅ Fragment loaded: " + fragment.getClass().getSimpleName());
            return true;

        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to load fragment", e);
            return false;
        }
    }

    private void showFAB() {
        fabQuickAdd.setVisibility(View.VISIBLE);
        fabQuickAdd.show();
    }

    private void hideFAB() {
        fabQuickAdd.hide();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Handle back press for fragments
        if (currentFragment != null) {
            // If not on home fragment, go to home
            if (!(currentFragment instanceof HomeFragment)) {
                bottomNavigation.setSelectedItemId(R.id.nav_home);
                return;
            }
        }

        // Default back press behavior
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check if user is still logged in
        if (!prefsManager.isLoggedIn()) {
            Log.d(TAG, "User logged out, redirecting to login");
            navigateToLogin();
        }
    }

    // Public methods for fragments to use
    public void navigateToFragment(Fragment fragment, int menuItemId) {
        loadFragment(fragment, menuItemId);
        bottomNavigation.setSelectedItemId(menuItemId);
    }

    public SharedPrefsManager getPrefsManager() {
        return prefsManager;
    }
}