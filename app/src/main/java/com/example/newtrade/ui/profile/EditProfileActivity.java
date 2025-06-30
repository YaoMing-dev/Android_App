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

    // Data
    private User currentUser;
    private Uri selectedImageUri;
    private String compressedImagePath;

    // Utils
    private SharedPrefsManager prefsManager;
    private LocationManager locationManager;
    private Double currentLatitude;
    private Double currentLongitude;

    // State
    private boolean isDataChanged = false;
    private boolean isSaving = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        prefsManager = new SharedPrefsManager(this);
        locationManager = new LocationManager(this, this);

        initViews();
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
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Profile");
        }
    }

    private void setupListeners() {
        // Text change listeners to track changes
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                isDataChanged = true;
                updateSaveButton();
            }
        };

        etFullName.addTextChangedListener(textWatcher);
        etDisplayName.addTextChangedListener(textWatcher);
        etPhone.addTextChangedListener(textWatcher);
        etBio.addTextChangedListener(textWatcher);
        etLocation.addTextChangedListener(textWatcher);

        // Buttons
        btnChangeAvatar.setOnClickListener(v -> showImagePickerDialog());
        btnGetLocation.setOnClickListener(v -> getCurrentLocation());
        btnSave.setOnClickListener(v -> saveProfile());

        // Avatar click
        ivAvatar.setOnClickListener(v -> showImagePickerDialog());

        // Validation listeners
        etFullName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!ValidationUtils.isValidName(s.toString())) {
                    tilFullName.setError("Name must be at least 2 characters");
                } else {
                    tilFullName.setError(null);
                }
            }
        });

        etPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String phone = s.toString().trim();
                if (!phone.isEmpty() && !ValidationUtils.isValidPhone(phone)) {
                    tilPhone.setError("Invalid phone number");
                } else {
                    tilPhone.setError(null);
                }
            }
        });
    }

    private void loadUserProfile() {
        Long userId = prefsManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Call<StandardResponse<User>> call = ApiClient.getUserService().getMyProfile(userId);
        call.enqueue(new Callback<StandardResponse<User>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<User>> call,
                                   @NonNull Response<StandardResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    currentUser = response.body().getData();
                    populateUserData();
                } else {
                    Log.e(TAG, "Failed to load profile: " + response.message());
                    Toast.makeText(EditProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<User>> call, @NonNull Throwable t) {
                Log.e(TAG, "Error loading profile", t);
                Toast.makeText(EditProfileActivity.this, "Error loading profile", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void populateUserData() {
        if (currentUser == null) return;

        // Populate fields
        if (currentUser.getFullName() != null) {
            etFullName.setText(currentUser.getFullName());
        }
        if (currentUser.getDisplayName() != null) {
            etDisplayName.setText(currentUser.getDisplayName());
        }
        if (currentUser.getEmail() != null) {
            etEmail.setText(currentUser.getEmail());
            etEmail.setEnabled(false); // Email usually can't be changed
        }
        if (currentUser.getPhone() != null) {
            etPhone.setText(currentUser.getPhone());
        }
        if (currentUser.getBio() != null) {
            etBio.setText(currentUser.getBio());
        }
        if (currentUser.getLocation() != null) {
            etLocation.setText(currentUser.getLocation());
        }

        // Load avatar
        if (currentUser.getAvatarUrl() != null && !currentUser.getAvatarUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentUser.getAvatarUrl())
                    .placeholder(R.drawable.ic_person_placeholder)
                    .error(R.drawable.ic_person_placeholder)
                    .circleCrop()
                    .into(ivAvatar);
        }

        // Store current location
        if (currentUser.getLatitude() != null && currentUser.getLongitude() != null) {
            currentLatitude = currentUser.getLatitude();
            currentLongitude = currentUser.getLongitude();
        }

        isDataChanged = false;
        updateSaveButton();
    }

    private void updateSaveButton() {
        btnSave.setEnabled(isDataChanged && !isSaving && isValidData());
    }

    private boolean isValidData() {
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        return ValidationUtils.isValidName(fullName) &&
                (phone.isEmpty() || ValidationUtils.isValidPhone(phone));
    }

    private void showImagePickerDialog() {
        String[] options = {"Take Photo", "Choose from Gallery", "Remove Photo"};

        new AlertDialog.Builder(this)
                .setTitle("Change Profile Picture")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            checkCameraPermissionAndTakePhoto();
                            break;
                        case 1:
                            pickImageFromGallery();
                            break;
                        case 2:
                            removeProfilePicture();
                            break;
                    }
                })
                .show();
    }

    private void checkCameraPermissionAndTakePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, Constants.PERMISSION_REQUEST_CAMERA);
        } else {
            takePhotoFromCamera();
        }
    }

    private void takePhotoFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, Constants.REQUEST_CODE_CAMERA);
        } else {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, Constants.REQUEST_CODE_GALLERY);
    }

    private void removeProfilePicture() {
        selectedImageUri = null;
        compressedImagePath = null;
        ivAvatar.setImageResource(R.drawable.ic_person_placeholder);
        isDataChanged = true;
        updateSaveButton();
    }

    private void getCurrentLocation() {
        btnGetLocation.setEnabled(false);
        btnGetLocation.setText("Getting location...");
        locationManager.requestLocation();
    }

    private void saveProfile() {
        if (!isValidData() || isSaving) return;

        isSaving = true;
        btnSave.setEnabled(false);
        btnSave.setText("Saving...");

        // First upload avatar if changed
        if (selectedImageUri != null && compressedImagePath != null) {
            uploadAvatarThenSaveProfile();
        } else {
            saveProfileData(null);
        }
    }

    private void uploadAvatarThenSaveProfile() {
        File imageFile = new File(compressedImagePath);
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);

        Call<StandardResponse<Map<String, String>>> call = ApiClient.getProductService().uploadAvatarImage(imagePart);
        call.enqueue(new Callback<StandardResponse<Map<String, String>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, String>>> call,
                                   @NonNull Response<StandardResponse<Map<String, String>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Map<String, String> data = response.body().getData();
                    String avatarUrl = data.get("imageUrl");
                    saveProfileData(avatarUrl);
                } else {
                    isSaving = false;
                    btnSave.setEnabled(true);
                    btnSave.setText("Save");
                    Toast.makeText(EditProfileActivity.this, "Failed to upload avatar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, String>>> call, @NonNull Throwable t) {
                isSaving = false;
                btnSave.setEnabled(true);
                btnSave.setText("Save");
                Log.e(TAG, "Failed to upload avatar", t);
                Toast.makeText(EditProfileActivity.this, "Failed to upload avatar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfileData(String avatarUrl) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("fullName", etFullName.getText().toString().trim());
        userData.put("displayName", etDisplayName.getText().toString().trim());

        String phone = etPhone.getText().toString().trim();
        if (!phone.isEmpty()) {
            userData.put("phone", phone);
        }

        String bio = etBio.getText().toString().trim();
        if (!bio.isEmpty()) {
            userData.put("bio", bio);
        }

        String location = etLocation.getText().toString().trim();
        if (!location.isEmpty()) {
            userData.put("location", location);
        }

        if (currentLatitude != null && currentLongitude != null) {
            userData.put("latitude", currentLatitude);
            userData.put("longitude", currentLongitude);
        }

        if (avatarUrl != null) {
            userData.put("avatarUrl", avatarUrl);
        } else if (selectedImageUri == null && currentUser != null) {
            // Remove avatar case
            userData.put("avatarUrl", "");
        }

        Call<StandardResponse<User>> call = ApiClient.getUserService()
                .updateProfile(userData, prefsManager.getUserId());

        call.enqueue(new Callback<StandardResponse<User>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<User>> call,
                                   @NonNull Response<StandardResponse<User>> response) {
                isSaving = false;
                btnSave.setText("Save");

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    User updatedUser = response.body().getData();

                    // Update SharedPrefs
                    prefsManager.saveUserData(
                            updatedUser.getId(),
                            updatedUser.getEmail(),
                            updatedUser.getDisplayNameOrFullName(),
                            updatedUser.getAvatarUrl()
                    );

                    Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    btnSave.setEnabled(true);
                    String errorMsg = "Failed to update profile";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    Toast.makeText(EditProfileActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<User>> call, @NonNull Throwable t) {
                isSaving = false;
                btnSave.setEnabled(true);
                btnSave.setText("Save");
                Log.e(TAG, "Failed to update profile", t);
                Toast.makeText(EditProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == Constants.REQUEST_CODE_GALLERY && data != null) {
                selectedImageUri = data.getData();
                processSelectedImage();
            } else if (requestCode == Constants.REQUEST_CODE_CAMERA && data != null) {
                // Handle camera result - usually bitmap in extras
                // For simplicity, we'll ask user to use gallery
                Toast.makeText(this, "Please use gallery to select photos", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void processSelectedImage() {
        if (selectedImageUri == null) return;

        try {
            // Compress image
            String realPath = ImageUtils.getRealPathFromURI(this, selectedImageUri);
            if (realPath != null) {
                compressedImagePath = ImageUtils.compressImage(realPath);

                // Display compressed image
                Glide.with(this)
                        .load(compressedImagePath)
                        .circleCrop()
                        .into(ivAvatar);

                isDataChanged = true;
                updateSaveButton();
            } else {
                Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing image", e);
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Constants.PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhotoFromCamera();
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // LocationManager.LocationCallback implementation
    @Override
    public void onLocationReceived(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();

        // Reverse geocode to get address
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(currentLatitude, currentLongitude, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String locationText = address.getLocality();
                if (locationText == null) {
                    locationText = address.getSubAdminArea();
                }
                if (locationText == null) {
                    locationText = address.getAdminArea();
                }
                if (locationText == null) {
                    locationText = address.getCountryName();
                }

                if (locationText != null) {
                    etLocation.setText(locationText);
                    isDataChanged = true;
                    updateSaveButton();
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoder error", e);
        }

        btnGetLocation.setEnabled(true);
        btnGetLocation.setText("Get Current Location");
    }

    @Override
    public void onLocationError(String error) {
        Log.e(TAG, "Location error: " + error);
        btnGetLocation.setEnabled(true);
        btnGetLocation.setText("Get Current Location");
        Toast.makeText(this, "Failed to get location: " + error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (isDataChanged) {
            new AlertDialog.Builder(this)
                    .setTitle("Unsaved Changes")
                    .setMessage("You have unsaved changes. Do you want to save them?")
                    .setPositiveButton("Save", (dialog, which) -> saveProfile())
                    .setNegativeButton("Discard", (dialog, which) -> super.onBackPressed())
                    .setNeutralButton("Cancel", null)
                    .show();
        } else {
            super.onBackPressed();
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
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.cleanup();
        }
    }
}