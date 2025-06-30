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
import com.example.newtrade.utils.NetworkUtils;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";

    // UI Components
    private CircleImageView ivProfilePicture;
    private TextView tvDisplayName, tvEmail, tvMemberSince, tvListingsCount, tvRating;
    private LinearLayout llMyProducts, llSettings, llSavedItems, llTransactions, llHelp, llLogout;
    private FloatingActionButton fabEditProfile;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View loadingView, errorView, contentView;

    // Data
    private User currentUser;
    private Call<StandardResponse<User>> currentApiCall;

    // Utils
    private SharedPrefsManager prefsManager;
    private boolean isDataLoaded = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefsManager = new SharedPrefsManager(requireContext());

        initViews(view);
        setupListeners();

        // Only load data if it hasn't been loaded before or if user is null
        if (!isDataLoaded || currentUser == null) {
            loadUserProfile();
        } else {
            // Use existing data if already loaded
            updateUI();
        }
    }

    private void initViews(View view) {
        // Finding views safely with null checks
        ivProfilePicture = view.findViewById(R.id.iv_profile_picture);
        tvDisplayName = view.findViewById(R.id.tv_display_name);
        tvEmail = view.findViewById(R.id.tv_email);
        tvMemberSince = view.findViewById(R.id.tv_member_since);
        tvRating = findViewSafely(view, R.id.tv_rating);  // May not exist in some layouts
        tvListingsCount = findViewSafely(view, R.id.tv_listings_count);  // May not exist

        // Finding navigation items with null checks
        llMyProducts = findViewSafely(view, R.id.ll_my_products);
        llSettings = findViewSafely(view, R.id.ll_settings);
        llSavedItems = findViewSafely(view, R.id.ll_saved_items);
        llTransactions = findViewSafely(view, R.id.ll_transactions);
        llHelp = findViewSafely(view, R.id.ll_help);
        llLogout = findViewSafely(view, R.id.ll_logout);

        fabEditProfile = findViewSafely(view, R.id.fab_edit_profile);

        // Setup swipe refresh if exists
        swipeRefreshLayout = findViewSafely(view, R.id.swipe_refresh_layout);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeResources(R.color.primary_color);
        }

        // State views
        loadingView = findViewSafely(view, R.id.loading_view);
        errorView = findViewSafely(view, R.id.error_view);
        contentView = findViewSafely(view, R.id.content_view);
    }

    /**
     * Safely find a view without crashing if the ID doesn't exist
     */
    @SuppressWarnings("unchecked")
    private <T extends View> T findViewSafely(View parent, int id) {
        try {
            return (T) parent.findViewById(id);
        } catch (Exception e) {
            Log.w(TAG, "View with ID " + id + " not found");
            return null;
        }
    }

    private void setupListeners() {
        if (fabEditProfile != null) {
            fabEditProfile.setOnClickListener(v -> openEditProfile());
        }

        setupClickListenerSafely(llMyProducts, v -> openMyProducts());
        setupClickListenerSafely(llSettings, v -> openSettings());
        setupClickListenerSafely(llSavedItems, v -> openSavedItems());
        setupClickListenerSafely(llTransactions, v -> openTransactions());
        setupClickListenerSafely(llHelp, v -> openHelp());
        setupClickListenerSafely(llLogout, v -> performLogout());

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::refreshData);
        }

        // Add retry button if error view exists
        View btnRetry = errorView != null ? errorView.findViewById(R.id.btn_retry) : null;
        if (btnRetry != null) {
            btnRetry.setOnClickListener(v -> refreshData());
        }
    }

    /**
     * Set click listener only if view is not null
     */
    private void setupClickListenerSafely(View view, View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    private void showLoading() {
        if (swipeRefreshLayout != null && !swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
        }

        if (loadingView != null) loadingView.setVisibility(View.VISIBLE);
        if (errorView != null) errorView.setVisibility(View.GONE);
        if (contentView != null) contentView.setVisibility(View.GONE);
    }

    private void showContent() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }

        if (loadingView != null) loadingView.setVisibility(View.GONE);
        if (errorView != null) errorView.setVisibility(View.GONE);
        if (contentView != null) contentView.setVisibility(View.VISIBLE);
    }

    private void showError() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }

        if (loadingView != null) loadingView.setVisibility(View.GONE);
        if (errorView != null) errorView.setVisibility(View.VISIBLE);

        // Keep content visible if we already have data
        if (contentView != null) {
            contentView.setVisibility(currentUser != null ? View.VISIBLE : View.GONE);
        }
    }

    private void loadUserProfile() {
        // Cancel any ongoing requests to avoid duplicate calls
        if (currentApiCall != null && !currentApiCall.isCanceled()) {
            currentApiCall.cancel();
        }

        Long userId = prefsManager.getUserId();
        if (userId == null) {
            redirectToLogin();
            return;
        }

        // Check network connectivity first
        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            showError();
            showNetworkErrorSnackbar();
            return;
        }

        showLoading();

        currentApiCall = ApiClient.getUserService().getMyProfile(userId);
        currentApiCall.enqueue(new Callback<StandardResponse<User>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<User>> call,
                                   @NonNull Response<StandardResponse<User>> response) {
                if (isAdded() && getContext() != null) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        currentUser = response.body().getData();
                        isDataLoaded = true;
                        showContent();
                        updateUI();
                    } else {
                        Log.e(TAG, "Failed to load profile: " + response.message());

                        // Check for specific HTTP errors
                        if (response.code() == 401) {
                            // Unauthorized - token expired or invalid
                            prefsManager.logout();
                            redirectToLogin();
                            return;
                        }

                        showError();
                        if (getView() != null) {
                            Snackbar.make(getView(), "Failed to load profile", Snackbar.LENGTH_LONG)
                                    .setAction("Retry", v -> refreshData())
                                    .show();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<User>> call, @NonNull Throwable t) {
                if (isAdded() && getContext() != null && !call.isCanceled()) {
                    Log.e(TAG, "Error loading profile", t);
                    showError();
                    if (getView() != null) {
                        Snackbar.make(getView(), "Error loading profile: " + t.getMessage(),
                                Snackbar.LENGTH_LONG)
                                .setAction("Retry", v -> refreshData())
                                .show();
                    }
                }
            }
        });
    }

    private void showNetworkErrorSnackbar() {
        if (getView() != null) {
            Snackbar.make(getView(), "No internet connection", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Retry", v -> refreshData())
                    .show();
        }
    }

    private void updateUI() {
        if (currentUser == null) return;

        // User basic info
        if (tvDisplayName != null) {
            tvDisplayName.setText(currentUser.getDisplayNameOrFullName());
        }

        if (tvEmail != null) {
            tvEmail.setText(currentUser.getEmail());
        }

        if (tvMemberSince != null && currentUser.getCreatedAt() != null) {
            try {
                String year = currentUser.getCreatedAt().substring(0, 4);
                tvMemberSince.setText(getString(R.string.joined_year, year));
            } catch (StringIndexOutOfBoundsException e) {
                Log.e(TAG, "Invalid date format", e);
                tvMemberSince.setText(getString(R.string.member_since, "Unknown"));
            }
        }

        // Rating
        if (tvRating != null) {
            if (currentUser.getRating() != null) {
                tvRating.setText(getString(R.string.rating_format, currentUser.getFormattedRating()));
                tvRating.setVisibility(View.VISIBLE);
            } else {
                tvRating.setVisibility(View.GONE);
            }
        }

        // Stats (placeholder - you can implement these)
        if (tvListingsCount != null) {
            tvListingsCount.setText("0");
        }

        // User avatar
        if (ivProfilePicture != null) {
            String avatarUrl = currentUser.getAvatarUrlOrDefault();
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Glide.with(this)
                        .load(avatarUrl)
                        .placeholder(R.drawable.ic_person_placeholder)
                        .error(R.drawable.ic_person_placeholder)
                        .circleCrop()
                        .into(ivProfilePicture);
            } else {
                ivProfilePicture.setImageResource(R.drawable.ic_person_placeholder);
            }
        }
    }

    public void refreshData() {
        Log.d(TAG, "Refreshing profile data");
        loadUserProfile();
    }

    private void openEditProfile() {
        if (getContext() != null) {
            Intent intent = new Intent(getContext(), EditProfileActivity.class);
            startActivity(intent);
        }
    }

    private void openMyProducts() {
        if (getContext() != null) {
            // TODO: Implement MyProductsActivity
            Toast.makeText(getContext(), "My Products - Coming soon", Toast.LENGTH_SHORT).show();
        }
    }

    private void openSettings() {
        if (getContext() != null) {
            Intent intent = new Intent(getContext(), SettingsActivity.class);
            startActivity(intent);
        }
    }

    private void openSavedItems() {
        if (getContext() != null) {
            Intent intent = new Intent(getContext(), SavedItemsActivity.class);
            startActivity(intent);
        }
    }

    private void openTransactions() {
        if (getContext() != null) {
            Intent intent = new Intent(getContext(), TransactionsActivity.class);
            startActivity(intent);
        }
    }

    private void openHelp() {
        if (getContext() != null) {
            // TODO: Implement HelpActivity
            Toast.makeText(getContext(), "Help - Coming soon", Toast.LENGTH_SHORT).show();
        }
    }

    private void performLogout() {
        prefsManager.logout();
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

    @Override
    public void onResume() {
        super.onResume();
        // Only refresh if we need to update data (e.g., after edit profile)
        if (currentUser != null && isDataLoaded) {
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