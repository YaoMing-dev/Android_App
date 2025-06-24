package com.example.newtrade.ui.product;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Category;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
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

public class AddProductFragment extends Fragment {

    private static final String TAG = "AddProductFragment";
    private static final int REQUEST_IMAGE_PICK = 1001;
    private static final int REQUEST_LOCATION_PERMISSION = 1002;

    // Price limits according to database DECIMAL(12,2)
    private static final BigDecimal MAX_PRICE = new BigDecimal("9999999999.99");
    private static final BigDecimal MIN_PRICE = new BigDecimal("0");

    // UI Components
    private ImageView ivProductImage;
    private TextInputEditText etTitle, etDescription, etPrice, etLocation, etTags;
    private Spinner spinnerCategory, spinnerCondition;
    private MaterialCheckBox cbNegotiable;
    private Button btnSelectImage, btnGetLocation, btnPublish;

    // Data
    private final List<Category> categories = new ArrayList<>();
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
        loadCategories();
        setupConditionSpinner();
        setupListeners();

        Log.d(TAG, "AddProductFragment created successfully");
    }

    private void initViews(View view) {
        try {
            ivProductImage = view.findViewById(R.id.iv_product_image);
            etTitle = view.findViewById(R.id.et_title);
            etDescription = view.findViewById(R.id.et_description);
            etPrice = view.findViewById(R.id.et_price);
            etLocation = view.findViewById(R.id.et_location);
            etTags = view.findViewById(R.id.et_tags);
            spinnerCategory = view.findViewById(R.id.spinner_category);
            spinnerCondition = view.findViewById(R.id.spinner_condition);
            cbNegotiable = view.findViewById(R.id.cb_negotiable);
            btnSelectImage = view.findViewById(R.id.btn_select_image);
            btnGetLocation = view.findViewById(R.id.btn_get_location);
            btnPublish = view.findViewById(R.id.btn_publish);

            Log.d(TAG, "✅ AddProductFragment views initialized");
        } catch (Exception e) {
            Log.w(TAG, "Some AddProductFragment views not found: " + e.getMessage());
        }
    }

    private void initUtils() {
        prefsManager = SharedPrefsManager.getInstance(requireContext());
    }

    private void initLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    }

    private void loadCategories() {
        Log.d(TAG, "Loading categories from backend...");

        ApiClient.getApiService().getCategories().enqueue(new Callback<StandardResponse<List<Category>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<List<Category>>> call, @NonNull Response<StandardResponse<List<Category>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Category> loadedCategories = response.body().getData();
                    if (loadedCategories != null) {
                        categories.clear();
                        categories.addAll(loadedCategories);
                        setupCategorySpinner();
                        Log.d(TAG, "✅ Categories loaded: " + categories.size());
                    }
                } else {
                    Log.e(TAG, "❌ Failed to load categories");
                    loadSampleCategories();
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<List<Category>>> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Categories API error", t);
                loadSampleCategories();
            }
        });
    }

    private void loadSampleCategories() {
        categories.clear();
        categories.add(new Category(1L, "Electronics", "Phones, laptops, gadgets", "smartphone", true));
        categories.add(new Category(2L, "Fashion", "Clothing, shoes, accessories", "shirt", true));
        categories.add(new Category(3L, "Home & Garden", "Furniture, appliances", "home", true));
        categories.add(new Category(4L, "Sports", "Exercise equipment, outdoor gear", "dumbbell", true));
        categories.add(new Category(5L, "Books", "Books and educational materials", "book", true));
        categories.add(new Category(6L, "Vehicles", "Cars, motorcycles, parts", "car", true));
        categories.add(new Category(7L, "Beauty", "Cosmetics, health products", "heart", true));
        categories.add(new Category(8L, "Toys", "Toys and kids items", "toys", true));

        setupCategorySpinner();
        Log.d(TAG, "✅ Loaded sample categories");
    }

    private void setupCategorySpinner() {
        List<String> categoryNames = new ArrayList<>();
        categoryNames.add("Select Category");

        for (Category category : categories) {
            categoryNames.add(category.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, categoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupConditionSpinner() {
        String[] conditions = {"Select Condition", "NEW", "LIKE_NEW", "GOOD", "FAIR", "POOR"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, conditions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCondition.setAdapter(adapter);
    }

    private void setupListeners() {
        if (btnSelectImage != null) {
            btnSelectImage.setOnClickListener(v -> selectImage());
        }

        if (btnGetLocation != null) {
            btnGetLocation.setOnClickListener(v -> getCurrentLocation());
        }

        if (btnPublish != null) {
            btnPublish.setOnClickListener(v -> publishProduct());
        }
    }

    private boolean validateForm() {
        // Title validation
        if (TextUtils.isEmpty(getTextFromEditText(etTitle))) {
            setErrorAndFocus(etTitle, "Title is required");
            return false;
        }

        // Description validation
        if (TextUtils.isEmpty(getTextFromEditText(etDescription))) {
            setErrorAndFocus(etDescription, "Description is required");
            return false;
        }

        // Price validation
        String priceStr = getTextFromEditText(etPrice);
        if (TextUtils.isEmpty(priceStr)) {
            setErrorAndFocus(etPrice, "Price is required");
            return false;
        }

        try {
            BigDecimal price = new BigDecimal(priceStr);

            if (price.compareTo(MIN_PRICE) < 0) {
                setErrorAndFocus(etPrice, "Price cannot be negative");
                return false;
            }

            if (price.compareTo(MAX_PRICE) > 0) {
                NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                String maxPriceFormatted = formatter.format(MAX_PRICE);
                setErrorAndFocus(etPrice, "Price cannot exceed " + maxPriceFormatted);
                Toast.makeText(getContext(),
                        "Maximum price allowed is " + maxPriceFormatted,
                        Toast.LENGTH_LONG).show();
                return false;
            }

            if (price.scale() > 2) {
                setErrorAndFocus(etPrice, "Price can have maximum 2 decimal places");
                return false;
            }

        } catch (NumberFormatException e) {
            setErrorAndFocus(etPrice, "Invalid price format");
            return false;
        }

        // Location validation
        if (TextUtils.isEmpty(getTextFromEditText(etLocation))) {
            setErrorAndFocus(etLocation, "Location is required");
            return false;
        }

        // Category validation
        if (spinnerCategory.getSelectedItemPosition() == 0) {
            Toast.makeText(getContext(), "Please select a category", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Condition validation
        if (spinnerCondition.getSelectedItemPosition() == 0) {
            Toast.makeText(getContext(), "Please select condition", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Image validation
        if (uploadedImageUrl == null) {
            Toast.makeText(getContext(), "Please select and upload an image first", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @NonNull
    private String getTextFromEditText(@Nullable TextInputEditText editText) {
        if (editText != null && editText.getText() != null) {
            return editText.getText().toString().trim();
        }
        return "";
    }

    private void setErrorAndFocus(@Nullable TextInputEditText editText, String error) {
        if (editText != null) {
            editText.setError(error);
            editText.requestFocus();
        }
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }

        if (btnGetLocation != null) {
            btnGetLocation.setText("📍 Getting location...");
            btnGetLocation.setEnabled(false);
        }

        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                resetLocationButton();

                if (location != null) {
                    getAddressFromLocation(location.getLatitude(), location.getLongitude());
                } else {
                    Toast.makeText(getContext(), "Unable to get current location. Please enter manually.", Toast.LENGTH_LONG).show();
                }
            }).addOnFailureListener(e -> {
                resetLocationButton();
                Toast.makeText(getContext(), "Failed to get location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "❌ Location error", e);
            });
        } catch (SecurityException e) {
            resetLocationButton();
            Log.e(TAG, "❌ Location permission error", e);
        }
    }

    private void resetLocationButton() {
        if (btnGetLocation != null) {
            btnGetLocation.setText("📍 Get Location");
            btnGetLocation.setEnabled(true);
        }
    }

    private void getAddressFromLocation(double latitude, double longitude) {
        try {
            Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String fullAddress = address.getAddressLine(0);
                if (fullAddress != null && etLocation != null) {
                    etLocation.setText(fullAddress);
                    Toast.makeText(getContext(), "📍 Location detected", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "✅ Location detected: " + fullAddress);
                }
            } else {
                Toast.makeText(getContext(), "Unable to determine address", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e(TAG, "❌ Geocoder error", e);
            Toast.makeText(getContext(), "Geocoder service unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                // Show selected image immediately
                if (ivProductImage != null) {
                    Glide.with(this)
                            .load(selectedImageUri)
                            .centerCrop()
                            .into(ivProductImage);
                }

                // Upload image to backend
                uploadImageToBackend();
            }
        }
    }

    private void uploadImageToBackend() {
        if (selectedImageUri == null || isUploading) return;

        isUploading = true;
        updateImageButton("Uploading...", false);

        try {
            File file = createFileFromUri(selectedImageUri);
            if (file == null) {
                resetImageButton();
                Toast.makeText(getContext(), "Failed to process selected image", Toast.LENGTH_SHORT).show();
                return;
            }

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

            ApiClient.getApiService().uploadProductImage(body).enqueue(new Callback<StandardResponse<Map<String, String>>>() {
                @Override
                public void onResponse(@NonNull Call<StandardResponse<Map<String, String>>> call, @NonNull Response<StandardResponse<Map<String, String>>> response) {
                    resetImageButton();

                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Map<String, String> data = response.body().getData();
                        if (data != null) {
                            uploadedImageUrl = data.get("imageUrl");
                            updateImageButton("✅ Image Uploaded", true);
                            Toast.makeText(getContext(), "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "✅ Image uploaded: " + uploadedImageUrl);
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "❌ Image upload failed");
                    }

                    if (file.exists()) {
                        boolean deleted = file.delete();
                        Log.d(TAG, "Temp file deleted: " + deleted);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<StandardResponse<Map<String, String>>> call, @NonNull Throwable t) {
                    resetImageButton();
                    Toast.makeText(getContext(), "Upload error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "❌ Image upload error", t);

                    if (file.exists()) {
                        boolean deleted = file.delete();
                        Log.d(TAG, "Temp file deleted: " + deleted);
                    }
                }
            });

        } catch (Exception e) {
            resetImageButton();
            Toast.makeText(getContext(), "Error preparing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "❌ Image preparation error", e);
        }
    }

    private void updateImageButton(String text, boolean enabled) {
        if (btnSelectImage != null) {
            btnSelectImage.setText(text);
            btnSelectImage.setEnabled(enabled);
        }
    }

    private void resetImageButton() {
        updateImageButton("📷 Select Image", true);
        isUploading = false;
    }

    @Nullable
    private File createFileFromUri(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            String fileName = getFileName(uri);
            File file = new File(requireContext().getCacheDir(), fileName);

            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
            }

            inputStream.close();
            return file;
        } catch (IOException e) {
            Log.e(TAG, "❌ Error creating file from URI", e);
            return null;
        }
    }

    @NonNull
    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        return result != null ? result : "image.jpg";
    }

    private void publishProduct() {
        if (!validateForm()) {
            return;
        }

        updatePublishButton("Publishing...", false);

        try {
            Map<String, Object> productData = new HashMap<>();
            productData.put("title", getTextFromEditText(etTitle));
            productData.put("description", getTextFromEditText(etDescription));

            String priceStr = getTextFromEditText(etPrice);
            BigDecimal price = new BigDecimal(priceStr);
            productData.put("price", price);

            int categoryIndex = spinnerCategory.getSelectedItemPosition();
            if (categoryIndex > 0 && categoryIndex <= categories.size()) {
                productData.put("categoryId", categories.get(categoryIndex - 1).getId());
            }

            productData.put("condition", spinnerCondition.getSelectedItem().toString());
            productData.put("location", getTextFromEditText(etLocation));

            List<String> imageUrls = new ArrayList<>();
            imageUrls.add(uploadedImageUrl);
            productData.put("imageUrls", imageUrls);

            String tags = getTextFromEditText(etTags);
            if (!TextUtils.isEmpty(tags)) {
                productData.put("tags", tags);
            }

            Log.d(TAG, "Publishing product: " + productData);

            ApiClient.getApiService().createProduct(productData).enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                @Override
                public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                    updatePublishButton("Publish Product", true);

                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(getContext(), "🎉 Product published successfully! 🎉", Toast.LENGTH_LONG).show();
                        clearForm();
                        Log.d(TAG, "✅ Product published successfully");
                    } else {
                        String errorMsg = response.body() != null ? response.body().getMessage() : "Unknown error";
                        Toast.makeText(getContext(), "❌ Publish failed: " + errorMsg, Toast.LENGTH_LONG).show();
                        Log.e(TAG, "❌ Publish failed: " + errorMsg);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                    updatePublishButton("Publish Product", true);
                    Toast.makeText(getContext(), "❌ Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "❌ Publish network error", t);
                }
            });

        } catch (Exception e) {
            updatePublishButton("Publish Product", true);
            Toast.makeText(getContext(), "❌ Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "❌ Publish error", e);
        }
    }

    private void updatePublishButton(String text, boolean enabled) {
        if (btnPublish != null) {
            btnPublish.setText(text);
            btnPublish.setEnabled(enabled);
        }
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
        if (ivProductImage != null) ivProductImage.setImageResource(R.drawable.ic_add_photo_placeholder);

        resetImageButton();
        selectedImageUri = null;
        uploadedImageUrl = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}