// app/src/main/java/com/example/newtrade/ui/auth/SplashActivity.java
package com.example.newtrade.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.newtrade.MainActivity;
import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.utils.SharedPrefsManager;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final int SPLASH_DELAY = 2000; // 2 seconds

    // UI Components
    private ImageView ivLogo;
    private ProgressBar progressBar;

    // Utils
    private SharedPrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize API client
        ApiClient.init(this);

        // Initialize views and utilities
        initViews();
        initUtils();

        // Start splash sequence
        startSplashSequence();

        Log.d(TAG, "✅ SplashActivity created successfully");
    }

    private void initViews() {
        ivLogo = findViewById(R.id.iv_logo);
        progressBar = findViewById(R.id.progress_bar);

        Log.d(TAG, "✅ Views initialized");
    }

    private void initUtils() {
        prefsManager = SharedPrefsManager.getInstance(this);

        // Increment app launch count
        prefsManager.incrementAppLaunchCount();

        // Log current state for debugging
        prefsManager.logCurrentState();

        Log.d(TAG, "✅ Utils initialized");
    }

    private void startSplashSequence() {
        // Show splash screen for specified duration
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                checkAuthenticationAndNavigate();
            }
        }, SPLASH_DELAY);
    }

    private void checkAuthenticationAndNavigate() {
        if (prefsManager.isLoggedIn()) {
            Log.d(TAG, "✅ User is logged in, navigating to MainActivity");
            navigateToMain();
        } else {
            Log.d(TAG, "❌ User is not logged in, navigating to LoginActivity");
            navigateToLogin();
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "SplashActivity destroyed");
    }
}