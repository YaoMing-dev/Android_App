// app/src/main/java/com/example/newtrade/ui/product/AddProductFragment.java
// ✅ FIXED: Added displaySelectedImage + Fixed callback types + Updated publishProduct
package com.example.newtrade.ui.product;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Category;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.auth.LoginActivity;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
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

public class AddProductFragment extends Fragment {

    private static final String TAG = "AddProductFragment";
    private static final int REQUEST_IMAGE_PICK = 1001;
    private static final int REQUEST_LOCATION_PERMISSION = 1002;

    // UI Components
    private ImageView ivProductImage;
    private TextInputEditText etTitle, etDescription, etPrice, etLocation;
    private Spinner spinnerCategory, spinnerCondition;
    private MaterialCheckBox cbNegotiable;
    private Button btnSelectImage, btnGetLocation, btnPublish;

    // Data
    private final List<Category> categories = new ArrayList<>();
    private Uri selectedImageUri;
    private String uploadedImageUrl;
    private boolean isUploading = false;
    private SharedPrefsManager prefsManager;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initUtils();
        initLocationServices();
        loadCategories();
        setupConditionSpinner();
        setupListeners();

        Log.d(TAG, "AddProductFragment created successfully");
    }

    private void initViews(View view) {
        try {
            ivProductImage = view.findViewById(R.id.iv_product_image);
            etTitle = view.findViewById(R.id.et_title);
            etDescription = view.findViewById(R.id.et_description);
            etPrice = view.findViewById(R.id.et_price);
            etLocation = view.findViewById(R.id.et_location);
            spinnerCategory = view.findViewById(R.id.spinner_category);
            spinnerCondition = view.findViewById(R.id.spinner_condition);
            cbNegotiable = view.findViewById(R.id.cb_negotiable);
            btnSelectImage = view.findViewById(R.id.btn_select_image);
            btnGetLocation = view.findViewById(R.id.btn_get_location);
            btnPublish = view.findViewById(R.id.btn_publish);

            Log.d(TAG, "✅ AddProductFragment views initialized");
        } catch (Exception e) {
            Log.w(TAG, "Some AddProductFragment views not found: " + e.getMessage());
        }
    }

    private void initUtils() {
        prefsManager = SharedPrefsManager.getInstance(requireContext());
    }

    private void initLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    }

    // ✅ FIXED: Use correct callback type for Map instead of Category
    private void loadCategories() {
        Log.d(TAG, "Loading categories from backend...");

        ApiClient.getApiService().getCategories().enqueue(new Callback<StandardResponse<List<Map<String, Object>>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<List<Map<String, Object>>>> call,
                                   @NonNull Response<StandardResponse<List<Map<String, Object>>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Map<String, Object>> categoriesData = response.body().getData();
                    if (categoriesData != null) {
                        categories.clear();

                        // Convert Map to Category objects
                        for (Map<String, Object> categoryData : categoriesData) {
                            try {
                                Category category = new Category();

                                // Parse ID safely
                                if (categoryData.get("id") instanceof Number) {
                                    category.setId(((Number) categoryData.get("id")).longValue());
                                }

                                category.setName((String) categoryData.get("name"));
                                category.setDescription((String) categoryData.get("description"));
                                category.setIcon((String) categoryData.get("icon"));

                                // Parse isActive safely
                                Boolean isActive = (Boolean) categoryData.get("isActive");
                                category.setActive(isActive != null ? isActive : true);

                                categories.add(category);
                            } catch (Exception e) {
                                Log.w(TAG, "Error parsing category: " + categoryData, e);
                            }
                        }

                        setupCategorySpinner();
                        Log.d(TAG, "✅ Categories loaded: " + categories.size());
                    } else {
                        Log.e(TAG, "❌ Categories data is null");
                        loadSampleCategories();
                    }
                } else {
                    Log.e(TAG, "❌ Failed to load categories - Response not successful");
                    loadSampleCategories();
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<List<Map<String, Object>>>> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Categories API error", t);
                loadSampleCategories();
            }
        });
    }

    private void loadSampleCategories() {
        categories.clear();
        categories.add(new Category(1L, "Electronics", "Phones, laptops, gadgets", "smartphone", true));
        categories.add(new Category(2L, "Fashion", "Clothing and accessories", "fashion", true));
        categories.add(new Category(3L, "Home & Garden", "Home decor and garden", "home", true));
        categories.add(new Category(4L, "Books", "Books and educational materials", "books", true));
        categories.add(new Category(5L, "Sports", "Sports and outdoor equipment", "sports", true));
        categories.add(new Category(6L, "Beauty", "Beauty and health products", "beauty", true));
        categories.add(new Category(7L, "Vehicles", "Cars and motorbikes", "vehicles", true));
        categories.add(new Category(8L, "Toys", "Toys and kids items", "toys", true));

        setupCategorySpinner();
        Log.d(TAG, "✅ Sample categories loaded");
    }

    private void setupCategorySpinner() {
        List<String> categoryNames = new ArrayList<>();
        categoryNames.add("Select Category");

        for (Category category : categories) {
            categoryNames.add(category.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, categoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        if (spinnerCategory != null) {
            spinnerCategory.setAdapter(adapter);
        }
    }

    private void setupConditionSpinner() {
        String[] conditions = {"Select Condition", "New", "Like New", "Good", "Fair", "Poor"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, conditions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        if (spinnerCondition != null) {
            spinnerCondition.setAdapter(adapter);
        }
    }

    private void setupListeners() {
        if (btnSelectImage != null) {
            btnSelectImage.setOnClickListener(v -> selectImage());
        }

        if (btnGetLocation != null) {
            btnGetLocation.setOnClickListener(v -> getCurrentLocation());
        }

        if (btnPublish != null) {
            btnPublish.setOnClickListener(v -> {
                if (validateForm()) {
                    publishProduct();
                }
            });
        }
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                Log.d(TAG, "✅ Image selected: " + selectedImageUri);

                // ✅ FIXED: Display selected image immediately
                displaySelectedImage();

                // Update button status
                updateImageButton("📷 Image Selected", true);

                // Upload to backend
                uploadImageToBackend();
            }
        }
    }

    // ✅ FIXED: Method to display selected image immediately
    private void displaySelectedImage() {
        if (selectedImageUri != null && ivProductImage != null) {
            try {
                Glide.with(this)
                        .load(selectedImageUri)
                        .centerCrop()
                        .placeholder(R.drawable.ic_placeholder_image)
                        .error(R.drawable.ic_placeholder_image)
                        .into(ivProductImage);

                Log.d(TAG, "✅ Selected image displayed");
            } catch (Exception e) {
                Log.e(TAG, "❌ Error displaying image", e);
                if (ivProductImage != null) {
                    ivProductImage.setImageResource(R.drawable.ic_placeholder_image);
                }
            }
        }
    }

    private void uploadImageToBackend() {
        if (selectedImageUri == null || isUploading) return;

        isUploading = true;
        updateImageButton("⏳ Uploading...", false);

        try {
            File imageFile = createFileFromUri(selectedImageUri);
            if (imageFile == null) {
                resetImageButton();
                showError("Failed to prepare image file");
                return;
            }

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);

            ApiClient.getApiService().uploadProductImage(imagePart).enqueue(new Callback<StandardResponse<Map<String, String>>>() {
                @Override
                public void onResponse(@NonNull Call<StandardResponse<Map<String, String>>> call,
                                       @NonNull Response<StandardResponse<Map<String, String>>> response) {
                    isUploading = false;

                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Map<String, String> data = response.body().getData();
                        if (data != null && data.containsKey("imageUrl")) {
                            uploadedImageUrl = data.get("imageUrl");
                            updateImageButton("✅ Image Ready", true);
                            Toast.makeText(getContext(), "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "✅ Image uploaded successfully: " + uploadedImageUrl);
                        }
                    } else {
                        resetImageButton();
                        showError("Image upload failed: " + response.code());
                        Log.e(TAG, "❌ Image upload failed: " + response.code());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<StandardResponse<Map<String, String>>> call, @NonNull Throwable t) {
                    isUploading = false;
                    resetImageButton();
                    showError("Upload error: " + t.getMessage());
                    Log.e(TAG, "❌ Image upload error", t);
                }
            });

        } catch (Exception e) {
            isUploading = false;
            resetImageButton();
            showError("Error preparing image: " + e.getMessage());
            Log.e(TAG, "❌ Image preparation error", e);
        }
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }

        updateLocationButton("⏳ Getting Location...", false);

        try {
            fusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
                updateLocationButton("📍 Get Location", true);

                if (task.isSuccessful() && task.getResult() != null) {
                    Location location = task.getResult();
                    getAddressFromLocation(location.getLatitude(), location.getLongitude());
                } else {
                    showError("Unable to get current location");
                }
            });
        } catch (SecurityException e) {
            updateLocationButton("📍 Get Location", true);
            showError("Location permission denied");
        }
    }

    private void getAddressFromLocation(double latitude, double longitude) {
        try {
            Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String locationText = address.getLocality() + ", " + address.getCountryName();
                if (etLocation != null) {
                    etLocation.setText(locationText);
                }
                Toast.makeText(getContext(), "Location updated: " + locationText, Toast.LENGTH_SHORT).show();
            } else {
                showError("Unable to get address from location");
            }
        } catch (IOException e) {
            Log.e(TAG, "❌ Error getting address", e);
            showError("Error getting address: " + e.getMessage());
        }
    }

    private boolean validateForm() {
        // Title validation
        String title = getTextFromEditText(etTitle);
        if (TextUtils.isEmpty(title)) {
            showError("Please enter product title");
            return false;
        }

        // Price validation
        String priceStr = getTextFromEditText(etPrice);
        if (TextUtils.isEmpty(priceStr)) {
            showError("Please enter price");
            return false;
        }

        try {
            BigDecimal price = new BigDecimal(priceStr);
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                showError("Price must be greater than 0");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Please enter valid price");
            return false;
        }

        // Category validation
        if (spinnerCategory.getSelectedItemPosition() <= 0) {
            showError("Please select category");
            return false;
        }

        // Condition validation
        if (spinnerCondition.getSelectedItemPosition() <= 0) {
            showError("Please select condition");
            return false;
        }

        // Image validation
        if (uploadedImageUrl == null) {
            showError("Please select and upload an image first");
            return false;
        }

        return true;
    }

    // ✅ FIXED: Updated publishProduct with proper data format
    private void publishProduct() {
        updatePublishButton("Publishing...", false);

        try {
            // Check if user is logged in
            Long userId = prefsManager.getUserId();
            if (userId == null || userId <= 0) {
                showError("Please login to publish products");
                updatePublishButton("Publish Product", true);
                return;
            }

            // Create proper ProductRequest matching backend
            Map<String, Object> productRequest = new HashMap<>();
            productRequest.put("title", getTextFromEditText(etTitle));
            productRequest.put("description", getTextFromEditText(etDescription));

            String priceStr = getTextFromEditText(etPrice);
            BigDecimal price = new BigDecimal(priceStr);
            productRequest.put("price", price);

            int categoryIndex = spinnerCategory.getSelectedItemPosition();
            if (categoryIndex > 0 && categoryIndex <= categories.size()) {
                productRequest.put("categoryId", categories.get(categoryIndex - 1).getId());
            }

            // Fix condition format - backend expects enum
            String conditionText = spinnerCondition.getSelectedItem().toString();
            String condition = conditionText.toUpperCase().replace(" ", "_");
            productRequest.put("condition", condition);

            productRequest.put("location", getTextFromEditText(etLocation));

            // ImageUrls as List, not single imageUrl
            List<String> imageUrls = new ArrayList<>();
            if (uploadedImageUrl != null) {
                imageUrls.add(uploadedImageUrl);
            }
            productRequest.put("imageUrls", imageUrls);

            Log.d(TAG, "Publishing product with User-ID: " + userId);
            Log.d(TAG, "Product data: " + productRequest);

            ApiClient.getApiService().createProduct(productRequest).enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                @Override
                public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                       @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                    updatePublishButton("Publish Product", true);

                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(getContext(), "🎉 Product published successfully! 🎉", Toast.LENGTH_LONG).show();
                        clearForm();
                        Log.d(TAG, "✅ Product published successfully");
                    } else {
                        String errorMsg = "Failed to publish product";
                        if (response.body() != null && response.body().getMessage() != null) {
                            errorMsg = response.body().getMessage();
                        }
                        showError(errorMsg);
                        Log.e(TAG, "❌ Publish failed: " + errorMsg);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                    updatePublishButton("Publish Product", true);
                    Log.e(TAG, "❌ Publish failed", t);
                    showError("Network error: " + t.getMessage());
                }
            });

        } catch (Exception e) {
            updatePublishButton("Publish Product", true);
            Log.e(TAG, "❌ Error preparing product data", e);
            showError("Error: " + e.getMessage());
        }
    }

    // Helper Methods
    private String getTextFromEditText(TextInputEditText editText) {
        return editText != null ? editText.getText().toString().trim() : "";
    }

    private void updateImageButton(String text, boolean enabled) {
        if (btnSelectImage != null) {
            btnSelectImage.setText(text);
            btnSelectImage.setEnabled(enabled);
        }
    }

    private void resetImageButton() {
        updateImageButton("📷 Select Image", true);
    }

    private void updateLocationButton(String text, boolean enabled) {
        if (btnGetLocation != null) {
            btnGetLocation.setText(text);
            btnGetLocation.setEnabled(enabled);
        }
    }

    private void updatePublishButton(String text, boolean enabled) {
        if (btnPublish != null) {
            btnPublish.setText(text);
            btnPublish.setEnabled(enabled);
        }
    }

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Error: " + message);
    }

    private void clearForm() {
        if (etTitle != null) etTitle.setText("");
        if (etDescription != null) etDescription.setText("");
        if (etPrice != null) etPrice.setText("");
        if (etLocation != null) etLocation.setText("");
        if (spinnerCategory != null) spinnerCategory.setSelection(0);
        if (spinnerCondition != null) spinnerCondition.setSelection(0);
        if (cbNegotiable != null) cbNegotiable.setChecked(false);
        if (ivProductImage != null) ivProductImage.setImageResource(R.drawable.ic_placeholder_image);

        selectedImageUri = null;
        uploadedImageUrl = null;
        resetImageButton();
    }

    @Nullable
    private File createFileFromUri(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            String fileName = getFileName(uri);
            File file = new File(requireContext().getCacheDir(), fileName);

            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
            }

            inputStream.close();
            return file;
        } catch (IOException e) {
            Log.e(TAG, "❌ Error creating file from URI", e);
            return null;
        }
    }

    @NonNull
    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        return result != null ? result : "image_" + System.currentTimeMillis() + ".jpg";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                showError("Location permission denied");
            }
        }
    }
}