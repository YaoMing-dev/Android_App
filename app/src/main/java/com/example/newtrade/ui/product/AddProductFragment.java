// app/src/main/java/com/example/newtrade/ui/product/AddProductFragment.java
package com.example.newtrade.ui.product;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import com.example.newtrade.R;
import com.example.newtrade.adapters.ImageAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddProductFragment extends Fragment {

    private static final String TAG = "AddProductFragment";
    private static final int REQUEST_IMAGE_PICK = 1001;
    private static final int REQUEST_CAMERA_CAPTURE = 1002;
    private static final int REQUEST_LOCATION_PERMISSION = 1003;
    private static final int REQUEST_CAMERA_PERMISSION = 1004;

    // UI Components
    private TextInputEditText etTitle, etDescription, etPrice, etLocation, etTags;
    private AutoCompleteTextView spinnerCategory, spinnerCondition;
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
        etTitle = view.findViewById(R.id.et_title);
        etDescription = view.findViewById(R.id.et_description);
        etPrice = view.findViewById(R.id.et_price);
        etLocation = view.findViewById(R.id.et_location);
        etTags = view.findViewById(R.id.et_tags);

        spinnerCategory = view.findViewById(R.id.spinner_category);
        spinnerCondition = view.findViewById(R.id.spinner_condition);

        btnSelectImage = view.findViewById(R.id.btn_select_image);
        btnGetLocation = view.findViewById(R.id.btn_get_location);
        btnPreview = view.findViewById(R.id.btn_preview);
        btnPublish = view.findViewById(R.id.btn_publish);

        cbNegotiable = view.findViewById(R.id.cb_negotiable);
        cbTermsConditions = view.findViewById(R.id.cb_terms_conditions);
        cbDataProcessing = view.findViewById(R.id.cb_data_processing);

        rvSelectedImages = view.findViewById(R.id.rv_selected_images);

        // Initially disable publish button
        if (btnPublish != null) {
            btnPublish.setEnabled(false);
        }

        Log.d(TAG, "✅ Views initialized");
    }

    private void initUtils() {
        prefsManager = SharedPrefsManager.getInstance(requireContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    }

    private void setupAdapters() {
        // ✅ SỬA LỖI: Sử dụng đúng signature của interface
        imageAdapter = new ImageAdapter(selectedImagePaths, new ImageAdapter.OnImageClickListener() {
            @Override
            public void onImageClick(int position) {
                // View image at position
                viewImage(position);
            }

            @Override
            public void onImageRemove(int position) {
                // Remove image at position
                removeImage(position);
            }
        });

        if (rvSelectedImages != null) {
            rvSelectedImages.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
            rvSelectedImages.setAdapter(imageAdapter);
        }

        // Condition spinner
        ArrayAdapter<String> conditionAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, conditionDisplayNames);
        if (spinnerCondition != null) {
            spinnerCondition.setAdapter(conditionAdapter);
        }
    }

    private void setupListeners() {
        // Text change listeners for validation
        if (etTitle != null) {
            etTitle.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    validateForm();
                }
            });
        }

        if (etPrice != null) {
            etPrice.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    validateForm();
                }
            });
        }

        // Button listeners
        if (btnSelectImage != null) {
            btnSelectImage.setOnClickListener(v -> showImageSelectionDialog());
        }

        if (btnGetLocation != null) {
            btnGetLocation.setOnClickListener(v -> getCurrentLocation());
        }

        if (btnPreview != null) {
            btnPreview.setOnClickListener(v -> previewProduct());
        }

        if (btnPublish != null) {
            btnPublish.setOnClickListener(v -> publishProduct());
        }
    }

    private void loadCategories() {
        Log.d(TAG, "🔍 Loading categories");

        ApiClient.getProductService().getCategories()
                .enqueue(new Callback<StandardResponse<List<Map<String, Object>>>>() {
                    @Override
                    public void onResponse(@NonNull Call<StandardResponse<List<Map<String, Object>>>> call,
                                           @NonNull Response<StandardResponse<List<Map<String, Object>>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<List<Map<String, Object>>> apiResponse = response.body();

                            if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                                List<String> categoryNames = new ArrayList<>();
                                categoryNames.add("Select Category");

                                for (Map<String, Object> categoryData : apiResponse.getData()) {
                                    String categoryName = (String) categoryData.get("name");
                                    if (categoryName != null) {
                                        categoryNames.add(categoryName);
                                    }
                                }

                                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                                        android.R.layout.simple_dropdown_item_1line, categoryNames);
                                if (spinnerCategory != null) {
                                    spinnerCategory.setAdapter(adapter);
                                }

                                Log.d(TAG, "✅ Loaded " + (categoryNames.size() - 1) + " categories");
                            } else {
                                Log.e(TAG, "Categories API Error: " + apiResponse.getMessage());
                                loadMockCategories();
                            }
                        } else {
                            Log.e(TAG, "Categories response unsuccessful: " + response.code());
                            loadMockCategories();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<StandardResponse<List<Map<String, Object>>>> call,
                                          @NonNull Throwable t) {
                        Log.e(TAG, "Categories API call failed", t);
                        loadMockCategories();
                    }
                });
    }

    // ✅ THÊM loadMockCategories METHOD
    private void loadMockCategories() {
        List<String> categoryNames = new ArrayList<>();
        categoryNames.add("Select Category");

        // Add default categories
        for (String categoryName : Constants.PRODUCT_CATEGORIES) {
            categoryNames.add(categoryName);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, categoryNames);

        if (spinnerCategory != null) {
            spinnerCategory.setAdapter(adapter);
        }

        Log.d(TAG, "✅ Mock categories loaded: " + (categoryNames.size() - 1));
    }

    private void loadDraftIfExists() {
        if (prefsManager != null && prefsManager.hasDraft()) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Continue Draft")
                    .setMessage("You have an unsaved draft. Would you like to continue?")
                    .setPositiveButton("Continue", (dialog, which) -> loadDraft())
                    .setNegativeButton("Discard", (dialog, which) -> {
                        if (prefsManager != null) {
                            prefsManager.clearDraft();
                        }
                    })
                    .show();
        }
    }

    private void loadDraft() {
        if (prefsManager != null) {
            if (etTitle != null) etTitle.setText(prefsManager.getDraftTitle());
            if (etDescription != null) etDescription.setText(prefsManager.getDraftDescription());
            if (etPrice != null) etPrice.setText(prefsManager.getDraftPrice());
            if (etLocation != null) etLocation.setText(prefsManager.getDraftLocation());
            if (spinnerCategory != null) spinnerCategory.setText(prefsManager.getDraftCategory(), false);
            if (spinnerCondition != null) spinnerCondition.setText(prefsManager.getDraftCondition(), false);
        }
    }

    private void showImageSelectionDialog() {
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
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
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
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }

        Toast.makeText(requireContext(), "Getting current location...", Toast.LENGTH_SHORT).show();

        if (prefsManager != null) {
            prefsManager.saveLastLocation(currentLatitude, currentLongitude, currentAddress);
        }
    }

    // ✅ SỬA LỖI: Sử dụng position thay vì imagePath
    private void viewImage(int position) {
        if (position >= 0 && position < selectedImagePaths.size()) {
            String imagePath = selectedImagePaths.get(position);
            Toast.makeText(requireContext(), "Viewing image: " + imagePath, Toast.LENGTH_SHORT).show();
            // TODO: Implement image preview
        }
    }

    // ✅ SỬA LỖI: Sử dụng position thay vì imagePath
    private void removeImage(int position) {
        if (position >= 0 && position < selectedImagePaths.size()) {
            selectedImagePaths.remove(position);
            if (imageAdapter != null) {
                imageAdapter.notifyItemRemoved(position);
            }
            validateForm();
        }
    }

    private void previewProduct() {
        Toast.makeText(requireContext(), "Preview feature coming soon", Toast.LENGTH_SHORT).show();
    }

    private void publishProduct() {
        if (!validateForm()) {
            return;
        }

        if (isLoading) {
            return;
        }

        isLoading = true;
        saveDraftAutomatically();

        // Create product data
        Map<String, Object> productData = new HashMap<>();
        productData.put("title", etTitle.getText().toString().trim());
        productData.put("description", etDescription.getText().toString().trim());
        productData.put("price", new BigDecimal(etPrice.getText().toString().trim()));
        productData.put("location", etLocation.getText().toString().trim());
        productData.put("category", spinnerCategory.getText().toString());
        productData.put("condition", spinnerCondition.getText().toString());
        productData.put("negotiable", cbNegotiable.isChecked());
        productData.put("tags", etTags.getText().toString().trim());

        Long userId = prefsManager.getUserId();
        if (userId == null) {
            showError("Please log in first");
            isLoading = false;
            return;
        }

        ApiClient.getProductService().createProduct(userId, productData)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                           @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                        isLoading = false;

                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, Object>> apiResponse = response.body();

                            if (apiResponse.isSuccess()) {
                                Toast.makeText(requireContext(), "Product published successfully!", Toast.LENGTH_SHORT).show();

                                // Clear draft
                                if (prefsManager != null) {
                                    prefsManager.clearDraft();
                                }

                                // Navigate back to home
                                try {
                                    NavController navController = Navigation.findNavController(requireView());
                                    navController.navigate(R.id.action_addProduct_to_home);
                                } catch (Exception e) {
                                    Log.e(TAG, "Navigation error", e);
                                }
                            } else {
                                showError(apiResponse.getMessage());
                            }
                        } else {
                            showError("Failed to publish product");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                          @NonNull Throwable t) {
                        isLoading = false;
                        Log.e(TAG, "Failed to publish product", t);
                        showError("Network error. Please try again.");
                    }
                });
    }

    private boolean validateForm() {
        if (etTitle == null || etPrice == null || etDescription == null ||
                etLocation == null || spinnerCategory == null || spinnerCondition == null) {
            return false;
        }

        String title = etTitle.getText().toString().trim();
        String price = etPrice.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String category = spinnerCategory.getText().toString();
        String condition = spinnerCondition.getText().toString();

        boolean isValid = !title.isEmpty() && !price.isEmpty() && !description.isEmpty() &&
                !location.isEmpty() && !category.isEmpty() && !condition.isEmpty() &&
                !selectedImagePaths.isEmpty();

        if (cbTermsConditions != null && cbDataProcessing != null) {
            isValid = isValid && cbTermsConditions.isChecked() && cbDataProcessing.isChecked();
        }

        if (btnPublish != null) {
            btnPublish.setEnabled(isValid);
        }

        return isValid;
    }

    private void saveDraftAutomatically() {
        if (prefsManager == null || etTitle == null || etDescription == null ||
                etPrice == null || etLocation == null || spinnerCategory == null || spinnerCondition == null) {
            return;
        }

        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String price = etPrice.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String category = spinnerCategory.getText().toString();
        String condition = spinnerCondition.getText().toString();

        if (!title.isEmpty() || !description.isEmpty() || !price.isEmpty()) {
            prefsManager.saveDraftProduct(title, description, price, category, condition, location);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == android.app.Activity.RESULT_OK && data != null) {
            if (requestCode == REQUEST_IMAGE_PICK) {
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    selectedImagePaths.add(imageUri.toString());
                    if (imageAdapter != null) {
                        imageAdapter.notifyDataSetChanged();
                    }
                    validateForm();
                }
            } else if (requestCode == REQUEST_CAMERA_CAPTURE) {
                Toast.makeText(requireContext(), "Photo captured", Toast.LENGTH_SHORT).show();
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
        saveDraftAutomatically();
        Log.d(TAG, "AddProductFragment destroyed");
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
        Log.w(TAG, "Error: " + message);
    }
}