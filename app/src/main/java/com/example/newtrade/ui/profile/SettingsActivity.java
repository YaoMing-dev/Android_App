// app/src/main/java/com/example/newtrade/ui/profile/SettingsActivity.java
package com.example.newtrade.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.auth.LoginActivity;
import com.example.newtrade.utils.NetworkUtils;
import com.example.newtrade.utils.SharedPrefsManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";

    // UI Components
    private Toolbar toolbar;
    private Switch switchNotifications, switchLocationSharing, switchShowOnline;
    private LinearLayout llChangePassword, llPrivacy, llTerms, llDeactivateAccount, llDeleteAccount;
    private TextView tvAppVersion;
    private ProgressBar progressBar;

    // Data
    private SharedPrefsManager prefsManager;
    private Map<String, Object> currentSettings = new HashMap<>();

    // State
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();
        initUtils();
        setupToolbar();
        setupListeners();

        loadUserSettings();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        switchNotifications = findViewById(R.id.switch_notifications);
        switchLocationSharing = findViewById(R.id.switch_location_sharing);
        switchShowOnline = findViewById(R.id.switch_show_online);
        llChangePassword = findViewById(R.id.ll_change_password);
        llPrivacy = findViewById(R.id.ll_privacy);
        llTerms = findViewById(R.id.ll_terms);
        llDeactivateAccount = findViewById(R.id.ll_deactivate_account);
        llDeleteAccount = findViewById(R.id.ll_delete_account);
        tvAppVersion = findViewById(R.id.tv_app_version);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void initUtils() {
        prefsManager = new SharedPrefsManager(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }
    }

    private void setupListeners() {
        // Switch listeners
        switchNotifications.setOnCheckedChangeListener(this::onNotificationSettingChanged);
        switchLocationSharing.setOnCheckedChangeListener(this::onLocationSharingChanged);
        switchShowOnline.setOnCheckedChangeListener(this::onShowOnlineChanged);

        // Menu item listeners
        llChangePassword.setOnClickListener(v -> changePassword());
        llPrivacy.setOnClickListener(v -> showPrivacyPolicy());
        llTerms.setOnClickListener(v -> showTermsOfService());
        llDeactivateAccount.setOnClickListener(v -> confirmDeactivateAccount());
        llDeleteAccount.setOnClickListener(v -> confirmDeleteAccount());

        // App version
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            tvAppVersion.setText("Version " + versionName);
        } catch (Exception e) {
            tvAppVersion.setText("Version 1.0.0");
        }
    }

    private void loadUserSettings() {
        setLoading(true);

        Call<StandardResponse<Map<String, Object>>> call = ApiClient.getUserService()
                .getUserSettings(prefsManager.getUserId());

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                setLoading(false);
                handleSettingsResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                setLoading(false);
                Log.e(TAG, "Failed to load settings", t);
                // Continue with default settings
                initializeDefaultSettings();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void handleSettingsResponse(Response<StandardResponse<Map<String, Object>>> response) {
        try {
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                currentSettings = response.body().getData();
                updateSettingsUI();
            } else {
                Log.w(TAG, "Failed to load settings, using defaults");
                initializeDefaultSettings();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing settings response", e);
            initializeDefaultSettings();
        }
    }

    private void initializeDefaultSettings() {
        currentSettings.put("notificationsEnabled", true);
        currentSettings.put("locationSharingEnabled", true);
        currentSettings.put("showOnlineStatus", true);
        updateSettingsUI();
    }

    private void updateSettingsUI() {
        // Temporarily disable listeners to avoid triggering updates
        switchNotifications.setOnCheckedChangeListener(null);
        switchLocationSharing.setOnCheckedChangeListener(null);
        switchShowOnline.setOnCheckedChangeListener(null);

        // Update switch states
        switchNotifications.setChecked((Boolean) currentSettings.getOrDefault("notificationsEnabled", true));
        switchLocationSharing.setChecked((Boolean) currentSettings.getOrDefault("locationSharingEnabled", true));
        switchShowOnline.setChecked((Boolean) currentSettings.getOrDefault("showOnlineStatus", true));

        // Restore listeners
        switchNotifications.setOnCheckedChangeListener(this::onNotificationSettingChanged);
        switchLocationSharing.setOnCheckedChangeListener(this::onLocationSharingChanged);
        switchShowOnline.setOnCheckedChangeListener(this::onShowOnlineChanged);
    }

    // FR-4.2.1: Push notifications settings
    private void onNotificationSettingChanged(CompoundButton buttonView, boolean isChecked) {
        currentSettings.put("notificationsEnabled", isChecked);
        saveSettings();
    }

    private void onLocationSharingChanged(CompoundButton buttonView, boolean isChecked) {
        currentSettings.put("locationSharingEnabled", isChecked);
        saveSettings();
    }

    private void onShowOnlineChanged(CompoundButton buttonView, boolean isChecked) {
        currentSettings.put("showOnlineStatus", isChecked);
        saveSettings();
    }

    private void saveSettings() {
        if (isLoading) return;

        Call<StandardResponse<Map<String, Object>>> call = ApiClient.getUserService()
                .updateUserSettings(currentSettings, prefsManager.getUserId());

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                if (!response.isSuccessful()) {
                    Log.w(TAG, "Failed to save settings");
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to save settings", t);
            }
        });
    }

    private void changePassword() {
        // TODO: Implement ChangePasswordActivity
        Toast.makeText(this, "Change password - Coming soon", Toast.LENGTH_SHORT).show();
    }

    private void showPrivacyPolicy() {
        // TODO: Implement PrivacyPolicyActivity or WebView
        Toast.makeText(this, "Privacy policy - Coming soon", Toast.LENGTH_SHORT).show();
    }

    private void showTermsOfService() {
        // TODO: Implement TermsActivity or WebView
        Toast.makeText(this, "Terms of service - Coming soon", Toast.LENGTH_SHORT).show();
    }

    // FR-1.2.3: Option to deactivate account
    private void confirmDeactivateAccount() {
        new AlertDialog.Builder(this)
                .setTitle("Deactivate Account")
                .setMessage("Are you sure you want to deactivate your account?\n\n" +
                        "Your profile and listings will be hidden, but your data will be preserved. " +
                        "You can reactivate your account by logging in again.")
                .setPositiveButton("Deactivate", (dialog, which) -> deactivateAccount())
                .setNegativeButton("Cancel", null)
                .show();
    }

    // FR-1.2.3: Option to permanently delete account
    private void confirmDeleteAccount() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("⚠️ WARNING: This action cannot be undone!\n\n" +
                        "Are you sure you want to permanently delete your account?\n\n" +
                        "This will:\n" +
                        "• Delete all your listings\n" +
                        "• Remove all your messages\n" +
                        "• Delete your profile permanently\n" +
                        "• Cancel any pending transactions")
                .setPositiveButton("Delete Forever", (dialog, which) -> confirmDeleteFinal())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDeleteFinal() {
        new AlertDialog.Builder(this)
                .setTitle("Final Confirmation")
                .setMessage("Type 'DELETE' to confirm permanent account deletion:")
                .setView(R.layout.dialog_confirm_delete)
                .setPositiveButton("Delete", (dialog, which) -> {
                    // TODO: Get text from dialog and verify it says "DELETE"
                    deleteAccount();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deactivateAccount() {
        setLoading(true);

        // TODO: Create deactivate account API endpoint
        Call<StandardResponse<Void>> call = ApiClient.getUserService()
                .deactivateAccount(prefsManager.getUserId());

        call.enqueue(new Callback<StandardResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Void>> call,
                                   @NonNull Response<StandardResponse<Void>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(SettingsActivity.this, "Account deactivated", Toast.LENGTH_SHORT).show();
                    logout();
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "Failed to deactivate account";
                    showError(message);
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Void>> call, @NonNull Throwable t) {
                setLoading(false);
                Log.e(TAG, "Failed to deactivate account", t);
                showError(NetworkUtils.getNetworkErrorMessage(t));
            }
        });
    }

    private void deleteAccount() {
        setLoading(true);

        Call<StandardResponse<Void>> call = ApiClient.getUserService()
                .deleteAccount(prefsManager.getUserId());

        call.enqueue(new Callback<StandardResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Void>> call,
                                   @NonNull Response<StandardResponse<Void>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(SettingsActivity.this, "Account deleted permanently", Toast.LENGTH_SHORT).show();
                    logout();
                } else {
                    String message = response.body() != null ? response.body().getMessage() : "Failed to delete account";
                    showError(message);
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Void>> call, @NonNull Throwable t) {
                setLoading(false);
                Log.e(TAG, "Failed to delete account", t);
                showError(NetworkUtils.getNetworkErrorMessage(t));
            }
        });
    }

    private void logout() {
        prefsManager.logout();
        ApiClient.clearServices();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        progressBar.setVisibility(loading ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}