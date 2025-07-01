// app/src/main/java/com/example/newtrade/ui/profile/ProfileFragment.java
package com.example.newtrade.ui.profile;

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
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.models.User;
import com.example.newtrade.ui.auth.LoginActivity;
import com.example.newtrade.ui.product.MyProductsActivity;
import com.example.newtrade.utils.NetworkUtils;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";

    // UI Components
    private CircleImageView ivProfilePicture;
    private TextView tvDisplayName, tvEmail, tvMemberSince, tvListingsCount, tvRating, tvTransactions;
    private MaterialCardView cardStats;
    private LinearLayout llMyProducts, llSettings, llSavedItems, llTransactions, llHelp, llLogout;
    private MaterialButton btnEditProfile;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View loadingView, errorView, contentView;

    // Data
    private User currentUser;
    private SharedPrefsManager prefsManager;
    private Call<StandardResponse<User>> currentApiCall;

    // State
    private boolean isDataLoaded = false;

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
    }

    private void initViews(View view) {
        ivProfilePicture = view.findViewById(R.id.iv_profile_picture);
        tvDisplayName = view.findViewById(R.id.tv_display_name);
        tvEmail = view.findViewById(R.id.tv_email);
        tvMemberSince = view.findViewById(R.id.tv_member_since);
        tvListingsCount = view.findViewById(R.id.tv_listings_count);
        tvRating = view.findViewById(R.id.tv_rating);
        tvTransactions = view.findViewById(R.id.tv_transactions);
        cardStats = view.findViewById(R.id.card_stats);
        llMyProducts = view.findViewById(R.id.ll_my_products);
        llSettings = view.findViewById(R.id.ll_settings);
        llSavedItems = view.findViewById(R.id.ll_saved_items);
        llTransactions = view.findViewById(R.id.ll_transactions);
        llHelp = view.findViewById(R.id.ll_help);
        llLogout = view.findViewById(R.id.ll_logout);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        loadingView = view.findViewById(R.id.view_loading);
        errorView = view.findViewById(R.id.view_error);
        contentView = view.findViewById(R.id.view_content);
    }

    private void initUtils() {
        prefsManager = new SharedPrefsManager(requireContext());
    }

    private void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);

        btnEditProfile.setOnClickListener(v -> openEditProfile());
        llMyProducts.setOnClickListener(v -> openMyProducts());
        llSettings.setOnClickListener(v -> openSettings());
        llSavedItems.setOnClickListener(v -> openSavedItems());
        llTransactions.setOnClickListener(v -> openTransactions());
        llHelp.setOnClickListener(v -> openHelp());
        llLogout.setOnClickListener(v -> showLogoutDialog());
    }

    // FR-1.2.1: Profile includes display name, profile picture, bio, contact info, and rating
    private void loadUserProfile() {
        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            showErrorState("No internet connection");
            return;
        }

        showLoadingState();

        Long userId = prefsManager.getUserId();
        if (userId == null) {
            redirectToLogin();
            return;
        }

        currentApiCall = ApiClient.getUserService().getMyProfile(userId);
        currentApiCall.enqueue(new Callback<StandardResponse<User>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<User>> call,
                                   @NonNull Response<StandardResponse<User>> response) {
                handleProfileResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<User>> call, @NonNull Throwable t) {
                handleProfileError(t);
            }
        });
    }

    public void refreshData() {
        Log.d(TAG, "Refreshing profile data");
        loadUserProfile();
    }

    private void handleProfileResponse(Response<StandardResponse<User>> response) {
        swipeRefreshLayout.setRefreshing(false);

        try {
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                currentUser = response.body().getData();

                if (currentUser != null) {
                    updateUI();
                    loadUserStats();
                    isDataLoaded = true;
                } else {
                    showErrorState("Invalid user data");
                }
            } else {
                String message = response.body() != null ? response.body().getMessage() : "Failed to load profile";
                showErrorState(message);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing profile response", e);
            showErrorState("Error loading profile");
        }
    }

    private void handleProfileError(Throwable t) {
        swipeRefreshLayout.setRefreshing(false);

        Log.e(TAG, "Failed to load profile", t);

        if (t.getMessage() != null && t.getMessage().contains("401")) {
            // Unauthorized - redirect to login
            redirectToLogin();
        } else {
            String errorMessage = NetworkUtils.getNetworkErrorMessage(t);
            showErrorState(errorMessage);
        }
    }

    private void loadUserStats() {
        Long userId = prefsManager.getUserId();
        if (userId == null) return;

        Call<StandardResponse<Map<String, Object>>> call = ApiClient.getUserService().getUserStats(userId);
        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                handleStatsResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to load user stats", t);
                // Don't show error for stats, just log it
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void handleStatsResponse(Response<StandardResponse<Map<String, Object>>> response) {
        try {
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                Map<String, Object> stats = response.body().getData();

                if (stats != null) {
                    updateStatsUI(stats);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing stats response", e);
        }
    }

    private void updateUI() {
        if (currentUser == null) return;

        // Display name
        String displayName = currentUser.getDisplayOrFullName();
        tvDisplayName.setText(displayName);

        // Email
        tvEmail.setText(currentUser.getEmail());

        // Profile picture
        String avatarUrl = currentUser.getProfileImageUrl();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_account_circle)
                    .error(R.drawable.ic_account_circle)
                    .into(ivProfilePicture);
        } else {
            ivProfilePicture.setImageResource(R.drawable.ic_account_circle);
        }

        // Member since
        if (currentUser.getCreatedAt() != null) {
            // TODO: Format date properly
            tvMemberSince.setText("Member since " + currentUser.getCreatedAt().toString());
        } else {
            tvMemberSince.setText("Member since unknown");
        }

        // FR-7.2.1: Profile shows average rating, total transactions
        tvRating.setText(currentUser.getRatingString());
        tvTransactions.setText(currentUser.getTransactionCountText());

        showContentState();
    }

    private void updateStatsUI(Map<String, Object> stats) {
        try {
            // Listings count
            Object listingsCount = stats.get("totalListings");
            if (listingsCount instanceof Number) {
                int count = ((Number) listingsCount).intValue();
                tvListingsCount.setText(String.valueOf(count));
            }

            // Update other stats as needed
            Object activeListings = stats.get("activeListings");
            Object soldListings = stats.get("soldListings");
            Object totalViews = stats.get("totalViews");

            // TODO: Update UI with additional stats if needed

        } catch (Exception e) {
            Log.e(TAG, "Error updating stats UI", e);
        }
    }

    private void openEditProfile() {
        Intent intent = new Intent(requireContext(), EditProfileActivity.class);
        startActivity(intent);
    }

    // FR-2.2.1: View/edit/delete listings from user dashboard
    private void openMyProducts() {
        Intent intent = new Intent(requireContext(), MyProductsActivity.class);
        startActivity(intent);
    }

    private void openSettings() {
        Intent intent = new Intent(requireContext(), SettingsActivity.class);
        startActivity(intent);
    }

    // FR-9.2.1: View saved items, offer history, purchase history
    private void openSavedItems() {
        Intent intent = new Intent(requireContext(), SavedItemsActivity.class);
        startActivity(intent);
    }

    private void openTransactions() {
        Intent intent = new Intent(requireContext(), TransactionsActivity.class);
        startActivity(intent);
    }

    private void openHelp() {
        // TODO: Implement HelpActivity
        Toast.makeText(requireContext(), "Help - Coming soon", Toast.LENGTH_SHORT).show();
    }

    // FR-1.1.5: Logout option accessible via profile
    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        // Call logout API
        Long userId = prefsManager.getUserId();
        if (userId != null) {
            Call<StandardResponse<Void>> call = ApiClient.getAuthService().logout(userId);
            call.enqueue(new Callback<StandardResponse<Void>>() {
                @Override
                public void onResponse(@NonNull Call<StandardResponse<Void>> call,
                                       @NonNull Response<StandardResponse<Void>> response) {
                    // Logout successful or failed, clear local data anyway
                    completeLogout();
                }

                @Override
                public void onFailure(@NonNull Call<StandardResponse<Void>> call, @NonNull Throwable t) {
                    // Even if API call fails, clear local data
                    completeLogout();
                }
            });
        } else {
            completeLogout();
        }
    }

    private void completeLogout() {
        // Clear local data
        prefsManager.logout();
        ApiClient.clearServices();

        // Redirect to login
        redirectToLogin();
    }

    private void redirectToLogin() {
        if (getContext() != null) {
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        }
    }

    private void showLoadingState() {
        loadingView.setVisibility(View.VISIBLE);
        contentView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
    }

    private void showContentState() {
        loadingView.setVisibility(View.GONE);
        contentView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
    }

    private void showErrorState(String message) {
        loadingView.setVisibility(View.GONE);
        contentView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);

        // TODO: Set error message in error view
        Log.e(TAG, "Error state: " + message);

        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh profile when fragment becomes visible
        // This will update any changes made in EditProfile
        if (isDataLoaded) {
            refreshData();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Cancel any pending API call to avoid memory leaks
        if (currentApiCall != null) {
            currentApiCall.cancel();
        }
    }
}