// app/src/main/java/com/example/newtrade/ui/profile/ProfileFragment.java
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
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.models.User;
import com.example.newtrade.ui.auth.LoginActivity;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";

    // UI Components
    private ImageView ivAvatar;
    private TextView tvName, tvEmail, tvJoinDate, tvRating, tvProductsCount, tvSalesCount;
    private MaterialCardView cardMyProducts, cardSettings, cardSavedItems, cardTransactions, cardHelp;
    private LinearLayout layoutStats, llLogout;
    private FloatingActionButton fabEditProfile;

    // Data
    private User currentUser;

    // Utils
    private SharedPrefsManager prefsManager;

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
        loadUserProfile();
    }

    private void initViews(View view) {
        ivAvatar = view.findViewById(R.id.iv_avatar);
        tvName = view.findViewById(R.id.tv_name);
        tvEmail = view.findViewById(R.id.tv_email);
        tvJoinDate = view.findViewById(R.id.tv_join_date);
        tvRating = view.findViewById(R.id.tv_rating);
        tvProductsCount = view.findViewById(R.id.tv_products_count);
        tvSalesCount = view.findViewById(R.id.tv_sales_count);

        cardMyProducts = view.findViewById(R.id.ll_my_products);
        cardSettings = view.findViewById(R.id.ll_settings);
        cardSavedItems = view.findViewById(R.id.ll_saved_items);
        cardTransactions = view.findViewById(R.id.ll_transactions);
        cardHelp = view.findViewById(R.id.ll_help);
        llLogout = view.findViewById(R.id.ll_logout);

        layoutStats = view.findViewById(R.id.layout_stats);
        fabEditProfile = view.findViewById(R.id.fab_edit_profile);
    }

    private void setupListeners() {
        fabEditProfile.setOnClickListener(v -> openEditProfile());
        cardMyProducts.setOnClickListener(v -> openMyProducts());
        cardSettings.setOnClickListener(v -> openSettings());
        cardSavedItems.setOnClickListener(v -> openSavedItems());
        cardTransactions.setOnClickListener(v -> openTransactions());
        cardHelp.setOnClickListener(v -> openHelp());
        llLogout.setOnClickListener(v -> performLogout());
    }

    private void loadUserProfile() {
        Long userId = prefsManager.getUserId();
        if (userId == null) {
            redirectToLogin();
            return;
        }

        Call<StandardResponse<User>> call = ApiClient.getUserService().getMyProfile(userId);
        call.enqueue(new Callback<StandardResponse<User>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<User>> call,
                                   @NonNull Response<StandardResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    currentUser = response.body().getData();
                    updateUI();
                } else {
                    Log.e(TAG, "Failed to load profile: " + response.message());
                    Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<User>> call, @NonNull Throwable t) {
                Log.e(TAG, "Error loading profile", t);
                Toast.makeText(getContext(), "Error loading profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        if (currentUser == null) return;

        // User basic info
        tvName.setText(currentUser.getDisplayNameOrFullName());
        tvEmail.setText(currentUser.getEmail());

        if (currentUser.getCreatedAt() != null) {
            // Simple date formatting - you can improve this
            String year = currentUser.getCreatedAt().substring(0, 4);
            tvJoinDate.setText("Joined " + year);
        }

        // Rating
        if (currentUser.getRating() != null) {
            tvRating.setText("⭐ " + currentUser.getFormattedRating());
            tvRating.setVisibility(View.VISIBLE);
        } else {
            tvRating.setVisibility(View.GONE);
        }

        // Stats (placeholder - you can implement these)
        tvProductsCount.setText("0");
        tvSalesCount.setText("0");

        // User avatar
        String avatarUrl = currentUser.getAvatarUrlOrDefault();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_person_placeholder)
                    .error(R.drawable.ic_person_placeholder)
                    .circleCrop()
                    .into(ivAvatar);
        } else {
            ivAvatar.setImageResource(R.drawable.ic_person_placeholder);
        }
    }

    // ✅ ADD: Missing refreshData method
    public void refreshData() {
        Log.d(TAG, "Refreshing profile data");
        loadUserProfile();
    }

    private void openEditProfile() {
        Intent intent = new Intent(getContext(), EditProfileActivity.class);
        startActivity(intent);
    }

    private void openMyProducts() {
        // TODO: Implement MyProductsActivity
        Toast.makeText(getContext(), "My Products - Coming soon", Toast.LENGTH_SHORT).show();
    }

    private void openSettings() {
        // TODO: Implement SettingsActivity
        Toast.makeText(getContext(), "Settings - Coming soon", Toast.LENGTH_SHORT).show();
    }

    private void openSavedItems() {
        // TODO: Implement SavedItemsActivity
        Toast.makeText(getContext(), "Saved Items - Coming soon", Toast.LENGTH_SHORT).show();
    }

    private void openTransactions() {
        // TODO: Implement TransactionsActivity
        Toast.makeText(getContext(), "Transactions - Coming soon", Toast.LENGTH_SHORT).show();
    }

    private void openHelp() {
        // TODO: Implement HelpActivity
        Toast.makeText(getContext(), "Help - Coming soon", Toast.LENGTH_SHORT).show();
    }

    private void performLogout() {
        prefsManager.logout();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh profile when returning to fragment
        refreshData();
    }
}