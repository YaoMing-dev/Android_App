// app/src/main/java/com/example/newtrade/ui/profile/EditProfileActivity.java
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.api.ApiService;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.NavigationUtils;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
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

    // UI Components
    private MaterialToolbar toolbar;
    private CircleImageView ivProfilePicture;
    private TextInputEditText etDisplayName, etEmail, etBio, etContactInfo;
    private Button btnChangePhoto, btnSaveChanges;

    // Data
    private SharedPrefsManager prefsManager;
    private boolean isLoading = false;
    private boolean hasImageChanged = false;
    private String currentImageUrl = "";
    private String uploadedAvatarUrl = "";

    // ✅ FIX: Modern Activity Result API
    private ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        // Display selected image
                        Glide.with(this)
                                .load(selectedImageUri)
                                .centerCrop()
                                .into(ivProfilePicture);

                        hasImageChanged = true;
                        uploadedAvatarUrl = ""; // Reset uploaded URL

                        // Upload avatar immediately
                        uploadAvatarToServer(selectedImageUri);

                        Log.d(TAG, "✅ Avatar image selected");
                    }
                }
            }
    );

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
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Profile");
        }
    }

    private void setupListeners() {
        btnChangePhoto.setOnClickListener(v -> selectImage());
        btnSaveChanges.setOnClickListener(v -> saveProfileChanges());
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent); // ✅ FIX: Use modern API
    }

    // ✅ FIX: Upload avatar with proper API
    private void uploadAvatarToServer(Uri imageUri) {
        Log.d(TAG, "Uploading avatar to server...");

        setLoading(true);
        btnChangePhoto.setText("Uploading...");

        try {
            File imageFile = createTempFileFromUri(imageUri);
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);

            ApiService apiService = ApiClient.getApiService();
            Call<StandardResponse<Map<String, String>>> call = apiService.uploadAvatar(imagePart);

            call.enqueue(new Callback<StandardResponse<Map<String, String>>>() {
                @Override
                public void onResponse(Call<StandardResponse<Map<String, String>>> call,
                                       Response<StandardResponse<Map<String, String>>> response) {

                    setLoading(false);
                    btnChangePhoto.setText("Change Photo");

                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<Map<String, String>> standardResponse = response.body();

                        if (standardResponse.isSuccess()) {
                            Map<String, String> data = standardResponse.getData();
                            uploadedAvatarUrl = data.get("imageUrl");

                            Log.d(TAG, "✅ Avatar uploaded successfully: " + uploadedAvatarUrl);
                            Toast.makeText(EditProfileActivity.this, "Avatar uploaded successfully!", Toast.LENGTH_SHORT).show();

                        } else {
                            showError("Failed to upload avatar: " + standardResponse.getMessage());
                            hasImageChanged = false;
                        }
                    } else {
                        showError("Failed to upload avatar to server");
                        hasImageChanged = false;
                        Log.e(TAG, "Avatar upload response not successful: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<StandardResponse<Map<String, String>>> call, Throwable t) {
                    setLoading(false);
                    btnChangePhoto.setText("Change Photo");
                    Log.e(TAG, "❌ Avatar upload failed", t);
                    showError("Network error while uploading avatar: " + t.getMessage());
                    hasImageChanged = false;
                }
            });

        } catch (Exception e) {
            setLoading(false);
            btnChangePhoto.setText("Change Photo");
            Log.e(TAG, "❌ Error preparing avatar for upload", e);
            showError("Error preparing avatar: " + e.getMessage());
            hasImageChanged = false;
        }
    }

    private void loadCurrentProfile() {
        Log.d(TAG, "Loading current profile data...");

        Long userId = prefsManager.getUserId();
        if (userId == null || userId <= 0) {
            showError("Please login to edit profile");
            finish();
            return;
        }

        // Load from SharedPrefs first
        etDisplayName.setText(prefsManager.getUserName());
        etEmail.setText(prefsManager.getUserEmail());
        currentImageUrl = prefsManager.getUserProfilePicture();

        // Load profile picture
        if (!TextUtils.isEmpty(currentImageUrl)) {
            Glide.with(this)
                    .load(currentImageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.ic_person)
                    .into(ivProfilePicture);
        }

        // TODO: Load additional profile data from API if needed
        Log.d(TAG, "✅ Profile data loaded");
    }

    private void saveProfileChanges() {
        if (!validateForm()) {
            return;
        }

        setLoading(true);
        btnSaveChanges.setText("Saving...");

        Long userId = prefsManager.getUserId();
        if (userId == null || userId <= 0) {
            setLoading(false);
            showError("Please login to save changes");
            return;
        }

        // Create update request
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("displayName", getTextFromEditText(etDisplayName));
        updateRequest.put("email", getTextFromEditText(etEmail));
        updateRequest.put("bio", getTextFromEditText(etBio));
        updateRequest.put("contactInfo", getTextFromEditText(etContactInfo));

        // Add avatar URL if changed
        if (hasImageChanged && !TextUtils.isEmpty(uploadedAvatarUrl)) {
            updateRequest.put("profilePicture", uploadedAvatarUrl);
        }

        // TODO: Call API to update profile
        // For now, just update SharedPrefs
        prefsManager.saveUserName(getTextFromEditText(etDisplayName));
        prefsManager.saveUserEmail(getTextFromEditText(etEmail));

        if (hasImageChanged && !TextUtils.isEmpty(uploadedAvatarUrl)) {
            prefsManager.saveUserProfilePicture(uploadedAvatarUrl);
        }

        setLoading(false);
        btnSaveChanges.setText("Save Changes");

        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();

        // Return success result
        setResult(Activity.RESULT_OK);
        finish();
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validate display name
        if (TextUtils.isEmpty(getTextFromEditText(etDisplayName))) {
            etDisplayName.setError("Display name is required");
            isValid = false;
        }

        // Validate email
        String email = getTextFromEditText(etEmail);
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Invalid email format");
            isValid = false;
        }

        return isValid;
    }

    // ===== HELPER METHODS =====

    private File createTempFileFromUri(Uri uri) throws Exception {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        File tempFile = new File(getCacheDir(), "temp_avatar.jpg");
        FileOutputStream outputStream = new FileOutputStream(tempFile);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        inputStream.close();
        outputStream.close();
        return tempFile;
    }

    private String getTextFromEditText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        btnSaveChanges.setEnabled(!loading);
        btnChangePhoto.setEnabled(!loading);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}