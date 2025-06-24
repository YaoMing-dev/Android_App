// app/src/main/java/com/example/newtrade/ui/profile/ProfileFragment.java
package com.example.newtrade.ui.profile;

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
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    // UI Components
    private CircleImageView ivProfilePicture;
    private TextView tvDisplayName;
    private TextView tvEmail;
    private TextView tvListingsCount;
    private TextView tvSoldCount;
    private TextView tvRating;
    private LinearLayout llMyListings;
    private LinearLayout llSavedItems;
    private LinearLayout llSettings;
    private LinearLayout llAbout;
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
        ivProfilePicture = view.findViewById(R.id.iv_profile_picture);
        tvDisplayName = view.findViewById(R.id.tv_display_name);
        tvEmail = view.findViewById(R.id.tv_email);
        tvListingsCount = view.findViewById(R.id.tv_listings_count);
        tvSoldCount = view.findViewById(R.id.tv_sold_count);
        tvRating = view.findViewById(R.id.tv_rating);
        llMyListings = view.findViewById(R.id.ll_my_listings);
        llSavedItems = view.findViewById(R.id.ll_saved_items);
        llSettings = view.findViewById(R.id.ll_settings);
        llAbout = view.findViewById(R.id.ll_about);
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

        // TODO: Setup other click listeners
    }

    private void loadUserData() {
        if (prefsManager.isLoggedIn()) {
            if (tvDisplayName != null) {
                tvDisplayName.setText(prefsManager.getUserName());
            }
            if (tvEmail != null) {
                tvEmail.setText(prefsManager.getUserEmail());
            }
        }
    }

    private void logout() {
        prefsManager.clearSession();
        // TODO: Navigate to login
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}