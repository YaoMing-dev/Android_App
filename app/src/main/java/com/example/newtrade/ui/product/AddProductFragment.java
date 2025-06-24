// File: app/src/main/java/com/example/newtrade/ui/product/AddProductFragment.java
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Category;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
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
    private MaterialCheckBox cbNegotiable;
    private Button btnSelectImage, btnGetLocation, btnPublish;

    // Data
    private List<Category> categories = new ArrayList<>();
    private Uri selectedImageUri;
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
        ApiClient.getApiService().getCategories()
                .enqueue(new Callback<StandardResponse<List<Map<String, Object>>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<List<Map<String, Object>>>> call,
                                           Response<StandardResponse<List<Map<String, Object>>>> response) {

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            List<Map<String, Object>> categoryData = response.body().getData();

                            categories.clear();
                            for (Map<String, Object> data : categoryData) {
                                Category category = new Category();
                                category.setId(((Number) data.get("id")).longValue());
                                category.setName((String) data.get("name"));
                                categories.add(category);
                            }

                            setupCategorySpinner();
                            Log.d(TAG, "✅ Loaded " + categories.size() + " categories");
                        } else {
                            Log.e(TAG, "❌ Failed to load categories");
                            Toast.makeText(getContext(), "Failed to load categories", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<List<Map<String, Object>>>> call, Throwable t) {
                        Log.e(TAG, "❌ Failed to load categories", t);
                        Toast.makeText(getContext(), "Failed to load categories: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupCategorySpinner() {
        List<String> categoryNames = new ArrayList<>();
        categoryNames.add("Select Category");

        for (Category category : categories) {
            categoryNames.add(category.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categoryNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupConditionSpinner() {
        String[] conditions = {"Select Condition", "NEW", "LIKE_NEW", "GOOD", "FAIR", "POOR"};
        String[] conditionDisplay = {"Select Condition", "New", "Like New", "Good", "Fair", "Poor"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                conditionDisplay
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCondition.setAdapter(adapter);
    }

    private void setupListeners() {
        btnSelectImage.setOnClickListener(v -> selectImage());
        btnGetLocation.setOnClickListener(v -> getCurrentLocation());
        btnPublish.setOnClickListener(v -> publishProduct());
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
            return;
        }

        btnGetLocation.setEnabled(false);
        btnGetLocation.setText("Getting location...");

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        btnGetLocation.setEnabled(true);
                        btnGetLocation.setText("📍 Use My Current Location");

                        if (location != null) {
                            getAddressFromLocation(location.getLatitude(), location.getLongitude());
                        } else {
                            Toast.makeText(getContext(), "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    btnGetLocation.setEnabled(true);
                    btnGetLocation.setText("📍 Use My Current Location");
                    Toast.makeText(getContext(), "Failed to get location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void getAddressFromLocation(double latitude, double longitude) {
        try {
            Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                StringBuilder addressBuilder = new StringBuilder();

                if (address.getSubThoroughfare() != null) {
                    addressBuilder.append(address.getSubThoroughfare()).append(" ");
                }
                if (address.getThoroughfare() != null) {
                    addressBuilder.append(address.getThoroughfare()).append(", ");
                }
                if (address.getSubLocality() != null) {
                    addressBuilder.append(address.getSubLocality()).append(", ");
                }
                if (address.getLocality() != null) {
                    addressBuilder.append(address.getLocality()).append(", ");
                }
                if (address.getAdminArea() != null) {
                    addressBuilder.append(address.getAdminArea());
                }

                String fullAddress = addressBuilder.toString();
                if (fullAddress.endsWith(", ")) {
                    fullAddress = fullAddress.substring(0, fullAddress.length() - 2);
                }

                etLocation.setText(fullAddress);
                Toast.makeText(getContext(), "Location detected!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "✅ Location detected: " + fullAddress);
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
                ivProductImage.setImageURI(selectedImageUri);
                btnSelectImage.setText("✅ Photo Selected");
            }
        }
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

    private void publishProduct() {
        if (!validateInputs()) {
            return;
        }

        btnPublish.setEnabled(false);
        btnPublish.setText("Publishing...");

        // Create product data
        Map<String, Object> productData = new HashMap<>();
        productData.put("title", etTitle.getText().toString().trim());
        productData.put("description", etDescription.getText().toString().trim());

        // Price
        String priceText = etPrice.getText().toString().trim();
        productData.put("price", Double.parseDouble(priceText));

        productData.put("location", etLocation.getText().toString().trim());

        // Category
        int categoryIndex = spinnerCategory.getSelectedItemPosition() - 1;
        if (categoryIndex >= 0 && categoryIndex < categories.size()) {
            productData.put("categoryId", categories.get(categoryIndex).getId());
        }

        // Condition - đúng field name
        String[] conditions = {"", "NEW", "LIKE_NEW", "GOOD", "FAIR", "POOR"};
        int conditionIndex = spinnerCondition.getSelectedItemPosition();
        if (conditionIndex > 0 && conditionIndex < conditions.length) {
            productData.put("condition", conditions[conditionIndex]);
        }

        // Optional fields
        if (cbNegotiable.isChecked()) {
            productData.put("isNegotiable", true);
        }

        String tags = etTags.getText().toString().trim();
        if (!TextUtils.isEmpty(tags)) {
            productData.put("tags", tags);
        }

        Log.d(TAG, "Product data: " + productData.toString());

        // Call API
        ApiClient.getApiService().createProduct(productData)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {

                        btnPublish.setEnabled(true);
                        btnPublish.setText("🚀 Publish Listing");

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(getContext(), "Product published successfully! 🎉", Toast.LENGTH_LONG).show();
                            clearForm();

                            // Navigate back to home
                            if (getActivity() != null) {
                                requireActivity().onBackPressed();
                            }
                        } else {
                            String errorMsg = response.body() != null ? response.body().getMessage() : "Unknown error";
                            Toast.makeText(getContext(), "Failed to publish: " + errorMsg, Toast.LENGTH_LONG).show();
                            Log.e(TAG, "❌ Publish failed: " + errorMsg);
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        btnPublish.setEnabled(true);
                        btnPublish.setText("🚀 Publish Listing");

                        Log.e(TAG, "❌ Failed to publish product", t);
                        Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validateInputs() {
        // Title
        if (TextUtils.isEmpty(etTitle.getText())) {
            etTitle.setError("Title is required");
            etTitle.requestFocus();
            return false;
        }

        // Description
        if (TextUtils.isEmpty(etDescription.getText())) {
            etDescription.setError("Description is required");
            etDescription.requestFocus();
            return false;
        }

        // Price
        if (TextUtils.isEmpty(etPrice.getText())) {
            etPrice.setError("Price is required");
            etPrice.requestFocus();
            return false;
        }

        try {
            double price = Double.parseDouble(etPrice.getText().toString().trim());
            if (price < 0) {
                etPrice.setError("Price must be positive");
                etPrice.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            etPrice.setError("Invalid price format");
            etPrice.requestFocus();
            return false;
        }

        // Location
        if (TextUtils.isEmpty(etLocation.getText())) {
            etLocation.setError("Location is required");
            etLocation.requestFocus();
            return false;
        }

        // Category
        if (spinnerCategory.getSelectedItemPosition() == 0) {
            Toast.makeText(getContext(), "Please select a category", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Condition
        if (spinnerCondition.getSelectedItemPosition() == 0) {
            Toast.makeText(getContext(), "Please select condition", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Image (required theo đề thi)
        if (selectedImageUri == null) {
            Toast.makeText(getContext(), "Please select at least one image", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void clearForm() {
        etTitle.setText("");
        etDescription.setText("");
        etPrice.setText("");
        etLocation.setText("");
        etTags.setText("");
        spinnerCategory.setSelection(0);
        spinnerCondition.setSelection(0);
        cbNegotiable.setChecked(false);
        ivProductImage.setImageResource(R.drawable.ic_add_photo_placeholder);
        btnSelectImage.setText("📷 Select Photo");
        selectedImageUri = null;
    }
}