// File: app/src/main/java/com/example/newtrade/ui/product/AddProductFragment.java
// THAY THẾ TOÀN BỘ:

package com.example.newtrade.ui.product;

import android.app.Activity;
import android.content.Intent;
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
import androidx.fragment.app.Fragment;

import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Category;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.textfield.TextInputEditText;

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

    // UI Components
    private ImageView ivProductImage;
    private TextInputEditText etTitle, etDescription, etPrice, etLocation;
    private Spinner spinnerCategory, spinnerCondition;
    private Button btnSelectImage, btnPublish;

    // Data
    private List<Category> categories = new ArrayList<>();
    private Uri selectedImageUri;
    private SharedPrefsManager prefsManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initUtils();
        loadCategories();
        setupConditionSpinner();
        setupListeners();

        Log.d(TAG, "AddProductFragment created successfully");
    }

    private void initViews(View view) {
        ivProductImage = view.findViewById(R.id.iv_product_image);
        etTitle = view.findViewById(R.id.et_title);
        etDescription = view.findViewById(R.id.et_description);
        etPrice = view.findViewById(R.id.et_price);
        etLocation = view.findViewById(R.id.et_location);
        spinnerCategory = view.findViewById(R.id.spinner_category);
        spinnerCondition = view.findViewById(R.id.spinner_condition);
        btnSelectImage = view.findViewById(R.id.btn_select_image);
        btnPublish = view.findViewById(R.id.btn_publish);
    }

    private void initUtils() {
        prefsManager = SharedPrefsManager.getInstance(requireContext());
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
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<List<Map<String, Object>>>> call, Throwable t) {
                        Log.e(TAG, "❌ Failed to load categories", t);
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

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                conditions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCondition.setAdapter(adapter);
    }

    private void setupListeners() {
        btnSelectImage.setOnClickListener(v -> selectImage());
        btnPublish.setOnClickListener(v -> publishProduct());
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                ivProductImage.setImageURI(selectedImageUri);
                btnSelectImage.setText("Image Selected");
            }
        }
    }

    private void publishProduct() {
        // Validate required fields
        if (!validateInputs()) {
            return;
        }

        // Disable button to prevent double submission
        btnPublish.setEnabled(false);
        btnPublish.setText("Publishing...");

        // Create product data
        Map<String, Object> productData = new HashMap<>();
        productData.put("title", etTitle.getText().toString().trim());
        productData.put("description", etDescription.getText().toString().trim());
        productData.put("price", Double.parseDouble(etPrice.getText().toString().trim()));
        productData.put("location", etLocation.getText().toString().trim());

        // Category
        int categoryIndex = spinnerCategory.getSelectedItemPosition() - 1; // -1 vì có "Select Category"
        if (categoryIndex >= 0 && categoryIndex < categories.size()) {
            productData.put("categoryId", categories.get(categoryIndex).getId());
        }

        // Condition
        String condition = (String) spinnerCondition.getSelectedItem();
        if (!"Select Condition".equals(condition)) {
            productData.put("conditionType", condition);
        }

        // Call API
        ApiClient.getApiService().createProduct(productData)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {

                        btnPublish.setEnabled(true);
                        btnPublish.setText("Publish Product");

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(getContext(), "Product published successfully!", Toast.LENGTH_SHORT).show();
                            clearForm();

                            // Navigate back to home
                            if (getActivity() != null) {
                                requireActivity().onBackPressed();
                            }
                        } else {
                            String errorMsg = response.body() != null ? response.body().getMessage() : "Unknown error";
                            Toast.makeText(getContext(), "Failed to publish: " + errorMsg, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        btnPublish.setEnabled(true);
                        btnPublish.setText("Publish Product");

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

        // Image (required theo đề thi: "at least 1 photo")
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
        spinnerCategory.setSelection(0);
        spinnerCondition.setSelection(0);
        ivProductImage.setImageResource(R.drawable.ic_add_photo);
        btnSelectImage.setText("Select Image");
        selectedImageUri = null;
    }
}