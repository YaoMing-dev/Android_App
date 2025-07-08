// app/src/main/java/com/example/newtrade/ui/product/AddProductActivity.java
package com.example.newtrade.ui.product;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.api.ApiService;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.SharedPrefsManager;
import com.example.newtrade.utils.NavigationUtils;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddProductActivity extends AppCompatActivity {

    private static final String TAG = "AddProductActivity";
    private static final int REQUEST_IMAGE_PICK = 1001;

    // UI Components
    private MaterialToolbar toolbar;
    private EditText etTitle, etDescription, etPrice, etLocation;
    private Spinner spCategory, spCondition;
    private ImageView ivProductImage;
    private Button btnSelectImage, btnPublish;

    // Data
    private SharedPrefsManager prefsManager;
    private Uri selectedImageUri;
    private boolean isLoading = false;
    private String uploadedImageUrl = null; // Store uploaded image URL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        initViews();
        setupToolbar();
        setupSpinners();
        setupListeners();

        Log.d(TAG, "AddProductActivity created");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        etPrice = findViewById(R.id.et_price);
        etLocation = findViewById(R.id.et_location);
        spCategory = findViewById(R.id.sp_category);
        spCondition = findViewById(R.id.sp_condition);
        ivProductImage = findViewById(R.id.iv_product_image);
        btnSelectImage = findViewById(R.id.btn_select_image);
        btnPublish = findViewById(R.id.btn_publish);

        prefsManager = SharedPrefsManager.getInstance(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add Product");
        }
    }

    private void setupSpinners() {
        // ✅ FIXED: Sync with Fragment categories
        List<String> categories = new ArrayList<>();
        categories.add("Electronics");
        categories.add("Fashion");
        categories.add("Home & Garden");
        categories.add("Sports");
        categories.add("Books");
        categories.add("Automotive");  // ✅ ADDED: Match with Fragment
        categories.add("Other");

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(categoryAdapter);

        // ✅ KEEP: Condition spinner unchanged
        List<String> conditions = new ArrayList<>();
        conditions.add("New");
        conditions.add("Like New");
        conditions.add("Good");
        conditions.add("Fair");
        conditions.add("Poor");

        ArrayAdapter<String> conditionAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, conditions);
        conditionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCondition.setAdapter(conditionAdapter);
    }

    private void setupListeners() {
        btnSelectImage.setOnClickListener(v -> selectImage());
        btnPublish.setOnClickListener(v -> publishProduct());

        // Form validation
        TextWatcher formWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                updatePublishButton();
            }
        };

        etTitle.addTextChangedListener(formWatcher);
        etDescription.addTextChangedListener(formWatcher);
        etPrice.addTextChangedListener(formWatcher);
        etLocation.addTextChangedListener(formWatcher);
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
                        .centerCrop()
                        .into(ivProductImage);

                // Reset uploaded image URL when new image selected
                uploadedImageUrl = null;
                updatePublishButton();
            }
        }
    }

    private void publishProduct() {
        if (isLoading) return;

        if (!validateForm()) return;

        setLoading(true);

        // Step 1: Upload image first if selected
        if (selectedImageUri != null && uploadedImageUrl == null) {
            uploadImageToServer();
        } else {
            // Step 2: Create product with uploaded image URL
            createProductOnServer();
        }
    }

    private void uploadImageToServer() {
        Log.d(TAG, "Uploading image to server...");

        try {
            // Convert URI to File
            File imageFile = createTempFileFromUri(selectedImageUri);

            // Create RequestBody
            RequestBody requestFile = RequestBody.create(
                    MediaType.parse("image/*"), imageFile);

            MultipartBody.Part imagePart = MultipartBody.Part.createFormData(
                    "file", imageFile.getName(), requestFile);

            // ✅ FIXED: Get userId and add to upload request
            Long userId = prefsManager.getUserId();
            if (userId == null || userId <= 0) {
                setLoading(false);
                showError("Please login to upload images");
                return;
            }

            // Upload to server with User-ID header
            ApiService apiService = ApiClient.getApiService();
            Call<StandardResponse<Map<String, String>>> call =
                    apiService.uploadProductImageWithUserId(imagePart, userId);

            call.enqueue(new Callback<StandardResponse<Map<String, String>>>() {
                @Override
                public void onResponse(Call<StandardResponse<Map<String, String>>> call,
                                       Response<StandardResponse<Map<String, String>>> response) {

                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<Map<String, String>> standardResponse = response.body();

                        if (standardResponse.isSuccess()) {
                            Map<String, String> data = standardResponse.getData();
                            uploadedImageUrl = data.get("imageUrl");

                            Log.d(TAG, "✅ Image uploaded successfully: " + uploadedImageUrl);

                            // Now create product with uploaded image URL
                            createProductOnServer();

                        } else {
                            setLoading(false);
                            showError("Failed to upload image: " + standardResponse.getMessage());
                        }
                    } else {
                        setLoading(false);
                        // ✅ FIXED: Better error logging
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                            Log.e(TAG, "❌ Image upload HTTP " + response.code() + " Error: " + errorBody);
                            showError("Failed to upload image (HTTP " + response.code() + ")");
                        } catch (Exception e) {
                            showError("Failed to upload image to server");
                        }
                    }
                }

                @Override
                public void onFailure(Call<StandardResponse<Map<String, String>>> call, Throwable t) {
                    setLoading(false);
                    Log.e(TAG, "❌ Image upload failed", t);
                    showError("Network error while uploading image: " + t.getMessage());
                }
            });

        } catch (Exception e) {
            setLoading(false);
            Log.e(TAG, "❌ Error preparing image for upload", e);
            showError("Error preparing image: " + e.getMessage());
        }
    }

    private void createProductOnServer() {
        Log.d(TAG, "Creating product on server...");

        // Check if user is logged in
        Long userId = prefsManager.getUserId();
        if (userId == null || userId <= 0) {
            setLoading(false);
            showError("Please login to publish products");
            return;
        }

        // Prepare product data
        Map<String, Object> productRequest = new HashMap<>();
        productRequest.put("title", etTitle.getText().toString().trim());
        productRequest.put("description", etDescription.getText().toString().trim());

        try {
            BigDecimal price = new BigDecimal(etPrice.getText().toString().trim());
            productRequest.put("price", price);
        } catch (NumberFormatException e) {
            setLoading(false);
            showError("Invalid price format");
            return;
        }

        // Category
        int categoryIndex = spCategory.getSelectedItemPosition();
        productRequest.put("categoryId", (long) (categoryIndex + 1)); // Categories start from 1

        // Condition - Convert to backend enum format
        String conditionText = spCondition.getSelectedItem().toString();
        String condition = conditionText.toUpperCase().replace(" ", "_");
        productRequest.put("condition", condition);

        productRequest.put("location", etLocation.getText().toString().trim());

        // ✅ FIXED: Add sellerId field
        productRequest.put("sellerId", userId);

        // Image URLs
        List<String> imageUrls = new ArrayList<>();
        if (uploadedImageUrl != null) {
            imageUrls.add(uploadedImageUrl);
        }
        productRequest.put("imageUrls", imageUrls);

        Log.d(TAG, "Product request: " + productRequest);

        // ✅ FIXED: Call API with User-ID header
        ApiService apiService = ApiClient.getApiService();
        Call<StandardResponse<Map<String, Object>>> call =
                apiService.createProductWithUserId(productRequest, userId);

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                   Response<StandardResponse<Map<String, Object>>> response) {

                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Map<String, Object>> standardResponse = response.body();

                    if (standardResponse.isSuccess()) {
                        Log.d(TAG, "✅ Product created successfully");
                        Toast.makeText(AddProductActivity.this,
                                "🎉 Product published successfully!", Toast.LENGTH_LONG).show();
                        finish();

                    } else {
                        showError("Failed to create product: " + standardResponse.getMessage());
                    }
                } else {
                    // ✅ FIXED: Better error logging
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "❌ Product creation HTTP " + response.code() + " Error: " + errorBody);
                        showError("Failed to create product (HTTP " + response.code() + "): " + errorBody);
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Error reading error body", e);
                        showError("Failed to create product on server (HTTP " + response.code() + ")");
                    }
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                setLoading(false);
                Log.e(TAG, "❌ Product creation failed", t);
                showError("Network error while creating product: " + t.getMessage());
            }
        });
    }

    private File createTempFileFromUri(Uri uri) throws Exception {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        File tempFile = File.createTempFile("upload_image", ".jpg", getCacheDir());

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

    private boolean validateForm() {
        boolean isValid = true;

        if (etTitle.getText().toString().trim().isEmpty()) {
            etTitle.setError("Title is required");
            isValid = false;
        }

        if (etDescription.getText().toString().trim().isEmpty()) {
            etDescription.setError("Description is required");
            isValid = false;
        }

        if (etPrice.getText().toString().trim().isEmpty()) {
            etPrice.setError("Price is required");
            isValid = false;
        }

        if (etLocation.getText().toString().trim().isEmpty()) {
            etLocation.setError("Location is required");
            isValid = false;
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select a product image", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        btnPublish.setEnabled(!loading);
        btnPublish.setText(loading ? "Publishing..." : "Publish Product");
    }

    private void updatePublishButton() {
        boolean isFormValid = !etTitle.getText().toString().trim().isEmpty() &&
                !etDescription.getText().toString().trim().isEmpty() &&
                !etPrice.getText().toString().trim().isEmpty() &&
                !etLocation.getText().toString().trim().isEmpty() &&
                selectedImageUri != null;

        btnPublish.setEnabled(!isLoading && isFormValid);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (NavigationUtils.handleBackButton(this, item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}