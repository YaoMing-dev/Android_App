// app/src/main/java/com/example/newtrade/MainActivity.java
package com.example.newtrade;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.newtrade.api.ApiClient;
import com.example.newtrade.ui.auth.LoginActivity;
import com.example.newtrade.ui.home.HomeFragment;
import com.example.newtrade.ui.search.SearchFragment;
import com.example.newtrade.ui.product.AddProductFragment;
import com.example.newtrade.ui.chat.MessagesFragment;
import com.example.newtrade.ui.profile.ProfileFragment;
import com.example.newtrade.ui.product.AddProductActivity;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    // UI Components
    private BottomNavigationView bottomNavigation;
    private FloatingActionButton fabQuickAdd;

    // Fragment Management
    private FragmentManager fragmentManager;
    private Fragment currentFragment;

    // Fragments
    private HomeFragment homeFragment;
    private SearchFragment searchFragment;
    private AddProductFragment addProductFragment;
    private MessagesFragment messagesFragment;
    private ProfileFragment profileFragment;

    // Utils
    private SharedPrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize API Client
        ApiClient.init(this);

        // Initialize utils
        prefsManager = new SharedPrefsManager(this);

        // Check if user is logged in
        if (!prefsManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        // Initialize views and setup
        initViews();
        setupBottomNavigation();
        setupFAB();
        setupBackPressHandler(); // ✅ Fix deprecated onBackPressed

        // Load initial fragment
        loadFragment(getHomeFragment(), "HOME");

        Log.d(TAG, "MainActivity created successfully");
    }

    private void initViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation);
        fabQuickAdd = findViewById(R.id.fab_quick_add);
        fragmentManager = getSupportFragmentManager();
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                loadFragment(getHomeFragment(), "HOME");
                showFAB(true);
                return true;
            } else if (itemId == R.id.nav_search) {
                loadFragment(getSearchFragment(), "SEARCH");
                showFAB(false);
                return true;
            } else if (itemId == R.id.nav_add_product) {
                openAddProductActivity();
                return true;
            } else if (itemId == R.id.nav_messages) {
                loadFragment(getMessagesFragment(), "MESSAGES");
                showFAB(false);
                return true;
            } else if (itemId == R.id.nav_profile) {
                loadFragment(getProfileFragment(), "PROFILE");
                showFAB(false);
                return true;
            }

            return false;
        });

        // Set default selection
        bottomNavigation.setSelectedItemId(R.id.nav_home);
    }

    private void setupFAB() {
        fabQuickAdd.setOnClickListener(v -> openAddProductActivity());
    }

    // ✅ Fix deprecated onBackPressed with modern approach
    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // If not on home fragment, go to home
                if (currentFragment != homeFragment) {
                    bottomNavigation.setSelectedItemId(R.id.nav_home);
                    return;
                }

                // If on home fragment, check if it can handle back press
                if (currentFragment instanceof HomeFragment) {
                    HomeFragment home = (HomeFragment) currentFragment;
                    if (home.onBackPressed()) {
                        return;
                    }
                }

                // Default behavior - exit app
                finish();
            }
        });
    }

    private void loadFragment(Fragment fragment, String tag) {
        if (fragment == null) return;

        // Don't reload the same fragment
        if (currentFragment == fragment) return;

        try {
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            // Hide current fragment if exists
            if (currentFragment != null) {
                transaction.hide(currentFragment);
            }

            // Add or show new fragment
            Fragment existingFragment = fragmentManager.findFragmentByTag(tag);
            if (existingFragment != null) {
                transaction.show(existingFragment);
                currentFragment = existingFragment;
            } else {
                transaction.add(R.id.fragment_container, fragment, tag);
                currentFragment = fragment;
            }

            transaction.commit();
            Log.d(TAG, "Fragment loaded: " + tag);

        } catch (Exception e) {
            Log.e(TAG, "Error loading fragment: " + tag, e);
        }
    }

    private void showFAB(boolean show) {
        if (show) {
            fabQuickAdd.show();
        } else {
            fabQuickAdd.hide();
        }
    }

    private void openAddProductActivity() {
        Intent intent = new Intent(this, AddProductActivity.class);
        startActivity(intent);
    }

    // Fragment getters with lazy initialization
    private HomeFragment getHomeFragment() {
        if (homeFragment == null) {
            homeFragment = new HomeFragment();
        }
        return homeFragment;
    }

    private SearchFragment getSearchFragment() {
        if (searchFragment == null) {
            searchFragment = new SearchFragment();
        }
        return searchFragment;
    }

    private AddProductFragment getAddProductFragment() {
        if (addProductFragment == null) {
            addProductFragment = new AddProductFragment();
        }
        return addProductFragment;
    }

    private MessagesFragment getMessagesFragment() {
        if (messagesFragment == null) {
            messagesFragment = new MessagesFragment();
        }
        return messagesFragment;
    }

    private ProfileFragment getProfileFragment() {
        if (profileFragment == null) {
            profileFragment = new ProfileFragment();
        }
        return profileFragment;
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check authentication status
        if (!prefsManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        // Refresh current fragment if needed
        refreshCurrentFragment();
    }

    // ✅ Fix method calls to fragments - check if method exists before calling
    private void refreshCurrentFragment() {
        if (currentFragment instanceof HomeFragment) {
            ((HomeFragment) currentFragment).refreshData();
        } else if (currentFragment instanceof MessagesFragment) {
            ((MessagesFragment) currentFragment).refreshData();
        } else if (currentFragment instanceof ProfileFragment) {
            ((ProfileFragment) currentFragment).refreshData();
        }
        // SearchFragment and AddProductFragment don't need refreshData
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up fragments
        homeFragment = null;
        searchFragment = null;
        addProductFragment = null;
        messagesFragment = null;
        profileFragment = null;
    }
}