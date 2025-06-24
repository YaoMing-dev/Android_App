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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.newtrade.R;
import com.example.newtrade.ui.auth.LoginActivity;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    // UI Components - chỉ những cái có thật trong layout
    private TextView tvDisplayName;
    private TextView tvListingsCount;
    private TextView tvSoldCount;
    private TextView tvRating;
    private LinearLayout llMyListings;
    private LinearLayout llSavedItems;
    private LinearLayout llPurchaseHistory;
    private LinearLayout llReviews;
    private LinearLayout llLogout;
    private FloatingActionButton fabEditProfile;

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

        Log.d(TAG, "ProfileFragment created successfully");
    }

    private void initViews(View view) {
        // Chỉ init những view thực sự có trong layout
        tvDisplayName = view.findViewById(R.id.tv_display_name);
        tvListingsCount = view.findViewById(R.id.tv_listings_count);
        tvSoldCount = view.findViewById(R.id.tv_sold_count);
        tvRating = view.findViewById(R.id.tv_rating);
        llMyListings = view.findViewById(R.id.ll_my_listings);
        llSavedItems = view.findViewById(R.id.ll_saved_items);
        llPurchaseHistory = view.findViewById(R.id.ll_purchase_history);
        llReviews = view.findViewById(R.id.ll_reviews);
        llLogout = view.findViewById(R.id.ll_logout);
        fabEditProfile = view.findViewById(R.id.fab_edit_profile);
    }

    private void initUtils() {
        prefsManager = SharedPrefsManager.getInstance(requireContext());
    }

    private void setupListeners() {
        if (llLogout != null) {
            llLogout.setOnClickListener(v -> logout());
        }

        if (fabEditProfile != null) {
            fabEditProfile.setOnClickListener(v -> {
                // TODO: Navigate to edit profile
            });
        }

        // Setup other click listeners chỉ khi view tồn tại
        if (llMyListings != null) {
            llMyListings.setOnClickListener(v -> {
                // TODO: Navigate to my listings
            });
        }

        if (llSavedItems != null) {
            llSavedItems.setOnClickListener(v -> {
                // TODO: Navigate to saved items
            });
        }
    }

    private void loadUserData() {
        if (prefsManager.isLoggedIn()) {
            if (tvDisplayName != null) {
                tvDisplayName.setText(prefsManager.getUserName());
            }
        }

        // Set default stats
        if (tvListingsCount != null) {
            tvListingsCount.setText("0");
        }
        if (tvSoldCount != null) {
            tvSoldCount.setText("0");
        }
        if (tvRating != null) {
            tvRating.setText("0.0");
        }
    }

    private void logout() {
        prefsManager.clearUserSession();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}