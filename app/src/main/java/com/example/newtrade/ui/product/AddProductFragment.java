// app/src/main/java/com/example/newtrade/ui/product/AddProductFragment.java
package com.example.newtrade.ui.product;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.api.ApiService;
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

    // UI Components - ✅ SỬA THÀNH AutoCompleteTextView
    private TextInputEditText etTitle, etDescription, etPrice, etLocation, etTags;
    private AutoCompleteTextView spinnerCategory, spinnerCondition; // ✅ SỬA TẠI ĐÂY
    private ImageView ivSelectedImage;
    private Button btnSelectImage, btnGetLocation, btnPreview, btnPublish;
    private MaterialCheckBox cbNegotiable, cbTermsConditions, cbDataProcessing;

    // Data & Utils
    private SharedPrefsManager prefsManager;
    private FusedLocationProviderClient fusedLocationClient;
    private Uri selectedImageUri;
    private String uploadedImageUrl;
    private boolean isPublishing = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_product, container, false);

        initViews(view);
        setupSpinners();
        setupListeners();

        prefsManager = SharedPrefsManager.getInstance(requireContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        Log.d(TAG, "AddProductFragment created");
        return view;
    }

    private void initViews(View view) {
        // Form fields
        etTitle = view.findViewById(R.id.et_title);
        etDescription = view.findViewById(R.id.et_description);
        etPrice = view.findViewById(R.id.et_price);
        etLocation = view.findViewById(R.id.et_location);
        etTags = view.findViewById(R.id.et_tags);

        // ✅ SỬA: AutoCompleteTextView thay vì Spinner
        spinnerCategory = view.findViewById(R.id.spinner_category);
        spinnerCondition = view.findViewById(R.id.spinner_condition);

        // Image
        ivSelectedImage = view.findViewById(R.id.iv_selected_image);
        btnSelectImage = view.findViewById(R.id.btn_select_image);

        // Action buttons
        btnGetLocation = view.findViewById(R.id.btn_get_location);
        btnPreview = view.findViewById(R.id.btn_preview);
        btnPublish = view.findViewById(R.id.btn_publish);

        // Checkboxes
        cbNegotiable = view.findViewById(R.id.cb_negotiable);
        cbTermsConditions = view.findViewById(R.id.cb_terms_conditions);
        cbDataProcessing = view.findViewById(R.id.cb_data_processing);
    }

    private void setupSpinners() {
        // ✅ Category AutoCompleteTextView
        String[] categories = {
                "Electronics", "Fashion", "Home & Garden",
                "Sports", "Books", "Automotive", "Other"
        };

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, categories);
        spinnerCategory.setAdapter(categoryAdapter);
        spinnerCategory.setText(categories[2], false); // Default to "Home & Garden"

        // ✅ Condition AutoCompleteTextView
        String[] conditions = {"New", "Like New", "Good", "Fair", "Poor"};

        ArrayAdapter<String> conditionAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, conditions);
        spinnerCondition.setAdapter(conditionAdapter);
        spinnerCondition.setText(conditions[0], false); // Default to "New"
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
        // Text change listeners
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

        // Spinner listeners
        spinnerCategory.setOnItemClickListener((parent, view, position, id) -> updatePublishButtonState());
        spinnerCondition.setOnItemClickListener((parent, view, position, id) -> updatePublishButtonState());
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
                // Display selected image
                Glide.with(this)
                        .load(selectedImageUri)
                        .centerCrop()
                        .into(ivSelectedImage);

                // Show image, hide placeholder
                ivSelectedImage.setVisibility(View.VISIBLE);

                // Reset uploaded URL when new image is selected
                uploadedImageUrl = null;
                updatePublishButtonState();

                Log.d(TAG, "Image selected: " + selectedImageUri);
            }
        }
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

        btnGetLocation.setEnabled(false);
        btnGetLocation.setText("Getting...");

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    btnGetLocation.setEnabled(true);
                    btnGetLocation.setText("GPS");

                    if (location != null) {
                        String locationText = String.format("%.4f, %.4f",
                                location.getLatitude(), location.getLongitude());
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

    private void previewListing() {
        if (!validateForm()) return;
        Toast.makeText(requireContext(), "Preview functionality coming soon", Toast.LENGTH_SHORT).show();
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

    private boolean validateForm() {
        boolean isValid = true;

        // Validate title
        if (TextUtils.isEmpty(getTextFromEditText(etTitle))) {
            etTitle.setError("Title is required");
            isValid = false;
        }

        // Validate description
        if (TextUtils.isEmpty(getTextFromEditText(etDescription))) {
            etDescription.setError("Description is required");
            isValid = false;
        }

        // Validate price
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

        // Validate location
        if (TextUtils.isEmpty(getTextFromEditText(etLocation))) {
            etLocation.setError("Location is required");
            isValid = false;
        }

        // Validate image
        if (selectedImageUri == null) {
            Toast.makeText(requireContext(), "Please select a product image", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
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

            // Step 1: Upload image first if not already uploaded
            if (selectedImageUri != null && uploadedImageUrl == null) {
                uploadImageToServer();
            } else {
                // Step 2: Create product with uploaded image URL
                createProductOnServer();
            }

        } catch (Exception e) {
            updatePublishButton("Publish Listing", true);
            Log.e(TAG, "❌ Error in publish workflow", e);
            showError("Error: " + e.getMessage());
        }
    }

    private void uploadImageToServer() {
        Log.d(TAG, "Uploading image to server...");

        try {
            File imageFile = createTempFileFromUri(selectedImageUri);
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
                            uploadedImageUrl = data.get("imageUrl");

                            Log.d(TAG, "✅ Image uploaded successfully: " + uploadedImageUrl);
                            createProductOnServer();

                        } else {
                            updatePublishButton("Publish Listing", true);
                            showError("Failed to upload image: " + standardResponse.getMessage());
                        }
                    } else {
                        updatePublishButton("Publish Listing", true);
                        showError("Failed to upload image to server");
                        Log.e(TAG, "Image upload response not successful: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<StandardResponse<Map<String, String>>> call, Throwable t) {
                    updatePublishButton("Publish Listing", true);
                    Log.e(TAG, "❌ Image upload failed", t);
                    showError("Network error while uploading image: " + t.getMessage());
                }
            });

        } catch (Exception e) {
            updatePublishButton("Publish Listing", true);
            Log.e(TAG, "❌ Error preparing image for upload", e);
            showError("Error preparing image: " + e.getMessage());
        }
    }

    private void createProductOnServer() {
        Log.d(TAG, "Creating product on server...");

        Long userId = prefsManager.getUserId();
        if (userId == null || userId <= 0) {
            updatePublishButton("Publish Listing", true);
            showError("Please login to publish products");
            return;
        }

        // Create product request
        Map<String, Object> productRequest = new HashMap<>();
        productRequest.put("title", getTextFromEditText(etTitle));
        productRequest.put("description", getTextFromEditText(etDescription));

        // Price
        String priceStr = getTextFromEditText(etPrice);
        try {
            BigDecimal price = new BigDecimal(priceStr);
            productRequest.put("price", price);
        } catch (NumberFormatException e) {
            updatePublishButton("Publish Listing", true);
            showError("Invalid price format");
            return;
        }

        // ✅ Category - Get từ AutoCompleteTextView
        String categoryText = spinnerCategory.getText().toString();
        int categoryIndex = getCategoryIndex(categoryText);
        productRequest.put("categoryId", (long) (categoryIndex + 1));

        // ✅ Condition - Get từ AutoCompleteTextView
        String conditionText = spinnerCondition.getText().toString();
        String condition = conditionText.toUpperCase().replace(" ", "_");
        productRequest.put("condition", condition);

        productRequest.put("location", getTextFromEditText(etLocation));

        // Additional fields
        String tags = getTextFromEditText(etTags);
        if (!TextUtils.isEmpty(tags)) {
            productRequest.put("tags", tags);
        }

        productRequest.put("isNegotiable", cbNegotiable != null && cbNegotiable.isChecked());

        // Image URLs
        List<String> imageUrls = new ArrayList<>();
        if (uploadedImageUrl != null) {
            imageUrls.add(uploadedImageUrl);
        }
        productRequest.put("imageUrls", imageUrls);

        Log.d(TAG, "Publishing product with data: " + productRequest);

        // Call API
        ApiService apiService = ApiClient.getApiService();
        Call<StandardResponse<Map<String, Object>>> call = apiService.createProduct(productRequest);

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                   Response<StandardResponse<Map<String, Object>>> response) {

                updatePublishButton("Publish Listing", true);

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Map<String, Object>> standardResponse = response.body();

                    if (standardResponse.isSuccess()) {
                        Log.d(TAG, "✅ Product created successfully");
                        Toast.makeText(requireContext(),
                                "🎉 Product published successfully!", Toast.LENGTH_LONG).show();

                        clearForm();

                        // ✅ Navigate về Home thay vì finish activity
                        try {
                            NavController navController = Navigation.findNavController(requireView());
                            navController.navigate(R.id.nav_home);
                            Log.d(TAG, "✅ Navigated to home after successful publish");
                        } catch (Exception e) {
                            Log.e(TAG, "❌ Navigation failed, staying on current fragment", e);
                            // Không làm gì - user vẫn ở AddProductFragment với form đã clear
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

    private int getCategoryIndex(String categoryText) {
        String[] categories = {
                "Electronics", "Fashion", "Home & Garden",
                "Sports", "Books", "Automotive", "Other"
        };

        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equals(categoryText)) {
                return i;
            }
        }
        return 0; // Default to Electronics
    }

    private File createTempFileFromUri(Uri uri) throws Exception {
        InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
        if (inputStream == null) {
            throw new Exception("Could not open input stream from URI");
        }

        File tempFile = File.createTempFile("upload_image", ".jpg", requireContext().getCacheDir());

        FileOutputStream outputStream = new FileOutputStream(tempFile);
        byte[] buffer = new byte[4096];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        outputStream.close();
        inputStream.close();

        Log.d(TAG, "Temp file created: " + tempFile.getAbsolutePath() + " (Size: " + tempFile.length() + " bytes)");
        return tempFile;
    }

    private void updatePublishButton(String text, boolean enabled) {
        if (btnPublish != null) {
            btnPublish.setText(text);
            btnPublish.setEnabled(enabled);
        }
        isPublishing = !enabled;
    }

    private void updatePublishButtonState() {
        boolean isFormValid = !TextUtils.isEmpty(getTextFromEditText(etTitle)) &&
                !TextUtils.isEmpty(getTextFromEditText(etDescription)) &&
                !TextUtils.isEmpty(getTextFromEditText(etPrice)) &&
                !TextUtils.isEmpty(getTextFromEditText(etLocation)) &&
                selectedImageUri != null &&
                (cbTermsConditions == null || cbTermsConditions.isChecked()) &&
                (cbDataProcessing == null || cbDataProcessing.isChecked());

        if (btnPublish != null) {
            btnPublish.setEnabled(!isPublishing && isFormValid);
        }
    }

    private String getTextFromEditText(TextInputEditText editText) {
        if (editText == null) return "";
        return editText.getText().toString().trim();
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Error: " + message);
    }

    private void clearForm() {
        if (etTitle != null) etTitle.setText("");
        if (etDescription != null) etDescription.setText("");
        if (etPrice != null) etPrice.setText("");
        if (etLocation != null) etLocation.setText("");
        if (etTags != null) etTags.setText("");

        if (spinnerCategory != null) spinnerCategory.setText("Home & Garden", false);
        if (spinnerCondition != null) spinnerCondition.setText("New", false);

        if (cbNegotiable != null) cbNegotiable.setChecked(false);
        if (cbTermsConditions != null) cbTermsConditions.setChecked(false);
        if (cbDataProcessing != null) cbDataProcessing.setChecked(false);

        selectedImageUri = null;
        uploadedImageUrl = null;

        if (ivSelectedImage != null) {
            ivSelectedImage.setVisibility(View.GONE);
        }
    }
}