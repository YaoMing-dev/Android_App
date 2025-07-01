// app/src/main/java/com/example/newtrade/ui/product/AddProductActivity.java
package com.example.newtrade.ui.product;

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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Category;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.ImageUtils;
import com.example.newtrade.utils.LocationManager;
import com.example.newtrade.utils.SharedPrefsManager;
import com.example.newtrade.utils.ValidationUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.IOException;
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

public class AddProductActivity extends AppCompatActivity implements LocationManager.LocationCallback {
    private static final String TAG = "AddProductActivity";

    // UI Components
    protected Toolbar toolbar;
    protected TextInputLayout tilTitle, tilDescription, tilPrice, tilLocation;
    protected EditText etTitle, etDescription, etPrice, etLocation;
    protected AutoCompleteTextView actvCategory, actvCondition;
    protected RecyclerView rvImages;
    protected Button btnAddImage, btnGetLocation, btnPreview, btnPublish;
    protected ImageView ivLocationIcon;
    protected ProgressBar progressBar;

    // Data
    private List<Category> categories = new ArrayList<>();
    private List<String> selectedImagePaths = new ArrayList<>();
    private Category selectedCategory;
    private Product.ProductCondition selectedCondition;
    private SharedPrefsManager prefsManager;
    private LocationManager locationManager;

    // Location
    private Double currentLatitude;
    private Double currentLongitude;
    private String currentLocationAddress;

    // Image capture
    private Uri photoUri;
    private static final String PHOTO_FILE_NAME = "product_photo.jpg";

    // State
    private boolean isLoading = false;
    private boolean isLocationLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        initViews();
        initUtils();
        setupToolbar();
        setupListeners();
        setupRecyclerView();

