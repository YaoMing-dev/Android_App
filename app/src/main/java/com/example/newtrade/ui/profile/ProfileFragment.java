// app/src/main/java/com/example/newtrade/ui/profile/ProfileFragment.java
package com.example.newtrade.ui.profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.auth.LoginActivity;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashMap;
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

    // ✅ FIX: Modern Activity Result API
    private ActivityResultLauncher<Intent> editProfileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Log.d(TAG, "✅ Profile updated, refreshing data");
                    loadUserProfile(); // Reload profile data
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
        llPurchaseHistory = view.findViewById(R.id.ll_purchase_history);
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
        // ✅ FIX: Edit Profile FAB with proper error handling
        if (fabEditProfile != null) {
            fabEditProfile.setOnClickListener(v -> {
                try {
                    Log.d(TAG, "🔍 Edit profile button clicked");
                    Intent intent = new Intent(requireContext(), EditProfileActivity.class);
                    editProfileLauncher.launch(intent);
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error opening EditProfileActivity: " + e.getMessage());
                    Toast.makeText(requireContext(), "Error opening edit profile: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

        // My Listings
        if (llMyListings != null) {
            llMyListings.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(requireContext(), MyListingsActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error opening MyListingsActivity: " + e.getMessage());
                    Toast.makeText(requireContext(), "Feature coming soon!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Saved Items
        if (llSavedItems != null) {
            llSavedItems.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(requireContext(), SavedItemsActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error opening SavedItemsActivity: " + e.getMessage());
                    Toast.makeText(requireContext(), "Feature coming soon!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Account Settings
        if (llAccountSettings != null) {
            llAccountSettings.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(requireContext(), SettingsActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error opening SettingsActivity: " + e.getMessage());
                    Toast.makeText(requireContext(), "Feature coming soon!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Logout
        if (llLogout != null) {
            llLogout.setOnClickListener(v -> showLogoutDialog());
        }

        // Other menu items with placeholder actions
        setupPlaceholderListeners();
    }

    private void setupPlaceholderListeners() {
        if (llPurchaseHistory != null) {
            llPurchaseHistory.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "Purchase History - Coming soon!", Toast.LENGTH_SHORT).show());
        }

        if (llHelpSupport != null) {
            llHelpSupport.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "Help & Support - Coming soon!", Toast.LENGTH_SHORT).show());
        }

        if (llAbout != null) {
            llAbout.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "About TradeUp v1.0", Toast.LENGTH_SHORT).show());
        }
    }

    private void loadUserProfile() {
        Log.d(TAG, "📋 Loading user profile...");

        // First load from SharedPrefs
        loadProfileFromPrefs();

        // Then try to load from API
        loadProfileFromAPI();
    }

    private void loadProfileFromPrefs() {
        String userName = prefsManager.getUserName();
        String userEmail = prefsManager.getUserEmail();
        String profilePicture = prefsManager.getUserProfilePicture();

        // Set basic info
        if (tvDisplayName != null) {
            tvDisplayName.setText(userName != null ? userName : "User");
        }

        if (tvEmail != null) {
            tvEmail.setText(userEmail != null ? userEmail : "user@example.com");
        }

        if (tvMemberSince != null) {
            tvMemberSince.setText("Member since 2024");
        }

        // Set profile picture
        if (ivProfilePicture != null) {
            if (profilePicture != null && !profilePicture.isEmpty()) {
                Glide.with(this)
                        .load(profilePicture)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(ivProfilePicture);
            } else {
                ivProfilePicture.setImageResource(R.drawable.ic_person);
            }
        }

        // Set mock stats for now
        updateStatsUI(5, 3, 2); // 5 listings, 3 sold, 2 bought

        Log.d(TAG, "✅ Profile loaded from SharedPrefs");
    }

    private void loadProfileFromAPI() {
        Long userId = prefsManager.getUserId();
        if (userId == null || userId <= 0) {
            Log.w(TAG, "Invalid user ID, skipping API call");
            return;
        }

        Log.d(TAG, "📋 Loading profile from API for user: " + userId);

        // TODO: Implement when API is ready
        // For now, just populate with mock data
        populateMockProfile();

        /*
        ApiClient.getUserService().getCurrentUserProfile()
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                         Response<StandardResponse<Map<String, Object>>> response) {

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Map<String, Object> profile = response.body().getData();
                            populateProfile(profile);
                            Log.d(TAG, "✅ Profile loaded from API");
                        } else {
                            Log.w(TAG, "⚠️ Failed to load profile from API");
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        Log.e(TAG, "❌ Failed to load profile", t);
                    }
                });
        */
    }

    private void populateMockProfile() {
        // Mock data for testing
        updateStatsUI(8, 5, 3); // 8 listings, 5 sold, 3 bought
    }

    private void updateStatsUI(int listings, int sold, int bought) {
        if (tvListingsCount != null) tvListingsCount.setText(String.valueOf(listings));
        if (tvSoldCount != null) tvSoldCount.setText(String.valueOf(sold));
        if (tvBoughtCount != null) tvBoughtCount.setText(String.valueOf(bought));
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    performLogout();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        Log.d(TAG, "🚪 Performing logout...");

        // Clear user data
        prefsManager.clearUserSession();

        // Navigate to login
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finish();
        }

        Log.d(TAG, "✅ Logout completed");
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh profile when fragment becomes visible
        loadProfileFromPrefs();
    }
}