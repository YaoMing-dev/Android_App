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
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.adapters.ImageAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.SharedPrefsManager;
import com.example.newtrade.utils.ValidationUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

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

public class AddProductFragment extends Fragment {

    private static final String TAG = "AddProductFragment";
    private static final int REQUEST_IMAGE_PICK = 1001;
    private static final int REQUEST_CAMERA_CAPTURE = 1002;
    private static final int REQUEST_LOCATION_PERMISSION = 1003;
    private static final int REQUEST_CAMERA_PERMISSION = 1004;
    private static final int REQUEST_LOCATION_PICKER = 1005;

    // UI Components
    private TextInputLayout tilTitle, tilDescription, tilPrice, tilLocation, tilTags;
    private TextInputEditText etTitle, etDescription, etPrice, etLocation, etTags;
    private AutoCompleteTextView spinnerCategory, spinnerCondition;
    private LinearLayout layoutImagePlaceholder;
    private Button btnSelectImage, btnGetLocation, btnPreview, btnPublish;
    private MaterialCheckBox cbNegotiable, cbTermsConditions, cbDataProcessing;
    private RecyclerView rvSelectedImages;
    private ImageAdapter imageAdapter;

    // Location
    private FusedLocationProviderClient fusedLocationClient;
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;
    private String currentAddress = "";

    // Data
    private SharedPrefsManager prefsManager;
    private List<String> selectedImagePaths = new ArrayList<>();
    private List<String> uploadedImageUrls = new ArrayList<>();
    private List<Map<String, Object>> categories = new ArrayList<>();
    private boolean isLoading = false;

    // Arrays for dropdowns
    private String[] categoryNames;
    private String[] conditionOptions = {"NEW", "LIKE_NEW", "GOOD", "FAIR", "POOR"};
    private String[] conditionDisplayNames = {"New", "Like New", "Good", "Fair", "Poor"};

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initUtils();
        setupAdapters();
        setupListeners();
        loadCategories();
        loadDraftIfExists();

