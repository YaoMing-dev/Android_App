// app/src/main/java/com/example/newtrade/ui/product/AddProductFragment.java
package com.example.newtrade.ui.product;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Category;
import com.example.newtrade.models.ProductRequest;
import com.example.newtrade.models.ProductResponse;
import com.example.newtrade.services.FileUploadService;
import com.example.newtrade.utils.ApiCallback;
import com.example.newtrade.utils.ImagePickerHelper;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AddProductFragment extends Fragment {

    private static final String TAG = "AddProductFragment";

    // UI Components
    private RecyclerView rvImages;
    private MaterialButton btnAddImages;
    private AutoCompleteTextView spinnerCategory, spinnerCondition;
    private TextInputEditText etTitle, etDescription, etPrice, etLocation;
    private MaterialButton btnSubmit;

    // Data
    private final List<Category> categories = new ArrayList<>();
    private final List<String> uploadedImageUrls = new ArrayList<>();
    private ProductImageAdapter imageAdapter;
    private SharedPrefsManager prefsManager;

    // Condition options
    private final String[] conditionOptions = {"NEW", "LIKE_NEW", "GOOD", "FAIR", "POOR"};
    private final String[] conditionDisplayNames = {"New", "Like New", "Good", "Fair", "Poor"};

    // State
    private boolean isLoading = false;
    private boolean isImageUploading = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeComponents(view);
        setupUI();
        loadInitialData();

        Log.d(TAG, "✅ AddProductFragment initialized");
    }

    // =============================================
    // INITIALIZATION
    // =============================================

    private void initializeComponents(View view) {
        // Find views
        rvImages = view.findViewById(R.id.rv_images);
        btnAddImages = view.findViewById(R.id.btn_add_images);
        spinnerCategory = view.findViewById(R.id.spinner_category);
        spinnerCondition = view.findViewById(R.id.spinner_condition);
        etTitle = view.findViewById(R.id.et_title);
        etDescription = view.findViewById(R.id.et_description);
        etPrice = view.findViewById(R.id.et_price);
        etLocation = view.findViewById(R.id.et_location);
        btnSubmit = view.findViewById(R.id.btn_submit);

        // Initialize utils
        prefsManager = new SharedPrefsManager(requireContext());
    }

    private void setupUI() {
        setupRecyclerView();
        setupSpinners();
        setupListeners();
        setupValidation();
    }

    private void setupRecyclerView() {
        imageAdapter = new ProductImageAdapter(uploadedImageUrls, this::onImageDelete);
        rvImages.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvImages.setAdapter(imageAdapter);
    }

    private void setupSpinners() {
        // Setup condition spinner
        ArrayAdapter<String> conditionAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, conditionDisplayNames);
        spinnerCondition.setAdapter(conditionAdapter);

        // Set default condition
        spinnerCondition.setText(conditionDisplayNames[0], false);
    }

    private void setupListeners() {
        btnAddImages.setOnClickListener(v -> handleAddImages());
        btnSubmit.setOnClickListener(v -> handleSubmitProduct());

        // Category selection listener
        spinnerCategory.setOnItemClickListener((parent, view, position, id) -> validateForm());
        spinnerCondition.setOnItemClickListener((parent, view, position, id) -> validateForm());
    }

    private void setupValidation() {
        TextWatcher validationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateForm();
            }
        };

        etTitle.addTextChangedListener(validationWatcher);
        etDescription.addTextChangedListener(validationWatcher);
        etPrice.addTextChangedListener(validationWatcher);
        etLocation.addTextChangedListener(validationWatcher);
    }

    private void loadInitialData() {
        loadCategories();
    }

    // =============================================
    // DATA LOADING
    // =============================================

    private void loadCategories() {
        Log.d(TAG, "Loading categories...");

        ApiClient.getCategoryService().getAllActiveCategories()
                .enqueue(new ApiCallback<List<Category>>() {
                    @Override
                    public void onSuccess(List<Category> result) {
                        handleCategoriesLoaded(result);
                    }

                    @Override
                    public void onError(String error) {
                        handleCategoriesError(error);
                    }
                });
    }

    private void handleCategoriesLoaded(List<Category> loadedCategories) {
        if (!isAdded()) return;

        categories.clear();
        categories.addAll(loadedCategories);
        setupCategorySpinner();

        Log.d(TAG, "✅ Loaded " + categories.size() + " categories");
    }

    private void handleCategoriesError(String error) {
        if (!isAdded()) return;

        Log.e(TAG, "Failed to load categories: " + error);
        showError("Failed to load categories. Please check your connection.");
    }

    private void setupCategorySpinner() {
        List<String> categoryNames = new ArrayList<>();
        for (Category category : categories) {
            categoryNames.add(category.getName());
        }

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, categoryNames);
        spinnerCategory.setAdapter(categoryAdapter);
    }

    // =============================================
    // IMAGE HANDLING
    // =============================================

    private void handleAddImages() {
        if (uploadedImageUrls.size() >= 10) {
            showError("Maximum 10 images allowed");
            return;
        }

        if (isImageUploading) {
            showError("Please wait for current image to finish uploading");
            return;
        }

        ImagePickerHelper.showImagePickerDialog(this, new ImagePickerHelper.ImagePickerListener() {
            @Override
            public void onImageSelected(File imageFile) {
                uploadImage(imageFile);
            }

            @Override
            public void onError(String error) {
                showError("Image selection failed: " + error);
            }
        });
    }

    private void uploadImage(File imageFile) {
        if (!isAdded()) return;

        setImageUploadingState(true);

        FileUploadService.uploadProductImage(imageFile, new FileUploadService.UploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                handleImageUploadSuccess(imageUrl);
            }

            @Override
            public void onError(String error) {
                handleImageUploadError(error);
            }

            @Override
            public void onProgress(int progress) {
                handleImageUploadProgress(progress);
            }
        });
    }

    private void handleImageUploadSuccess(String imageUrl) {
        if (!isAdded()) return;

        uploadedImageUrls.add(imageUrl);
        imageAdapter.notifyItemInserted(uploadedImageUrls.size() - 1);

        setImageUploadingState(false);
        validateForm();

        showSuccess("Image uploaded successfully!");
        Log.d(TAG, "✅ Image uploaded: " + imageUrl);
    }

    private void handleImageUploadError(String error) {
        if (!isAdded()) return;

        setImageUploadingState(false);
        showError("Upload failed: " + error);
        Log.e(TAG, "❌ Image upload failed: " + error);
    }

    private void handleImageUploadProgress(int progress) {
        if (!isAdded()) return;
        btnAddImages.setText("Uploading " + progress + "%");
    }

    private void onImageDelete(int position) {
        if (position >= 0 && position < uploadedImageUrls.size()) {
            uploadedImageUrls.remove(position);
            imageAdapter.notifyItemRemoved(position);
            validateForm();
            showSuccess("Image removed");
        }
    }

    private void setImageUploadingState(boolean uploading) {
        isImageUploading = uploading;
        btnAddImages.setEnabled(!uploading && !isLoading);
        btnAddImages.setText(uploading ? "Uploading..." : "Add Images");
    }

    // =============================================
    // PRODUCT CREATION
    // =============================================

    private void handleSubmitProduct() {
        if (!validateAllInputs()) {
            return;
        }

        createProduct();
    }

    private void createProduct() {
        setLoadingState(true);

        ProductRequest request = buildProductRequest();
        if (request == null) {
            setLoadingState(false);
            return;
        }

        Log.d(TAG, "Creating product: " + request.toString());

        ApiClient.getProductService().createProduct(request)
                .enqueue(new ApiCallback<ProductResponse>() {
                    @Override
                    public void onSuccess(ProductResponse result) {
                        handleProductCreated(result);
                    }

                    @Override
                    public void onError(String error) {
                        handleProductCreationError(error);
                    }
                });
    }

    private ProductRequest buildProductRequest() {
        try {
            ProductRequest request = new ProductRequest();

            // Basic info
            request.setTitle(getTextValue(etTitle));
            request.setDescription(getTextValue(etDescription));
            request.setPrice(new BigDecimal(getTextValue(etPrice)));
            request.setLocation(getTextValue(etLocation));

            // Category
            Long categoryId = getSelectedCategoryId();
            if (categoryId == null) {
                showError("Please select a valid category");
                return null;
            }
            request.setCategoryId(categoryId);

            // Condition
            String condition = getSelectedCondition();
            if (condition == null) {
                showError("Please select a valid condition");
                return null;
            }
            request.setCondition(condition);

            // Images
            request.setImageUrls(new ArrayList<>(uploadedImageUrls));

            return request;

        } catch (NumberFormatException e) {
            showError("Invalid price format");
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error building product request", e);
            showError("Error preparing product data");
            return null;
        }
    }

    private void handleProductCreated(ProductResponse product) {
        if (!isAdded()) return;

        setLoadingState(false);
        showSuccess("Product created successfully!");
        clearForm();

        Log.d(TAG, "✅ Product created: " + product.getId());
    }

    private void handleProductCreationError(String error) {
        if (!isAdded()) return;

        setLoadingState(false);
        showError("Failed to create product: " + error);
        Log.e(TAG, "❌ Product creation failed: " + error);
    }

    // =============================================
    // VALIDATION
    // =============================================

    private boolean validateAllInputs() {
        // Reset errors
        clearErrors();

        boolean isValid = true;

        // Title validation
        String title = getTextValue(etTitle);
        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            isValid = false;
        } else if (title.length() < 3) {
            etTitle.setError("Title must be at least 3 characters");
            isValid = false;
        }

        // Description validation
        String description = getTextValue(etDescription);
        if (description.isEmpty()) {
            etDescription.setError("Description is required");
            isValid = false;
        } else if (description.length() < 10) {
            etDescription.setError("Description must be at least 10 characters");
            isValid = false;
        }

        // Price validation
        String priceStr = getTextValue(etPrice);
        if (priceStr.isEmpty()) {
            etPrice.setError("Price is required");
            isValid = false;
        } else {
            try {
                double price = Double.parseDouble(priceStr);
                if (price <= 0) {
                    etPrice.setError("Price must be greater than 0");
                    isValid = false;
                } else if (price > 999999999) {
                    etPrice.setError("Price is too high");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                etPrice.setError("Invalid price format");
                isValid = false;
            }
        }

        // Location validation
        String location = getTextValue(etLocation);
        if (location.isEmpty()) {
            etLocation.setError("Location is required");
            isValid = false;
        }

        // Category validation
        if (getSelectedCategoryId() == null) {
            showError("Please select a category");
            isValid = false;
        }

        // Condition validation
        if (getSelectedCondition() == null) {
            showError("Please select a condition");
            isValid = false;
        }

        // Images validation
        if (uploadedImageUrls.isEmpty()) {
            showError("Please add at least one image");
            isValid = false;
        }

        return isValid;
    }

    private void validateForm() {
        if (!isAdded()) return;

        boolean hasRequiredFields =
                !getTextValue(etTitle).isEmpty() &&
                        !getTextValue(etDescription).isEmpty() &&
                        !getTextValue(etPrice).isEmpty() &&
                        !getTextValue(etLocation).isEmpty() &&
                        getSelectedCategoryId() != null &&
                        getSelectedCondition() != null &&
                        !uploadedImageUrls.isEmpty();

        btnSubmit.setEnabled(hasRequiredFields && !isLoading && !isImageUploading);
    }

    private void clearErrors() {
        etTitle.setError(null);
        etDescription.setError(null);
        etPrice.setError(null);
        etLocation.setError(null);
    }

    // =============================================
    // HELPER METHODS
    // =============================================

    private String getTextValue(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private Long getSelectedCategoryId() {
        String selectedCategoryName = spinnerCategory.getText().toString().trim();
        if (selectedCategoryName.isEmpty()) return null;

        for (Category category : categories) {
            if (category.getName().equals(selectedCategoryName)) {
                return category.getId();
            }
        }
        return null;
    }

    private String getSelectedCondition() {
        String selectedConditionDisplay = spinnerCondition.getText().toString().trim();
        if (selectedConditionDisplay.isEmpty()) return null;

        for (int i = 0; i < conditionDisplayNames.length; i++) {
            if (conditionDisplayNames[i].equals(selectedConditionDisplay)) {
                return conditionOptions[i];
            }
        }
        return null;
    }

    private void setLoadingState(boolean loading) {
        isLoading = loading;
        btnSubmit.setEnabled(!loading && !isImageUploading);
        btnSubmit.setText(loading ? "Creating Product..." : "Create Product");
        btnAddImages.setEnabled(!loading && !isImageUploading);

        // Disable inputs during loading
        etTitle.setEnabled(!loading);
        etDescription.setEnabled(!loading);
        etPrice.setEnabled(!loading);
        etLocation.setEnabled(!loading);
        spinnerCategory.setEnabled(!loading);
        spinnerCondition.setEnabled(!loading);
    }

    private void clearForm() {
        etTitle.setText("");
        etDescription.setText("");
        etPrice.setText("");
        etLocation.setText("");
        spinnerCategory.setText("", false);
        spinnerCondition.setText(conditionDisplayNames[0], false);

        uploadedImageUrls.clear();
        imageAdapter.notifyDataSetChanged();

        clearErrors();
        validateForm();
    }

    // =============================================
    // UI FEEDBACK
    // =============================================

    private void showError(String message) {
        if (isAdded()) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error: " + message);
        }
    }

    private void showSuccess(String message) {
        if (isAdded()) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Success: " + message);
        }
    }

    // =============================================
    // LIFECYCLE
    // =============================================

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            ImagePickerHelper.handleActivityResult(requestCode, resultCode, data, requireContext());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clear any pending operations
        isLoading = false;
        isImageUploading = false;
    }
}