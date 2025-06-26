// app/src/main/java/com/example/newtrade/ui/profile/SettingsActivity.java
package com.example.newtrade.ui.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.auth.LoginActivity;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private LinearLayout llEditProfile, llMyListings, llSavedItems, llOfferHistory;
    private LinearLayout llNotifications, llPrivacy, llAbout, llLogout, llDeleteAccount;
    private SwitchMaterial switchNotifications, switchLocationServices;
    private TextView tvUserName, tvUserEmail;

    // Utils
    private SharedPrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();
        setupToolbar();
        setupListeners();
        displayUserInfo();

        Log.d(TAG, "SettingsActivity created");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvUserName = findViewById(R.id.tv_user_name);
        tvUserEmail = findViewById(R.id.tv_user_email);

        llEditProfile = findViewById(R.id.ll_edit_profile);
        llMyListings = findViewById(R.id.ll_my_listings);
        llSavedItems = findViewById(R.id.ll_saved_items);
        llOfferHistory = findViewById(R.id.ll_offer_history);
        llNotifications = findViewById(R.id.ll_notifications);
        llPrivacy = findViewById(R.id.ll_privacy);
        llAbout = findViewById(R.id.ll_about);
        llLogout = findViewById(R.id.ll_logout);
        llDeleteAccount = findViewById(R.id.ll_delete_account);

        switchNotifications = findViewById(R.id.switch_notifications);
        switchLocationServices = findViewById(R.id.switch_location);

        prefsManager = SharedPrefsManager.getInstance(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }
    }

    private void setupListeners() {
        llEditProfile.setOnClickListener(v -> openEditProfile());
        llMyListings.setOnClickListener(v -> openMyListings());
        llSavedItems.setOnClickListener(v -> openSavedItems());
        llOfferHistory.setOnClickListener(v -> openOfferHistory());
        llLogout.setOnClickListener(v -> showLogoutDialog());
        llDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(this, isChecked ? "Notifications enabled" : "Notifications disabled",
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void displayUserInfo() {
        String displayName = prefsManager.getUserName();
        String email = prefsManager.getUserEmail();

        tvUserName.setText(displayName != null ? displayName : "User");
        tvUserEmail.setText(email != null ? email : "No email");
    }

    private void openEditProfile() {
        Intent intent = new Intent(this, EditProfileActivity.class);
        startActivity(intent);
    }

    private void openMyListings() {
        Intent intent = new Intent(this, MyListingsActivity.class);
        startActivity(intent);
    }

    private void openSavedItems() {
        Intent intent = new Intent(this, SavedItemsActivity.class);
        startActivity(intent);
    }

    private void openOfferHistory() {
        Intent intent = new Intent(this, OfferHistoryActivity.class);
        startActivity(intent);
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> performLogout())
                .setNegativeButton("No", null)
                .show();
    }

    private void performLogout() {
        // Call API logout if needed
        prefsManager.clearUserSession();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("This action cannot be undone. Are you sure you want to permanently delete your account?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    Toast.makeText(this, "Account deletion feature coming soon", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}