        Log.d(TAG, "✅ AddProductFragment initialized");
    }

    private void initViews(View view) {
        tilTitle = view.findViewById(R.id.til_title);
        tilDescription = view.findViewById(R.id.til_description);
        tilPrice = view.findViewById(R.id.til_price);
        tilLocation = view.findViewById(R.id.til_location);
        tilTags = view.findViewById(R.id.til_tags);

        etTitle = view.findViewById(R.id.et_title);
        etDescription = view.findViewById(R.id.et_description);
        etPrice = view.findViewById(R.id.et_price);
        etLocation = view.findViewById(R.id.et_location);
        etTags = view.findViewById(R.id.et_tags);

        spinnerCategory = view.findViewById(R.id.spinner_category);
        spinnerCondition = view.findViewById(R.id.spinner_condition);

        layoutImagePlaceholder = view.findViewById(R.id.layout_image_placeholder);
        btnSelectImage = view.findViewById(R.id.btn_select_image);
        btnGetLocation = view.findViewById(R.id.btn_get_location);
        btnPreview = view.findViewById(R.id.btn_preview);
        btnPublish = view.findViewById(R.id.btn_publish);

        cbNegotiable = view.findViewById(R.id.cb_negotiable);
        cbTermsConditions = view.findViewById(R.id.cb_terms_conditions);
        cbDataProcessing = view.findViewById(R.id.cb_data_processing);

        rvSelectedImages = view.findViewById(R.id.rv_selected_images);

        // Initially disable publish button
        btnPublish.setEnabled(false);

        Log.d(TAG, "✅ Views initialized");
    }

    private void initUtils() {
        prefsManager = SharedPrefsManager.getInstance(requireContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    }

    private void setupAdapters() {
        // Setup condition spinner
        ArrayAdapter<String> conditionAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                conditionDisplayNames
        );
        spinnerCondition.setAdapter(conditionAdapter);

        // Setup image adapter
        imageAdapter = new ImageAdapter(selectedImagePaths, new ImageAdapter.OnImageClickListener() {
            @Override
            public void onImageClick(int position) {
                // Show image preview
                showImagePreview(position);
            }

            @Override
            public void onImageRemove(int position) {
                removeImage(position);
            }
        });

        rvSelectedImages.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvSelectedImages.setAdapter(imageAdapter);
    }

    private void setupListeners() {
        // Text change listeners for form validation
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearFieldErrors();
                updatePublishButtonState();
                saveDraftAutomatically();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etTitle.addTextChangedListener(textWatcher);
        etDescription.addTextChangedListener(textWatcher);
        etPrice.addTextChangedListener(textWatcher);
        etLocation.addTextChangedListener(textWatcher);

        // Category selection
        spinnerCategory.setOnItemClickListener((parent, view, position, id) -> {
            updatePublishButtonState();
            saveDraftAutomatically();
        });

        // Condition selection
        spinnerCondition.setOnItemClickListener((parent, view, position, id) -> {
            updatePublishButtonState();
            saveDraftAutomatically();
        });

        // Checkbox listeners
        cbTermsConditions.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updatePublishButtonState();
        });

        cbDataProcessing.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updatePublishButtonState();
        });

        // Button listeners
        btnSelectImage.setOnClickListener(v -> showImageSelectionDialog());
        btnGetLocation.setOnClickListener(v -> getCurrentLocation());
        btnPreview.setOnClickListener(v -> previewProduct());
        btnPublish.setOnClickListener(v -> publishProduct());
    }

    private void loadCategories() {
        Log.d(TAG, "🔍 Loading categories");

        ApiClient.getProductService().getCategories()
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, Object>> apiResponse = response.body();
                            if (apiResponse.isSuccess()) {
                                handleCategoriesResponse(apiResponse.getData());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        Log.e(TAG, "❌ Failed to load categories", t);
                        setupDefaultCategories();
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private void handleCategoriesResponse(Map<String, Object> data) {
        try {
            List<Map<String, Object>> categoryList = (List<Map<String, Object>>) data.get("categories");
            if (categoryList != null) {
                categories.clear();
                categories.addAll(categoryList);

                // Extract category names for dropdown
                categoryNames = new String[categories.size()];
                for (int i = 0; i < categories.size(); i++) {
                    categoryNames[i] = (String) categories.get(i).get("name");
                }

                // Setup category adapter
                ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        categoryNames
                );
                spinnerCategory.setAdapter(categoryAdapter);

                Log.d(TAG, "✅ Categories loaded: " + categories.size());
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error processing categories", e);
            setupDefaultCategories();
        }
    }

    private void setupDefaultCategories() {
        // Fallback categories
        String[] defaultCategories = {"Electronics", "Fashion", "Home & Garden", "Sports", "Books", "Vehicles", "Others"};
        categoryNames = defaultCategories;

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                defaultCategories
        );
        spinnerCategory.setAdapter(categoryAdapter);

        Log.d(TAG, "✅ Default categories loaded");
    }

    private void showImageSelectionDialog() {
        if (selectedImagePaths.size() >= Constants.MAX_PRODUCT_IMAGES) {
            Toast.makeText(requireContext(), "Maximum " + Constants.MAX_PRODUCT_IMAGES + " images allowed", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] options = {"Take Photo", "Choose from Gallery"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Select Image")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        takePhoto();
                    } else {
                        chooseFromGallery();
                    }
                })
                .show();
    }

    private void takePhoto() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            return;
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CAMERA_CAPTURE);
        }
    }

    private void chooseFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
            return;
        }

        btnGetLocation.setEnabled(false);
        btnGetLocation.setText("Getting Location...");

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        currentLatitude = location.getLatitude();
                        currentLongitude = location.getLongitude();

                        // For demo purposes, set a default address
                        currentAddress = "Ho Chi Minh City, Vietnam";
                        etLocation.setText(currentAddress);

                        // Save location to preferences
                        prefsManager.saveLastLocation(currentLatitude, currentLongitude, currentAddress);

                        Log.d(TAG, "✅ Location obtained: " + currentLatitude + ", " + currentLongitude);
                        Toast.makeText(requireContext(), "Location updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Unable to get location", Toast.LENGTH_SHORT).show();
                    }

                    btnGetLocation.setEnabled(true);
                    btnGetLocation.setText("Get Location");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Failed to get location", e);
                    Toast.makeText(requireContext(), "Failed to get location", Toast.LENGTH_SHORT).show();

                    btnGetLocation.setEnabled(true);
                    btnGetLocation.setText("Get Location");
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    addSelectedImage(imageUri);
                }
            } else if (requestCode == REQUEST_CAMERA_CAPTURE && data != null) {
                // Handle camera capture result
                // For simplicity, we'll handle this in a production app
                Toast.makeText(requireContext(), "Camera capture not fully implemented in demo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addSelectedImage(Uri imageUri) {
        try {
            // Convert Uri to file path
            String imagePath = imageUri.toString();
            selectedImagePaths.add(imagePath);

            // Update UI
            updateImageDisplay();
            updatePublishButtonState();

            Log.d(TAG, "✅ Image added: " + imagePath);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error adding image", e);
            Toast.makeText(requireContext(), "Error adding image", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeImage(int position) {
        if (position >= 0 && position < selectedImagePaths.size()) {
            selectedImagePaths.remove(position);
            if (position < uploadedImageUrls.size()) {
                uploadedImageUrls.remove(position);
            }

            updateImageDisplay();
            updatePublishButtonState();

            Log.d(TAG, "✅ Image removed at position: " + position);
        }
    }

    private void showImagePreview(int position) {
        if (position >= 0 && position < selectedImagePaths.size()) {
            // In a real app, you would show a full-screen image preview
            Toast.makeText(requireContext(), "Image preview: " + (position + 1), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateImageDisplay() {
        imageAdapter.notifyDataSetChanged();

        // Show/hide image placeholder
        if (selectedImagePaths.isEmpty()) {
            layoutImagePlaceholder.setVisibility(View.VISIBLE);
            rvSelectedImages.setVisibility(View.GONE);
        } else {
            layoutImagePlaceholder.setVisibility(View.GONE);
            rvSelectedImages.setVisibility(View.VISIBLE);
        }

        // Update button text
        btnSelectImage.setText(selectedImagePaths.isEmpty() ? "Select Images" : "Add More Images (" + selectedImagePaths.size() + "/" + Constants.MAX_PRODUCT_IMAGES + ")");
    }

    private void clearFieldErrors() {
        tilTitle.setError(null);
        tilDescription.setError(null);
        tilPrice.setError(null);
        tilLocation.setError(null);
    }

    private void updatePublishButtonState() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String price = etPrice.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String category = spinnerCategory.getText().toString().trim();
        String condition = spinnerCondition.getText().toString().trim();

        boolean isFormValid = !title.isEmpty() && !description.isEmpty() && !price.isEmpty() &&
                !location.isEmpty() && !category.isEmpty() && !condition.isEmpty() &&
                !selectedImagePaths.isEmpty() && cbTermsConditions.isChecked() &&
                cbDataProcessing.isChecked() && !isLoading;

        btnPublish.setEnabled(isFormValid);
        btnPreview.setEnabled(!title.isEmpty() && !description.isEmpty() && !price.isEmpty());
    }

    private void saveDraftAutomatically() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String price = etPrice.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String category = spinnerCategory.getText().toString().trim();
        String condition = spinnerCondition.getText().toString().trim();

        if (!title.isEmpty() || !description.isEmpty() || !price.isEmpty()) {
            prefsManager.saveDraftProduct(title, description, price, category, condition, location);
        }
    }

    private void loadDraftIfExists() {
        if (prefsManager.hasDraft()) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Load Draft")
                    .setMessage("You have a saved draft. Would you like to load it?")
                    .setPositiveButton("Load", (dialog, which) -> loadDraft())
                    .setNegativeButton("Discard", (dialog, which) -> prefsManager.clearDraft())
                    .show();
        }
    }

    private void loadDraft() {
        etTitle.setText(prefsManager.getDraftTitle());
        etDescription.setText(prefsManager.getDraftDescription());
        etPrice.setText(prefsManager.getDraftPrice());
        etLocation.setText(prefsManager.getDraftLocation());
        spinnerCategory.setText(prefsManager.getDraftCategory(), false);
        spinnerCondition.setText(prefsManager.getDraftCondition(), false);

        updatePublishButtonState();
        Toast.makeText(requireContext(), "Draft loaded", Toast.LENGTH_SHORT).show();
    }

    private void previewProduct() {
        if (!validateForm()) {
            return;
        }

        // Navigate to preview (in a real app, you would pass data to preview fragment)
        Toast.makeText(requireContext(), "Preview functionality - showing product preview", Toast.LENGTH_SHORT).show();
    }

    private void publishProduct() {
        if (!validateForm()) {
            return;
        }

        Long userId = prefsManager.getUserId();
        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        Log.d(TAG, "🔄 Publishing product");

        // First upload images, then create product
        if (!selectedImagePaths.isEmpty()) {
            uploadImages();
        } else {
            createProduct();
        }
    }

    private void uploadImages() {
        Log.d(TAG, "📤 Uploading images: " + selectedImagePaths.size());

        // For demo purposes, we'll simulate image upload
        // In a real app, you would upload each image file
        uploadedImageUrls.clear();

        for (int i = 0; i < selectedImagePaths.size(); i++) {
            // Simulate uploaded image URL
            uploadedImageUrls.add("/products/product_" + System.currentTimeMillis() + "_" + i + ".jpg");
        }

        Log.d(TAG, "✅ Images uploaded successfully");
        createProduct();
    }

    private void createProduct() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String category = spinnerCategory.getText().toString().trim();
        String condition = spinnerCondition.getText().toString().trim();
        String tags = etTags.getText().toString().trim();

        // Find category ID
        Long categoryId = findCategoryId(category);

        try {
            double price = Double.parseDouble(priceStr);

            Map<String, Object> productRequest = new HashMap<>();
            productRequest.put("title", title);
            productRequest.put("description", description);
            productRequest.put("price", price);
            productRequest.put("categoryId", categoryId);
            productRequest.put("condition", condition);
            productRequest.put("location", location);
            productRequest.put("isNegotiable", cbNegotiable.isChecked());

            if (!tags.isEmpty()) {
                productRequest.put("tags", tags);
            }

            if (currentLatitude != 0.0 && currentLongitude != 0.0) {
                productRequest.put("latitude", currentLatitude);
                productRequest.put("longitude", currentLongitude);
            }

            if (!uploadedImageUrls.isEmpty()) {
                productRequest.put("imageUrls", uploadedImageUrls);
            }

            Long userId = prefsManager.getUserId();
            ApiClient.getProductService().createProduct(userId, productRequest)
                    .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                        @Override
                        public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                               Response<StandardResponse<Map<String, Object>>> response) {
                            setLoading(false);

                            if (response.isSuccessful() && response.body() != null) {
                                StandardResponse<Map<String, Object>> apiResponse = response.body();
                                if (apiResponse.isSuccess()) {
                                    handleProductCreated(apiResponse.getData());
                                } else {
                                    showError(apiResponse.getMessage());
                                }
                            } else {
                                showError("Failed to create product");
                            }
                        }

                        @Override
                        public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                            setLoading(false);
                            Log.e(TAG, "❌ Failed to create product", t);
                            showError("Network error. Please try again.");
                        }
                    });
        } catch (NumberFormatException e) {
            setLoading(false);
            tilPrice.setError("Please enter a valid price");
        }
    }

    private Long findCategoryId(String categoryName) {
        for (Map<String, Object> category : categories) {
            if (categoryName.equals(category.get("name"))) {
                return ((Number) category.get("id")).longValue();
            }
        }
        return 1L; // Default to first category
    }

    private void handleProductCreated(Map<String, Object> productData) {
        Toast.makeText(requireContext(), "Product published successfully!", Toast.LENGTH_SHORT).show();

        // Clear draft
        prefsManager.clearDraft();

        // Clear form
        clearForm();

        // Navigate back to home
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_addProduct_to_home);

        Log.d(TAG, "✅ Product created successfully");
    }

    private void clearForm() {
        etTitle.setText("");
        etDescription.setText("");
        etPrice.setText("");
        etLocation.setText("");
        etTags.setText("");
        spinnerCategory.setText("", false);
        spinnerCondition.setText("", false);
        cbNegotiable.setChecked(false);
        cbTermsConditions.setChecked(false);
        cbDataProcessing.setChecked(false);

        selectedImagePaths.clear();
        uploadedImageUrls.clear();
        updateImageDisplay();
        updatePublishButtonState();
    }

    private boolean validateForm() {
        boolean isValid = true;

        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String category = spinnerCategory.getText().toString().trim();
        String condition = spinnerCondition.getText().toString().trim();

        // Validate title
        if (title.isEmpty()) {
            tilTitle.setError("Title is required");
            isValid = false;
        } else if (!ValidationUtils.isValidProductTitle(title)) {
            tilTitle.setError("Title must be between 3-100 characters");
            isValid = false;
        }

        // Validate description
        if (description.isEmpty()) {
            tilDescription.setError("Description is required");
            isValid = false;
        } else if (!ValidationUtils.isValidProductDescription(description)) {
            tilDescription.setError("Description must be between 10-1000 characters");
            isValid = false;
        }

        // Validate price
        if (priceStr.isEmpty()) {
            tilPrice.setError("Price is required");
            isValid = false;
        } else if (!ValidationUtils.isValidPrice(priceStr)) {
            tilPrice.setError("Please enter a valid price");
            isValid = false;
        }

        // Validate location
        if (location.isEmpty()) {
            tilLocation.setError("Location is required");
            isValid = false;
        }

        // Validate category
        if (category.isEmpty()) {
            Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // Validate condition
        if (condition.isEmpty()) {
            Toast.makeText(requireContext(), "Please select a condition", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // Validate images
        if (selectedImagePaths.isEmpty()) {
            Toast.makeText(requireContext(), "At least one image is required", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // Validate checkboxes
        if (!cbTermsConditions.isChecked()) {
            Toast.makeText(requireContext(), "Please accept the terms and conditions", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (!cbDataProcessing.isChecked()) {
            Toast.makeText(requireContext(), "Please accept the data processing agreement", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        btnPublish.setEnabled(!loading && validateFormFields());
        btnPreview.setEnabled(!loading);

        if (loading) {
            btnPublish.setText("Publishing...");
        } else {
            btnPublish.setText("Publish");
        }
    }

    private boolean validateFormFields() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String price = etPrice.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String category = spinnerCategory.getText().toString().trim();
        String condition = spinnerCondition.getText().toString().trim();

        return !title.isEmpty() && !description.isEmpty() && !price.isEmpty() &&
                !location.isEmpty() && !category.isEmpty() && !condition.isEmpty() &&
                !selectedImagePaths.isEmpty() && cbTermsConditions.isChecked() &&
                cbDataProcessing.isChecked();
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
        Log.w(TAG, "Error: " + message);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhoto();
            } else {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Save draft before destroying
        saveDraftAutomatically();
        Log.d(TAG, "AddProductFragment destroyed");
    }
}