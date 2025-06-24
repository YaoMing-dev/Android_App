// File: app/src/main/java/com/example/newtrade/ui/profile/ProfileFragment.java
package com.example.newtrade.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
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
    private LinearLayout llMyListings, llSavedItems, llPurchaseHistory, llReviews;
    private LinearLayout llAccountSettings, llHelpSupport, llAbout, llLogout;
    private FloatingActionButton fabEditProfile;
    private ImageView ivProfileMenu;

    // Data
    private SharedPrefsManager prefsManager;

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
        loadUserData();
        loadUserStats();

        Log.d(TAG, "ProfileFragment created successfully");
    }

    private void initViews(View view) {
        try {
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
            llReviews = view.findViewById(R.id.ll_reviews);
            llAccountSettings = view.findViewById(R.id.ll_account_settings);
            llHelpSupport = view.findViewById(R.id.ll_help_support);
            llAbout = view.findViewById(R.id.ll_about);
            llLogout = view.findViewById(R.id.ll_logout);

            // Actions
            fabEditProfile = view.findViewById(R.id.fab_edit_profile);
            ivProfileMenu = view.findViewById(R.id.iv_profile_menu);

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
            // 🔥 MY LISTINGS
            if (llMyListings != null) {
                llMyListings.setOnClickListener(v -> {
                    Toast.makeText(getContext(), "Opening My Listings... 📋", Toast.LENGTH_SHORT).show();
                    // TODO: Navigate to MyListingsActivity
                });
            }

            // 🔥 SAVED ITEMS
            if (llSavedItems != null) {
                llSavedItems.setOnClickListener(v -> {
                    Toast.makeText(getContext(), "Opening Saved Items... ❤️", Toast.LENGTH_SHORT).show();
                    // TODO: Navigate to SavedItemsActivity
                });
            }

            // 🔥 PURCHASE HISTORY
            if (llPurchaseHistory != null) {
                llPurchaseHistory.setOnClickListener(v -> {
                    Toast.makeText(getContext(), "Opening Purchase History... 🛒", Toast.LENGTH_SHORT).show();
                    // TODO: Navigate to PurchaseHistoryActivity
                });
            }

            // 🔥 REVIEWS
            if (llReviews != null) {
                llReviews.setOnClickListener(v -> {
                    Toast.makeText(getContext(), "Opening Reviews... ⭐", Toast.LENGTH_SHORT).show();
                    // TODO: Navigate to ReviewsActivity
                });
            }

            // 🔥 ACCOUNT SETTINGS
            if (llAccountSettings != null) {
                llAccountSettings.setOnClickListener(v -> {
                    openAccountSettings();
                });
            }

            // 🔥 HELP & SUPPORT
            if (llHelpSupport != null) {
                llHelpSupport.setOnClickListener(v -> {
                    showHelpAndSupport();
                });
            }

            // 🔥 ABOUT
            if (llAbout != null) {
                llAbout.setOnClickListener(v -> {
                    showAboutDialog();
                });
            }

            // 🔥 LOGOUT
            if (llLogout != null) {
                llLogout.setOnClickListener(v -> {
                    showLogoutConfirmation();
                });
            }

            // 🔥 EDIT PROFILE FAB
            if (fabEditProfile != null) {
                fabEditProfile.setOnClickListener(v -> {
                    Toast.makeText(getContext(), "Edit Profile feature coming soon! ✏️", Toast.LENGTH_SHORT).show();
                    // TODO: Navigate to EditProfileActivity
                });
            }

            // 🔥 PROFILE MENU
            if (ivProfileMenu != null) {
                ivProfileMenu.setOnClickListener(v -> {
                    showProfileMenu(v);
                });
            }

            Log.d(TAG, "✅ ProfileFragment listeners setup");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error setting up listeners", e);
        }
    }

    private void loadUserData() {
        try {
            // Load from SharedPreferences first
            String userName = prefsManager.getUserName();
            String userEmail = prefsManager.getUserEmail();

            if (userName != null) {
                tvDisplayName.setText(userName);
            }

            if (userEmail != null) {
                tvEmail.setText(userEmail);
            }

            // Set member since date (for now use a default)
            tvMemberSince.setText("Member since 2024");

            // TODO: Load profile picture from backend
            if (ivProfilePicture != null) {
                // Default profile picture
                ivProfilePicture.setImageResource(R.drawable.ic_person);
            }

            Log.d(TAG, "✅ User data loaded");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error loading user data", e);
        }
    }

    private void loadUserStats() {
        try {
            // Set default stats for now
            if (tvListingsCount != null) {
                tvListingsCount.setText("0");
            }
            if (tvSoldCount != null) {
                tvSoldCount.setText("0");
            }
            if (tvBoughtCount != null) {
                tvBoughtCount.setText("0");
            }

            // TODO: Load real stats from backend
            loadStatsFromBackend();

            Log.d(TAG, "✅ User stats loaded");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error loading user stats", e);
        }
    }

    private void loadStatsFromBackend() {
        Long userId = prefsManager.getUserId();
        if (userId == null) return;

        // TODO: Implement when user stats API is available
        Log.d(TAG, "📊 Loading stats for user: " + userId);
    }

    private void openAccountSettings() {
        Toast.makeText(getContext(), "Account Settings feature coming soon! ⚙️", Toast.LENGTH_SHORT).show();

        // For now, show a simple dialog with options
        String[] options = {"Change Password", "Notification Settings", "Privacy Settings", "Data Export"};

        new AlertDialog.Builder(requireContext())
                .setTitle("Account Settings")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            Toast.makeText(getContext(), "Change Password coming soon! 🔐", Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            Toast.makeText(getContext(), "Notification Settings coming soon! 🔔", Toast.LENGTH_SHORT).show();
                            break;
                        case 2:
                            Toast.makeText(getContext(), "Privacy Settings coming soon! 🔒", Toast.LENGTH_SHORT).show();
                            break;
                        case 3:
                            Toast.makeText(getContext(), "Data Export coming soon! 📁", Toast.LENGTH_SHORT).show();
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showHelpAndSupport() {
        String[] options = {"Contact Support", "FAQ", "Report a Problem", "Feature Request"};

        new AlertDialog.Builder(requireContext())
                .setTitle("Help & Support")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            Toast.makeText(getContext(), "Contact Support: support@tradeup.com 📧", Toast.LENGTH_LONG).show();
                            break;
                        case 1:
                            Toast.makeText(getContext(), "FAQ coming soon! ❓", Toast.LENGTH_SHORT).show();
                            break;
                        case 2:
                            Toast.makeText(getContext(), "Report Problem feature coming soon! 🐛", Toast.LENGTH_SHORT).show();
                            break;
                        case 3:
                            Toast.makeText(getContext(), "Feature Request coming soon! 💡", Toast.LENGTH_SHORT).show();
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("About TradeUp")
                .setMessage("TradeUp v1.0\n\n" +
                        "Your local marketplace for buying and selling used items.\n\n" +
                        "Built with ❤️ for the community.\n\n" +
                        "© 2024 TradeUp Team")
                .setPositiveButton("OK", null)
                .setNeutralButton("Rate App", (dialog, which) -> {
                    Toast.makeText(getContext(), "Thanks for rating! ⭐", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Sign Out", (dialog, which) -> {
                    performLogout();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        try {
            // Show loading
            Toast.makeText(getContext(), "Signing out... 👋", Toast.LENGTH_SHORT).show();

            // Call logout API if needed
            Long userId = prefsManager.getUserId();
            if (userId != null) {
                ApiClient.getApiService().logout().enqueue(new Callback<StandardResponse<String>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<String>> call, Response<StandardResponse<String>> response) {
                        Log.d(TAG, "✅ Logout API called");
                        completeLogout();
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<String>> call, Throwable t) {
                        Log.w(TAG, "❌ Logout API failed, proceeding anyway");
                        completeLogout();
                    }
                });
            } else {
                completeLogout();
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error during logout", e);
            completeLogout();
        }
    }

    private void completeLogout() {
        try {
            // Clear shared preferences
            prefsManager.clearUserData();

            // Navigate to login
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            if (getActivity() != null) {
                getActivity().finish();
            }

            Log.d(TAG, "✅ Logout completed");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error completing logout", e);
        }
    }

    private void showProfileMenu(View view) {
        try {
            PopupMenu popup = new PopupMenu(getContext(), view);
            popup.getMenuInflater().inflate(R.menu.profile_menu, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.action_settings) {
                    openAccountSettings();
                    return true;
                } else if (itemId == R.id.action_analytics) {
                    Toast.makeText(getContext(), "Analytics feature coming soon! 📊", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.action_export_data) {
                    Toast.makeText(getContext(), "Export Data feature coming soon! 📁", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.action_help) {
                    showHelpAndSupport();
                    return true;
                }
                return false;
            });

            popup.show();
        } catch (Exception e) {
            Log.e(TAG, "❌ Error showing profile menu", e);
            Toast.makeText(getContext(), "Menu temporarily unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh user data when fragment becomes visible
        loadUserData();
        loadUserStats();
    }
}