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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.api.ApiService;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.ImageUtils;
import com.example.newtrade.utils.NavigationUtils;
import com.example.newtrade.utils.SharedPrefsManager;
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
        ImageUtils.loadAvatarImage(this, profilePicture, ivProfilePicture);

        // Load additional profile data from server
        loadProfileFromServer();
    }

    private void loadProfileFromServer() {
        ApiService apiService = ApiClient.getApiService();
        Call<StandardResponse<Map<String, Object>>> call = apiService.getCurrentUserProfile();

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
        try {
            if (profile.get("bio") != null) {
                etBio.setText(profile.get("bio").toString());
            }
            if (profile.get("contactInfo") != null) {
                etContactInfo.setText(profile.get("contactInfo").toString());
            }
            if (profile.get("profilePicture") != null) {
                String imageUrl = profile.get("profilePicture").toString();
                if (!TextUtils.isEmpty(imageUrl)) {
                    ImageUtils.loadAvatarImage(this, imageUrl, ivProfilePicture);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error populating profile", e);
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
                // ✅ FIX: Display selected image directly with Glide (không dùng ImageUtils)
                Glide.with(this)
                        .load(selectedImageUri)
                        .placeholder(R.drawable.placeholder_avatar)
                        .error(R.drawable.placeholder_avatar)
                        .circleCrop()
                        .into(ivProfilePicture);

                hasImageChanged = false; // Will be set to true after successful upload

                Log.d(TAG, "Image selected: " + selectedImageUri);
            }
        }
    }

    private void saveChanges() {
        if (isLoading) return;

        String displayName = etDisplayName.getText().toString().trim();
        String bio = etBio.getText().toString().trim();
        String contactInfo = etContactInfo.getText().toString().trim();

        // Validate display name
        if (displayName.isEmpty()) {
            etDisplayName.setError("Display name is required");
            etDisplayName.requestFocus();
            return;
        }

        setLoading(true);

        // Step 1: Upload avatar if changed
        if (selectedImageUri != null && !hasImageChanged) {
            uploadAvatarToServer();
            return; // uploadAvatarToServer will call updateProfileOnServer after success
        }

        // Step 2: Update profile info
        updateProfileOnServer();
    }

    private void uploadAvatarToServer() {
        Log.d(TAG, "🔄 Uploading avatar to server...");

        try {
            File imageFile = createTempFileFromUri(selectedImageUri);

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);

            Log.d(TAG, "📁 Image file created: " + imageFile.getName() + " (" + imageFile.length() + " bytes)");

            ApiService apiService = ApiClient.getApiService();

            // ✅ FIX: Sử dụng uploadAndUpdateAvatar thay vì uploadAvatar
            Call<StandardResponse<Map<String, String>>> call = apiService.uploadAndUpdateAvatar(imagePart);

            call.enqueue(new Callback<StandardResponse<Map<String, String>>>() {
                @Override
                public void onResponse(Call<StandardResponse<Map<String, String>>> call,
                                       Response<StandardResponse<Map<String, String>>> response) {

                    Log.d(TAG, "📥 Upload response: " + response.code());

                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<Map<String, String>> standardResponse = response.body();

                        if (standardResponse.isSuccess()) {
                            handleAvatarUploadSuccess(standardResponse.getData());
                            // ✅ FIX: Không cần gọi updateProfileOnServer() nữa vì đã update trong uploadAndUpdateAvatar
                            setLoading(false);
                            Toast.makeText(EditProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            setLoading(false);
                            showError("Upload failed: " + standardResponse.getMessage());
                        }
                    } else {
                        setLoading(false);
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                            Log.e(TAG, "❌ Upload error body: " + errorBody);
                            showError("Upload failed: " + response.code() + " - " + errorBody);
                        } catch (Exception e) {
                            showError("Upload failed: Server error " + response.code());
                        }
                    }
                }

                @Override
                public void onFailure(Call<StandardResponse<Map<String, String>>> call, Throwable t) {
                    setLoading(false);
                    showError("Upload failed: " + t.getMessage());
                    Log.e(TAG, "Avatar upload failed", t);
                }
            });

        } catch (Exception e) {
            setLoading(false);
            showError("Failed to prepare image: " + e.getMessage());
            Log.e(TAG, "Error preparing avatar upload", e);
        }
    }

    // ✅ FIXED: handleAvatarUploadSuccess với fresh Glide load
    private void handleAvatarUploadSuccess(Map<String, String> response) {
        try {
            // ✅ FIX: Thử các key khác nhau từ response
            String avatarUrl = null;

            if (response.get("imageUrl") != null) {
                avatarUrl = response.get("imageUrl").toString();
            } else if (response.get("avatarUrl") != null) {
                avatarUrl = response.get("avatarUrl").toString();
            } else if (response.get("url") != null) {
                avatarUrl = response.get("url").toString();
            }

            if (avatarUrl != null) {
                // ✅ CẬP NHẬT SHAREDPREFS VỚI AVATAR URL MỚI
                prefsManager.updateProfilePicture(avatarUrl);

                // ✅ FIX: Update UI ngay lập tức với Glide fresh load
                String fullUrl = ImageUtils.buildFullImageUrl(avatarUrl);
                Glide.with(this)
                        .load(fullUrl)
                        .placeholder(R.drawable.placeholder_avatar)
                        .error(R.drawable.placeholder_avatar)
                        .skipMemoryCache(true) // ✅ Force fresh load
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .circleCrop()
                        .into(ivProfilePicture);

                // Mark as successful
                hasImageChanged = true;

                Toast.makeText(this, "Avatar updated successfully!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "✅ Avatar upload successful: " + avatarUrl);

            } else {
                Log.w(TAG, "No avatar URL in response: " + response.toString());
            }

        } catch (Exception e) {
            Log.e(TAG, "Error handling avatar upload success", e);
        }
    }

    private void updateProfileOnServer() {
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("displayName", etDisplayName.getText().toString().trim());
        profileData.put("bio", etBio.getText().toString().trim());
        profileData.put("contactInfo", etContactInfo.getText().toString().trim());

        ApiService apiService = ApiClient.getApiService();
        Call<StandardResponse<Map<String, Object>>> call = apiService.updateUserProfile(profileData);

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                   Response<StandardResponse<Map<String, Object>>> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Map<String, Object>> standardResponse = response.body();

                    if (standardResponse.isSuccess()) {
                        // Update SharedPrefs
                        prefsManager.saveUserData(
                                prefsManager.getUserId(),
                                etDisplayName.getText().toString().trim(),
                                prefsManager.getUserEmail(),
                                prefsManager.getUserProfilePicture()
                        );

                        Toast.makeText(EditProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        showError("Update failed: " + standardResponse.getMessage());
                    }
                } else {
                    showError("Update failed: Server error " + response.code());
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                setLoading(false);
                showError("Update failed: " + t.getMessage());
                Log.e(TAG, "Profile update failed", t);
            }
        });
    }

    private File createTempFileFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        if (inputStream == null) {
            throw new IOException("Cannot open input stream from URI");
        }

        // ✅ FIX: Tạo file với extension đúng
        File tempFile = File.createTempFile("avatar_", ".jpg", getCacheDir());

        FileOutputStream outputStream = new FileOutputStream(tempFile);
        byte[] buffer = new byte[4096]; // Tăng buffer size
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        outputStream.close();
        inputStream.close();

        Log.d(TAG, "📁 Temp file created: " + tempFile.getAbsolutePath() + " (" + tempFile.length() + " bytes)");

        return tempFile;
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        btnSaveChanges.setEnabled(!loading);
        btnChangePhoto.setEnabled(!loading);
        btnSaveChanges.setText(loading ? "Saving..." : "Save Changes");
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e(TAG, message);
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