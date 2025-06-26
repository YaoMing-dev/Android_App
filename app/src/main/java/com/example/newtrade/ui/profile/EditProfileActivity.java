// app/src/main/java/com/example/newtrade/ui/profile/EditProfileActivity.java
package com.example.newtrade.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";
    private static final int REQUEST_IMAGE_PICK = 1001;

    // UI Components
    private MaterialToolbar toolbar;
    private ImageView ivProfilePicture;
    private EditText etDisplayName, etBio, etContactInfo;
    private Button btnSave, btnChangePhoto;

    // Data
    private SharedPrefsManager prefsManager;
    private Uri selectedImageUri;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initViews();
        setupToolbar();
        setupListeners();
        loadCurrentProfile();

        Log.d(TAG, "EditProfileActivity created");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivProfilePicture = findViewById(R.id.iv_profile_picture);
        etDisplayName = findViewById(R.id.et_display_name);
        etBio = findViewById(R.id.et_bio);
        etContactInfo = findViewById(R.id.et_contact_info);
        btnSave = findViewById(R.id.btn_save);
        btnChangePhoto = findViewById(R.id.btn_change_photo);

        prefsManager = SharedPrefsManager.getInstance(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Profile");
        }
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveProfile());
        btnChangePhoto.setOnClickListener(v -> selectImage());

        // Enable save button when data changes
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                updateSaveButton();
            }
        };

        etDisplayName.addTextChangedListener(textWatcher);
        etBio.addTextChangedListener(textWatcher);
        etContactInfo.addTextChangedListener(textWatcher);
    }

    private void loadCurrentProfile() {
        // Load from SharedPrefs
        String displayName = prefsManager.getUserName();
        String email = prefsManager.getUserEmail();

        if (displayName != null) {
            etDisplayName.setText(displayName);
        }

        // Load profile picture placeholder
        Glide.with(this)
                .load(R.drawable.ic_placeholder_image)
                .circleCrop()
                .into(ivProfilePicture);

        // TODO: Load full profile from API
        loadProfileFromAPI();
    }

    private void loadProfileFromAPI() {
        Long userId = prefsManager.getUserId();
        if (userId == null) return;

        // TODO: Call API to get user profile
        // For now, skip API call
        Log.d(TAG, "Loading profile from API for user: " + userId);
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                Glide.with(this)
                        .load(selectedImageUri)
                        .circleCrop()
                        .into(ivProfilePicture);
                updateSaveButton();
            }
        }
    }

    private void saveProfile() {
        if (isLoading) return;

        String displayName = etDisplayName.getText().toString().trim();
        String bio = etBio.getText().toString().trim();
        String contactInfo = etContactInfo.getText().toString().trim();

        if (displayName.isEmpty()) {
            etDisplayName.setError("Display name is required");
            return;
        }

        isLoading = true;
        btnSave.setEnabled(false);
        btnSave.setText("Saving...");

        // ✅ FIX: Update user session với display name mới
        Long userId = prefsManager.getUserId();
        String email = prefsManager.getUserEmail();
        boolean isEmailVerified = prefsManager.isEmailVerified();

        // Update user session với display name mới
        prefsManager.saveUserSession(userId, email, displayName, isEmailVerified);

        // TODO: Call API to update profile
        updateProfileAPI(displayName, bio, contactInfo);
    }

    private void updateProfileAPI(String displayName, String bio, String contactInfo) {
        Map<String, String> profileData = new HashMap<>();
        profileData.put("displayName", displayName);
        profileData.put("bio", bio);
        profileData.put("contactInfo", contactInfo);

        Long userId = prefsManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "User session expired", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Implement API call when UserService is available
        Log.d(TAG, "Updating profile: " + profileData);

        // Simulate success for now
        new android.os.Handler().postDelayed(() -> {
            isLoading = false;
            btnSave.setEnabled(true);
            btnSave.setText("Save Changes");

            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        }, 1000);
    }

    private void updateSaveButton() {
        btnSave.setEnabled(!isLoading && !etDisplayName.getText().toString().trim().isEmpty());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}