        loadCategories();
        setupConditionSpinner();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tilTitle = findViewById(R.id.til_title);
        tilDescription = findViewById(R.id.til_description);
        tilPrice = findViewById(R.id.til_price);
        tilLocation = findViewById(R.id.til_location);
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        etPrice = findViewById(R.id.et_price);
        etLocation = findViewById(R.id.et_location);
        actvCategory = findViewById(R.id.actv_category);
        actvCondition = findViewById(R.id.actv_condition);
        rvImages = findViewById(R.id.rv_images);
        btnAddImage = findViewById(R.id.btn_add_image);
        btnGetLocation = findViewById(R.id.btn_get_location);
        btnPreview = findViewById(R.id.btn_preview);
        btnPublish = findViewById(R.id.btn_publish);
        ivLocationIcon = findViewById(R.id.iv_location_icon);
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
            getSupportActionBar().setTitle("Add Product");
        }
    }

    private void setupListeners() {
        // Text validation listeners
        etTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilTitle.setError(null);
                updatePublishButtonState();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        etDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilDescription.setError(null);
                updatePublishButtonState();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        etPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilPrice.setError(null);
                updatePublishButtonState();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        etLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilLocation.setError(null);
                updatePublishButtonState();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Button listeners
        btnAddImage.setOnClickListener(v -> showImageSourceDialog());
        btnGetLocation.setOnClickListener(v -> getCurrentLocation());
        btnPreview.setOnClickListener(v -> previewListing());
        btnPublish.setOnClickListener(v -> publishProduct());

        // Category selection
        actvCategory.setOnItemClickListener((parent, view, position, id) -> {
            selectedCategory = categories.get(position);
            updatePublishButtonState();
        });

        // Condition selection
        actvCondition.setOnItemClickListener((parent, view, position, id) -> {
            selectedCondition = Product.ProductCondition.values()[position];
            updatePublishButtonState();
        });
    }

    private void setupRecyclerView() {
        rvImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        // TODO: Create ProductImageAdapter
        // ProductImageAdapter adapter = new ProductImageAdapter(selectedImagePaths, this);
        // rvImages.setAdapter(adapter);
    }

    private void loadCategories() {
        Call<StandardResponse<Map<String, Object>>> call = ApiClient.getProductService().getCategories();
        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                handleCategoriesResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to load categories", t);
                Toast.makeText(AddProductActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void handleCategoriesResponse(Response<StandardResponse<Map<String, Object>>> response) {
        try {
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                Map<String, Object> data = response.body().getData();
                List<Map<String, Object>> categoryMaps = (List<Map<String, Object>>) data.get("content");

                if (categoryMaps != null) {
                    categories.clear();
                    for (Map<String, Object> categoryMap : categoryMaps) {
                        Category category = parseCategoryFromMap(categoryMap);
                        if (category != null) {
                            categories.add(category);
                        }
                    }
                    setupCategorySpinner();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing categories response", e);
        }
    }

    private Category parseCategoryFromMap(Map<String, Object> categoryMap) {
        try {
            Category category = new Category();
            category.setId(((Number) categoryMap.get("id")).longValue());
            category.setName((String) categoryMap.get("name"));
            category.setDescription((String) categoryMap.get("description"));
            return category;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing category", e);
            return null;
        }
    }

    private void setupCategorySpinner() {
        List<String> categoryNames = new ArrayList<>();
        for (Category category : categories) {
            categoryNames.add(category.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categoryNames);
        actvCategory.setAdapter(adapter);
    }

    // FR-2.1.1: Required fields validation
    private void setupConditionSpinner() {
        List<String> conditionNames = new ArrayList<>();
        for (Product.ProductCondition condition : Product.ProductCondition.values()) {
            conditionNames.add(condition.getDisplayName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, conditionNames);
        actvCondition.setAdapter(adapter);
    }

    private void showImageSourceDialog() {
        // FR-2.1.4: Image upload (up to 10), supports JPEG/PNG
        if (selectedImagePaths.size() >= Constants.MAX_PRODUCT_IMAGES) {
            Toast.makeText(this, "Maximum " + Constants.MAX_PRODUCT_IMAGES + " images allowed",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo");
        builder.setItems(new CharSequence[]{"Camera", "Gallery"}, (dialog, which) -> {
            if (which == 0) {
                openCamera();
            } else {
                openGallery();
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
            File photoFile = new File(getCacheDir(), PHOTO_FILE_NAME);
            photoUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", photoFile);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(cameraIntent, Constants.REQUEST_CODE_CAMERA);
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, Constants.REQUEST_CODE_GALLERY);
    }

    // FR-2.1.3: Location autofill using GPS (with permission check)
    private void getCurrentLocation() {
        if (!locationManager.hasLocationPermission()) {
            locationManager.requestLocationPermission();
            return;
        }

        setLocationLoading(true);
        locationManager.getCurrentLocation();
    }

    private void setLocationLoading(boolean loading) {
        isLocationLoading = loading;
        btnGetLocation.setEnabled(!loading);
        btnGetLocation.setText(loading ? "Getting..." : "Get Location");
        ivLocationIcon.setVisibility(loading ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == Constants.REQUEST_CODE_CAMERA && photoUri != null) {
                processSelectedImage(photoUri);
            } else if (requestCode == Constants.REQUEST_CODE_GALLERY && data != null) {
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    processSelectedImage(imageUri);
                }
            }
        }
    }

    private void processSelectedImage(Uri imageUri) {
        String fileName = ImageUtils.generateImageFileName();
        String compressedPath = ImageUtils.compressImage(this, imageUri, fileName);

        if (compressedPath != null) {
            selectedImagePaths.add(compressedPath);
            updateImageRecyclerView();
            updatePublishButtonState();
        } else {
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateImageRecyclerView() {
        // TODO: Notify adapter of changes
        Log.d(TAG, "Images count: " + selectedImagePaths.size());
    }

    // FR-2.1.5: Users can preview listing before submission
    private void previewListing() {
        if (!validateForm()) {
            return;
        }

        // TODO: Show preview dialog or navigate to preview activity
        Toast.makeText(this, "Preview - Coming soon", Toast.LENGTH_SHORT).show();
    }

    private void publishProduct() {
        if (!validateForm()) {
            return;
        }

        setLoading(true);

        // Prepare product data
        Map<String, Object> productData = new HashMap<>();
        productData.put("title", etTitle.getText().toString().trim());
        productData.put("description", etDescription.getText().toString().trim());
        productData.put("price", new BigDecimal(etPrice.getText().toString().trim()));
        productData.put("condition", selectedCondition.name());
        productData.put("location", etLocation.getText().toString().trim());
        productData.put("categoryId", selectedCategory.getId());

        if (!selectedImagePaths.isEmpty()) {
            productData.put("imageUrls", selectedImagePaths);
        }

        Call<StandardResponse<Product>> call = ApiClient.getProductService()
                .createProduct(productData, prefsManager.getUserId());

        call.enqueue(new Callback<StandardResponse<Product>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Product>> call,
                                   @NonNull Response<StandardResponse<Product>> response) {
                setLoading(false);
                handlePublishResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Product>> call, @NonNull Throwable t) {
                setLoading(false);
                Log.e(TAG, "Failed to publish product", t);
                showError("Failed to publish product: " + t.getMessage());
            }
        });
    }

    private void handlePublishResponse(Response<StandardResponse<Product>> response) {
        try {
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                Toast.makeText(this, "Product published successfully!", Toast.LENGTH_SHORT).show();

                // Navigate to product detail
                Product product = response.body().getData();
                if (product != null) {
                    Intent intent = new Intent(this, ProductDetailActivity.class);
                    intent.putExtra(Constants.BUNDLE_PRODUCT_ID, product.getId());
                    startActivity(intent);
                }

                finish();
            } else {
                String message = response.body() != null ? response.body().getMessage() : "Failed to publish";
                showError(message);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing publish response", e);
            showError("Failed to publish product");
        }
    }

    private boolean validateForm() {
        boolean isValid = true;

        // FR-2.1.1: Required fields validation
        String title = etTitle.getText().toString().trim();
        if (!ValidationUtils.isValidProductTitle(title)) {
            tilTitle.setError("Title must be between " + Constants.MIN_PRODUCT_TITLE_LENGTH +
                    "-" + Constants.MAX_PRODUCT_TITLE_LENGTH + " characters");
            isValid = false;
        }

        String description = etDescription.getText().toString().trim();
        if (!ValidationUtils.isValidProductDescription(description)) {
            tilDescription.setError("Description must be between " + Constants.MIN_PRODUCT_DESCRIPTION_LENGTH +
                    "-" + Constants.MAX_PRODUCT_DESCRIPTION_LENGTH + " characters");
            isValid = false;
        }

        String priceStr = etPrice.getText().toString().trim();
        if (!ValidationUtils.isValidPrice(priceStr)) {
            tilPrice.setError("Please enter a valid price");
            isValid = false;
        }

        String location = etLocation.getText().toString().trim();
        if (!ValidationUtils.isValidLocation(location)) {
            tilLocation.setError("Please enter a valid location");
            isValid = false;
        }

        if (selectedCategory == null) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (selectedCondition == null) {
            Toast.makeText(this, "Please select item condition", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // FR-2.1.1: At least 1 photo required
        if (selectedImagePaths.isEmpty()) {
            Toast.makeText(this, "Please add at least one photo", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    private void updatePublishButtonState() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String price = etPrice.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        boolean isFormValid = ValidationUtils.isValidProductTitle(title) &&
                ValidationUtils.isValidProductDescription(description) &&
                ValidationUtils.isValidPrice(price) &&
                ValidationUtils.isValidLocation(location) &&
                selectedCategory != null &&
                selectedCondition != null &&
                !selectedImagePaths.isEmpty();

        btnPublish.setEnabled(isFormValid && !isLoading);
        btnPreview.setEnabled(isFormValid && !isLoading);
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        updatePublishButtonState();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // LocationManager.LocationCallback implementation
    @Override
    public void onLocationReceived(Location location) {
        setLocationLoading(false);
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();

        // Reverse geocoding to get address
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(
                    currentLatitude, currentLongitude, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                currentLocationAddress = address.getAddressLine(0);
                etLocation.setText(currentLocationAddress);
            } else {
                etLocation.setText(String.format("%.6f, %.6f", currentLatitude, currentLongitude));
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoder failed", e);
            etLocation.setText(String.format("%.6f, %.6f", currentLatitude, currentLongitude));
        }

        updatePublishButtonState();
    }

    @Override
    public void onLocationError(String error) {
        setLocationLoading(false);
        showError("Failed to get location: " + error);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Constants.PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
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
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.cleanup();
        }
    }
}