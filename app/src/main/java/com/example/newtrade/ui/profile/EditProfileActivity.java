// app/src/main/java/com/example/newtrade/ui/profile/EditProfileActivity.java
package com.example.newtrade.ui.profile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.models.User;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.ImageUtils;
import com.example.newtrade.utils.LocationManager;
import com.example.newtrade.utils.NetworkUtils;
import com.example.newtrade.utils.SharedPrefsManager;
import com.example.newtrade.utils.ValidationUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity implements LocationManager.LocationCallback {
    private static final String TAG = "EditProfileActivity";

    // UI Components
    private Toolbar toolbar;
    private ImageView ivAvatar;
    private TextInputLayout tilFullName, tilDisplayName, tilEmail, tilPhone, tilBio, tilLocation;
    private EditText etFullName, etDisplayName, etEmail, etPhone, etBio, etLocation;
    private MaterialButton btnSave, btnChangeAvatar, btnGetLocation;
    private ProgressBar progressBar;

    // Data
    private User currentUser;
    private Uri selectedImageUri;
    private String compressedImagePath;

    // Utils
    private SharedPrefsManager prefsManager;
    private LocationManager locationManager;

    // State
    private boolean isLoading = false;
    private boolean hasChanges = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initViews();
        initUtils();
        setupToolbar();
        setupListeners();

        loadUserProfile();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivAvatar = findViewById(R.id.iv_avatar);
        tilFullName = findViewById(R.id.til_full_name);
        tilDisplayName = findViewById(R.id.til_display_name);
        tilEmail = findViewById(R.id.til_email);
        tilPhone = findViewById(R.id.til_phone);
        tilBio = findViewById(R.id.til_bio);
        tilLocation = findViewById(R.id.til_location);
        etFullName = findViewById(R.id.et_full_name);
        etDisplayName = findViewById(R.id.et_display_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etBio = findViewById(R.id.et_bio);
        etLocation = findViewById(R.id.et_location);
        btnSave = findViewById(R.id.btn_save);
        btnChangeAvatar = findViewById(R.id.btn_change_avatar);
        btnGetLocation = findViewById(R.id.btn_get_location);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void initUtils() {
        prefsManager = new SharedPrefsManager(this);
        locationManager = new LocationManager(this, this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Profile");
        }
    }

    private void setupListeners() {
        // Text change listeners
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hasChanges = true;
                clearFieldErrors();
                updateSaveButtonState();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        etFullName.addTextChangedListener(textWatcher);
        etDisplayName.addTextChangedListener(textWatcher);
        etPhone.addTextChangedListener(textWatcher);
        etBio.addTextChangedListener(textWatcher);
        etLocation.addTextChangedListener(textWatcher);

        // Buttons
        btnSave.setOnClickListener(v -> saveProfile());
        btnChangeAvatar.setOnClickListener(v -> changeAvatar());
        btnGetLocation.setOnClickListener(v -> getCurrentLocation());
    }

    private void clearFieldErrors() {
        tilFullName.setError(null);
        tilDisplayName.setError(null);
        tilPhone.setError(null);
        tilBio.setError(null);
        tilLocation.setError(null);
    }

    private void loadUserProfile() {
        setLoading(true);

        Call<StandardResponse<User>> call = ApiClient.getUserService().getMyProfile(prefsManager.getUserId());
        call.enqueue(new Callback<StandardResponse<User>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<User>> call,
                                   @NonNull Response<StandardResponse<User>> response) {
                setLoading(false);
                handleProfileResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<User>> call, @NonNull Throwable t) {
                setLoading(false);
                handleLoadingError(t);
            }
        });
    }

    private void handleProfileResponse(Response<StandardResponse<User>> response) {
        try {
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                currentUser = response.body().getData();
                if (currentUser != null) {
                    populateUserData();
                } else {
                    showError("Failed to load profile data");
                    finish();
                }
            } else {
                String message = response.body() != null ? response.body().getMessage() : "Failed to load profile";
                showError(message);
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing profile response", e);
            showError("Error loading profile");
            finish();
        }
    }

    private void populateUserData() {
        if (currentUser == null) return;

        // Profile picture
        String avatarUrl = currentUser.getProfileImageUrl();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_person_placeholder)
                    .error(R.drawable.ic_person_placeholder)
                    .into(ivAvatar);
        }

        // Basic info
        etFullName.setText(currentUser.getFullName());
        etDisplayName.setText(currentUser.getDisplayName());
        etEmail.setText(currentUser.getEmail());
        etPhone.setText(currentUser.getPhoneNumber());
        etBio.setText(currentUser.getBio());
        etLocation.setText(currentUser.getLocation());

        // Email is not editable
        etEmail.setEnabled(false);

        hasChanges = false;
        updateSaveButtonState();
    }

    // FR-1.2.2: Users can update profile and profile photo
    private void changeAvatar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Profile Picture");
        builder.setItems(new CharSequence[]{"Camera", "Gallery", "Remove Photo"}, (dialog, which) -> {
            switch (which) {
                case 0:
                    openCamera();
                    break;
                case 1:
                    openGallery();
                    break;
                case 2:
                    removePhoto();
                    break;
            }
        });
        builder.show();
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    Constants.PERMISSION_REQUEST_CAMERA);
            return;
        }

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, Constants.REQUEST_CODE_CAMERA);
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, Constants.REQUEST_CODE_GALLERY);
    }

    private void removePhoto() {
        ivAvatar.setImageResource(R.drawable.ic_person_placeholder);
        selectedImageUri = null;
        compressedImagePath = null;
        hasChanges = true;
        updateSaveButtonState();
    }

    private void getCurrentLocation() {
        if (!locationManager.hasLocationPermission()) {
            locationManager.requestLocationPermission();
            return;
        }

        btnGetLocation.setEnabled(false);
        btnGetLocation.setText("Getting...");
        locationManager.getCurrentLocation();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == Constants.REQUEST_CODE_CAMERA && data != null) {
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    processSelectedImage(imageUri);
                }
            } else if (requestCode == Constants.REQUEST_CODE_GALLERY && data != null) {
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    processSelectedImage(imageUri);
                }
            }
        }
    }

    private void processSelectedImage(Uri imageUri) {
        selectedImageUri = imageUri;

        String fileName = ImageUtils.generateImageFileName();
        compressedImagePath = ImageUtils.compressImage(this, imageUri, fileName);

        if (compressedImagePath != null) {
            Glide.with(this)
                    .load(imageUri)
                    .placeholder(R.drawable.ic_person_placeholder)
                    .error(R.drawable.ic_person_placeholder)
                    .into(ivAvatar);

            hasChanges = true;
            updateSaveButtonState();
        } else {
            showError("Failed to process image");
        }
    }

    private void saveProfile() {
        if (!validateForm()) {
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            showError("No internet connection");
            return;
        }

        setLoading(true);

        // Prepare profile data
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("fullName", etFullName.getText().toString().trim());
        profileData.put("displayName", etDisplayName.getText().toString().trim());
        profileData.put("phoneNumber", etPhone.getText().toString().trim());
        profileData.put("bio", etBio.getText().toString().trim());
        profileData.put("location", etLocation.getText().toString().trim());

        // If image changed, upload it first
        if (compressedImagePath != null) {
            uploadImageAndSaveProfile(profileData);
        } else {
            saveProfileData(profileData);
        }
    }

    private void uploadImageAndSaveProfile(Map<String, Object> profileData) {
        File imageFile = new File(compressedImagePath);
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("avatar", imageFile.getName(), requestFile);

        // TODO: Create upload avatar endpoint
        // For now, just save profile without image
        saveProfileData(profileData);
    }

    private void saveProfileData(Map<String, Object> profileData) {
        Call<StandardResponse<User>> call = ApiClient.getUserService()
                .updateProfile(profileData, prefsManager.getUserId());

        call.enqueue(new Callback<StandardResponse<User>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<User>> call,
                                   @NonNull Response<StandardResponse<User>> response) {
                setLoading(false);
                handleSaveResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<User>> call, @NonNull Throwable t) {
                setLoading(false);
                Log.e(TAG, "Failed to save profile", t);
                showError(NetworkUtils.getNetworkErrorMessage(t));
            }
        });
    }

    private void handleSaveResponse(Response<StandardResponse<User>> response) {
        try {
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                User updatedUser = response.body().getData();
                if (updatedUser != null) {
                    // Update SharedPreferences with new data
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("id", updatedUser.getId());
                    userData.put("email", updatedUser.getEmail());
                    userData.put("displayName", updatedUser.getDisplayName());
                    userData.put("fullName", updatedUser.getFullName());
                    userData.put("avatarUrl", updatedUser.getAvatarUrl());
                    userData.put("isEmailVerified", updatedUser.getIsEmailVerified());
                    prefsManager.saveLoginData(userData);
                }

                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                hasChanges = false;
                updateSaveButtonState();

                // Set result to refresh ProfileFragment
                setResult(RESULT_OK);
            } else {
                String message = response.body() != null ? response.body().getMessage() : "Failed to save profile";
                handleSaveError(message, response.code());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing save response", e);
            showError("Failed to save profile");
        }
    }

    private void handleSaveError(String message, int responseCode) {
        if (responseCode == 409 || message.toLowerCase().contains("display")) {
            tilDisplayName.setError("Display name already taken");
        } else if (message.toLowerCase().contains("phone")) {
            tilPhone.setError("Invalid phone number");
        } else {
            showError(message);
        }
    }

    private boolean validateForm() {
        boolean isValid = true;

        String fullName = etFullName.getText().toString().trim();
        if (!ValidationUtils.isValidName(fullName)) {
            tilFullName.setError("Full name must be 2-50 characters");
            isValid = false;
        }

        String displayName = etDisplayName.getText().toString().trim();
        if (!ValidationUtils.isValidDisplayName(displayName)) {
            tilDisplayName.setError("Display name must be 2-50 characters");
            isValid = false;
        }

        String phone = etPhone.getText().toString().trim();
        if (!phone.isEmpty() && !ValidationUtils.isValidPhoneNumber(phone)) {
            tilPhone.setError("Please enter a valid phone number");
            isValid = false;
        }

        String bio = etBio.getText().toString().trim();
        if (!ValidationUtils.isValidBio(bio)) {
            tilBio.setError("Bio must be less than 500 characters");
            isValid = false;
        }

        String location = etLocation.getText().toString().trim();
        if (!location.isEmpty() && !ValidationUtils.isValidLocation(location)) {
            tilLocation.setError("Please enter a valid location");
            isValid = false;
        }

        return isValid;
    }

    private void updateSaveButtonState() {
        btnSave.setEnabled(hasChanges && !isLoading);
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!loading && hasChanges);
        btnSave.setText(loading ? "Saving..." : "Save Changes");
    }

    private void handleLoadingError(Throwable t) {
        Log.e(TAG, "Failed to load profile", t);

        if (!NetworkUtils.isNetworkAvailable(this)) {
            showError("No internet connection");
        } else {
            showError(NetworkUtils.getNetworkErrorMessage(t));
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // LocationManager.LocationCallback implementation
    @Override
    public void onLocationReceived(Location location) {
        btnGetLocation.setEnabled(true);
        btnGetLocation.setText("Get Location");

        // Reverse geocoding to get address
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(), location.getLongitude(), 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String locationString = address.getAddressLine(0);
                etLocation.setText(locationString);
                hasChanges = true;
                updateSaveButtonState();
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoder failed", e);
            showError("Failed to get address");
        }
    }

    @Override
    public void onLocationError(String error) {
        btnGetLocation.setEnabled(true);
        btnGetLocation.setText("Get Location");
        showError("Failed to get location: " + error);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Constants.PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                showError("Camera permission required");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (hasChanges) {
            new AlertDialog.Builder(this)
                    .setTitle("Unsaved Changes")
                    .setMessage("You have unsaved changes. Are you sure you want to leave?")
                    .setPositiveButton("Leave", (dialog, which) -> super.onBackPressed())
                    .setNegativeButton("Stay", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.cleanup();
        }
    }
}