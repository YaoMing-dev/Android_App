// app/src/main/java/com/example/newtrade/ui/profile/ProfileFragment.java
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
import androidx.fragment.app.Fragment;

import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.models.User;
import com.example.newtrade.ui.auth.LoginActivity;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    // UI Components
    private TextView tvDisplayName;
    private TextView tvEmail;
    private TextView tvListingsCount;
    private TextView tvSoldCount;
    private TextView tvBoughtCount;
    private TextView tvMemberSince;
    private LinearLayout llMyListings;
    private LinearLayout llSavedItems;
    private LinearLayout llPurchaseHistory;
    private LinearLayout llReviews;
    private LinearLayout llAccountSettings;
    private LinearLayout llHelpSupport;
    private LinearLayout llAbout;
    private LinearLayout llLogout;
    private TextView tvLogout;
    private FloatingActionButton fabEditProfile;
    private ImageView ivProfileMenu;

    // Utils
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
            // Text views for user info
            tvDisplayName = view.findViewById(R.id.tv_display_name);
            tvEmail = view.findViewById(R.id.tv_email);
            tvListingsCount = view.findViewById(R.id.tv_listings_count);
            tvSoldCount = view.findViewById(R.id.tv_sold_count);
            tvBoughtCount = view.findViewById(R.id.tv_bought_count);
            tvMemberSince = view.findViewById(R.id.tv_member_since);

            // Menu items
            llMyListings = view.findViewById(R.id.ll_my_listings);
            llSavedItems = view.findViewById(R.id.ll_saved_items);
            llPurchaseHistory = view.findViewById(R.id.ll_purchase_history);
            llReviews = view.findViewById(R.id.ll_reviews);
            llAccountSettings = view.findViewById(R.id.ll_account_settings);
            llHelpSupport = view.findViewById(R.id.ll_help_support);
            llAbout = view.findViewById(R.id.ll_about);

            // Logout - can be both LinearLayout and TextView
            llLogout = view.findViewById(R.id.ll_logout);
            tvLogout = view.findViewById(R.id.tv_logout);

            // FAB and menu
            fabEditProfile = view.findViewById(R.id.fab_edit_profile);
            ivProfileMenu = view.findViewById(R.id.iv_profile_menu);

            Log.d(TAG, "✅ All ProfileFragment views initialized successfully");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error initializing ProfileFragment views", e);
            Toast.makeText(getContext(), "Layout initialization error", Toast.LENGTH_SHORT).show();
        }
    }

    private void initUtils() {
        prefsManager = SharedPrefsManager.getInstance(requireContext());
    }

    private void setupListeners() {
        // Logout click listener
        if (llLogout != null) {
            llLogout.setOnClickListener(v -> logout());
        }
        if (tvLogout != null) {
            tvLogout.setOnClickListener(v -> logout());
        }

        // Profile menu
        if (ivProfileMenu != null) {
            ivProfileMenu.setOnClickListener(v -> showProfileMenu(v));
        }

        // Edit profile FAB
        if (fabEditProfile != null) {
            fabEditProfile.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Edit Profile feature coming soon", Toast.LENGTH_SHORT).show();
            });
        }

        // Menu items
        if (llMyListings != null) {
            llMyListings.setOnClickListener(v -> {
                Toast.makeText(getContext(), "My Listings feature coming soon", Toast.LENGTH_SHORT).show();
            });
        }

        if (llSavedItems != null) {
            llSavedItems.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Saved Items feature coming soon", Toast.LENGTH_SHORT).show();
            });
        }

        if (llPurchaseHistory != null) {
            llPurchaseHistory.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Purchase History feature coming soon", Toast.LENGTH_SHORT).show();
            });
        }

        if (llReviews != null) {
            llReviews.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Reviews feature coming soon", Toast.LENGTH_SHORT).show();
            });
        }

        if (llAccountSettings != null) {
            llAccountSettings.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Account Settings feature coming soon", Toast.LENGTH_SHORT).show();
            });
        }

        if (llHelpSupport != null) {
            llHelpSupport.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Help & Support feature coming soon", Toast.LENGTH_SHORT).show();
            });
        }

        if (llAbout != null) {
            llAbout.setOnClickListener(v -> {
                Toast.makeText(getContext(), "About feature coming soon", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void loadUserData() {
        try {
            // Load from SharedPrefs first
            String displayName = prefsManager.getUserName();
            String email = prefsManager.getUserEmail();

            if (tvDisplayName != null && displayName != null) {
                tvDisplayName.setText(displayName);
            }

            if (tvEmail != null && email != null) {
                tvEmail.setText(email);
            }

            // Show member since date
            if (tvMemberSince != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
                tvMemberSince.setText("Member since " + sdf.format(new Date()));
            }

            Log.d(TAG, "✅ User data loaded from SharedPrefs");

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

            Log.d(TAG, "✅ User stats loaded");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error loading user stats", e);
        }
    }

    private void showProfileMenu(View anchor) {
        try {
            PopupMenu popup = new PopupMenu(requireContext(), anchor);
            popup.getMenuInflater().inflate(R.menu.profile_menu, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.action_settings) {
                    Toast.makeText(getContext(), "Settings", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.action_help) {
                    Toast.makeText(getContext(), "Help", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });

            popup.show();
        } catch (Exception e) {
            Log.e(TAG, "❌ Error showing profile menu", e);
        }
    }

    private void logout() {
        try {
            // Clear all user data
            prefsManager.clearUserData();

            // Navigate to login
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();

            Log.d(TAG, "✅ User logged out successfully");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error during logout", e);
            Toast.makeText(getContext(), "Logout failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
        loadUserStats();
    }
}