// app/src/main/java/com/example/newtrade/ui/product/AddProductFragment.java
package com.example.newtrade.ui.product;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    private MaterialCheckBox cbNegotiable, cbTermsConditions, cbDataProcessing;
    private Button btnSelectImage, btnGetLocation, btnPreview, btnPublish;

    // Data
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
        setupSpinners();
        setupListeners();

        Log.d(TAG, "AddProductFragment created successfully");
    }

    private void initViews(View view) {
        ivProductImage = view.findViewById(R.id.iv_product_image);
        etTitle = view.findViewById(R.id.et_title);
        etDescription = view.findViewById(R.id.et_description);
        etPrice = view.findViewById(R.id.et_price);
        etLocation = view.findViewById(R.id.et_location);
        etTags = view.findViewById(R.id.et_tags);
        spinnerCategory = view.findViewById(R.id.spinner_category);
        spinnerCondition = view.findViewById(R.id.spinner_condition);
        cbNegotiable = view.findViewById(R.id.cb_negotiable);
        cbTermsConditions = view.findViewById(R.id.cb_terms_conditions);
        cbDataProcessing = view.findViewById(R.id.cb_data_processing);
        btnSelectImage = view.findViewById(R.id.btn_select_image);
        btnGetLocation = view.findViewById(R.id.btn_get_location);
        btnPreview = view.findViewById(R.id.btn_preview);
        btnPublish = view.findViewById(R.id.btn_publish);

        Log.d(TAG, "✅ AddProductFragment views initialized");
    }

    private void initUtils() {
        prefsManager = SharedPrefsManager.getInstance(requireContext());
    }

    private void initLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    }

    private void setupSpinners() {
        // Category Spinner
        String[] categories = {"Select Category", "Electronics", "Fashion", "Home & Garden", "Sports", "Books", "Automotive"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Condition Spinner
        String[] conditions = {"Select Condition", "New", "Like New", "Good", "Fair", "Poor"};
        ArrayAdapter<String> conditionAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, conditions);
        conditionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCondition.setAdapter(conditionAdapter);
    }

    private void setupListeners() {
        // Image selection
        if (btnSelectImage != null) {
            btnSelectImage.setOnClickListener(v -> selectImage());
        }

        // Location
        if (btnGetLocation != null) {
            btnGetLocation.setOnClickListener(v -> getCurrentLocation());
        }

        // Preview
        if (btnPreview != null) {
            btnPreview.setOnClickListener(v -> previewListing());
        }

        // Publish
        if (btnPublish != null) {
            btnPublish.setOnClickListener(v -> validateAndPublish());
        }

        // Form validation listeners
        setupFormValidationListeners();
    }

    private void setupFormValidationListeners() {
        // Add text watchers to enable/disable publish button
        if (etTitle != null) {
            etTitle.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    updatePublishButtonState();
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });
        }

        // Checkbox listeners
        if (cbTermsConditions != null) {
            cbTermsConditions.setOnCheckedChangeListener((buttonView, isChecked) -> updatePublishButtonState());
        }

        if (cbDataProcessing != null) {
            cbDataProcessing.setOnCheckedChangeListener((buttonView, isChecked) -> updatePublishButtonState());
        }
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, REQUEST_LOCATION_PERMISSION);
            return;
        }

        updateLocationButton("⏳ Getting Location...", false);

        try {
            fusedLocationClient.getCurrentLocation(
                    com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY,
                    null
            ).addOnCompleteListener(task -> {
                updateLocationButton("📍 Get Location", true);

                if (task.isSuccessful() && task.getResult() != null) {
                    Location location = task.getResult();
                    handleLocationResult(location);
                } else {
                    showError("Unable to get current location");
                }
            });

        } catch (SecurityException e) {
            updateLocationButton("📍 Get Location", true);
            showError("Location permission denied");
        }
    }

    private void handleLocationResult(Location location) {
        try {
            Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(), location.getLongitude(), 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String locationText = address.getAddressLine(0);
                if (etLocation != null) {
                    etLocation.setText(locationText);
                }
                Log.d(TAG, "✅ Location set: " + locationText);
            } else {
                showError("Unable to get address from location");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error getting address", e);
            showError("Error getting location address");
        }
    }

    private void previewListing() {
        if (!validateBasicForm()) {
            return;
        }

        // TODO: Show preview dialog or navigate to preview screen
        Toast.makeText(requireContext(), "Preview functionality - Coming Soon", Toast.LENGTH_SHORT).show();
    }

    private void validateAndPublish() {
        if (!validateCompleteForm()) {
            return;
        }

        publishProduct();
    }

    private boolean validateBasicForm() {
        // Title validation
        String title = getTextFromEditText(etTitle);
        if (TextUtils.isEmpty(title)) {
            showError("Please enter product title");
            etTitle.requestFocus();
            return false;
        }

        // Description validation
        String description = getTextFromEditText(etDescription);
        if (TextUtils.isEmpty(description)) {
            showError("Please enter product description");
            etDescription.requestFocus();
            return false;
        }

        // Price validation
        String priceStr = getTextFromEditText(etPrice);
        if (TextUtils.isEmpty(priceStr)) {
            showError("Please enter price");
            etPrice.requestFocus();
            return false;
        }

        try {
            BigDecimal price = new BigDecimal(priceStr);
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                showError("Price must be greater than 0");
                etPrice.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Please enter valid price");
            etPrice.requestFocus();
            return false;
        }

        // Category validation
        if (spinnerCategory.getSelectedItemPosition() <= 0) {
            showError("Please select a category");
            return false;
        }

        // Condition validation
        if (spinnerCondition.getSelectedItemPosition() <= 0) {
            showError("Please select item condition");
            return false;
        }

        // Location validation
        String location = getTextFromEditText(etLocation);
        if (TextUtils.isEmpty(location)) {
            showError("Please enter or get current location");
            etLocation.requestFocus();
            return false;
        }

        return true;
    }

    private boolean validateCompleteForm() {
        // First check basic form
        if (!validateBasicForm()) {
            return false;
        }

        // Image validation
        if (selectedImageUri == null) {
            showError("Please select at least one product image");
            return false;
        }

        // Terms and conditions validation
        if (cbTermsConditions != null && !cbTermsConditions.isChecked()) {
            showError("Please accept terms and conditions to continue");
            return false;
        }

        // Data processing validation
        if (cbDataProcessing != null && !cbDataProcessing.isChecked()) {
            showError("Please agree to data processing policy");
            return false;
        }

        return true;
    }

    private void publishProduct() {
        updatePublishButton("Publishing...", false);

        try {
            // Check if user is logged in
            Long userId = prefsManager.getUserId();
            if (userId == null || userId <= 0) {
                showError("Please login to publish products");
                updatePublishButton("Publish Listing", true);
                return;
            }

            // Create product request
            Map<String, Object> productRequest = new HashMap<>();
            productRequest.put("title", getTextFromEditText(etTitle));
            productRequest.put("description", getTextFromEditText(etDescription));

            String priceStr = getTextFromEditText(etPrice);
            BigDecimal price = new BigDecimal(priceStr);
            productRequest.put("price", price);

            // Category
            int categoryIndex = spinnerCategory.getSelectedItemPosition();
            productRequest.put("categoryId", (long) categoryIndex); // Mock category ID

            // Condition
            String conditionText = spinnerCondition.getSelectedItem().toString();
            String condition = conditionText.toUpperCase().replace(" ", "_");
            productRequest.put("condition", condition);

            productRequest.put("location", getTextFromEditText(etLocation));

            // Additional fields
            productRequest.put("tags", getTextFromEditText(etTags));
            productRequest.put("isNegotiable", cbNegotiable != null && cbNegotiable.isChecked());

            // Mock image URL for now
            List<String> imageUrls = new ArrayList<>();
            imageUrls.add("https://example.com/product-image.jpg");
            productRequest.put("imageUrls", imageUrls);

            Log.d(TAG, "Publishing product with data: " + productRequest);

            // TODO: Replace with real API call
            mockPublishProduct(productRequest);

        } catch (Exception e) {
            updatePublishButton("Publish Listing", true);
            Log.e(TAG, "❌ Error preparing product data", e);
            showError("Error: " + e.getMessage());
        }
    }

    private void mockPublishProduct(Map<String, Object> productRequest) {
        // Simulate API call delay
        new android.os.Handler().postDelayed(() -> {
            updatePublishButton("Publish Listing", true);

            // Simulate success
            Toast.makeText(requireContext(), "🎉 Product published successfully!", Toast.LENGTH_LONG).show();
            clearForm();

            Log.d(TAG, "✅ Mock product published successfully");
        }, 2000);
    }

    private void clearForm() {
        if (etTitle != null) etTitle.setText("");
        if (etDescription != null) etDescription.setText("");
        if (etPrice != null) etPrice.setText("");
        if (etLocation != null) etLocation.setText("");
        if (etTags != null) etTags.setText("");
        if (spinnerCategory != null) spinnerCategory.setSelection(0);
        if (spinnerCondition != null) spinnerCondition.setSelection(0);
        if (cbNegotiable != null) cbNegotiable.setChecked(false);
        if (cbTermsConditions != null) cbTermsConditions.setChecked(false);
        if (cbDataProcessing != null) cbDataProcessing.setChecked(false);

        selectedImageUri = null;
        if (ivProductImage != null) {
            ivProductImage.setImageResource(R.drawable.ic_placeholder_image);
        }

        updatePublishButtonState();
    }

    private void updatePublishButtonState() {
        if (btnPublish == null) return;

        boolean canPublish = validateBasicForm() &&
                selectedImageUri != null &&
                (cbTermsConditions == null || cbTermsConditions.isChecked()) &&
                (cbDataProcessing == null || cbDataProcessing.isChecked());

        btnPublish.setEnabled(canPublish && !isUploading);
        btnPublish.setAlpha(canPublish ? 1.0f : 0.5f);

        // Update preview button
        if (btnPreview != null) {
            boolean canPreview = validateBasicForm();
            btnPreview.setEnabled(canPreview);
            btnPreview.setAlpha(canPreview ? 1.0f : 0.5f);
        }
    }

    // Helper methods
    private String getTextFromEditText(TextInputEditText editText) {
        return editText != null ? editText.getText().toString().trim() : "";
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                displaySelectedImage();
                updatePublishButtonState();
            }
        }
    }

    private void displaySelectedImage() {
        if (selectedImageUri != null && ivProductImage != null) {
            Glide.with(this)
                    .load(selectedImageUri)
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_placeholder_image)
                    .into(ivProductImage);

            if (btnSelectImage != null) {
                btnSelectImage.setText("✅ Image Selected");
            }

            Log.d(TAG, "✅ Image selected and displayed");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                showError("Location permission is required to get current location");
            }
        }
    }
}