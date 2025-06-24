// File: app/src/main/java/com/example/newtrade/ui/product/AddProductFragment.java
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
    private TextInputEditText etTitle, etDescription, etPrice, etLocation, etTags;
    private Spinner spinnerCategory, spinnerCondition;
    private MaterialCheckBox cbNegotiable;
    private Button btnSelectImage, btnGetLocation, btnPublish;

    // Data
    private List<Category> categories = new ArrayList<>();
    private Uri selectedImageUri;
    private String uploadedImageUrl; // 🔥 LƯU URL SAU KHI UPLOAD
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
            etTags = view.findViewById(R.id.et_tags);
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

    private void loadCategories() {
        Log.d(TAG, "Loading categories from backend...");

        ApiClient.getApiService().getCategories().enqueue(new Callback<StandardResponse<List<Map<String, Object>>>>() {
            @Override
            public void onResponse(Call<StandardResponse<List<Map<String, Object>>>> call,
                                   Response<StandardResponse<List<Map<String, Object>>>> response) {
                try {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        List<Map<String, Object>> categoryData = response.body().getData();
                        categories.clear();

                        for (Map<String, Object> data : categoryData) {
                            Category category = new Category();
                            category.setId(((Number) data.get("id")).longValue());
                            category.setName((String) data.get("name"));
                            categories.add(category);
                        }

                        setupCategorySpinner();
                        Log.d(TAG, "✅ Loaded " + categories.size() + " categories");
                    } else {
                        Log.w(TAG, "❌ Failed to load categories from backend");
                        loadSampleCategories();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error parsing categories", e);
                    loadSampleCategories();
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<List<Map<String, Object>>>> call, Throwable t) {
                Log.e(TAG, "❌ Categories API call failed", t);
                loadSampleCategories();
            }
        });
    }

    private void loadSampleCategories() {
        categories.clear();
        categories.add(new Category(1L, "Electronics", "Electronics devices", "electronics", true));
        categories.add(new Category(2L, "Fashion", "Clothing and accessories", "fashion", true));
        categories.add(new Category(3L, "Home & Garden", "Home decor and garden", "home", true));
        categories.add(new Category(4L, "Books", "Books and educational materials", "books", true));
        categories.add(new Category(5L, "Sports", "Sports and outdoor equipment", "sports", true));
        categories.add(new Category(6L, "Beauty", "Beauty and health products", "beauty", true));
        categories.add(new Category(7L, "Vehicles", "Cars and motorbikes", "vehicles", true));
        categories.add(new Category(8L, "Toys", "Toys and kids items", "toys", true));

        setupCategorySpinner();
        Log.d(TAG, "✅ Loaded sample categories");
    }

    private void setupCategorySpinner() {
        List<String> categoryNames = new ArrayList<>();
        categoryNames.add("Select Category"); // Default option

        for (Category category : categories) {
            categoryNames.add(category.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, categoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupConditionSpinner() {
        String[] conditions = {"Select Condition", "NEW", "LIKE_NEW", "GOOD", "FAIR", "POOR"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, conditions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCondition.setAdapter(adapter);
    }

    private void setupListeners() {
        if (btnSelectImage != null) {
            btnSelectImage.setOnClickListener(v -> selectImage());
        }

        if (btnGetLocation != null) {
            btnGetLocation.setOnClickListener(v -> getCurrentLocation());
        }

        if (btnPublish != null) {
            btnPublish.setOnClickListener(v -> publishProduct());
        }
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }

        btnGetLocation.setText("📍 Getting location...");
        btnGetLocation.setEnabled(false);

        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                btnGetLocation.setText("📍 Get Location");
                btnGetLocation.setEnabled(true);

                if (location != null) {
                    getAddressFromLocation(location.getLatitude(), location.getLongitude());
                } else {
                    Toast.makeText(getContext(), "Unable to get current location", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                btnGetLocation.setText("📍 Get Location");
                btnGetLocation.setEnabled(true);
                Toast.makeText(getContext(), "Failed to get location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } catch (SecurityException e) {
            btnGetLocation.setText("📍 Get Location");
            btnGetLocation.setEnabled(true);
            Log.e(TAG, "❌ Location permission error", e);
        }
    }

    private void getAddressFromLocation(double latitude, double longitude) {
        try {
            Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String fullAddress = address.getAddressLine(0);
                if (fullAddress != null) {
                    etLocation.setText(fullAddress);
                    Toast.makeText(getContext(), "📍 Location detected", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "✅ Location detected: " + fullAddress);
                }
            } else {
                Toast.makeText(getContext(), "Unable to determine address", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e(TAG, "❌ Geocoder error", e);
            Toast.makeText(getContext(), "Geocoder service unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                // Show selected image immediately
                Glide.with(this)
                        .load(selectedImageUri)
                        .centerCrop()
                        .into(ivProductImage);

                // 🔥 UPLOAD NGAY KHI CHỌN XONG
                uploadImageToServer();
            }
        }
    }

    // 🔥 UPLOAD ẢNH LÊN SERVER
    private void uploadImageToServer() {
        if (isUploading) return;

        isUploading = true;
        btnSelectImage.setText("📤 Uploading...");
        btnSelectImage.setEnabled(false);

        try {
            // Convert URI to File
            File imageFile = createFileFromUri(selectedImageUri);
            if (imageFile == null) {
                Toast.makeText(getContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
                resetImageButton();
                return;
            }

            // Create multipart body
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);

            // Call upload API
            ApiClient.getApiService().uploadProductImage(body)
                    .enqueue(new Callback<StandardResponse<Map<String, String>>>() {
                        @Override
                        public void onResponse(Call<StandardResponse<Map<String, String>>> call,
                                               Response<StandardResponse<Map<String, String>>> response) {

                            isUploading = false;

                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                Map<String, String> data = response.body().getData();
                                uploadedImageUrl = data.get("imageUrl"); // 🔥 LƯU URL

                                btnSelectImage.setText("✅ Image Ready");
                                btnSelectImage.setEnabled(true);

                                // 🔥 HIỂN THỊ ẢNH ĐÃ UPLOAD
                                if (uploadedImageUrl != null && !uploadedImageUrl.isEmpty()) {
                                    Glide.with(AddProductFragment.this)
                                            .load(uploadedImageUrl)
                                            .centerCrop()
                                            .into(ivProductImage);
                                }

                                Toast.makeText(getContext(), "Image uploaded successfully! 🎉", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "✅ Image uploaded: " + uploadedImageUrl);
                            } else {
                                Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                                resetImageButton();
                                Log.e(TAG, "❌ Upload failed: " + response.message());
                            }
                        }

                        @Override
                        public void onFailure(Call<StandardResponse<Map<String, String>>> call, Throwable t) {
                            isUploading = false;
                            Log.e(TAG, "❌ Image upload failed", t);
                            Toast.makeText(getContext(), "Upload failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            resetImageButton();
                        }
                    });

        } catch (Exception e) {
            isUploading = false;
            Log.e(TAG, "❌ Error preparing image upload", e);
            Toast.makeText(getContext(), "Error processing image", Toast.LENGTH_SHORT).show();
            resetImageButton();
        }
    }

    private File createFileFromUri(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            String fileName = getFileName(uri);
            if (fileName == null) fileName = "image_" + System.currentTimeMillis() + ".jpg";

            File file = new File(requireContext().getCacheDir(), fileName);
            FileOutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            return file;
        } catch (IOException e) {
            Log.e(TAG, "❌ Error creating file from URI", e);
            return null;
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
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
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void resetImageButton() {
        btnSelectImage.setText("📷 Select Image");
        btnSelectImage.setEnabled(true);
        isUploading = false;
    }

    // 🔥 PUBLISH PRODUCT
    private void publishProduct() {
        if (!validateForm()) {
            return;
        }

        btnPublish.setText("Publishing...");
        btnPublish.setEnabled(false);

        try {
            // Prepare product data
            Map<String, Object> productData = new HashMap<>();
            productData.put("title", etTitle.getText().toString().trim());
            productData.put("description", etDescription.getText().toString().trim());

            // Parse price
            String priceStr = etPrice.getText().toString().trim();
            productData.put("price", Double.parseDouble(priceStr));

            // Get selected category ID
            int categoryIndex = spinnerCategory.getSelectedItemPosition() - 1; // -1 vì có "Select Category"
            if (categoryIndex >= 0 && categoryIndex < categories.size()) {
                productData.put("categoryId", categories.get(categoryIndex).getId());
            }

            // Get selected condition
            String[] conditions = {"NEW", "LIKE_NEW", "GOOD", "FAIR", "POOR"};
            int conditionIndex = spinnerCondition.getSelectedItemPosition() - 1; // -1 vì có "Select Condition"
            if (conditionIndex >= 0 && conditionIndex < conditions.length) {
                productData.put("condition", conditions[conditionIndex]);
            }

            productData.put("location", etLocation.getText().toString().trim());
            productData.put("negotiable", cbNegotiable.isChecked());

            // Add image URLs
            List<String> imageUrls = new ArrayList<>();
            if (uploadedImageUrl != null) {
                imageUrls.add(uploadedImageUrl);
            }
            productData.put("imageUrls", imageUrls);

            // Add tags if any
            String tags = etTags.getText().toString().trim();
            if (!TextUtils.isEmpty(tags)) {
                productData.put("tags", tags);
            }

            // Call API
            ApiClient.getApiService().createProduct(productData).enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                @Override
                public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                       Response<StandardResponse<Map<String, Object>>> response) {

                    btnPublish.setText("Publish Product");
                    btnPublish.setEnabled(true);

                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(getContext(), "Product published successfully! 🎉", Toast.LENGTH_LONG).show();
                        clearForm();
                        Log.d(TAG, "✅ Product published successfully");
                    } else {
                        String errorMsg = response.body() != null ? response.body().getMessage() : "Unknown error";
                        Toast.makeText(getContext(), "Failed to publish: " + errorMsg, Toast.LENGTH_LONG).show();
                        Log.e(TAG, "❌ Publish failed: " + errorMsg);
                    }
                }

                @Override
                public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                    btnPublish.setText("Publish Product");
                    btnPublish.setEnabled(true);

                    Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "❌ Publish network error", t);
                }
            });

        } catch (Exception e) {
            btnPublish.setText("Publish Product");
            btnPublish.setEnabled(true);

            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "❌ Publish error", e);
        }
    }

    private boolean validateForm() {
        // Title
        if (TextUtils.isEmpty(etTitle.getText().toString().trim())) {
            etTitle.setError("Title is required");
            etTitle.requestFocus();
            return false;
        }

        // Description
        if (TextUtils.isEmpty(etDescription.getText().toString().trim())) {
            etDescription.setError("Description is required");
            etDescription.requestFocus();
            return false;
        }

        // Price
        String priceStr = etPrice.getText().toString().trim();
        if (TextUtils.isEmpty(priceStr)) {
            etPrice.setError("Price is required");
            etPrice.requestFocus();
            return false;
        }

        try {
            double price = Double.parseDouble(priceStr);
            if (price < 0) {
                etPrice.setError("Price cannot be negative");
                etPrice.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            etPrice.setError("Invalid price format");
            etPrice.requestFocus();
            return false;
        }

        // Location
        if (TextUtils.isEmpty(etLocation.getText().toString().trim())) {
            etLocation.setError("Location is required");
            etLocation.requestFocus();
            return false;
        }

        // Category
        if (spinnerCategory.getSelectedItemPosition() == 0) {
            Toast.makeText(getContext(), "Please select a category", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Condition
        if (spinnerCondition.getSelectedItemPosition() == 0) {
            Toast.makeText(getContext(), "Please select condition", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Image validation - theo yêu cầu đề thi
        if (uploadedImageUrl == null) {
            Toast.makeText(getContext(), "Please select and upload an image first", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void clearForm() {
        etTitle.setText("");
        etDescription.setText("");
        etPrice.setText("");
        etLocation.setText("");
        etTags.setText("");
        spinnerCategory.setSelection(0);
        spinnerCondition.setSelection(0);
        cbNegotiable.setChecked(false);
        ivProductImage.setImageResource(R.drawable.ic_add_photo_placeholder);
        resetImageButton();
        selectedImageUri = null;
        uploadedImageUrl = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}