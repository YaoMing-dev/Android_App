// app/src/main/java/com/example/newtrade/ui/product/EditProductActivity.java
package com.example.newtrade.ui.product;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.api.ApiService;
import com.example.newtrade.api.ProductService;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

public class EditProductActivity extends AppCompatActivity {

    private static final String TAG = "EditProductActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private ImageView ivProductImage;
    private Button btnChangeImage;
    private TextInputEditText etTitle, etDescription, etPrice, etLocation;
    private Spinner spCategory, spCondition;
    private Button btnCancel, btnSave;
    private View loadingOverlay;

    // Data
    private SharedPrefsManager prefsManager;
    private Long productId;
    private String currentImageUrl;
    private String uploadedImageUrl;
    private Uri selectedImageUri;

    // Services
    private ProductService productService;
    private ApiService apiService;

    // Image picker launcher
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        initViews();
        setupToolbar();
        setupSpinners();
        setupListeners();
        initServices();

        prefsManager = SharedPrefsManager.getInstance(this);

        // Get product data from intent
        getProductDataFromIntent();

        // Setup image picker
        setupImagePicker();

        Log.d(TAG, "EditProductActivity created for product ID: " + productId);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivProductImage = findViewById(R.id.iv_product_image);
        btnChangeImage = findViewById(R.id.btn_change_image);
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        etPrice = findViewById(R.id.et_price);
        etLocation = findViewById(R.id.et_location);
        spCategory = findViewById(R.id.sp_category);
        spCondition = findViewById(R.id.sp_condition);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSave = findViewById(R.id.btn_save);
        loadingOverlay = findViewById(R.id.loading_overlay);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Product");
        }
    }

    private void setupSpinners() {
        // Category Spinner
        String[] categories = {
                "Electronics", "Fashion", "Home & Garden", "Sports & Outdoors",
                "Books", "Automotive", "Toys & Games", "Beauty & Health", "Other"
        };
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(categoryAdapter);

        // Condition Spinner
        String[] conditions = {"New", "Like New", "Good", "Fair", "Poor"};
        ArrayAdapter<String> conditionAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, conditions);
        conditionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCondition.setAdapter(conditionAdapter);
    }

    private void setupListeners() {
        btnChangeImage.setOnClickListener(v -> showImagePickerDialog());
        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveProductChanges());
    }

    private void initServices() {
        productService = ApiClient.getProductService();
        apiService = ApiClient.getApiService();
    }

    private void getProductDataFromIntent() {
        Intent intent = getIntent();
        productId = intent.getLongExtra("product_id", -1L);

        if (productId == -1L) {
            Toast.makeText(this, "Error: Invalid product ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Pre-fill form with existing data
        String title = intent.getStringExtra("product_title");
        String description = intent.getStringExtra("product_description");
        String price = intent.getStringExtra("product_price");
        String location = intent.getStringExtra("product_location");
        String condition = intent.getStringExtra("product_condition");
        String category = intent.getStringExtra("product_category");
        String imageUrl = intent.getStringExtra("product_image_url");

        // Set the data
        if (title != null) etTitle.setText(title);
        if (description != null) etDescription.setText(description);
        if (price != null) etPrice.setText(price.replace(".00", ""));
        if (location != null) etLocation.setText(location);

        // Set spinner selections
        if (condition != null) {
            setSpinnerSelection(spCondition, condition);
        }
        if (category != null) {
            setSpinnerSelection(spCategory, category);
        }

        // Load image
        if (imageUrl != null && !imageUrl.isEmpty()) {
            currentImageUrl = imageUrl;
            loadProductImage(imageUrl);
        }
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void loadProductImage(String imageUrl) {
        if (imageUrl.startsWith("/")) {
            imageUrl = Constants.BASE_URL + imageUrl;
        }

        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_placeholder_image)
                .error(R.drawable.ic_placeholder_image)
                .centerCrop()
                .into(ivProductImage);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            // Display selected image
                            ivProductImage.setImageURI(selectedImageUri);
                            // Upload image
                            uploadNewImage();
                        }
                    }
                }
        );
    }

    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] options = {"Choose from Gallery", "Take Photo"};

        builder.setTitle("Change Product Photo")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Gallery
                            openGallery();
                            break;
                        case 1: // Camera
                            openCamera();
                            break;
                    }
                })
                .show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Image"));
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            imagePickerLauncher.launch(intent);
        } else {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadNewImage() {
        if (selectedImageUri == null) return;

        setLoading(true);

        try {
            // Đọc dữ liệu từ URI thành mảng byte
            byte[] imageBytes = readBytesFromUri(selectedImageUri);

            RequestBody requestFile = RequestBody.create(
                    MediaType.parse(getContentResolver().getType(selectedImageUri)),
                    imageBytes
            );

            MultipartBody.Part imagePart = MultipartBody.Part.createFormData(
                    "file", "product_image.jpg", requestFile);

            apiService.uploadProductImage(imagePart).enqueue(new Callback<StandardResponse<Map<String, String>>>() {
                @Override
                public void onResponse(@NonNull Call<StandardResponse<Map<String, String>>> call,
                                       @NonNull Response<StandardResponse<Map<String, String>>> response) {
                    setLoading(false);

                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<Map<String, String>> standardResponse = response.body();
                        if (standardResponse.isSuccess()) {
                            Map<String, String> data = standardResponse.getData();
                            uploadedImageUrl = data.get("imageUrl");
                            Toast.makeText(EditProductActivity.this, "Image updated successfully", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "✅ Image uploaded: " + uploadedImageUrl);
                        } else {
                            showError("Failed to upload image: " + standardResponse.getMessage());
                        }
                    } else {
                        showError("Failed to upload image");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<StandardResponse<Map<String, String>>> call, @NonNull Throwable t) {
                    setLoading(false);
                    Log.e(TAG, "❌ Image upload failed", t);
                    showError("Network error while uploading image");
                }
            });

        } catch (Exception e) {
            setLoading(false);
            Log.e(TAG, "Error preparing image upload", e);
            showError("Error preparing image");
        }
    }
    private byte[] readBytesFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        inputStream.close();
        return byteBuffer.toByteArray();
    }

    private void saveProductChanges() {
        if (!validateInputs()) {
            return;
        }

        setLoading(true);

        // Prepare updated product data
        Map<String, Object> productUpdate = new HashMap<>();
        productUpdate.put("title", etTitle.getText().toString().trim());
        productUpdate.put("description", etDescription.getText().toString().trim());

        try {
            BigDecimal price = new BigDecimal(etPrice.getText().toString().trim());
            productUpdate.put("price", price);
        } catch (NumberFormatException e) {
            setLoading(false);
            showError("Invalid price format");
            return;
        }

        // Category
        int categoryIndex = spCategory.getSelectedItemPosition();
        productUpdate.put("categoryId", (long) (categoryIndex + 1));

        // Condition
        String conditionText = spCondition.getSelectedItem().toString();
        String condition = conditionText.toUpperCase().replace(" ", "_");
        productUpdate.put("condition", condition);

        productUpdate.put("location", etLocation.getText().toString().trim());

        // Image URLs - use new image if uploaded, otherwise keep current
        List<String> imageUrls = new ArrayList<>();
        if (uploadedImageUrl != null) {
            imageUrls.add(uploadedImageUrl);
        } else if (currentImageUrl != null) {
            imageUrls.add(currentImageUrl);
        }
        productUpdate.put("imageUrls", imageUrls);

        Log.d(TAG, "Updating product with data: " + productUpdate);

        // Call API to update product
        productService.updateProduct(productId, productUpdate).enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Map<String, Object>> standardResponse = response.body();

                    if (standardResponse.isSuccess()) {
                        Log.d(TAG, "✅ Product updated successfully");
                        Toast.makeText(EditProductActivity.this, "Product updated successfully!", Toast.LENGTH_SHORT).show();

                        // Return to previous screen
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        showError("Failed to update product: " + standardResponse.getMessage());
                    }
                } else {
                    showError("Failed to update product");
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                setLoading(false);
                Log.e(TAG, "❌ Product update failed", t);
                showError("Network error while updating product");
            }
        });
    }

    private boolean validateInputs() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String price = etPrice.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            etTitle.requestFocus();
            return false;
        }

        if (description.isEmpty()) {
            etDescription.setError("Description is required");
            etDescription.requestFocus();
            return false;
        }

        if (price.isEmpty()) {
            etPrice.setError("Price is required");
            etPrice.requestFocus();
            return false;
        }

        try {
            BigDecimal priceValue = new BigDecimal(price);
            if (priceValue.compareTo(BigDecimal.ZERO) <= 0) {
                etPrice.setError("Price must be greater than 0");
                etPrice.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            etPrice.setError("Invalid price format");
            etPrice.requestFocus();
            return false;
        }

        if (location.isEmpty()) {
            etLocation.setError("Location is required");
            etLocation.requestFocus();
            return false;
        }

        return true;
    }

    private void setLoading(boolean loading) {
        if (loading) {
            loadingOverlay.setVisibility(View.VISIBLE);
            btnSave.setEnabled(false);
        } else {
            loadingOverlay.setVisibility(View.GONE);
            btnSave.setEnabled(true);
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Error: " + message);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}