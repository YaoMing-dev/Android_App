// app/src/main/java/com/example/newtrade/ui/profile/EditProfileActivity.java
package com.example.newtrade.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.api.ApiService;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.NavigationUtils;
import com.example.newtrade.utils.SharedPrefsManager;
import com.example.newtrade.utils.ValidationUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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
                    .load(Constants.BASE_URL + profilePicture)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(ivProfilePicture);
        }

        // Load additional profile data from server
        loadProfileFromServer();
    }

    private void loadProfileFromServer() {
        ApiService apiService = ApiClient.getApiService();
        Call<StandardResponse<Map<String, Object>>> call = apiService.getCurrentUserProfile(); // ✅ FIXED - No ID needed

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                   Response<StandardResponse<Map<String, Object>>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Map<String, Object>> standardResponse = response.body();

                    if (standardResponse.isSuccess()) {
                        populateProfile(standardResponse.getData());
                    }
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                Log.e(TAG, "Failed to load profile from server", t);
            }
        });
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
                        .load(Constants.BASE_URL + imageUrl)
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

        // ✅ NEW: Use combined avatar upload + profile update
        if (hasImageChanged && selectedImageUri != null) {
            uploadAvatarAndUpdateProfile(displayName, email, bio, contactInfo);
        } else {
            // Update profile without image change
            updateProfileData(displayName, email, bio, contactInfo);
        }
    }

    private boolean validateInput(String displayName, String email) {
        boolean isValid = true;

        // Validate display name
        String displayNameError = ValidationUtils.getDisplayNameError(displayName);
        if (displayNameError != null) {
            etDisplayName.setError(displayNameError);
            isValid = false;
        }

        // Validate email
        String emailError = ValidationUtils.getEmailError(email);
        if (emailError != null) {
            etEmail.setError(emailError);
            isValid = false;
        }

        return isValid;
    }

    // ✅ NEW: Combined avatar upload + profile update
    private void uploadAvatarAndUpdateProfile(String displayName, String email, String bio, String contactInfo) {
        try {
            File imageFile = createTempFileFromUri(selectedImageUri);
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);

            ApiService apiService = ApiClient.getApiService();
            Call<StandardResponse<Map<String, String>>> call = apiService.uploadAndUpdateAvatar(imagePart); // ✅ NEW METHOD

            call.enqueue(new Callback<StandardResponse<Map<String, String>>>() {
                @Override
                public void onResponse(Call<StandardResponse<Map<String, String>>> call,
                                       Response<StandardResponse<Map<String, String>>> response) {

                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<Map<String, String>> standardResponse = response.body();

                        if (standardResponse.isSuccess()) {
                            Map<String, String> data = standardResponse.getData();
                            String uploadedImageUrl = data.get("avatarUrl");

                            Log.d(TAG, "✅ Avatar uploaded successfully: " + uploadedImageUrl);

                            // Now update profile data
                            updateProfileData(displayName, email, bio, contactInfo);

                        } else {
                            resetSaveButton();
                            showError("Failed to upload avatar: " + standardResponse.getMessage());
                        }
                    } else {
                        resetSaveButton();
                        showError("Failed to upload avatar to server");
                    }
                }

                @Override
                public void onFailure(Call<StandardResponse<Map<String, String>>> call, Throwable t) {
                    resetSaveButton();
                    Log.e(TAG, "❌ Avatar upload failed", t);
                    showError("Network error while uploading avatar");
                }
            });

        } catch (Exception e) {
            resetSaveButton();
            Log.e(TAG, "❌ Error preparing avatar for upload", e);
            showError("Error preparing avatar: " + e.getMessage());
        }
    }

    // ✅ FIXED: Use correct API endpoint
    private void updateProfileData(String displayName, String email, String bio, String contactInfo) {
        // Prepare update data
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("displayName", displayName);
        profileData.put("bio", bio);
        profileData.put("contactInfo", contactInfo);

        ApiService apiService = ApiClient.getApiService();
        Call<StandardResponse<Map<String, Object>>> call = apiService.updateUserProfile(profileData); // ✅ FIXED - No userId needed

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                   Response<StandardResponse<Map<String, Object>>> response) {

                resetSaveButton();

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Map<String, Object>> standardResponse = response.body();

                    if (standardResponse.isSuccess()) {
                        // Update SharedPrefs
                        Long userId = prefsManager.getUserId();
                        String currentProfilePicture = prefsManager.getUserProfilePicture();
                        prefsManager.saveUserData(userId, displayName, email, currentProfilePicture);

                        Toast.makeText(EditProfileActivity.this, "✅ Profile updated successfully!", Toast.LENGTH_SHORT).show();
                        hasImageChanged = false; // Reset flag
                        finish(); // Go back to profile

                    } else {
                        showError("Failed to update profile: " + standardResponse.getMessage());
                    }
                } else {
                    showError("Failed to update profile");
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                resetSaveButton();
                Log.e(TAG, "❌ Profile update failed", t);
                showError(Constants.getNetworkErrorMessage(t));
            }
        });
    }

    private File createTempFileFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        String fileName = "avatar_" + System.currentTimeMillis() + ".jpg";
        File tempFile = new File(getCacheDir(), fileName);

        FileOutputStream outputStream = new FileOutputStream(tempFile);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        outputStream.close();
        inputStream.close();

        return tempFile;
    }

    private void updateSaveButton(boolean enabled, String text) {
        btnSaveChanges.setEnabled(enabled);
        btnSaveChanges.setText(text);
    }

    private void resetSaveButton() {
        isLoading = false;
        updateSaveButton(true, "Save Changes");
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}