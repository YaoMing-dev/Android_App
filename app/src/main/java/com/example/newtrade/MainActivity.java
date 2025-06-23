// app/src/main/java/com/example/newtrade/MainActivity.java
package com.example.newtrade;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.newtrade.api.ApiClient;
import com.example.newtrade.ui.auth.LoginActivity;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // UI Components
    private BottomNavigationView bottomNavigation;
    private NavController navController;

    // Utils
    private SharedPrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize API client first
        ApiClient.init(this);

        // Initialize SharedPrefs
        prefsManager = SharedPrefsManager.getInstance(this);

        // Check authentication
        if (!prefsManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        setContentView(R.layout.activity_main);

        initViews();
        setupNavigation();
        setupFirebaseMessaging();

        Log.d(TAG, "MainActivity created for user: " + prefsManager.getUserName());
    }

    private void initViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation);
    }

    private void setupNavigation() {
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(bottomNavigation, navController);

        // Handle navigation item selection
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                navController.navigate(R.id.homeFragment);
                return true;
            } else if (itemId == R.id.nav_search) {
                navController.navigate(R.id.searchFragment);
                return true;
            } else if (itemId == R.id.nav_add_product) {
                navController.navigate(R.id.addProductFragment);
                return true;
            } else if (itemId == R.id.nav_messages) {
                navController.navigate(R.id.messagesFragment);
                return true;
            } else if (itemId == R.id.nav_profile) {
                navController.navigate(R.id.profileFragment);
                return true;
            }

            return false;
        });

        Log.d(TAG, "Navigation setup completed");
    }

    private void setupFirebaseMessaging() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();
                    Log.d(TAG, "FCM Token: " + token);

                    // Save token locally
                    prefsManager.saveFcmToken(token);

                    // TODO: Send token to server
                    // ApiClient.getUserService().updateFcmToken(token);
                });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Double check authentication when returning to app
        if (!prefsManager.isLoggedIn()) {
            navigateToLogin();
        }
    }

    @Override
    public void onBackPressed() {
        // Handle back button based on current fragment
        if (navController.getCurrentDestination() != null &&
                navController.getCurrentDestination().getId() == R.id.homeFragment) {
            // If on home, exit app
            super.onBackPressed();
        } else {
            // Otherwise navigate to home
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MainActivity destroyed");
    }
}