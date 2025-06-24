// File: app/src/main/java/com/example/newtrade/ui/profile/ProfileFragment.java
package com.example.newtrade.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.auth.LoginActivity;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    // UI Components
    private CircleImageView ivProfilePicture;
    private TextView tvDisplayName, tvEmail, tvMemberSince;
    private TextView tvListingsCount, tvSoldCount, tvBoughtCount;
    private LinearLayout llMyListings, llSavedItems, llPurchaseHistory;
    private LinearLayout llAccountSettings, llHelpSupport, llAbout, llLogout;
    private FloatingActionButton fabEditProfile;

    // Data
    private SharedPrefsManager prefsManager;
    private Map<String, Object> userProfile;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initUtils();
        setupListeners();
        loadUserProfile();

        Log.d(TAG, "ProfileFragment created successfully");
    }

    private void initViews(View view) {
        try {
            ivProfilePicture = view.findViewById(R.id.iv_profile_picture);
            tvDisplayName = view.findViewById(R.id.tv_display_name);
            tvEmail = view.findViewById(R.id.tv_email);
            tvMemberSince = view.findViewById(R.id.tv_member_since);

            tvListingsCount = view.findViewById(R.id.tv_listings_count);
            tvSoldCount = view.findViewById(R.id.tv_sold_count);
            tvBoughtCount = view.findViewById(R.id.tv_bought_count);

            llMyListings = view.findViewById(R.id.ll_my_listings);
            llSavedItems = view.findViewById(R.id.ll_saved_items);
            llPurchaseHistory = view.findViewById(R.id.ll_purchase_history);
            llAccountSettings = view.findViewById(R.id.ll_account_settings);
            llHelpSupport = view.findViewById(R.id.ll_help_support);
            llAbout = view.findViewById(R.id.ll_about);
            llLogout = view.findViewById(R.id.ll_logout);

            fabEditProfile = view.findViewById(R.id.fab_edit_profile);

            Log.d(TAG, "✅ ProfileFragment views initialized");
        } catch (Exception e) {
            Log.w(TAG, "Some ProfileFragment views not found: " + e.getMessage());
        }
    }

    private void initUtils() {
        prefsManager = SharedPrefsManager.getInstance(requireContext());
    }

    private void setupListeners() {
        try {
            // Edit Profile FAB
            if (fabEditProfile != null) {
                fabEditProfile.setOnClickListener(v -> openEditProfile());
            }

            // Menu Items
            if (llMyListings != null) {
                llMyListings.setOnClickListener(v -> openMyListings());
            }

            if (llSavedItems != null) {
                llSavedItems.setOnClickListener(v -> openSavedItems());
            }

            if (llPurchaseHistory != null) {
                llPurchaseHistory.setOnClickListener(v -> openPurchaseHistory());
            }

            if (llAccountSettings != null) {
                llAccountSettings.setOnClickListener(v -> openAccountSettings());
            }

            if (llHelpSupport != null) {
                llHelpSupport.setOnClickListener(v -> openHelpSupport());
            }

            if (llAbout != null) {
                llAbout.setOnClickListener(v -> openAbout());
            }

            if (llLogout != null) {
                llLogout.setOnClickListener(v -> showLogoutDialog());
            }

            // Profile Picture Click
            if (ivProfilePicture != null) {
                ivProfilePicture.setOnClickListener(v -> showProfilePictureOptions());
            }

            Log.d(TAG, "✅ ProfileFragment listeners setup");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error setting up listeners", e);
        }
    }

    private void loadUserProfile() {
        // Display current user info from SharedPrefs first
        displayCurrentUserInfo();

        // Then load from backend
        Long userId = prefsManager.getUserId();
        if (userId == null || userId <= 0) {
            Log.w(TAG, "❌ Invalid user ID, cannot load profile");
            return;
        }

        ApiClient.getApiService().getUserProfile(userId).enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                   Response<StandardResponse<Map<String, Object>>> response) {
                try {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        userProfile = response.body().getData();
                        displayBackendUserProfile();
                        Log.d(TAG, "✅ User profile loaded from backend");
                    } else {
                        Log.w(TAG, "❌ Failed to load user profile from backend");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error parsing user profile", e);
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                Log.e(TAG, "❌ User profile API call failed", t);
            }
        });
    }

    private void displayCurrentUserInfo() {
        // Display from SharedPrefs
        String displayName = prefsManager.getUserName();
        String email = prefsManager.getUserEmail();

        if (tvDisplayName != null && displayName != null) {
            tvDisplayName.setText(displayName);
        }

        if (tvEmail != null && email != null) {
            tvEmail.setText(email);
        }

        if (tvMemberSince != null) {
            tvMemberSince.setText("Member since 2024");
        }

        // Default stats
        if (tvListingsCount != null) tvListingsCount.setText("0");
        if (tvSoldCount != null) tvSoldCount.setText("0");
        if (tvBoughtCount != null) tvBoughtCount.setText("0");
    }

    private void displayBackendUserProfile() {
        try {
            if (userProfile == null) return;

            // Basic info
            String displayName = (String) userProfile.get("displayName");
            if (displayName != null && tvDisplayName != null) {
                tvDisplayName.setText(displayName);
            }

            String email = (String) userProfile.get("email");
            if (email != null && tvEmail != null) {
                tvEmail.setText(email);
            }

            // Profile picture
            String profilePicture = (String) userProfile.get("profilePicture");
            if (profilePicture != null && !profilePicture.isEmpty() && ivProfilePicture != null) {
                Glide.with(this)
                        .load(profilePicture)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .circleCrop()
                        .into(ivProfilePicture);
            }

            // Member since
            String createdAt = (String) userProfile.get("createdAt");
            if (createdAt != null && tvMemberSince != null) {
                try {
                    String year = createdAt.substring(0, 4);
                    tvMemberSince.setText("Member since " + year);
                } catch (Exception e) {
                    tvMemberSince.setText("Member since 2024");
                }
            }

            // Stats
            Object totalTransactionsObj = userProfile.get("totalTransactions");
            if (totalTransactionsObj instanceof Number && tvSoldCount != null) {
                int totalTransactions = ((Number) totalTransactionsObj).intValue();
                tvSoldCount.setText(String.valueOf(totalTransactions));
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error displaying backend user profile", e);
        }
    }

    // Navigation methods - 🔥 SIMPLIFY TO AVOID MISSING ACTIVITIES
    private void openEditProfile() {
        Toast.makeText(getContext(), "Edit profile feature coming soon! ✏️", Toast.LENGTH_SHORT).show();
    }

    private void openMyListings() {
        Toast.makeText(getContext(), "My listings feature coming soon! 📋", Toast.LENGTH_SHORT).show();
    }

    private void openSavedItems() {
        Toast.makeText(getContext(), "Saved items feature coming soon! ❤️", Toast.LENGTH_SHORT).show();
    }

    private void openPurchaseHistory() {
        Toast.makeText(getContext(), "Purchase history feature coming soon! 🛒", Toast.LENGTH_SHORT).show();
    }

    private void openAccountSettings() {
        Toast.makeText(getContext(), "Account settings feature coming soon! ⚙️", Toast.LENGTH_SHORT).show();
    }

    private void openHelpSupport() {
        showHelpSupportDialog();
    }

    private void openAbout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("About TradeUp");
        builder.setMessage("TradeUp v1.0\n\nA platform for buying and selling used items locally.\n\nDeveloped with ❤️");
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private void showHelpSupportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Help & Support");
        builder.setMessage("Need help? Contact us:\n\n📧 support@tradeup.com\n📞 +84 123 456 789\n\nFrequently Asked Questions:\n• How to sell items?\n• How to contact buyers?\n• Payment methods");
        builder.setPositiveButton("OK", null);
        builder.setNeutralButton("Contact Support", (dialog, which) -> {
            Toast.makeText(getContext(), "Opening email app... 📧", Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }

    private void showProfilePictureOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Profile Picture");

        String[] options = {"Change Photo", "Remove Photo", "Cancel"};
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Change Photo
                    Toast.makeText(getContext(), "Change photo feature coming soon! 📸", Toast.LENGTH_SHORT).show();
                    break;
                case 1: // Remove Photo
                    Toast.makeText(getContext(), "Remove photo feature coming soon! 🗑️", Toast.LENGTH_SHORT).show();
                    break;
                case 2: // Cancel
                    dialog.dismiss();
                    break;
            }
        });

        builder.show();
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");

        builder.setPositiveButton("Logout", (dialog, which) -> {
            performLogout();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void performLogout() {
        try {
            // Clear user session
            prefsManager.clearUserSession();

            // Navigate to login
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            if (getActivity() != null) {
                getActivity().finish();
            }

            Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "❌ Error during logout", e);
            Toast.makeText(getContext(), "Logout error", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh profile when returning to fragment
        loadUserProfile();
    }
}