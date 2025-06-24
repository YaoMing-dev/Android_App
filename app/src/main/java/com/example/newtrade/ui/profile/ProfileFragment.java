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
    }

    private void initViews(View view) {
        tvDisplayName = view.findViewById(R.id.tv_display_name);
        tvEmail = view.findViewById(R.id.tv_email);
        tvListingsCount = view.findViewById(R.id.tv_listings_count);
        tvSoldCount = view.findViewById(R.id.tv_sold_count);
        tvBoughtCount = view.findViewById(R.id.tv_bought_count);
        tvMemberSince = view.findViewById(R.id.tv_member_since);

        llMyListings = view.findViewById(R.id.ll_my_listings);
        llSavedItems = view.findViewById(R.id.ll_saved_items);
        llPurchaseHistory = view.findViewById(R.id.ll_purchase_history);
        llReviews = view.findViewById(R.id.ll_reviews);
        llAccountSettings = view.findViewById(R.id.ll_account_settings);
        llHelpSupport = view.findViewById(R.id.ll_help_support);
        llAbout = view.findViewById(R.id.ll_about);
        tvLogout = view.findViewById(R.id.tv_logout);
        fabEditProfile = view.findViewById(R.id.fab_edit_profile);
        ivProfileMenu = view.findViewById(R.id.iv_profile_menu);
    }

    private void initUtils() {
        prefsManager = SharedPrefsManager.getInstance(requireContext());
    }

    private void setupListeners() {
        tvLogout.setOnClickListener(v -> logout());

        ivProfileMenu.setOnClickListener(v -> showProfileMenu(v));

        fabEditProfile.setOnClickListener(v -> {
            // Navigate to edit profile
            Toast.makeText(getContext(), "Edit Profile", Toast.LENGTH_SHORT).show();
        });

        llMyListings.setOnClickListener(v -> {
            // Navigate to my listings
            Toast.makeText(getContext(), "My Listings", Toast.LENGTH_SHORT).show();
        });

        llSavedItems.setOnClickListener(v -> {
            // Navigate to saved items
            Toast.makeText(getContext(), "Saved Items", Toast.LENGTH_SHORT).show();
        });

        llPurchaseHistory.setOnClickListener(v -> {
            // Navigate to purchase history
            Toast.makeText(getContext(), "Purchase History", Toast.LENGTH_SHORT).show();
        });

        llReviews.setOnClickListener(v -> {
            // Navigate to reviews
            Toast.makeText(getContext(), "Reviews", Toast.LENGTH_SHORT).show();
        });

        llAccountSettings.setOnClickListener(v -> {
            // Navigate to settings
            Toast.makeText(getContext(), "Account Settings", Toast.LENGTH_SHORT).show();
        });

        llHelpSupport.setOnClickListener(v -> {
            // Navigate to help
            Toast.makeText(getContext(), "Help & Support", Toast.LENGTH_SHORT).show();
        });

        llAbout.setOnClickListener(v -> {
            // Navigate to about
            Toast.makeText(getContext(), "About", Toast.LENGTH_SHORT).show();
        });
    }

    private void showProfileMenu(View anchor) {
        PopupMenu popup = new PopupMenu(getContext(), anchor);
        popup.getMenuInflater().inflate(R.menu.profile_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_settings) {
                Toast.makeText(getContext(), "Settings", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.action_analytics) {
                Toast.makeText(getContext(), "Analytics", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.action_export_data) {
                Toast.makeText(getContext(), "Export Data", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.action_help) {
                Toast.makeText(getContext(), "Help", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void loadUserData() {
        if (prefsManager.isLoggedIn()) {
            tvDisplayName.setText(prefsManager.getUserName());
            tvEmail.setText(prefsManager.getUserEmail());
        }

        // Load full profile from API
        ApiClient.getUserService().getCurrentUserProfile()
                .enqueue(new Callback<StandardResponse<User>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<User>> call,
                                           Response<StandardResponse<User>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            User user = response.body().getData();
                            updateProfileUI(user);
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<User>> call, Throwable t) {
                        Log.e(TAG, "Failed to load profile: " + t.getMessage());
                    }
                });
    }

    private void loadUserStats() {
        ApiClient.getUserService().getUserStats()
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Map<String, Object> stats = response.body().getData();
                            updateStatsUI(stats);
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        Log.e(TAG, "Failed to load stats: " + t.getMessage());
                    }
                });
    }

    private void updateProfileUI(User user) {
        tvDisplayName.setText(user.getDisplayName());
        tvEmail.setText(user.getEmail());
        tvMemberSince.setText("Member since " + user.getFormattedJoinDate());
    }

    private void updateStatsUI(Map<String, Object> stats) {
        tvListingsCount.setText(String.valueOf(stats.get("listings")));
        tvSoldCount.setText(String.valueOf(stats.get("sold")));
        tvBoughtCount.setText(String.valueOf(stats.get("bought")));
    }

    private void logout() {
        // Call logout API
        ApiClient.getAuthService().logout()
                .enqueue(new Callback<StandardResponse<Map<String, String>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, String>>> call,
                                           Response<StandardResponse<Map<String, String>>> response) {
                        performLogout();
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, String>>> call, Throwable t) {
                        // Logout anyway
                        performLogout();
                    }
                });
    }

    private void performLogout() {
        prefsManager.clearUserSession();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}