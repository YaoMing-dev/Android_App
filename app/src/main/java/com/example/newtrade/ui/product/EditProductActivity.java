// app/src/main/java/com/example/newtrade/ui/product/EditProductActivity.java
package com.example.newtrade.ui.product;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.SharedPrefsManager;
import com.example.newtrade.utils.ValidationUtils;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProductActivity extends AppCompatActivity {

    private static final String TAG = "EditProductActivity";
    private static final int REQUEST_IMAGE_PICK = 1001;

    // UI Components
    private MaterialToolbar toolbar;
    private EditText etTitle, etDescription, etPrice, etLocation;
    private Spinner spCategory, spCondition;
    private ImageView ivProductImage;
    private Button btnSelectImage, btnUpdateProduct;

    // Data
    private SharedPrefsManager prefsManager;
    private Long productId;
    private String currentImageUrl;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        // Get product ID from intent
        productId = getIntent().getLongExtra(Constants.EXTRA_PRODUCT_ID, -1L);
        if (productId == -1L) {
            Log.e(TAG, "❌ Product ID not provided");
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupSpinners();
        setupListeners();
        loadProductData();

        Log.d(TAG, "EditProductActivity created for product: " + productId);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        etPrice = findViewById(R.id.et_price);
        etLocation = findViewById(R.id.et_location);
        spCategory = findViewById(R.id.sp_category);
        spCondition = findViewById(R.id.sp_condition);
        ivProductImage = findViewById(R.id.iv_product_image);
        btnSelectImage = findViewById(R.id.btn_select_image);
        btnUpdateProduct = findViewById(R.id.btn_update_product);

        prefsManager = SharedPrefsManager.getInstance(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Product");
        }
    }

    private void setupSpinners() {
        // Category spinner
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, Constants.PRODUCT_CATEGORIES);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(categoryAdapter);

        // Condition spinner
        String[] conditions = {"New", "Like New", "Good", "Fair", "Poor"};
        ArrayAdapter<String> conditionAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, conditions);
        conditionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCondition.setAdapter(conditionAdapter);
    }

    private void setupListeners() {
        btnSelectImage.setOnClickListener(v -> selectImage());
        btnUpdateProduct.setOnClickListener(v -> updateProduct());

        // Add text watchers for validation
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateForm();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etTitle.addTextChangedListener(textWatcher);
        etDescription.addTextChangedListener(textWatcher);
        etPrice.addTextChangedListener(textWatcher);
    }

    private void loadProductData() {
        Call<StandardResponse<Map<String, Object>>> call =
                ApiClient.getProductService().getProductById(productId);

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                   Response<StandardResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    populateFields(response.body().getData());
                } else {
                    showError("Failed to load product data");
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void populateFields(Map<String, Object> productData) {
        if (productData == null) return;

        etTitle.setText((String) productData.get("title"));
        etDescription.setText((String) productData.get("description"));
        etPrice.setText(String.valueOf(productData.get("price")));
        etLocation.setText((String) productData.get("location"));

        currentImageUrl = (String) productData.get("imageUrl");
        if (currentImageUrl != null) {
            Glide.with(this)
                    .load(currentImageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .into(ivProductImage);
        }

        // Set spinners
        String category = (String) productData.get("category");
        String condition = (String) productData.get("condition");

        // Set category selection
        ArrayAdapter<String> categoryAdapter = (ArrayAdapter<String>) spCategory.getAdapter();
        int categoryPos = categoryAdapter.getPosition(category);
        if (categoryPos >= 0) spCategory.setSelection(categoryPos);

        // Set condition selection
        ArrayAdapter<String> conditionAdapter = (ArrayAdapter<String>) spCondition.getAdapter();
        int conditionPos = conditionAdapter.getPosition(condition);
        if (conditionPos >= 0) spCondition.setSelection(conditionPos);
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    private void updateProduct() {
        if (isLoading) return;

        // Validate form
        if (!validateForm()) {
            return;
        }

        isLoading = true;
        btnUpdateProduct.setEnabled(false);
        btnUpdateProduct.setText("Updating...");

        // Create update data
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("title", etTitle.getText().toString().trim());
        updateData.put("description", etDescription.getText().toString().trim());
        updateData.put("price", Double.parseDouble(etPrice.getText().toString().trim()));
        updateData.put("location", etLocation.getText().toString().trim());
        updateData.put("category", spCategory.getSelectedItem().toString());
        updateData.put("condition", spCondition.getSelectedItem().toString());

        Call<StandardResponse<Map<String, Object>>> call =
                ApiClient.getProductService().updateProduct(prefsManager.getUserId(), productId, updateData);

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                   Response<StandardResponse<Map<String, Object>>> response) {
                isLoading = false;
                btnUpdateProduct.setEnabled(true);
                btnUpdateProduct.setText("Update Product");

                if (response.isSuccessful() && response.body() != null) {
                    showSuccess("Product updated successfully!");
                    finish();
                } else {
                    showError("Failed to update product");
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                isLoading = false;
                btnUpdateProduct.setEnabled(true);
                btnUpdateProduct.setText("Update Product");
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private boolean validateForm() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String price = etPrice.getText().toString().trim();

        // Validate title
        String titleError = ValidationUtils.getProductTitleErrorMessage(title);
        if (titleError != null) {
            etTitle.setError(titleError);
            return false;
        }

        // Validate description
        String descError = ValidationUtils.getProductDescriptionErrorMessage(description);
        if (descError != null) {
            etDescription.setError(descError);
            return false;
        }

        // Validate price
        String priceError = ValidationUtils.getPriceErrorMessage(price);
        if (priceError != null) {
            etPrice.setError(priceError);
            return false;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                ivProductImage.setImageURI(imageUri);
                // TODO: Upload image to server
            }
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}