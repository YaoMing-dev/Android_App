// app/src/main/java/com/example/newtrade/ui/product/AddProductFragment.java
package com.example.newtrade.ui.product;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
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
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.adapters.SelectedImageAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.api.ApiService;
import com.example.newtrade.models.PreviewData;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddProductFragment extends Fragment implements SelectedImageAdapter.OnImageActionListener {

    private static final String TAG = "AddProductFragment";
    private static final int REQUEST_IMAGE_PICK = 1001;
    private static final int REQUEST_LOCATION_PERMISSION = 1002;
    private static final int MAX_IMAGES = 10;

    // ✅ ENHANCED: UI Components with new multi-image support
    private TextInputEditText etTitle, etDescription, etPrice, etLocation, etTags;
    private AutoCompleteTextView spinnerCategory, spinnerCondition;

    // ✅ BACKWARD COMPATIBLE: Keep old single image views
    private ImageView ivSelectedImage;
    private Button btnSelectImage;
    private LinearLayout layoutImagePlaceholder;

    // ✅ NEW: Multi-image components
    private RecyclerView rvSelectedImages;
    private Button btnAddMoreImages;
    private SelectedImageAdapter imageAdapter;

    private Button btnGetLocation, btnPreview, btnPublish;
    private MaterialCheckBox cbNegotiable, cbTermsConditions, cbDataProcessing;

    // ✅ ENHANCED: Data & Utils with multi-image support
    private SharedPrefsManager prefsManager;
    private FusedLocationProviderClient fusedLocationClient;

    // ✅ BACKWARD COMPATIBLE: Keep single image vars for compatibility
    private Uri selectedImageUri;
    private String uploadedImageUrl;

    // ✅ NEW: Multi-image data
    private List<Uri> selectedImageUris;
    private List<String> uploadedImageUrls;

    private boolean isPublishing = false;
    private boolean useMultipleImages = false; // Feature flag

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_product, container, false);
        selectedImageUris = new ArrayList<>();
        uploadedImageUrls = new ArrayList<>();
        initViews(view);
        setupSpinners();
        setupImageRecyclerView();
        setupListeners();

        prefsManager = SharedPrefsManager.getInstance(requireContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // ✅ NEW: Initialize multi-image lists
        selectedImageUris = new ArrayList<>();
        uploadedImageUrls = new ArrayList<>();

        Log.d(TAG, "AddProductFragment created with enhanced image support");
        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ✅ NEW: Handle preview result
        getParentFragmentManager().setFragmentResultListener("preview_result", this, (requestKey, result) -> {
            boolean publishNow = result.getBoolean("publish_now", false);
            if (publishNow) {
                publishProduct();
            }
        });
    }

    private void initViews(View view) {
        // ✅ KEEP: Original form fields
        etTitle = view.findViewById(R.id.et_title);
        etDescription = view.findViewById(R.id.et_description);
        etPrice = view.findViewById(R.id.et_price);
        etLocation = view.findViewById(R.id.et_location);
        etTags = view.findViewById(R.id.et_tags);

        spinnerCategory = view.findViewById(R.id.spinner_category);
        spinnerCondition = view.findViewById(R.id.spinner_condition);

        // ✅ KEEP: Original single image views for compatibility
        ivSelectedImage = view.findViewById(R.id.iv_selected_image);
        btnSelectImage = view.findViewById(R.id.btn_select_image);
        layoutImagePlaceholder = view.findViewById(R.id.layout_image_placeholder);

        // ✅ NEW: Multi-image views
        rvSelectedImages = view.findViewById(R.id.rv_selected_images);
        btnAddMoreImages = view.findViewById(R.id.btn_add_more_images);

        // ✅ KEEP: Original buttons
        btnGetLocation = view.findViewById(R.id.btn_get_location);
        btnPreview = view.findViewById(R.id.btn_preview);
        btnPublish = view.findViewById(R.id.btn_publish);

        cbNegotiable = view.findViewById(R.id.cb_negotiable);
        cbTermsConditions = view.findViewById(R.id.cb_terms_conditions);
        cbDataProcessing = view.findViewById(R.id.cb_data_processing);
    }

    private void setupSpinners() {
        // ✅ KEEP: Original spinner setup unchanged
        String[] categories = {
                "Electronics", "Fashion", "Home & Garden",
                "Sports", "Books", "Automotive", "Other"
        };

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, categories);
        spinnerCategory.setAdapter(categoryAdapter);

        String[] conditions = {"New", "Like New", "Good", "Fair", "Poor"};
        ArrayAdapter<String> conditionAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, conditions);
        spinnerCondition.setAdapter(conditionAdapter);
    }

    // ✅ NEW: Setup multi-image RecyclerView
    private void setupImageRecyclerView() {
        // ✅ FIXED: Đảm bảo selectedImageUris không null
        if (selectedImageUris == null) {
            selectedImageUris = new ArrayList<>();
        }

        imageAdapter = new SelectedImageAdapter(selectedImageUris, this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(),
                LinearLayoutManager.HORIZONTAL, false);
        rvSelectedImages.setLayoutManager(layoutManager);
        rvSelectedImages.setAdapter(imageAdapter);

        // ✅ FIXED: Hide RecyclerView initially
        rvSelectedImages.setVisibility(View.GONE);
    }

    private void setupListeners() {
        // ✅ ENHANCED: Image selection with multi-image support
        btnSelectImage.setOnClickListener(v -> selectImages());
        btnAddMoreImages.setOnClickListener(v -> selectImages());

        // ✅ KEEP: Original listeners unchanged
        btnGetLocation.setOnClickListener(v -> getCurrentLocation());
        btnPreview.setOnClickListener(v -> showPreview()); // ✅ ENHANCED
        btnPublish.setOnClickListener(v -> validateAndPublish());

        // ✅ KEEP: Original form validation
        setupFormValidation();
    }

    // ✅ ENHANCED: Image selection with multiple support
    private void selectImages() {
        if (selectedImageUris.size() >= MAX_IMAGES) {
            Toast.makeText(requireContext(),
                    "Maximum " + MAX_IMAGES + " photos allowed", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(Intent.createChooser(intent, "Select Images"), REQUEST_IMAGE_PICK);
        } catch (Exception e) {
            Log.e(TAG, "Error opening image picker", e);
            Toast.makeText(requireContext(), "Cannot open image picker", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            handleImageSelection(data);
        }
    }

    // ✅ ENHANCED: Handle both single and multiple image selection
    private void handleImageSelection(Intent data) {
        // ✅ FIXED: Null check
        if (selectedImageUris == null) {
            selectedImageUris = new ArrayList<>();
        }

        List<Uri> newImages = new ArrayList<>();

        if (data.getClipData() != null) {
            // Multiple images selected
            int count = data.getClipData().getItemCount();
            for (int i = 0; i < count && selectedImageUris.size() < MAX_IMAGES; i++) {
                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                if (imageUri != null && !selectedImageUris.contains(imageUri)) {
                    newImages.add(imageUri);
                }
            }
        } else if (data.getData() != null) {
            // Single image selected
            Uri imageUri = data.getData();
            if (!selectedImageUris.contains(imageUri)) {
                newImages.add(imageUri);
            }
        }

        // Add new images up to the limit
        for (Uri uri : newImages) {
            if (selectedImageUris.size() < MAX_IMAGES) {
                selectedImageUris.add(uri);
            }
        }

        if (!newImages.isEmpty()) {
            // ✅ ENHANCED: Use multi-image mode if more than 1 image
            useMultipleImages = selectedImageUris.size() > 1;

            // ✅ BACKWARD COMPATIBLE: Set first image as main for compatibility
            if (!selectedImageUris.isEmpty()) {
                selectedImageUri = selectedImageUris.get(0);
            }

            updateImageViews();
            validateForm();

            Log.d(TAG, "Images selected: " + selectedImageUris.size() + ", Multi-mode: " + useMultipleImages);
        }
    }

    // ✅ ENHANCED: Update image views based on mode
    private void updateImageViews() {
        // ✅ FIXED: Null check
        if (selectedImageUris == null) {
            selectedImageUris = new ArrayList<>();
        }

        if (selectedImageUris.isEmpty()) {
            // No images - show placeholder
            layoutImagePlaceholder.setVisibility(View.VISIBLE);
            ivSelectedImage.setVisibility(View.GONE);
            rvSelectedImages.setVisibility(View.GONE);
            btnAddMoreImages.setVisibility(View.GONE);

        } else if (useMultipleImages) {
            // Multiple images - use RecyclerView
            layoutImagePlaceholder.setVisibility(View.GONE);
            ivSelectedImage.setVisibility(View.GONE);
            rvSelectedImages.setVisibility(View.VISIBLE);
            btnAddMoreImages.setVisibility(selectedImageUris.size() < MAX_IMAGES ? View.VISIBLE : View.GONE);

            // ✅ FIXED: Safe adapter update
            if (imageAdapter != null) {
                imageAdapter.updateData(selectedImageUris);
            }

        } else {
            // Single image - use original ImageView for compatibility
            layoutImagePlaceholder.setVisibility(View.GONE);
            ivSelectedImage.setVisibility(View.VISIBLE);
            rvSelectedImages.setVisibility(View.GONE);
            btnAddMoreImages.setVisibility(View.VISIBLE);

            // Load first image into original ImageView
            if (getContext() != null && !selectedImageUris.isEmpty()) {
                Glide.with(this)
                        .load(selectedImageUris.get(0))
                        .centerCrop()
                        .placeholder(R.drawable.image_placeholder)
                        .error(R.drawable.image_error)
                        .into(ivSelectedImage);
            }
        }
    }

    // ✅ NEW: Image adapter callbacks
    @Override
    public void onImageRemove(int position) {
        // ✅ FIXED: Null và bounds check
        if (selectedImageUris == null || position < 0 || position >= selectedImageUris.size()) {
            return;
        }

        selectedImageUris.remove(position);

        // Remove corresponding uploaded URL if exists
        if (uploadedImageUrls != null && position < uploadedImageUrls.size()) {
            uploadedImageUrls.remove(position);
        }

        // Update compatibility vars
        selectedImageUri = selectedImageUris.isEmpty() ? null : selectedImageUris.get(0);
        useMultipleImages = selectedImageUris.size() > 1;

        updateImageViews();
        validateForm();

        Log.d(TAG, "Image removed at position " + position + ", remaining: " + selectedImageUris.size());
    }

    @Override
    public void onImageClick(int position) {
        // ✅ FUTURE: Could implement full-screen image viewer
        Toast.makeText(requireContext(), "Image " + (position + 1) + " of " + selectedImageUris.size(),
                Toast.LENGTH_SHORT).show();
    }

    // ✅ ENHANCED: Preview functionality (FR-2.1.5)
    private void showPreview() {
        if (!validateForm()) {
            return;
        }

        try {
            // ✅ NEW: Create preview data
            PreviewData previewData = new PreviewData();
            previewData.setTitle(getTextFromEditText(etTitle));
            previewData.setDescription(getTextFromEditText(etDescription));
            previewData.setPrice(getTextFromEditText(etPrice));
            previewData.setCategory(spinnerCategory.getText().toString());
            previewData.setCondition(spinnerCondition.getText().toString());
            previewData.setLocation(getTextFromEditText(etLocation));
            previewData.setTags(getTextFromEditText(etTags));
            previewData.setNegotiable(cbNegotiable.isChecked());
            previewData.setImageUris(new ArrayList<>(selectedImageUris));

            // ✅ Navigate to preview using existing nav graph
            Bundle bundle = new Bundle();
            bundle.putParcelable("preview_data", previewData);

            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_addProduct_to_productPreview, bundle);

            Log.d(TAG, "✅ Navigated to preview with " + selectedImageUris.size() + " images");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error navigating to preview", e);
            Toast.makeText(requireContext(), "Error opening preview", Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ KEEP: Original location functionality unchanged
    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }

        btnGetLocation.setEnabled(false);
        btnGetLocation.setText("Getting...");

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    btnGetLocation.setEnabled(true);
                    btnGetLocation.setText("GPS");

                    if (location != null) {
                        String locationText = String.format("%.4f, %.4f", location.getLatitude(), location.getLongitude());
                        etLocation.setText(locationText);
                        Log.d(TAG, "Location obtained: " + locationText);
                        Toast.makeText(requireContext(), "Location updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    btnGetLocation.setEnabled(true);
                    btnGetLocation.setText("GPS");
                    Log.e(TAG, "Failed to get location", e);
                    Toast.makeText(requireContext(), "Failed to get location", Toast.LENGTH_SHORT).show();
                });
    }

    // ✅ KEEP: Original validation with image check enhanced
    private boolean validateForm() {
        boolean isValid = true;

        if (TextUtils.isEmpty(getTextFromEditText(etTitle))) {
            etTitle.setError("Title is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(getTextFromEditText(etDescription))) {
            etDescription.setError("Description is required");
            isValid = false;
        }

        String priceStr = getTextFromEditText(etPrice);
        if (TextUtils.isEmpty(priceStr)) {
            etPrice.setError("Price is required");
            isValid = false;
        } else {
            try {
                BigDecimal price = new BigDecimal(priceStr);
                if (price.compareTo(BigDecimal.ZERO) <= 0) {
                    etPrice.setError("Price must be greater than 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                etPrice.setError("Invalid price format");
                isValid = false;
            }
        }

        if (TextUtils.isEmpty(getTextFromEditText(etLocation))) {
            etLocation.setError("Location is required");
            isValid = false;
        }

        // ✅ FIXED: Safe image check
        if (selectedImageUris == null || selectedImageUris.isEmpty()) {
            Toast.makeText(requireContext(), "Please select at least 1 product image", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // ✅ KEEP: Enable/disable publish button
        btnPublish.setEnabled(isValid && cbTermsConditions.isChecked() && cbDataProcessing.isChecked());

        return isValid;
    }

    private void validateAndPublish() {
        if (isPublishing) return;

        if (!validateForm()) return;

        if (!cbTermsConditions.isChecked()) {
            Toast.makeText(requireContext(), "Please accept Terms & Conditions", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!cbDataProcessing.isChecked()) {
            Toast.makeText(requireContext(), "Please accept Data Processing terms", Toast.LENGTH_SHORT).show();
            return;
        }

        publishProduct();
    }

    // ✅ ENHANCED: Publish with multiple images support
    private void publishProduct() {
        updatePublishButton("Publishing...", false);

        try {
            Long userId = prefsManager.getUserId();
            if (userId == null || userId <= 0) {
                showError("Please login to publish products");
                updatePublishButton("Publish Listing", true);
                return;
            }

            // ✅ ENHANCED: Upload all images if any selected
            if (!selectedImageUris.isEmpty() && uploadedImageUrls.size() != selectedImageUris.size()) {
                uploadAllImages();
            } else {
                createProductOnServer();
            }

        } catch (Exception e) {
            updatePublishButton("Publish Listing", true);
            Log.e(TAG, "❌ Error in publish workflow", e);
            showError("Error: " + e.getMessage());
        }
    }

    // ✅ NEW: Upload all images sequentially
    private void uploadAllImages() {
        Log.d(TAG, "Uploading " + selectedImageUris.size() + " images...");
        uploadedImageUrls.clear();

        AtomicInteger uploadCount = new AtomicInteger(0);
        int totalImages = selectedImageUris.size();

        for (int i = 0; i < totalImages; i++) {
            final int index = i;
            Uri imageUri = selectedImageUris.get(i);
            uploadSingleImage(imageUri, index, uploadCount, totalImages);
        }
    }

    private void uploadSingleImage(Uri imageUri, int index, AtomicInteger uploadCount, int totalImages) {
        try {
            File imageFile = createTempFileFromUri(imageUri);
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);

            ApiService apiService = ApiClient.getApiService();
            Call<StandardResponse<Map<String, String>>> call = apiService.uploadProductImage(imagePart);

            call.enqueue(new Callback<StandardResponse<Map<String, String>>>() {
                @Override
                public void onResponse(Call<StandardResponse<Map<String, String>>> call,
                                       Response<StandardResponse<Map<String, String>>> response) {

                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<Map<String, String>> standardResponse = response.body();
                        if (standardResponse.isSuccess()) {
                            Map<String, String> data = standardResponse.getData();
                            String imageUrl = data.get("imageUrl");

                            // ✅ Ensure proper ordering
                            synchronized (uploadedImageUrls) {
                                // Extend list if needed
                                while (uploadedImageUrls.size() <= index) {
                                    uploadedImageUrls.add(null);
                                }
                                uploadedImageUrls.set(index, imageUrl);
                            }

                            int completed = uploadCount.incrementAndGet();
                            updatePublishButton("Uploading " + completed + "/" + totalImages + "...", false);

                            Log.d(TAG, "✅ Image " + (index + 1) + " uploaded: " + imageUrl);

                            if (completed == totalImages) {
                                // ✅ BACKWARD COMPATIBLE: Set first image as main
                                if (!uploadedImageUrls.isEmpty() && uploadedImageUrls.get(0) != null) {
                                    uploadedImageUrl = uploadedImageUrls.get(0);
                                }
                                createProductOnServer();
                            }
                        } else {
                            handleUploadError("Failed to upload image " + (index + 1) + ": " + standardResponse.getMessage());
                        }
                    } else {
                        handleUploadError("Failed to upload image " + (index + 1) + " to server");
                    }
                }

                @Override
                public void onFailure(Call<StandardResponse<Map<String, String>>> call, Throwable t) {
                    handleUploadError("Network error uploading image " + (index + 1) + ": " + t.getMessage());
                }
            });

        } catch (Exception e) {
            handleUploadError("Error preparing image " + (index + 1) + ": " + e.getMessage());
        }
    }

    private void handleUploadError(String message) {
        updatePublishButton("Publish Listing", true);
        Log.e(TAG, "❌ " + message);
        showError(message);
    }

    // ✅ ENHANCED: Create product with multiple images
    private void createProductOnServer() {
        Log.d(TAG, "Creating product on server with " + uploadedImageUrls.size() + " images...");

        Long userId = prefsManager.getUserId();
        Map<String, Object> productRequest = new HashMap<>();
        productRequest.put("title", getTextFromEditText(etTitle));
        productRequest.put("description", getTextFromEditText(etDescription));

        try {
            BigDecimal price = new BigDecimal(getTextFromEditText(etPrice));
            productRequest.put("price", price);
        } catch (NumberFormatException e) {
            handleUploadError("Invalid price format");
            return;
        }

        productRequest.put("category", spinnerCategory.getText().toString());
        productRequest.put("condition", spinnerCondition.getText().toString());
        productRequest.put("location", getTextFromEditText(etLocation));
        productRequest.put("negotiable", cbNegotiable.isChecked());
        productRequest.put("sellerId", userId);

        String tags = getTextFromEditText(etTags);
        if (!TextUtils.isEmpty(tags)) {
            productRequest.put("tags", tags);
        }

        // ✅ ENHANCED: Include all uploaded image URLs
        if (!uploadedImageUrls.isEmpty()) {
            productRequest.put("imageUrls", uploadedImageUrls);

            // ✅ BACKWARD COMPATIBLE: Also set single imageUrl for compatibility
            productRequest.put("imageUrl", uploadedImageUrls.get(0));
        }

        ApiService apiService = ApiClient.getApiService();
        Call<StandardResponse<Map<String, Object>>> call = apiService.createProduct(productRequest);

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                   Response<StandardResponse<Map<String, Object>>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Map<String, Object>> standardResponse = response.body();
                    if (standardResponse.isSuccess()) {
                        updatePublishButton("Publish Listing", true);

                        Toast.makeText(requireContext(),
                                "✅ Product published successfully with " + uploadedImageUrls.size() + " images!",
                                Toast.LENGTH_LONG).show();

                        clearForm();

                        try {
                            NavController navController = Navigation.findNavController(requireView());
                            navController.navigate(R.id.nav_home);
                            Log.d(TAG, "✅ Navigated to home after successful publish");
                        } catch (Exception e) {
                            Log.e(TAG, "❌ Navigation failed, staying on current fragment", e);
                            Toast.makeText(requireContext(),
                                    "Product published! You can continue using the app.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        showError("Failed to create product: " + standardResponse.getMessage());
                    }
                } else {
                    showError("Failed to create product on server (HTTP " + response.code() + ")");
                    Log.e(TAG, "Product creation response not successful: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                updatePublishButton("Publish Listing", true);
                Log.e(TAG, "❌ Product creation failed", t);
                showError("Network error while creating product: " + t.getMessage());
            }
        });
    }

    // ✅ ENHANCED: Clear form including all images
    private void clearForm() {
        etTitle.setText("");
        etDescription.setText("");
        etPrice.setText("");
        etLocation.setText("");
        etTags.setText("");

        cbNegotiable.setChecked(false);
        cbTermsConditions.setChecked(false);
        cbDataProcessing.setChecked(false);

        // ✅ Clear all image data
        selectedImageUris.clear();
        uploadedImageUrls.clear();
        selectedImageUri = null;
        uploadedImageUrl = null;
        useMultipleImages = false;

        updateImageViews();
        validateForm();

        Log.d(TAG, "Form cleared");
    }

    // ✅ KEEP: Helper methods unchanged
    private void setupFormValidation() {
        // TextWatcher implementation - same as before
        android.text.TextWatcher formWatcher = new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                validateForm();
            }
        };

        etTitle.addTextChangedListener(formWatcher);
        etDescription.addTextChangedListener(formWatcher);
        etPrice.addTextChangedListener(formWatcher);
        etLocation.addTextChangedListener(formWatcher);

        cbTermsConditions.setOnCheckedChangeListener((buttonView, isChecked) -> validateForm());
        cbDataProcessing.setOnCheckedChangeListener((buttonView, isChecked) -> validateForm());
    }

    private String getTextFromEditText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private void updatePublishButton(String text, boolean enabled) {
        btnPublish.setText(text);
        btnPublish.setEnabled(enabled);
        isPublishing = !enabled;
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private File createTempFileFromUri(Uri uri) throws Exception {
        InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
        String fileName = "temp_image_" + System.currentTimeMillis() + ".jpg";
        File tempFile = new File(requireContext().getCacheDir(), fileName);

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
}