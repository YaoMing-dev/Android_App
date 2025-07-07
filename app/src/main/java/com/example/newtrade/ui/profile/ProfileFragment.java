// app/src/main/java/com/example/newtrade/ui/profile/ProfileFragment.java
package com.example.newtrade.ui.profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.auth.LoginActivity;
import com.example.newtrade.ui.review.ReviewListActivity;
import com.example.newtrade.ui.transaction.TransactionHistoryActivity;
import com.example.newtrade.ui.profile.SavedItemsActivity;
import com.example.newtrade.ui.profile.MyListingsActivity;        // ✅ THÊM IMPORT NÀY
import com.example.newtrade.utils.ImageUtils;
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
    private LinearLayout llMyListings, llSavedItems, llTransactionHistory;
    private LinearLayout llReviews, llAccountSettings, llHelpSupport, llAbout, llLogout;
    private FloatingActionButton fabEditProfile;

    // Data
    private SharedPrefsManager prefsManager;
    private Map<String, Object> userProfile;

    // Activity Result API
    private ActivityResultLauncher<Intent> editProfileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Log.d(TAG, "✅ Profile updated, refreshing data");
                    loadUserProfile();
                    Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initData();
        setupListeners();
        loadUserProfile();

        Log.d(TAG, "✅ ProfileFragment created");
    }

    private void initViews(View view) {
        // Profile info
        ivProfilePicture = view.findViewById(R.id.iv_profile_picture);
        tvDisplayName = view.findViewById(R.id.tv_display_name);
        tvEmail = view.findViewById(R.id.tv_email);
        tvMemberSince = view.findViewById(R.id.tv_member_since);

        // Stats
        tvListingsCount = view.findViewById(R.id.tv_listings_count);
        tvSoldCount = view.findViewById(R.id.tv_sold_count);
        tvBoughtCount = view.findViewById(R.id.tv_bought_count);

        // Menu items
        llMyListings = view.findViewById(R.id.ll_my_listings);
        llSavedItems = view.findViewById(R.id.ll_saved_items);
        llTransactionHistory = view.findViewById(R.id.ll_transaction_history);
        llReviews = view.findViewById(R.id.ll_reviews);
        llAccountSettings = view.findViewById(R.id.ll_account_settings);
        llHelpSupport = view.findViewById(R.id.ll_help_support);
        llAbout = view.findViewById(R.id.ll_about);
        llLogout = view.findViewById(R.id.ll_logout);

        // FAB
        fabEditProfile = view.findViewById(R.id.fab_edit_profile);
    }

    private void initData() {
        prefsManager = SharedPrefsManager.getInstance(requireContext());
    }

    private void setupListeners() {
        // Edit Profile FAB
        if (fabEditProfile != null) {
            fabEditProfile.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(requireContext(), EditProfileActivity.class);
                    editProfileLauncher.launch(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error launching edit profile", e);
                    Toast.makeText(requireContext(), "Unable to open edit profile", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Menu item listeners
        if (llMyListings != null) {
            llMyListings.setOnClickListener(v -> openMyListings());
        }

        if (llSavedItems != null) {
            llSavedItems.setOnClickListener(v -> openSavedItems());
        }

        if (llTransactionHistory != null) {
            llTransactionHistory.setOnClickListener(v -> openTransactionHistory());
        }

        if (llReviews != null) {
            llReviews.setOnClickListener(v -> openReviews());
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
    }

    private void loadUserProfile() {
        try {
            // Load basic info from SharedPrefs
            displayBasicProfile();

            // Load detailed profile from server
            loadProfileFromServer();

        } catch (Exception e) {
            Log.e(TAG, "Error loading user profile", e);
        }
    }

    private void displayBasicProfile() {
        try {
            String displayName = prefsManager.getUserName();
            String email = prefsManager.getUserEmail();
            String profilePicture = prefsManager.getUserProfilePicture();

            Log.d(TAG, "🔍 displayBasicProfile called");
            Log.d(TAG, "  - Display name: " + displayName);
            Log.d(TAG, "  - Email: " + email);
            Log.d(TAG, "  - Profile picture URL: " + profilePicture);

            // Display name and email
            if (tvDisplayName != null) {
                tvDisplayName.setText(displayName != null && !displayName.isEmpty() ? displayName : "User");
            }

            if (tvEmail != null) {
                tvEmail.setText(email != null && !email.isEmpty() ? email : "No email");
            }

            // Load profile picture
            if (ivProfilePicture != null) {
                Log.d(TAG, "🔍 About to call ImageUtils.loadAvatarImage()");
                ImageUtils.loadAvatarImage(requireContext(), profilePicture, ivProfilePicture);
            } else {
                Log.e(TAG, "❌ ivProfilePicture is null!");
            }

            Log.d(TAG, "✅ displayBasicProfile completed");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error in displayBasicProfile", e);
        }
    }

    private void loadProfileFromServer() {
        ApiClient.getApiService().getCurrentUserProfile()
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {

                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, Object>> standardResponse = response.body();

                            if (standardResponse.isSuccess()) {
                                updateProfileFromServer(standardResponse.getData());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        Log.e(TAG, "Failed to load profile from server", t);
                    }
                });
    }

    private void updateProfileFromServer(Map<String, Object> profile) {
        try {
            // Update stats
            if (tvListingsCount != null && profile.get("totalListings") != null) {
                tvListingsCount.setText(profile.get("totalListings").toString());
            }

            if (tvSoldCount != null && profile.get("totalSold") != null) {
                tvSoldCount.setText(profile.get("totalSold").toString());
            }

            if (tvBoughtCount != null && profile.get("totalBought") != null) {
                tvBoughtCount.setText(profile.get("totalBought").toString());
            }

            // Update member since
            if (tvMemberSince != null && profile.get("createdAt") != null) {
                String createdAt = profile.get("createdAt").toString();
                tvMemberSince.setText("Member since " + createdAt.substring(0, 4));
            }

            // Update profile picture if different from SharedPrefs
            if (profile.get("profilePicture") != null) {
                String serverProfilePicture = profile.get("profilePicture").toString();
                String localProfilePicture = prefsManager.getUserProfilePicture();

                Log.d(TAG, "🔍 Profile picture update check:");
                Log.d(TAG, "  - Server URL: " + serverProfilePicture);
                Log.d(TAG, "  - Local URL: " + localProfilePicture);

                // If server has different URL, update local and reload image
                if (!serverProfilePicture.equals(localProfilePicture)) {
                    Log.d(TAG, "🔄 Avatar URLs different, updating...");
                    prefsManager.updateProfilePicture(serverProfilePicture);

                    if (ivProfilePicture != null) {
                        Log.d(TAG, "🔍 Calling ImageUtils.loadAvatarImage() with fresh server URL");
                        ImageUtils.loadAvatarImage(requireContext(), serverProfilePicture, ivProfilePicture);
                    }

                    Log.d(TAG, "✅ Updated avatar from server: " + serverProfilePicture);
                } else {
                    Log.d(TAG, "✅ Avatar URLs same, no update needed");
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error updating profile from server", e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "🔄 onResume() called, refreshing profile");
        displayBasicProfile();
    }

    // ===== MENU NAVIGATION METHODS =====

    // ✅ CẬP NHẬT: Navigate tới MyListingsActivity thay vì hiển thị toast
    private void openMyListings() {
        try {
            Intent intent = new Intent(requireContext(), MyListingsActivity.class);
            startActivity(intent);
            Log.d(TAG, "✅ Navigating to MyListingsActivity");
        } catch (Exception e) {
            Log.e(TAG, "Error opening my listings", e);
            Toast.makeText(requireContext(), "Unable to open my listings", Toast.LENGTH_SHORT).show();
        }
    }

    private void openSavedItems() {
        try {
            Intent intent = new Intent(requireContext(), SavedItemsActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening saved items", e);
            Toast.makeText(requireContext(), "Unable to open saved items", Toast.LENGTH_SHORT).show();
        }
    }

    private void openTransactionHistory() {
        try {
            Intent intent = new Intent(requireContext(), TransactionHistoryActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening transaction history", e);
            Toast.makeText(requireContext(), "Unable to open transaction history", Toast.LENGTH_SHORT).show();
        }
    }

    private void openReviews() {
        try {
            Intent intent = new Intent(requireContext(), ReviewListActivity.class);
            // Pass current user ID and name
            Long currentUserId = prefsManager.getUserId();
            String currentUserName = prefsManager.getUserName();

            intent.putExtra(ReviewListActivity.EXTRA_USER_ID, currentUserId);
            intent.putExtra(ReviewListActivity.EXTRA_USER_NAME, currentUserName);

            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening reviews", e);
            Toast.makeText(requireContext(), "Unable to open reviews", Toast.LENGTH_SHORT).show();
        }
    }

    private void openAccountSettings() {
        try {
            Intent intent = new Intent(requireContext(), SettingsActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening account settings", e);
            Toast.makeText(requireContext(), "Unable to open settings", Toast.LENGTH_SHORT).show();
        }
    }

    private void openHelpSupport() {
        showSimpleDialog("Help & Support", "For support, please contact us at support@tradeup.com");
    }

    private void openAbout() {
        showSimpleDialog("About TradeUp", "TradeUp v1.0\nA marketplace for buying and selling used items locally.");
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        try {
            // Clear user data
            prefsManager.clearUserSession();

            // Navigate to login
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            requireActivity().finish();

            Log.d(TAG, "✅ User logged out successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error during logout", e);
            Toast.makeText(requireContext(), "Error during logout", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSimpleDialog(String title, String message) {
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}