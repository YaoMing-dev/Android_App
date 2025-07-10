// app/src/main/java/com/example/newtrade/ui/profile/SettingsActivity.java
package com.example.newtrade.ui.profile;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.auth.LoginActivity;
import com.example.newtrade.ui.profile.SavedItemsActivity;
import com.example.newtrade.ui.transaction.TransactionHistoryActivity;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private LinearLayout llEditProfile, llMyListings, llSavedItems, llOfferHistory;
    private LinearLayout llTransactionHistory;
    private LinearLayout llNotifications, llLogout, llDeleteAccount;
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
        llTransactionHistory = findViewById(R.id.ll_transaction_history);
        llNotifications = findViewById(R.id.ll_notifications);
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
        llTransactionHistory.setOnClickListener(v -> openTransactionHistory());
        llLogout.setOnClickListener(v -> showLogoutDialog());
        llDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateNotificationSettings(isChecked);
        });

        switchLocationServices.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateLocationSettings(isChecked);
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

    private void openTransactionHistory() {
        Intent intent = new Intent(this, TransactionHistoryActivity.class);
        startActivity(intent);
    }

    private void updateNotificationSettings(boolean enabled) {
        prefsManager.setNotificationsEnabled(enabled);
        Toast.makeText(this, enabled ? "Notifications enabled" : "Notifications disabled",
                Toast.LENGTH_SHORT).show();
    }

    private void updateLocationSettings(boolean enabled) {
        prefsManager.setLocationEnabled(enabled);
        Toast.makeText(this, enabled ? "Location services enabled" : "Location services disabled",
                Toast.LENGTH_SHORT).show();
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
        // Clear session và navigate to login
        clearSessionAndNavigateToLogin();
    }

    private void clearSessionAndNavigateToLogin() {
        prefsManager.clearUserSession();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    // ===== ACCOUNT DELETION IMPLEMENTATION =====

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account Permanently")
                .setMessage("Are you sure you want to delete your account?\n\n" +
                        "⚠️ This action CANNOT be undone!\n\n" +
                        "All your data will be permanently deleted:\n" +
                        "• Profile information\n" +
                        "• Product listings\n" +
                        "• Messages and conversations\n" +
                        "• Transaction history\n" +
                        "• Reviews and ratings\n" +
                        "• Saved items")
                .setPositiveButton("DELETE ACCOUNT", (dialog, which) -> {
                    showEmailConfirmationDialog();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEmailConfirmationDialog() {
        // Create email input
        EditText etEmail = new EditText(this);
        etEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        etEmail.setHint("Enter your email to confirm");

        // Create container with padding
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(50, 30, 50, 30);
        container.addView(etEmail);

        new AlertDialog.Builder(this)
                .setTitle("Confirm Account Deletion")
                .setMessage("Please enter your email address to confirm account deletion.\n\n" +
                        "Your email: " + prefsManager.getUserEmail())
                .setView(container)
                .setPositiveButton("DELETE MY ACCOUNT", (dialog, which) -> {
                    String confirmEmail = etEmail.getText().toString().trim();
                    if (confirmEmail.isEmpty()) {
                        Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!confirmEmail.equals(prefsManager.getUserEmail())) {
                        Toast.makeText(this, "Email does not match your account", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    showFinalConfirmation(confirmEmail);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showFinalConfirmation(String confirmEmail) {
        LinearLayout container = createDeleteConfirmationInput();

        new AlertDialog.Builder(this)
                .setTitle("⚠️ FINAL WARNING")
                .setMessage("This is your LAST CHANCE to cancel!\n\n" +
                        "Type 'DELETE' to confirm permanent account deletion.")
                .setView(container)
                .setPositiveButton("CONFIRM DELETION", (dialog, which) -> {
                    EditText etConfirm = container.findViewById(android.R.id.edit);
                    String confirmation = etConfirm.getText().toString();

                    if ("DELETE".equals(confirmation)) {
                        performAccountDeletion(confirmEmail);
                    } else {
                        Toast.makeText(this, "Confirmation text does not match", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private LinearLayout createDeleteConfirmationInput() {
        EditText etConfirm = new EditText(this);
        etConfirm.setId(android.R.id.edit);
        etConfirm.setHint("Type DELETE to confirm");

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(50, 30, 50, 30);
        container.addView(etConfirm);

        return container;
    }

    private void performAccountDeletion(String confirmEmail) {
        // Show loading dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting your account...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Get user ID from preferences
        Long userId = prefsManager.getUserId();
        if (userId == null) {
            progressDialog.dismiss();
            showError("User ID not found. Please login again.");
            return;
        }

        // Call API to delete account với format đúng
        ApiClient.getUserService().deleteAccount(userId, confirmEmail)
                .enqueue(new Callback<StandardResponse<Void>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Void>> call,
                                           Response<StandardResponse<Void>> response) {
                        progressDialog.dismiss();

                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Void> apiResponse = response.body();

                            if (apiResponse.isSuccess()) {
                                handleAccountDeletionSuccess();
                            } else {
                                String errorMsg = apiResponse.getMessage() != null
                                        ? apiResponse.getMessage()
                                        : "Failed to delete account";
                                showError(errorMsg);
                            }
                        } else {
                            handleDeletionError(response);
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Void>> call, Throwable t) {
                        progressDialog.dismiss();
                        showError("Network error. Please check your connection and try again.");
                        Log.e(TAG, "Account deletion failed", t);
                    }
                });
    }

    private void handleDeletionError(Response<StandardResponse<Void>> response) {
        String errorMessage;
        switch (response.code()) {
            case 400:
                errorMessage = "Invalid request. Please check your email.";
                break;
            case 401:
                errorMessage = "Unauthorized. Please login again.";
                break;
            case 403:
                errorMessage = "You don't have permission to delete this account.";
                break;
            case 404:
                errorMessage = "Account not found.";
                break;
            default:
                errorMessage = "Failed to delete account. Error code: " + response.code();
        }
        showError(errorMessage);
    }

    private void handleAccountDeletionSuccess() {
        // Clear all local data
        clearAllAppData();

        // Show success message
        Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_LONG).show();

        // Navigate to login screen
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("account_deleted", true);
        startActivity(intent);
        finish();
    }

    private void clearAllAppData() {
        try {
            // Clear SharedPreferences
            prefsManager.clearUserSession();

            // Clear cache directory
            File cacheDir = getCacheDir();
            if (cacheDir != null && cacheDir.isDirectory()) {
                deleteDir(cacheDir);
            }

            // Clear files directory
            File filesDir = getFilesDir();
            if (filesDir != null && filesDir.isDirectory()) {
                deleteDir(filesDir);
            }

            // Clear databases
            for (String database : databaseList()) {
                deleteDatabase(database);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error clearing app data", e);
        }
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child));
                    if (!success) {
                        return false;
                    }
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    private void showError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
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

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh user info when returning to settings
        displayUserInfo();

        // Update switch states
        switchNotifications.setChecked(prefsManager.isNotificationsEnabled());
        switchLocationServices.setChecked(prefsManager.isLocationEnabled());
    }
}