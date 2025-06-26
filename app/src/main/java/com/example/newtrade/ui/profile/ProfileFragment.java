// app/src/main/java/com/example/newtrade/ui/profile/ProfileFragment.java - TOÀN BỘ
package com.example.newtrade.ui.profile;
import android.provider.MediaStore;
import android.net.Uri;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private static final int REQUEST_GALLERY_PICK = 1001;

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

        // ✅ SỬA LẠI - UNCOMMENT VÀ SỬA:
        ApiClient.getApiService().getUserProfile(userId).enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    userProfile = response.body().getData();
                    displayUserProfile();
                    Log.d(TAG, "✅ User profile loaded from backend");
                } else {
                    Log.e(TAG, "❌ Failed to load user profile");
                    // Still show local data
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ User profile API error", t);
                // Still show local data - don't show error to user
            }
        });
    }



    private void displayCurrentUserInfo() {
        // Display info from SharedPrefs
        String displayName = prefsManager.getUserName();
        String email = prefsManager.getUserEmail();

        if (tvDisplayName != null && displayName != null && !displayName.isEmpty()) {
            tvDisplayName.setText(displayName);
        } else if (tvDisplayName != null) {
            tvDisplayName.setText("User");
        }

        if (tvEmail != null && email != null && !email.isEmpty()) {
            tvEmail.setText(email);
        } else if (tvEmail != null) {
            tvEmail.setText("No email");
        }

        if (tvMemberSince != null) {
            tvMemberSince.setText("Member since 2024");
        }

        // Set mock statistics
        if (tvListingsCount != null) tvListingsCount.setText("0");
        if (tvSoldCount != null) tvSoldCount.setText("0");
        if (tvBoughtCount != null) tvBoughtCount.setText("0");
    }

    private void displayUserProfile() {
        if (userProfile == null) return;

        try {
            // Update UI with backend data
            if (userProfile.containsKey("displayName")) {
                String displayName = userProfile.get("displayName").toString();
                if (tvDisplayName != null) tvDisplayName.setText(displayName);
            }

            if (userProfile.containsKey("email")) {
                String email = userProfile.get("email").toString();
                if (tvEmail != null) tvEmail.setText(email);
            }

            if (userProfile.containsKey("profilePicture")) {
                String profilePicture = userProfile.get("profilePicture").toString();
                if (profilePicture != null && !profilePicture.isEmpty() && ivProfilePicture != null) {
                    Glide.with(this)
                            .load(profilePicture)
                            .placeholder(R.drawable.ic_person)
                            .error(R.drawable.ic_person)
                            .into(ivProfilePicture);
                }
            }

            // Update statistics if available
            if (userProfile.containsKey("stats")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> stats = (Map<String, Object>) userProfile.get("stats");

                if (stats != null) {
                    if (tvListingsCount != null) {
                        tvListingsCount.setText(stats.getOrDefault("totalListings", "0").toString());
                    }
                    if (tvSoldCount != null) {
                        tvSoldCount.setText(stats.getOrDefault("totalSold", "0").toString());
                    }
                    if (tvBoughtCount != null) {
                        tvBoughtCount.setText(stats.getOrDefault("totalBought", "0").toString());
                    }
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error displaying user profile", e);
        }
    }

    // 🔥 FIX: Implement all menu functions
    private void openEditProfile() {
        Log.d(TAG, "Opening Edit Profile");
        Toast.makeText(getContext(), "Edit profile feature coming soon! ✏️", Toast.LENGTH_SHORT).show();
        // TODO: Create EditProfileActivity
        /*
        Intent intent = new Intent(getActivity(), EditProfileActivity.class);
        startActivity(intent);
        */
    }

    private void openMyListings() {
        Log.d(TAG, "Opening My Listings");
        Toast.makeText(getContext(), "My listings feature coming soon! 📋", Toast.LENGTH_SHORT).show();
        // TODO: Create MyListingsActivity
        /*
        Intent intent = new Intent(getActivity(), MyListingsActivity.class);
        startActivity(intent);
        */
    }

    private void openSavedItems() {
        Log.d(TAG, "Opening Saved Items");
        Toast.makeText(getContext(), "Saved items feature coming soon! ❤️", Toast.LENGTH_SHORT).show();
        // TODO: Create SavedItemsActivity
        /*
        Intent intent = new Intent(getActivity(), SavedItemsActivity.class);
        startActivity(intent);
        */
    }

    private void openPurchaseHistory() {
        Log.d(TAG, "Opening Purchase History");
        Toast.makeText(getContext(), "Purchase history feature coming soon! 🛒", Toast.LENGTH_SHORT).show();
        // TODO: Create PurchaseHistoryActivity
        /*
        Intent intent = new Intent(getActivity(), PurchaseHistoryActivity.class);
        startActivity(intent);
        */
    }

    private void openAccountSettings() {
        Log.d(TAG, "Opening Account Settings");
        Toast.makeText(getContext(), "Account settings feature coming soon! ⚙️", Toast.LENGTH_SHORT).show();
        // TODO: Create AccountSettingsActivity
        /*
        Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
        startActivity(intent);
        */
    }

    private void openHelpSupport() {
        Log.d(TAG, "Opening Help & Support");
        showHelpSupportDialog();
    }

    private void openAbout() {
        Log.d(TAG, "Opening About");
        showAboutDialog();
    }

    private void showHelpSupportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Help & Support");
        builder.setMessage("Need help? Contact us:\n\n" +
                "📧 support@tradeup.com\n" +
                "📞 +84 123 456 789\n\n" +
                "Frequently Asked Questions:\n" +
                "• How to sell items?\n" +
                "• How to contact buyers?\n" +
                "• Payment methods\n" +
                "• Account verification");

        builder.setPositiveButton("OK", null);
        builder.setNeutralButton("Contact Support", (dialog, which) -> {
            Toast.makeText(getContext(), "Opening email app... 📧", Toast.LENGTH_SHORT).show();
            // TODO: Open email intent
        });
        builder.show();
    }

    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("About TradeUp");
        builder.setMessage("TradeUp v1.0\n\n" +
                "A platform for buying and selling used items locally.\n\n" +
                "Features:\n" +
                "• Buy & sell secondhand items\n" +
                "• Secure messaging\n" +
                "• Location-based search\n" +
                "• Safe transactions\n\n" +
                "Developed with ❤️ for local communities");

        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private void showProfilePictureOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Update Profile Picture");

        String[] options = {"Choose from Gallery", "Remove Photo"};

        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    openGallery();
                    break;
                case 1:
                    removeProfilePicture();
                    break;
            }
        });

        builder.show();
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_GALLERY_PICK);
    }

    private void removeProfilePicture() {
        if (ivProfilePicture != null) {
            ivProfilePicture.setImageResource(R.drawable.placeholder_avatar);
        }
        Toast.makeText(requireContext(), "Profile picture removed", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_GALLERY_PICK && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null && ivProfilePicture != null) {
                Glide.with(this)
                        .load(selectedImageUri)
                        .circleCrop()
                        .placeholder(R.drawable.placeholder_avatar)
                        .error(R.drawable.placeholder_avatar)
                        .into(ivProfilePicture);

                Toast.makeText(requireContext(), "Profile picture updated!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Sign Out");
        builder.setMessage("Are you sure you want to sign out of your account?");

        builder.setPositiveButton("Sign Out", (dialog, which) -> performLogout());
        builder.setNegativeButton("Cancel", null);

        // Make the positive button red for emphasis
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                getResources().getColor(R.color.error_color, null));
    }

    private void performLogout() {
        try {
            Log.d(TAG, "Performing logout");

            // Clear all user data
            prefsManager.clearUserData();

            // Navigate to login
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            if (getActivity() != null) {
                getActivity().finish();
            }

            Toast.makeText(getContext(), "Signed out successfully", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "✅ Logout successful");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error during logout", e);
            Toast.makeText(getContext(), "Error signing out", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh profile when returning to fragment
        loadUserProfile();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "ProfileFragment destroyed");
    }
}