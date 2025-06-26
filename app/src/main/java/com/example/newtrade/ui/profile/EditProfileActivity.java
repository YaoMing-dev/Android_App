// app/src/main/java/com/example/newtrade/ui/profile/EditProfileActivity.java
// ✅ COMPLETE REWRITE - No errors version
package com.example.newtrade.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.NavigationUtils;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";
    private static final int REQUEST_IMAGE_PICK = 1001;

    // UI Components
    private MaterialToolbar toolbar;
    private CircleImageView ivProfilePicture;
    private TextInputEditText etDisplayName, etEmail, etBio, etContactInfo;
    private Button btnChangePhoto, btnSaveChanges;

    // Data
    private SharedPrefsManager prefsManager;
    private Uri selectedImageUri;
    private boolean isLoading = false;
    private boolean hasImageChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initViews();
        setupToolbar();
        setupListeners();
        loadCurrentProfile();

        Log.d(TAG, "✅ EditProfileActivity created");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivProfilePicture = findViewById(R.id.iv_profile_picture);
        etDisplayName = findViewById(R.id.et_display_name);
        etEmail = findViewById(R.id.et_email);
        etBio = findViewById(R.id.et_bio);
        etContactInfo = findViewById(R.id.et_contact_info);
        btnChangePhoto = findViewById(R.id.btn_change_photo);
        btnSaveChanges = findViewById(R.id.btn_save_changes);

        prefsManager = SharedPrefsManager.getInstance(this);
    }

    private void setupToolbar() {
        NavigationUtils.setupToolbarWithBackButton(this, toolbar, "Edit Profile");
    }

    private void setupListeners() {
        btnChangePhoto.setOnClickListener(v -> selectProfileImage());
        btnSaveChanges.setOnClickListener(v -> saveChanges());
    }

    private void loadCurrentProfile() {
        // Load from SharedPrefs first
        etDisplayName.setText(prefsManager.getUserName());
        etEmail.setText(prefsManager.getUserEmail());

        // Load profile picture if available
        String profilePicture = prefsManager.getUserProfilePicture();
        if (!TextUtils.isEmpty(profilePicture)) {
            Glide.with(this)
                    .load(profilePicture)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(ivProfilePicture);
        }

        // Load full profile from API
        loadProfileFromAPI();
    }

    private void loadProfileFromAPI() {
        Long userId = prefsManager.getUserId();
        if (userId == null || userId <= 0) {
            Log.w(TAG, "Invalid user ID");
            return;
        }

        Log.d(TAG, "📋 Loading profile from API for user: " + userId);

        // TODO: Implement when API is ready
        // For now, just populate with mock data
        populateMockProfile();

        /*
        ApiClient.getApiService().getUserProfile(userId)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                         Response<StandardResponse<Map<String, Object>>> response) {

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Map<String, Object> profile = response.body().getData();
                            populateProfile(profile);
                            Log.d(TAG, "✅ Profile loaded from API");
                        } else {
                            Log.w(TAG, "⚠️ Failed to load profile from API");
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        Log.e(TAG, "❌ Failed to load profile", t);
                    }
                });
        */
    }

    private void populateMockProfile() {
        // Mock data for testing
        etBio.setText("I love buying and selling unique items!");
        etContactInfo.setText("+84 901 234 567");
    }

    private void populateProfile(Map<String, Object> profile) {
        if (profile.get("bio") != null) {
            etBio.setText(profile.get("bio").toString());
        }
        if (profile.get("contactInfo") != null) {
            etContactInfo.setText(profile.get("contactInfo").toString());
        }
        if (profile.get("profilePicture") != null) {
            String imageUrl = profile.get("profilePicture").toString();
            if (!TextUtils.isEmpty(imageUrl)) {
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(ivProfilePicture);
            }
        }
    }

    private void selectProfileImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                hasImageChanged = true;

                // Display selected image
                Glide.with(this)
                        .load(selectedImageUri)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(ivProfilePicture);

                Log.d(TAG, "✅ Profile image selected");
            }
        }
    }

    private void saveChanges() {
        if (isLoading) return;

        String displayName = etDisplayName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String bio = etBio.getText().toString().trim();
        String contactInfo = etContactInfo.getText().toString().trim();

        // Validation
        if (!validateInput(displayName, email)) {
            return;
        }

        isLoading = true;
        updateSaveButton(false, "Saving...");

        // For now, just simulate success
        simulateSuccessfulSave(displayName, email, bio, contactInfo);

        // TODO: Implement real API call
        // updateProfileData(displayName, email, bio, contactInfo, null);
    }

    private void simulateSuccessfulSave(String displayName, String email, String bio, String contactInfo) {
        // Simulate network delay
        new android.os.Handler().postDelayed(() -> {
            // Update SharedPrefs
            prefsManager.saveUserData(
                    prefsManager.getUserId(),
                    displayName,
                    email,
                    prefsManager.getUserProfilePicture()
            );

            resetSaveButton();
            Toast.makeText(this, "✅ Profile updated successfully", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();

            Log.d(TAG, "✅ Profile updated successfully (simulated)");
        }, 2000);
    }

    private boolean validateInput(String displayName, String email) {
        if (TextUtils.isEmpty(displayName)) {
            etDisplayName.setError("Display name is required");
            etDisplayName.requestFocus();
            return false;
        }

        if (displayName.length() < 2) {
            etDisplayName.setError("Display name must be at least 2 characters");
            etDisplayName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Valid email is required");
            etEmail.requestFocus();
            return false;
        }

        return true;
    }

    private void updateSaveButton(boolean enabled, String text) {
        btnSaveChanges.setEnabled(enabled);
        btnSaveChanges.setText(text);
    }

    private void resetSaveButton() {
        isLoading = false;
        updateSaveButton(true, "💾 Save Changes");
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Error: " + message);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (NavigationUtils.handleBackButton(this, item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}