// app/src/main/java/com/example/newtrade/ui/profile/SettingsActivity.java
package com.example.newtrade.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.example.newtrade.R;
import com.example.newtrade.ui.auth.LoginActivity;
import com.example.newtrade.utils.SharedPrefsManager;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings_container, new SettingsFragment())
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        private SharedPrefsManager prefsManager;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);

            prefsManager = new SharedPrefsManager(requireContext());

            setupPreferences();
        }

        private void setupPreferences() {
            // Notifications
            SwitchPreferenceCompat notificationsEnabled = findPreference("notifications_enabled");
            SwitchPreferenceCompat pushNotifications = findPreference("push_notifications");
            SwitchPreferenceCompat emailNotifications = findPreference("email_notifications");
            SwitchPreferenceCompat newMessages = findPreference("notify_new_messages");
            SwitchPreferenceCompat newOffers = findPreference("notify_new_offers");

            // Privacy
            SwitchPreferenceCompat showOnlineStatus = findPreference("show_online_status");
            SwitchPreferenceCompat showPhoneNumber = findPreference("show_phone_number");
            SwitchPreferenceCompat allowLocationTracking = findPreference("allow_location_tracking");

            // Account actions
            Preference changePassword = findPreference("change_password");
            Preference deleteAccount = findPreference("delete_account");
            Preference logout = findPreference("logout");

            // About
            Preference version = findPreference("app_version");
            Preference privacy = findPreference("privacy_policy");
            Preference terms = findPreference("terms_of_service");
            Preference support = findPreference("support");

            // Set up click listeners
            if (changePassword != null) {
                changePassword.setOnPreferenceClickListener(preference -> {
                    openChangePasswordActivity();
                    return true;
                });
            }

            if (deleteAccount != null) {
                deleteAccount.setOnPreferenceClickListener(preference -> {
                    showDeleteAccountDialog();
                    return true;
                });
            }

            if (logout != null) {
                logout.setOnPreferenceClickListener(preference -> {
                    showLogoutDialog();
                    return true;
                });
            }

            if (version != null) {
                try {
                    String versionName = requireContext().getPackageManager()
                            .getPackageInfo(requireContext().getPackageName(), 0).versionName;
                    version.setSummary("Version " + versionName);
                } catch (Exception e) {
                    version.setSummary("Unknown");
                }
            }

            if (privacy != null) {
                privacy.setOnPreferenceClickListener(preference -> {
                    openWebPage("https://yourapp.com/privacy");
                    return true;
                });
            }

            if (terms != null) {
                terms.setOnPreferenceClickListener(preference -> {
                    openWebPage("https://yourapp.com/terms");
                    return true;
                });
            }

            if (support != null) {
                support.setOnPreferenceClickListener(preference -> {
                    openSupportActivity();
                    return true;
                });
            }
        }

        private void openChangePasswordActivity() {
            // TODO: Open change password activity
            Toast.makeText(getContext(), "Change password coming soon", Toast.LENGTH_SHORT).show();
        }

        private void showDeleteAccountDialog() {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        // TODO: Implement account deletion
                        Toast.makeText(getContext(), "Account deletion coming soon", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }

        private void showLogoutDialog() {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Logout", (dialog, which) -> {
                        prefsManager.clearUserData();
                        Intent intent = new Intent(getContext(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        requireActivity().finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }

        private void openWebPage(String url) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(android.net.Uri.parse(url));
            startActivity(intent);
        }

        private void openSupportActivity() {
            // TODO: Open support/help activity
            Toast.makeText(getContext(), "Support coming soon", Toast.LENGTH_SHORT).show();
        }
    }
}