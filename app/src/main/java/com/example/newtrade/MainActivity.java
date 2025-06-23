// app/src/main/java/com/example/newtrade/MainActivity.java
package com.example.newtrade;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

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
            Log.d(TAG, "User not logged in, redirecting to login");
            navigateToLogin();
            return;
        }

        Log.d(TAG, "User logged in: " + prefsManager.getUserName() + " (ID: " + prefsManager.getUserId() + ")");

        setContentView(R.layout.activity_main);

        initViews();
        setupNavigation();
        setupFirebaseMessaging();

        Log.d(TAG, "MainActivity created successfully");
    }

    private void initViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation);

        if (bottomNavigation == null) {
            Log.e(TAG, "❌ Bottom navigation not found in layout");
            Toast.makeText(this, "Layout error - bottom navigation missing", Toast.LENGTH_LONG).show();
            return;
        }
    }

    private void setupNavigation() {
        try {
            navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            NavigationUI.setupWithNavController(bottomNavigation, navController);

            Log.d(TAG, "✅ Navigation setup completed");
        } catch (Exception e) {
            Log.e(TAG, "❌ Navigation setup failed", e);
            Toast.makeText(this, "Navigation setup error", Toast.LENGTH_LONG).show();
        }
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

                    // TODO: Send token to server when UserService is implemented
                    // sendFcmTokenToServer(token);
                });
    }

    private void sendFcmTokenToServer(String token) {
        // TODO: Implement when ready
        Log.d(TAG, "TODO: Send FCM token to server");
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
            Log.d(TAG, "Session lost, redirecting to login");
            navigateToLogin();
        }
    }

    @Override
    public void onBackPressed() {
        // Handle back button based on current fragment
        if (navController != null && navController.getCurrentDestination() != null &&
                navController.getCurrentDestination().getId() == R.id.homeFragment) {
            // If on home, exit app
            super.onBackPressed();
        } else if (bottomNavigation != null) {
            // Otherwise navigate to home
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MainActivity destroyed");
    }
}