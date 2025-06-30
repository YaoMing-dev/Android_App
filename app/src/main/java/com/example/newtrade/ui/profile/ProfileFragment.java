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
import com.example.newtrade.ui.product.MyProductsActivity;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";

    // UI Components
    private ImageView ivAvatar;
    private TextView tvName, tvEmail, tvJoinDate, tvRating, tvProductsCount, tvSalesCount;
    private MaterialCardView cardMyProducts, cardSettings, cardSavedItems, cardTransactions, cardHelp;
    private LinearLayout layoutStats;
    private MaterialButton btnEditProfile;

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

        cardMyProducts = view.findViewById(R.id.card_my_products);
        cardSettings = view.findViewById(R.id.card_settings);
        cardSavedItems = view.findViewById(R.id.card_saved_items);
        cardTransactions = view.findViewById(R.id.card_transactions);
        cardHelp = view.findViewById(R.id.card_help);

        layoutStats = view.findViewById(R.id.layout_stats);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
    }

    private void setupListeners() {
        btnEditProfile.setOnClickListener(v -> openEditProfile());
        cardMyProducts.setOnClickListener(v -> openMyProducts());
        cardSettings.setOnClickListener(v -> openSettings());
        cardSavedItems.setOnClickListener(v -> openSavedItems());
        cardTransactions.setOnClickListener(v -> openTransactions());
        cardHelp.setOnClickListener(v -> openHelp());
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
            // TODO: Format join date properly
            tvJoinDate.setText("Joined " + currentUser.getCreatedAt().substring(0, 4));
        }

        // User avatar
        if (currentUser.getAvatarUrl() != null && !currentUser.getAvatarUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentUser.getAvatarUrl())
                    .placeholder(R.drawable.ic_person_placeholder)
                    .error(R.drawable.ic_person_placeholder)
                    .circleCrop()
                    .into(ivAvatar);
        } else {
            ivAvatar.setImageResource(R.drawable.ic_person_placeholder);
        }

        // Rating
        if (currentUser.getAverageRating() != null && currentUser.getAverageRating() > 0) {
            tvRating.setText(currentUser.getFormattedRating());
            tvRating.setVisibility(View.VISIBLE);
        } else {
            tvRating.setVisibility(View.GONE);
        }

        // Stats
        if (currentUser.getTotalProducts() != null) {
            tvProductsCount.setText(String.valueOf(currentUser.getTotalProducts()));
        } else {
            tvProductsCount.setText("0");
        }

        // TODO: Load sales count from API
        tvSalesCount.setText("0");
    }

    public void refreshData() {
        loadUserProfile();
    }

    private void openEditProfile() {
        Intent intent = new Intent(requireContext(), EditProfileActivity.class);
        startActivity(intent);
    }

    private void openMyProducts() {
        Intent intent = new Intent(requireContext(), MyProductsActivity.class);
        startActivity(intent);
    }

    private void openSettings() {
        Intent intent = new Intent(requireContext(), SettingsActivity.class);
        startActivity(intent);
    }

    private void openSavedItems() {
        Intent intent = new Intent(requireContext(), SavedItemsActivity.class);
        startActivity(intent);
    }

    private void openTransactions() {
        Intent intent = new Intent(requireContext(), TransactionsActivity.class);
        startActivity(intent);
    }

    private void openHelp() {
        Intent intent = new Intent(requireContext(), HelpActivity.class);
        startActivity(intent);
    }

    private void redirectToLogin() {
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}