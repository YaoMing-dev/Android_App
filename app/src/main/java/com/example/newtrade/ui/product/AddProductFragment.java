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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

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

    // UI Components
    private TextInputEditText etTitle, etDescription, etPrice, etLocation, etTags;
    private AutoCompleteTextView spinnerCategory, spinnerCondition;
    private ImageView ivSelectedImage;
    private Button btnSelectImage, btnGetLocation, btnPreview, btnPublish;
    private MaterialCheckBox cbNegotiable;

    // Data
    private SharedPrefsManager prefsManager;
    private FusedLocationProviderClient fusedLocationClient;
    private Uri selectedImageUri;
    private String uploadedImageUrl;
    private boolean isLoading = false;

    // ✅ FIX: Modern Activity Result API
    private ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        // Display selected image
                        Glide.with(this)
                                .load(selectedImageUri)
                                .centerCrop()
                                .into(ivSelectedImage);

                        ivSelectedImage.setVisibility(View.VISIBLE);
                        uploadedImageUrl = null; // Reset uploaded URL
                        updatePublishButtonState();

                        Log.d(TAG, "✅ Image selected: " + selectedImageUri);
                    }
                }
            }
    );

    // ✅ FIX: Modern Permission Request API
    private ActivityResultLauncher<String[]> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                Boolean fineLocationGranted = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                Boolean coarseLocationGranted = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);

                if (fineLocationGranted != null && fineLocationGranted) {
                    getCurrentLocation();
                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                    getCurrentLocation();
                } else {
                    Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initData();
        setupSpinners();
        setupListeners();
        updatePublishButtonState();

        Log.d(TAG, "✅ AddProductFragment created successfully");
    }

    private void initViews(View view) {
        etTitle = view.findViewById(R.id.et_title);
        etDescription = view.findViewById(R.id.et_description);
        etPrice = view.findViewById(R.id.et_price);
        etLocation = view.findViewById(R.id.et_location);
        etTags = view.findViewById(R.id.et_tags);
        spinnerCategory = view.findViewById(R.id.spinner_category);
        spinnerCondition = view.findViewById(R.id.spinner_condition);
        ivSelectedImage = view.findViewById(R.id.iv_selected_image);
        btnSelectImage = view.findViewById(R.id.btn_select_image);
        btnGetLocation = view.findViewById(R.id.btn_get_location);
        btnPreview = view.findViewById(R.id.btn_preview);
        btnPublish = view.findViewById(R.id.btn_publish);
        cbNegotiable = view.findViewById(R.id.cb_negotiable);
    }

    private void initData() {
        prefsManager = SharedPrefsManager.getInstance(requireContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    }

    private void setupSpinners() {
        // ✅ FIX: Setup Category Spinner
        String[] categories = {
                "Electronics", "Fashion", "Home & Garden", "Sports", "Books",
                "Automotive", "Health & Beauty", "Toys & Games", "Music", "Other"
        };
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, categories);
        spinnerCategory.setAdapter(categoryAdapter);

        // ✅ FIX: Setup Condition Spinner
        String[] conditions = {"New", "Like New", "Good", "Fair", "Poor"};
        ArrayAdapter<String> conditionAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, conditions);
        spinnerCondition.setAdapter(conditionAdapter);
    }

    private void setupListeners() {
        btnSelectImage.setOnClickListener(v -> selectImage());
        btnGetLocation.setOnClickListener(v -> requestLocationPermission());
        btnPreview.setOnClickListener(v -> previewProduct());
        btnPublish.setOnClickListener(v -> publishProduct());

        // ✅ FIX: Text watchers for validation
        etTitle.addTextChangedListener(new SimpleTextWatcher(() -> updatePublishButtonState()));
        etDescription.addTextChangedListener(new SimpleTextWatcher(() -> updatePublishButtonState()));
        etPrice.addTextChangedListener(new SimpleTextWatcher(() -> updatePublishButtonState()));
        etLocation.addTextChangedListener(new SimpleTextWatcher(() -> updatePublishButtonState()));

        spinnerCategory.setOnItemClickListener((parent, view, position, id) -> updatePublishButtonState());
        spinnerCondition.setOnItemClickListener((parent, view, position, id) -> updatePublishButtonState());
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent); // ✅ FIX: Use modern API
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            permissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }); // ✅ FIX: Use modern API
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
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
                        Log.d(TAG, "✅ Location obtained: " + locationText);
                    } else {
                        Toast.makeText(requireContext(), "Unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    btnGetLocation.setEnabled(true);
                    btnGetLocation.setText("GPS");
                    Log.e(TAG, "❌ Failed to get location", e);
                    Toast.makeText(requireContext(), "Failed to get location", Toast.LENGTH_SHORT).show();
                });
    }

    private void previewProduct() {
        if (!validateForm()) {
            return;
        }

        // Navigate to preview fragment
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_addProduct_to_productPreview);
    }

    private void publishProduct() {
        if (!validateForm()) {
            return;
        }

        updatePublishButton("Publishing...", false);

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

        // Validate category
        if (TextUtils.isEmpty(spinnerCategory.getText().toString())) {
            spinnerCategory.setError("Category is required");
            isValid = false;
        }

        // Validate condition
        if (TextUtils.isEmpty(spinnerCondition.getText().toString())) {
            spinnerCondition.setError("Condition is required");
            isValid = false;
        }

        // Validate image
        if (selectedImageUri == null) {
            Toast.makeText(requireContext(), "Please select a product image", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
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

        productRequest.put("location", getTextFromEditText(etLocation));
        productRequest.put("category", spinnerCategory.getText().toString());
        productRequest.put("condition", spinnerCondition.getText().toString());
        productRequest.put("tags", getTextFromEditText(etTags));
        productRequest.put("negotiable", cbNegotiable.isChecked());
        productRequest.put("imageUrl", uploadedImageUrl);
        productRequest.put("sellerId", userId);

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
                        Toast.makeText(requireContext(), "Product published successfully!", Toast.LENGTH_SHORT).show();

                        // Clear form and navigate back
                        clearForm();
                        requireActivity().onBackPressed();

                    } else {
                        showError("Failed to create product: " + standardResponse.getMessage());
                    }
                } else {
                    showError("Failed to create product on server");
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

    // ===== HELPER METHODS =====

    private File createTempFileFromUri(Uri uri) throws Exception {
        InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
        File tempFile = new File(requireContext().getCacheDir(), "temp_image.jpg");
        FileOutputStream outputStream = new FileOutputStream(tempFile);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        inputStream.close();
        outputStream.close();
        return tempFile;
    }

    private String getTextFromEditText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private void updatePublishButtonState() {
        boolean isFormValid = !TextUtils.isEmpty(getTextFromEditText(etTitle)) &&
                !TextUtils.isEmpty(getTextFromEditText(etDescription)) &&
                !TextUtils.isEmpty(getTextFromEditText(etPrice)) &&
                !TextUtils.isEmpty(getTextFromEditText(etLocation)) &&
                !TextUtils.isEmpty(spinnerCategory.getText().toString()) &&
                !TextUtils.isEmpty(spinnerCondition.getText().toString()) &&
                selectedImageUri != null;

        btnPublish.setEnabled(isFormValid && !isLoading);
    }

    private void updatePublishButton(String text, boolean enabled) {
        btnPublish.setText(text);
        btnPublish.setEnabled(enabled);
        isLoading = !enabled;
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }

    private void clearForm() {
        etTitle.setText("");
        etDescription.setText("");
        etPrice.setText("");
        etLocation.setText("");
        etTags.setText("");
        spinnerCategory.setText("");
        spinnerCondition.setText("");
        cbNegotiable.setChecked(false);
        selectedImageUri = null;
        uploadedImageUrl = null;
        ivSelectedImage.setVisibility(View.GONE);
        updatePublishButtonState();
    }

    // ✅ FIX: Simple TextWatcher implementation
    private static class SimpleTextWatcher implements android.text.TextWatcher {
        private final Runnable callback;

        public SimpleTextWatcher(Runnable callback) {
            this.callback = callback;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(android.text.Editable s) {
            callback.run();
        }
    }
}