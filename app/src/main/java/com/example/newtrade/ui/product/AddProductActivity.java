package com.example.newtrade.ui.product;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.example.newtrade.adapters.ImageAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.location.LocationService;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.ImagePickerHelper;
import com.example.newtrade.utils.NavigationUtils;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddProductActivity extends AppCompatActivity implements
    LocationService.LocationCallback, ImageAdapter.OnImageActionListener {

    private static final String TAG = "AddProductActivity";
    private static final int LOCATION_PERMISSION_REQUEST = 100;
    private static final int IMAGE_PICK_REQUEST = 200;

    // UI Components
    private MaterialToolbar toolbar;
    private EditText etTitle, etDescription, etPrice;
    private AutoCompleteTextView etCategory, etCondition;
    private TextInputLayout tilLocation;
    private EditText etLocation;
    private MaterialButton btnGetLocation, btnAddImages, btnPreview, btnPublish;
    private RecyclerView rvImages;
    private LinearLayout llImageSection;

    // Data
    private SharedPrefsManager prefsManager;
    private LocationService locationService;
    private ImagePickerHelper imagePickerHelper;
    private ImageAdapter imageAdapter;
    private List<Uri> selectedImages = new ArrayList<>();
    private Double latitude, longitude;
    private String locationAddress;
    private boolean isEditMode = false;
    private Long productId;

    // Form data
    private String[] categories = {"Electronics", "Furniture", "Clothing", "Books", "Sports", "Vehicles", "Home & Garden", "Toys", "Art & Crafts", "Other"};
    private String[] conditions = {"New", "Like New", "Good", "Fair", "Poor"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        prefsManager = SharedPrefsManager.getInstance(this);
        locationService = new LocationService(this);
        locationService.setLocationCallback(this);
        imagePickerHelper = new ImagePickerHelper(this);

        checkEditMode();
        initViews();
        setupToolbar();
        setupDropdowns();
        setupImageSection();
        setupListeners();

        Log.d(TAG, "AddProductActivity created - Edit mode: " + isEditMode);
    }

    private void checkEditMode() {
        Intent intent = getIntent();
        productId = intent.getLongExtra("product_id", -1L);
        isEditMode = productId != -1L;

        if (isEditMode) {
            loadProductData();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        etPrice = findViewById(R.id.et_price);
        etCategory = findViewById(R.id.et_category);
        etCondition = findViewById(R.id.et_condition);
        tilLocation = findViewById(R.id.til_location);
        etLocation = findViewById(R.id.et_location);
        btnGetLocation = findViewById(R.id.btn_get_location);
        btnAddImages = findViewById(R.id.btn_add_images);
        btnPreview = findViewById(R.id.btn_preview);
        btnPublish = findViewById(R.id.btn_publish);
        rvImages = findViewById(R.id.rv_images);
        llImageSection = findViewById(R.id.ll_image_section);
    }

    private void setupToolbar() {
        String title = isEditMode ? "Edit Product" : "Add New Product";
        NavigationUtils.setupToolbarWithBackButton(this, toolbar, title);
    }

    private void setupDropdowns() {
        // Category dropdown
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_dropdown_item_1line, categories);
        etCategory.setAdapter(categoryAdapter);

        // Condition dropdown
        ArrayAdapter<String> conditionAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_dropdown_item_1line, conditions);
        etCondition.setAdapter(conditionAdapter);
    }

    private void setupImageSection() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvImages.setLayoutManager(layoutManager);

        imageAdapter = new ImageAdapter(selectedImages, this);
        rvImages.setAdapter(imageAdapter);
    }

    private void setupListeners() {
        btnGetLocation.setOnClickListener(v -> requestLocationPermissionAndGet());
        btnAddImages.setOnClickListener(v -> openImagePicker());
        btnPreview.setOnClickListener(v -> previewProduct());
        btnPublish.setOnClickListener(v -> publishProduct());
    }

    private void requestLocationPermissionAndGet() {
        if (checkLocationPermission()) {
            getCurrentLocation();
        } else {
            requestLocationPermission();
        }
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
               ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
            new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            },
            LOCATION_PERMISSION_REQUEST);
    }

    private void getCurrentLocation() {
        btnGetLocation.setEnabled(false);
        btnGetLocation.setText("Getting location...");
        locationService.getCurrentLocation();
    }

    @Override
    public void onLocationReceived(double latitude, double longitude, String address) {
        runOnUiThread(() -> {
            this.latitude = latitude;
            this.longitude = longitude;
            this.locationAddress = address;

            etLocation.setText(address);
            btnGetLocation.setText("📍 Location Found");
            btnGetLocation.setEnabled(true);

            Log.d(TAG, "✅ Location received: " + address);
        });
    }

    @Override
    public void onLocationError(String error) {
        runOnUiThread(() -> {
            btnGetLocation.setText("Get Location");
            btnGetLocation.setEnabled(true);
            Toast.makeText(this, "Failed to get location: " + error, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "❌ Location error: " + error);
        });
    }

    private void openImagePicker() {
        if (selectedImages.size() >= 10) {
            Toast.makeText(this, "Maximum 10 images allowed", Toast.LENGTH_SHORT).show();
            return;
        }
        imagePickerHelper.openImagePicker(IMAGE_PICK_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK_REQUEST && resultCode == RESULT_OK) {
            List<Uri> newImages = imagePickerHelper.handleImagePickerResult(data);

            for (Uri imageUri : newImages) {
                if (selectedImages.size() < 10) {
                    selectedImages.add(imageUri);
                }
            }

            imageAdapter.notifyDataSetChanged();
            updateImageSection();

            Log.d(TAG, "✅ Images selected: " + selectedImages.size() + " total");
        }
    }

    @Override
    public void onImageRemove(int position) {
        if (position >= 0 && position < selectedImages.size()) {
            selectedImages.remove(position);
            imageAdapter.notifyItemRemoved(position);
            updateImageSection();
        }
    }

    private void updateImageSection() {
        if (selectedImages.isEmpty()) {
            llImageSection.setVisibility(View.GONE);
        } else {
            llImageSection.setVisibility(View.VISIBLE);
            btnAddImages.setText("Add Images (" + selectedImages.size() + "/10)");
        }
    }

    private void previewProduct() {
        if (!validateForm()) return;

        Product product = createProductFromForm();

        Intent intent = new Intent(this, ProductPreviewActivity.class);
        intent.putExtra("product", product);
        intent.putStringArrayListExtra("image_uris", (ArrayList<String>) getImageUriStrings());
        startActivity(intent);
    }

    private void publishProduct() {
        if (!validateForm()) return;

        btnPublish.setEnabled(false);
        btnPublish.setText("Publishing...");

        if (isEditMode) {
            updateProduct();
        } else {
            createNewProduct();
        }
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Required fields validation
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
        } else {
            try {
                double price = Double.parseDouble(etPrice.getText().toString().trim());
                if (price <= 0) {
                    etPrice.setError("Price must be greater than 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                etPrice.setError("Invalid price format");
                isValid = false;
            }
        }

        if (etCategory.getText().toString().trim().isEmpty()) {
            etCategory.setError("Category is required");
            isValid = false;
        }

        if (etCondition.getText().toString().trim().isEmpty()) {
            etCondition.setError("Condition is required");
            isValid = false;
        }

        if (etLocation.getText().toString().trim().isEmpty()) {
            tilLocation.setError("Location is required");
            isValid = false;
        } else {
            tilLocation.setError(null);
        }

        if (!isEditMode && selectedImages.isEmpty()) {
            Toast.makeText(this, "At least 1 image is required", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    private Product createProductFromForm() {
        Product product = new Product();
        product.setTitle(etTitle.getText().toString().trim());
        product.setDescription(etDescription.getText().toString().trim());
        product.setPrice(Double.parseDouble(etPrice.getText().toString().trim()));
        product.setCategory(etCategory.getText().toString().trim());
        product.setCondition(etCondition.getText().toString().trim());
        product.setLocation(etLocation.getText().toString().trim());
        product.setLatitude(latitude);
        product.setLongitude(longitude);
        product.setSellerId(prefsManager.getUserId());
        product.setStatus("available");

        if (isEditMode) {
            product.setId(productId);
        }

        return product;
    }

    private void createNewProduct() {
        Product product = createProductFromForm();

        // Convert product to Map for API
        Map<String, Object> productMap = new HashMap<>();
        productMap.put("title", product.getTitle());
        productMap.put("description", product.getDescription());
        productMap.put("price", product.getPrice());
        productMap.put("category", product.getCategory());
        productMap.put("condition", product.getCondition());
        productMap.put("location", product.getLocation());
        productMap.put("latitude", product.getLatitude());
        productMap.put("longitude", product.getLongitude());
        productMap.put("sellerId", product.getSellerId());
        productMap.put("status", product.getStatus());

        // For now, call API without images (would need to implement image upload)
        ApiClient.getApiService().createProduct(productMap, new ArrayList<>())
            .enqueue(new Callback<StandardResponse<Product>>() {
                @Override
                public void onResponse(@NonNull Call<StandardResponse<Product>> call,
                                       @NonNull Response<StandardResponse<Product>> response) {
                    runOnUiThread(() -> {
                        btnPublish.setEnabled(true);
                        btnPublish.setText("Publish");

                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Product> apiResponse = response.body();

                            if (apiResponse.isSuccess()) {
                                Product createdProduct = apiResponse.getData();
                                Log.d(TAG, "✅ Product created: " + createdProduct.getId());

                                Toast.makeText(AddProductActivity.this, "Product published successfully!", Toast.LENGTH_SHORT).show();

                                // Go back to main activity or product detail
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                showError("Failed to publish product");
                            }
                        } else {
                            showError("Failed to publish product");
                        }
                    });
                }

                @Override
                public void onFailure(@NonNull Call<StandardResponse<Product>> call, @NonNull Throwable t) {
                    runOnUiThread(() -> {
                        btnPublish.setEnabled(true);
                        btnPublish.setText("Publish");
                        showError("Network error: " + t.getMessage());
                        Log.e(TAG, "❌ Create product API error", t);
                    });
                }
            });
    }

    private void updateProduct() {
        Product product = createProductFromForm();

        ApiClient.getApiService().updateProduct(productId, product)
            .enqueue(new Callback<StandardResponse<Product>>() {
                @Override
                public void onResponse(@NonNull Call<StandardResponse<Product>> call,
                                       @NonNull Response<StandardResponse<Product>> response) {
                    runOnUiThread(() -> {
                        btnPublish.setEnabled(true);
                        btnPublish.setText("Update");

                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Product> apiResponse = response.body();

                            if (apiResponse.isSuccess()) {
                                Log.d(TAG, "✅ Product updated: " + productId);

                                Toast.makeText(AddProductActivity.this, "Product updated successfully!", Toast.LENGTH_SHORT).show();

                                setResult(RESULT_OK);
                                finish();
                            } else {
                                showError("Failed to update product");
                            }
                        } else {
                            showError("Failed to update product");
                        }
                    });
                }

                @Override
                public void onFailure(@NonNull Call<StandardResponse<Product>> call, @NonNull Throwable t) {
                    runOnUiThread(() -> {
                        btnPublish.setEnabled(true);
                        btnPublish.setText("Update");
                        showError("Network error: " + t.getMessage());
                        Log.e(TAG, "❌ Update product API error", t);
                    });
                }
            });
    }

    private void loadProductData() {
        // Load existing product data for editing
        ApiClient.getApiService().getProductById(productId)
            .enqueue(new Callback<StandardResponse<Product>>() {
                @Override
                public void onResponse(@NonNull Call<StandardResponse<Product>> call,
                                       @NonNull Response<StandardResponse<Product>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<Product> apiResponse = response.body();

                        if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                            Product product = apiResponse.getData();
                            populateFormWithProduct(product);
                            Log.d(TAG, "✅ Product data loaded for editing");
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<StandardResponse<Product>> call, @NonNull Throwable t) {
                    Log.e(TAG, "❌ Failed to load product data", t);
                    showError("Failed to load product data");
                }
            });
    }

    private void populateFormWithProduct(Product product) {
        etTitle.setText(product.getTitle());
        etDescription.setText(product.getDescription());
        etPrice.setText(String.valueOf(product.getPrice()));
        etCategory.setText(product.getCategory());
        etCondition.setText(product.getCondition());
        etLocation.setText(product.getLocation());

        latitude = product.getLatitude();
        longitude = product.getLongitude();
        locationAddress = product.getLocation();

        btnPublish.setText("Update Product");
    }

    private List<String> getImageUriStrings() {
        List<String> uriStrings = new ArrayList<>();
        for (Uri uri : selectedImages) {
            uriStrings.add(uri.toString());
        }
        return uriStrings;
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission required for product location", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
