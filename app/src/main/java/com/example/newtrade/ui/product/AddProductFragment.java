// app/src/main/java/com/example/newtrade/ui/product/AddProductFragment.java
package com.example.newtrade.ui.product;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddProductFragment extends Fragment {

    private static final String TAG = "AddProductFragment";

    // UI Components
    private TextInputEditText etTitle, etDescription, etPrice, etLocation, etTags;
    private AutoCompleteTextView spinnerCategory, spinnerCondition;
    private ImageView ivSelectedImage;
    private Button btnSelectImage, btnGetLocation, btnPreview, btnPublish;
    private MaterialCheckBox cbNegotiable;

    // Data
    private SharedPrefsManager prefsManager;
    private FusedLocationProviderClient fusedLocationClient;
    private Uri selectedImageUri;
    private String uploadedImageUrl;
    private boolean isLoading = false;

    // ✅ FIX: Modern Activity Result API for Image Selection
    private ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        // ✅ FIX: Display selected image immediately
                        Glide.with(this)
                                .load(selectedImageUri)
                                .centerCrop()
                                .placeholder(R.drawable.placeholder_product)
                                .into(ivSelectedImage);

                        ivSelectedImage.setVisibility(View.VISIBLE);
                        uploadedImageUrl = null; // Reset uploaded URL
                        updatePublishButtonState();

                        Log.d(TAG, "✅ Image selected: " + selectedImageUri);
                        Toast.makeText(requireContext(), "Image selected", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    // ✅ FIX: Modern Permission Request API
    private ActivityResultLauncher<String[]> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                Boolean fineLocationGranted = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                Boolean coarseLocationGranted = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);

                if (fineLocationGranted != null && fineLocationGranted) {
                    getCurrentLocation();
                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                    getCurrentLocation();
                } else {
                    Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initData();
        setupSpinners();
        setupListeners();
        updatePublishButtonState();

        Log.d(TAG, "✅ AddProductFragment created successfully");
    }

    private void initViews(View view) {
        etTitle = view.findViewById(R.id.et_title);
        etDescription = view.findViewById(R.id.et_description);
        etPrice = view.findViewById(R.id.et_price);
        etLocation = view.findViewById(R.id.et_location);
        etTags = view.findViewById(R.id.et_tags);
        spinnerCategory = view.findViewById(R.id.spinner_category);
        spinnerCondition = view.findViewById(R.id.spinner_condition);
        ivSelectedImage = view.findViewById(R.id.iv_selected_image);
        btnSelectImage = view.findViewById(R.id.btn_select_image);
        btnGetLocation = view.findViewById(R.id.btn_get_location);
        btnPreview = view.findViewById(R.id.btn_preview);
        btnPublish = view.findViewById(R.id.btn_publish);
        cbNegotiable = view.findViewById(R.id.cb_negotiable);
    }

    private void initData() {
        prefsManager = SharedPrefsManager.getInstance(requireContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    }

    private void setupSpinners() {
        // ✅ FIX: Setup Category Spinner
        String[] categories = {
                "Electronics", "Fashion", "Home & Garden", "Sports", "Books",
                "Automotive", "Health & Beauty", "Toys & Games", "Music", "Other"
        };
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, categories);
        spinnerCategory.setAdapter(categoryAdapter);

        // ✅ FIX: Setup Condition Spinner
        String[] conditions = {"New", "Like New", "Good", "Fair", "Poor"};
        ArrayAdapter<String> conditionAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, conditions);
        spinnerCondition.setAdapter(conditionAdapter);
    }

    private void setupListeners() {
        btnSelectImage.setOnClickListener(v -> selectImage());
        btnGetLocation.setOnClickListener(v -> requestLocationPermission());
        btnPreview.setOnClickListener(v -> previewProduct());
        btnPublish.setOnClickListener(v -> publishProduct());

        // ✅ FIX: Text watchers for validation
        TextWatcher formWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updatePublishButtonState();
            }
        };

        etTitle.addTextChangedListener(formWatcher);
        etDescription.addTextChangedListener(formWatcher);
        etPrice.addTextChangedListener(formWatcher);
        etLocation.addTextChangedListener(formWatcher);
    }

    // ✅ FIX: Image selection method
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
        Log.d(TAG, "🖼️ Opening image picker");
    }

    private void requestLocationPermission() {
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        permissionLauncher.launch(permissions);
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        String locationText = "Current Location"; // Simplified for now
                        etLocation.setText(locationText);
                        Log.d(TAG, "✅ Location obtained");
                    } else {
                        Toast.makeText(requireContext(), "Cannot get current location", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get location", e);
                    Toast.makeText(requireContext(), "Failed to get location", Toast.LENGTH_SHORT).show();
                });
    }

    private void previewProduct() {
        if (!validateForm()) return;

        Toast.makeText(requireContext(), "Preview feature coming soon!", Toast.LENGTH_SHORT).show();
    }

    // ✅ FIX: Main publish method with proper image upload flow
    private void publishProduct() {
        if (isLoading) return;

        if (!validateForm()) return;

        updatePublishButton("Publishing...", false);
        isLoading = true;

        // Step 1: Upload image first if selected
        if (selectedImageUri != null && uploadedImageUrl == null) {
            uploadImageToServer();
        } else {
            // Step 2: Create product with uploaded image URL
            createProductOnServer();
        }
    }

    // ✅ FIX: Complete image upload implementation
    private void uploadImageToServer() {
        Log.d(TAG, "🔄 Uploading image to server...");

        try {
            File imageFile = createTempFileFromUri(selectedImageUri);
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);

            ApiClient.getApiService().uploadProductImage(imagePart)
                    .enqueue(new Callback<StandardResponse<Map<String, String>>>() {
                        @Override
                        public void onResponse(Call<StandardResponse<Map<String, String>>> call,
                                               Response<StandardResponse<Map<String, String>>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                StandardResponse<Map<String, String>> standardResponse = response.body();

                                if (standardResponse.isSuccess()) {
                                    Map<String, String> data = standardResponse.getData();
                                    uploadedImageUrl = data.get("imageUrl");

                                    Log.d(TAG, "✅ Image uploaded successfully: " + uploadedImageUrl);
                                    createProductOnServer();
                                } else {
                                    handleUploadError("Failed to upload image: " + standardResponse.getMessage());
                                }
                            } else {
                                handleUploadError("Server error while uploading image");
                            }
                        }

                        @Override
                        public void onFailure(Call<StandardResponse<Map<String, String>>> call, Throwable t) {
                            Log.e(TAG, "❌ Image upload failed", t);
                            handleUploadError("Network error: " + t.getMessage());
                        }
                    });

        } catch (Exception e) {
            Log.e(TAG, "❌ Error preparing image upload", e);
            handleUploadError("Error preparing image: " + e.getMessage());
        }
    }

    // ✅ FIX: Complete product creation implementation
    private void createProductOnServer() {
        Log.d(TAG, "🔄 Creating product on server...");

        try {
            Map<String, Object> productData = new HashMap<>();
            productData.put("title", etTitle.getText().toString().trim());
            productData.put("description", etDescription.getText().toString().trim());
            productData.put("price", Double.parseDouble(etPrice.getText().toString().trim()));
            productData.put("location", etLocation.getText().toString().trim());
            productData.put("categoryId", 1L); // Default category for now
            productData.put("condition", spinnerCondition.getText().toString());
            productData.put("primaryImageUrl", uploadedImageUrl != null ? uploadedImageUrl : "");
            productData.put("negotiable", cbNegotiable != null && cbNegotiable.isChecked());

            Long userId = prefsManager.getUserId();
            if (userId == null || userId <= 0) {
                handleUploadError("User not logged in");
                return;
            }

            ApiClient.getProductService().createProduct(productData)
                    .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                        @Override
                        public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                               Response<StandardResponse<Map<String, Object>>> response) {
                            isLoading = false;

                            if (response.isSuccessful() && response.body() != null) {
                                StandardResponse<Map<String, Object>> standardResponse = response.body();

                                if (standardResponse.isSuccess()) {
                                    Log.d(TAG, "✅ Product created successfully");
                                    Toast.makeText(requireContext(), "Product published successfully!", Toast.LENGTH_SHORT).show();

                                    // Clear form
                                    clearForm();

                                    // Navigate back to home
                                    try {
                                        NavController navController = Navigation.findNavController(requireView());
                                        navController.navigate(R.id.nav_home);
                                    } catch (Exception e) {
                                        requireActivity().onBackPressed();
                                    }
                                } else {
                                    updatePublishButton("Publish Listing", true);
                                    showError("Failed to create product: " + standardResponse.getMessage());
                                }
                            } else {
                                updatePublishButton("Publish Listing", true);
                                showError("Server error while creating product");
                            }
                        }

                        @Override
                        public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                            isLoading = false;
                            updatePublishButton("Publish Listing", true);
                            Log.e(TAG, "❌ Product creation failed", t);
                            showError("Network error: " + t.getMessage());
                        }
                    });

        } catch (Exception e) {
            isLoading = false;
            updatePublishButton("Publish Listing", true);
            Log.e(TAG, "❌ Error creating product", e);
            showError("Error creating product: " + e.getMessage());
        }
    }

    // ✅ FIX: Form validation
    private boolean validateForm() {
        boolean isValid = true;

        // Check title
        if (TextUtils.isEmpty(etTitle.getText().toString().trim())) {
            etTitle.setError("Title is required");
            isValid = false;
        }

        // Check description
        if (TextUtils.isEmpty(etDescription.getText().toString().trim())) {
            etDescription.setError("Description is required");
            isValid = false;
        }

        // Check price
        String priceText = etPrice.getText().toString().trim();
        if (TextUtils.isEmpty(priceText)) {
            etPrice.setError("Price is required");
            isValid = false;
        } else {
            try {
                double price = Double.parseDouble(priceText);
                if (price <= 0) {
                    etPrice.setError("Price must be greater than 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                etPrice.setError("Invalid price format");
                isValid = false;
            }
        }

        // Check location
        if (TextUtils.isEmpty(etLocation.getText().toString().trim())) {
            etLocation.setError("Location is required");
            isValid = false;
        }

        // Check category
        if (TextUtils.isEmpty(spinnerCategory.getText().toString().trim())) {
            spinnerCategory.setError("Category is required");
            isValid = false;
        }

        // Check condition
        if (TextUtils.isEmpty(spinnerCondition.getText().toString().trim())) {
            spinnerCondition.setError("Condition is required");
            isValid = false;
        }

        // ✅ FIX: Image is required
        if (selectedImageUri == null) {
            Toast.makeText(requireContext(), "Please select a product image", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    private void updatePublishButtonState() {
        if (btnPublish != null) {
            boolean hasRequiredFields = !TextUtils.isEmpty(etTitle.getText()) &&
                    !TextUtils.isEmpty(etDescription.getText()) &&
                    !TextUtils.isEmpty(etPrice.getText()) &&
                    !TextUtils.isEmpty(etLocation.getText()) &&
                    selectedImageUri != null;

            btnPublish.setEnabled(hasRequiredFields && !isLoading);
        }
    }

    // Helper methods
    private void handleUploadError(String message) {
        isLoading = false;
        updatePublishButton("Publish Listing", true);
        showError(message);
    }

    private void updatePublishButton(String text, boolean enabled) {
        if (btnPublish != null) {
            btnPublish.setText(text);
            btnPublish.setEnabled(enabled);
        }
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }

    private void clearForm() {
        etTitle.setText("");
        etDescription.setText("");
        etPrice.setText("");
        etLocation.setText("");
        etTags.setText("");
        spinnerCategory.setText("");
        spinnerCondition.setText("");

        selectedImageUri = null;
        uploadedImageUrl = null;

        if (ivSelectedImage != null) {
            ivSelectedImage.setVisibility(View.GONE);
        }

        updatePublishButtonState();
    }

    // ✅ FIX: Create temp file from URI
    private File createTempFileFromUri(Uri uri) throws Exception {
        InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
        File tempFile = File.createTempFile("upload_image", ".jpg", requireContext().getCacheDir());

        try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }

        if (inputStream != null) {
            inputStream.close();
        }

        return tempFile;
    }
}