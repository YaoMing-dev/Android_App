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
import com.example.newtrade.ui.product.adapter.ProductImageAdapter;
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
    private Toolbar toolbar;
    protected TextInputLayout tilTitle, tilDescription, tilPrice, tilLocation;
    protected EditText etTitle, etDescription, etPrice, etLocation;
    protected AutoCompleteTextView actvCategory, actvCondition;
    private RecyclerView rvImages;
    private ImageView ivAddImage;
    protected Button btnPreview, btnPublish;
    protected ProgressBar progressBar;

    // Data
    private List<Category> categories = new ArrayList<>();
    private List<Product.ProductCondition> conditions = new ArrayList<>();
    protected List<String> selectedImagePaths = new ArrayList<>();
    protected ProductImageAdapter imageAdapter;

    // Selected values
    protected Category selectedCategory;
    protected Product.ProductCondition selectedCondition;
    protected Double currentLatitude;
    protected Double currentLongitude;

    // Utils
    protected SharedPrefsManager prefsManager;
    private LocationManager locationManager;
    private File currentPhotoFile;

    // State
    private boolean isLoading = false;
    private boolean isLocationLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        ApiClient.init(this);
        prefsManager = new SharedPrefsManager(this);
        locationManager = new LocationManager(this, this);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        loadCategories();
        setupConditions();

        Log.d(TAG, "AddProductActivity initialized");
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
        ivAddImage = findViewById(R.id.iv_add_image);

        btnPreview = findViewById(R.id.btn_preview);
        btnPublish = findViewById(R.id.btn_publish);
        progressBar = findViewById(R.id.progress_bar);

        // Initially disable buttons
        btnPreview.setEnabled(false);
        btnPublish.setEnabled(false);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add Product");
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        imageAdapter = new ProductImageAdapter(selectedImagePaths, new ProductImageAdapter.OnImageClickListener() {
            @Override
            public void onImageClick(int position) {
                showImageOptionsDialog(position);
            }

            @Override
            public void onRemoveClick(int position) {
                removeImage(position);
            }
        });

        rvImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvImages.setAdapter(imageAdapter);
    }

    private void setupListeners() {
        // Text watchers for validation
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearFieldErrors();
                updateButtonStates();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etTitle.addTextChangedListener(textWatcher);
        etDescription.addTextChangedListener(textWatcher);
        etPrice.addTextChangedListener(textWatcher);
        etLocation.addTextChangedListener(textWatcher);

        // Category selection
        actvCategory.setOnItemClickListener((parent, view, position, id) -> {
            selectedCategory = categories.get(position);
            updateButtonStates();
        });

        // Condition selection
        actvCondition.setOnItemClickListener((parent, view, position, id) -> {
            selectedCondition = conditions.get(position);
            updateButtonStates();
        });

        // Add image button
        ivAddImage.setOnClickListener(v -> showImagePickerDialog());

        // Location field click
        etLocation.setOnClickListener(v -> showLocationOptionsDialog());

        // Preview button
        btnPreview.setOnClickListener(v -> showPreviewDialog());

        // Publish button
        btnPublish.setOnClickListener(v -> publishProduct());
    }

    private void loadCategories() {
        ApiClient.getProductService().getCategories()
                .enqueue(new Callback<StandardResponse<List<Category>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<List<Category>>> call,
                                           Response<StandardResponse<List<Category>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<List<Category>> apiResponse = response.body();

                            if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                                categories.clear();
                                categories.addAll(apiResponse.getData());
                                setupCategoryAdapter();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<List<Category>>> call, Throwable t) {
                        Log.e(TAG, "Failed to load categories", t);
                        showError("Failed to load categories");
                    }
                });
    }

    private void setupCategoryAdapter() {
        List<String> categoryNames = new ArrayList<>();
        for (Category category : categories) {
            categoryNames.add(category.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, categoryNames);
        actvCategory.setAdapter(adapter);
    }

    private void setupConditions() {
        conditions.clear();
        for (Product.ProductCondition condition : Product.ProductCondition.values()) {
            conditions.add(condition);
        }

        List<String> conditionNames = new ArrayList<>();
        for (Product.ProductCondition condition : conditions) {
            conditionNames.add(condition.getDisplayName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, conditionNames);
        actvCondition.setAdapter(adapter);
    }

    // Image handling methods
    private void showImagePickerDialog() {
        if (selectedImagePaths.size() >= Constants.MAX_PRODUCT_IMAGES) {
            showError("Maximum " + Constants.MAX_PRODUCT_IMAGES + " images allowed");
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_image_picker, null);

        view.findViewById(R.id.btn_camera).setOnClickListener(v -> {
            dialog.dismiss();
            checkCameraPermissionAndCapture();
        });

        view.findViewById(R.id.btn_gallery).setOnClickListener(v -> {
            dialog.dismiss();
            checkStoragePermissionAndPick();
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private void checkCameraPermissionAndCapture() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    Constants.REQUEST_CAMERA_PERMISSION);
        } else {
            captureImage();
        }
    }

    private void checkStoragePermissionAndPick() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    Constants.REQUEST_STORAGE_PERMISSION);
        } else {
            pickFromGallery();
        }
    }

    private void captureImage() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                currentPhotoFile = ImageUtils.createImageFile(this);
                if (currentPhotoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this,
                            "com.example.newtrade.fileprovider", currentPhotoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, Constants.REQUEST_CAPTURE_IMAGE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error creating image file", e);
                showError("Failed to create image file");
            }
        }
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, Constants.REQUEST_PICK_IMAGE);
    }

    // Location handling methods
    private void showLocationOptionsDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_location_picker, null);

        view.findViewById(R.id.btn_current_location).setOnClickListener(v -> {
            dialog.dismiss();
            getCurrentLocation();
        });

        view.findViewById(R.id.btn_manual_location).setOnClickListener(v -> {
            dialog.dismiss();
            etLocation.setEnabled(true);
            etLocation.setFocusable(true);
            etLocation.requestFocus();
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private void getCurrentLocation() {
        if (!locationManager.hasLocationPermission()) {
            locationManager.requestLocationPermission();
            return;
        }

        setLocationLoading(true);
        locationManager.getCurrentLocation();
    }

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
                String locationString = address.getAddressLine(0);
                etLocation.setText(locationString);
                etLocation.setEnabled(false);
            } else {
                etLocation.setText(String.format("%.6f, %.6f", currentLatitude, currentLongitude));
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoder failed", e);
            etLocation.setText(String.format("%.6f, %.6f", currentLatitude, currentLongitude));
        }

        updateButtonStates();
    }

    @Override
    public void onLocationError(String error) {
        setLocationLoading(false);
        showError("Failed to get location: " + error);
    }

    // Validation and publishing methods
    protected boolean validateForm() {
        boolean isValid = true;

        // Validate title
        String title = etTitle.getText().toString().trim();
        if (!ValidationUtils.isValidProductTitle(title)) {
            tilTitle.setError("Title must be between 5-200 characters");
            isValid = false;
        }

        // Validate description
        String description = etDescription.getText().toString().trim();
        if (!ValidationUtils.isValidProductDescription(description)) {
            tilDescription.setError("Description must be between 10-2000 characters");
            isValid = false;
        }

        // Validate price
        String priceStr = etPrice.getText().toString().trim();
        if (!ValidationUtils.isValidPrice(priceStr)) {
            tilPrice.setError("Please enter a valid price");
            isValid = false;
        }

        // Validate location
        String location = etLocation.getText().toString().trim();
        if (location.isEmpty()) {
            tilLocation.setError("Location is required");
            isValid = false;
        }

        // Validate category
        if (selectedCategory == null) {
            showError("Please select a category");
            isValid = false;
        }

        // Validate condition
        if (selectedCondition == null) {
            showError("Please select a condition");
            isValid = false;
        }

        // Validate images
        if (selectedImagePaths.isEmpty()) {
            showError("At least one image is required");
            isValid = false;
        }

        return isValid;
    }

    protected void publishProduct() {
        if (!validateForm()) {
            return;
        }

        setLoading(true);

        // First upload images, then create product
        uploadImagesAndCreateProduct();
    }

    private void uploadImagesAndCreateProduct() {
        List<String> uploadedImageUrls = new ArrayList<>();
        uploadNextImage(0, uploadedImageUrls);
    }

    private void uploadNextImage(int index, List<String> uploadedUrls) {
        if (index >= selectedImagePaths.size()) {
            // All images uploaded, now create product
            createProductWithImages(uploadedUrls);
            return;
        }

        String imagePath = selectedImagePaths.get(index);
        File imageFile = new File(imagePath);

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);

        ApiClient.getProductService().uploadProductImage(imagePart)
                .enqueue(new Callback<StandardResponse<Map<String, String>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, String>>> call,
                                           Response<StandardResponse<Map<String, String>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, String>> apiResponse = response.body();

                            if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                                String imageUrl = apiResponse.getData().get("imageUrl");
                                uploadedUrls.add(imageUrl);

                                // Upload next image
                                uploadNextImage(index + 1, uploadedUrls);
                            } else {
                                setLoading(false);
                                showError("Failed to upload image: " + apiResponse.getMessage());
                            }
                        } else {
                            setLoading(false);
                            showError("Failed to upload image");
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, String>>> call, Throwable t) {
                        setLoading(false);
                        Log.e(TAG, "Image upload failed", t);
                        showError("Network error during image upload");
                    }
                });
    }

    private void createProductWithImages(List<String> imageUrls) {
        Map<String, Object> productRequest = new HashMap<>();
        productRequest.put("title", etTitle.getText().toString().trim());
        productRequest.put("description", etDescription.getText().toString().trim());
        productRequest.put("price", new BigDecimal(etPrice.getText().toString().trim()));
        productRequest.put("condition", selectedCondition.name());
        productRequest.put("location", etLocation.getText().toString().trim());
        productRequest.put("categoryId", selectedCategory.getId());
        productRequest.put("imageUrls", imageUrls);

        if (currentLatitude != null && currentLongitude != null) {
            productRequest.put("latitude", currentLatitude);
            productRequest.put("longitude", currentLongitude);
            productRequest.put("locationRadius", Constants.DEFAULT_LOCATION_RADIUS);
        }

        Long userId = prefsManager.getUserId();

        ApiClient.getProductService().createProduct(productRequest, userId)
                .enqueue(new Callback<StandardResponse<Product>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Product>> call,
                                           Response<StandardResponse<Product>> response) {
                        setLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Product> apiResponse = response.body();

                            if (apiResponse.isSuccess()) {
                                Toast.makeText(AddProductActivity.this,
                                        "Product published successfully!", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                showError(apiResponse.getMessage());
                            }
                        } else {
                            showError("Failed to publish product");
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Product>> call, Throwable t) {
                        setLoading(false);
                        Log.e(TAG, "Create product failed", t);
                        showError("Network error during product creation");
                    }
                });
    }

    // Helper methods
    private void clearFieldErrors() {
        tilTitle.setError(null);
        tilDescription.setError(null);
        tilPrice.setError(null);
        tilLocation.setError(null);
    }

    protected void updateButtonStates() {
        boolean isFormComplete = !etTitle.getText().toString().trim().isEmpty() &&
                !etDescription.getText().toString().trim().isEmpty() &&
                !etPrice.getText().toString().trim().isEmpty() &&
                !etLocation.getText().toString().trim().isEmpty() &&
                selectedCategory != null &&
                selectedCondition != null &&
                !selectedImagePaths.isEmpty() &&
                !isLoading;

        btnPreview.setEnabled(isFormComplete);
        btnPublish.setEnabled(isFormComplete);
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        updateButtonStates();
    }

    private void setLocationLoading(boolean loading) {
        isLocationLoading = loading;
        etLocation.setEnabled(!loading);
        if (loading) {
            etLocation.setText("Getting location...");
        }
    }

    private void removeImage(int position) {
        selectedImagePaths.remove(position);
        imageAdapter.notifyItemRemoved(position);
        updateButtonStates();
    }

    private void showImageOptionsDialog(int position) {
        // Implementation for image options (view full size, remove, etc.)
    }

    private void showPreviewDialog() {
        // Implementation for product preview
        Intent intent = new Intent(this, ProductPreviewActivity.class);
        intent.putExtra("title", etTitle.getText().toString().trim());
        intent.putExtra("description", etDescription.getText().toString().trim());
        intent.putExtra("price", etPrice.getText().toString().trim());
        intent.putExtra("location", etLocation.getText().toString().trim());
        intent.putExtra("category", selectedCategory.getName());
        intent.putExtra("condition", selectedCondition.getDisplayName());
        intent.putStringArrayListExtra("images", new ArrayList<>(selectedImagePaths));
        startActivity(intent);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == Constants.REQUEST_CAPTURE_IMAGE) {
                if (currentPhotoFile != null) {
                    selectedImagePaths.add(currentPhotoFile.getAbsolutePath());
                    imageAdapter.notifyItemInserted(selectedImagePaths.size() - 1);
                    updateButtonStates();
                }
            } else if (requestCode == Constants.REQUEST_PICK_IMAGE) {
                if (data != null && data.getData() != null) {
                    Uri imageUri = data.getData();
                    String imagePath = ImageUtils.getRealPathFromUri(this, imageUri);
                    if (imagePath != null) {
                        selectedImagePaths.add(imagePath);
                        imageAdapter.notifyItemInserted(selectedImagePaths.size() - 1);
                        updateButtonStates();
                    }
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case Constants.REQUEST_CAMERA_PERMISSION:
                    captureImage();
                    break;
                case Constants.REQUEST_STORAGE_PERMISSION:
                    pickFromGallery();
                    break;
                case Constants.REQUEST_LOCATION_PERMISSION:
                    getCurrentLocation();
                    break;
            }
        } else {
            showError("Permission denied");
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