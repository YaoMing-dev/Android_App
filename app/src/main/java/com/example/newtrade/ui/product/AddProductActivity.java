// app/src/main/java/com/example/newtrade/ui/product/AddProductActivity.java
package com.example.newtrade.ui.product;

import android.app.Activity;
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
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class AddProductActivity extends AppCompatActivity {

    private static final String TAG = "AddProductActivity";
    private static final int REQUEST_IMAGE_PICK = 1001;

    // UI Components
    private MaterialToolbar toolbar;
    private EditText etTitle, etDescription, etPrice, etLocation;
    private Spinner spCategory, spCondition;
    private ImageView ivProductImage;
    private Button btnSelectImage, btnPublish;

    // Data
    private SharedPrefsManager prefsManager;
    private Uri selectedImageUri;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        initViews();
        setupToolbar();
        setupSpinners();
        setupListeners();

        Log.d(TAG, "AddProductActivity created");
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
        btnPublish = findViewById(R.id.btn_publish);

        prefsManager = SharedPrefsManager.getInstance(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add Product");
        }
    }

    private void setupSpinners() {
        // Category spinner
        List<String> categories = new ArrayList<>();
        categories.add("Electronics");
        categories.add("Fashion");
        categories.add("Home & Garden");
        categories.add("Sports");
        categories.add("Books");
        categories.add("Other");

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(categoryAdapter);

        // Condition spinner
        List<String> conditions = new ArrayList<>();
        conditions.add("New");
        conditions.add("Like New");
        conditions.add("Good");
        conditions.add("Fair");
        conditions.add("Poor");

        ArrayAdapter<String> conditionAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, conditions);
        conditionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCondition.setAdapter(conditionAdapter);
    }

    private void setupListeners() {
        btnSelectImage.setOnClickListener(v -> selectImage());
        btnPublish.setOnClickListener(v -> publishProduct());

        // Form validation
        TextWatcher formWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                updatePublishButton();
            }
        };

        etTitle.addTextChangedListener(formWatcher);
        etDescription.addTextChangedListener(formWatcher);
        etPrice.addTextChangedListener(formWatcher);
        etLocation.addTextChangedListener(formWatcher);
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                Glide.with(this)
                        .load(selectedImageUri)
                        .centerCrop()
                        .into(ivProductImage);
                updatePublishButton();
            }
        }
    }

    private void publishProduct() {
        if (isLoading) return;

        if (!validateForm()) return;

        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        isLoading = true;
        btnPublish.setEnabled(false);
        btnPublish.setText("Publishing...");

        // TODO: Call API to create product
        publishProductAPI(title, description, priceStr, location);
    }

    private boolean validateForm() {
        boolean isValid = true;

        if (etTitle.getText().toString().trim().isEmpty()) {
            etTitle.setError("Title is required");
            isValid = false;
        }

        if (etDescription.getText().toString().trim().isEmpty()) {
            etDescription.setError("Description is required");
            isValid = false;
        }

        if (etPrice.getText().toString().trim().isEmpty()) {
            etPrice.setError("Price is required");
            isValid = false;
        }

        if (etLocation.getText().toString().trim().isEmpty()) {
            etLocation.setError("Location is required");
            isValid = false;
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select a product image", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    private void publishProductAPI(String title, String description, String price, String location) {
        // TODO: Implement API call when ProductService is available
        Log.d(TAG, "Publishing product: " + title + ", Price: " + price);

        // Simulate success for now
        new android.os.Handler().postDelayed(() -> {
            isLoading = false;
            btnPublish.setEnabled(true);
            btnPublish.setText("Publish Product");

            Toast.makeText(this, "Product published successfully!", Toast.LENGTH_SHORT).show();
            finish();
        }, 2000);
    }

    private void updatePublishButton() {
        boolean isFormValid = !etTitle.getText().toString().trim().isEmpty() &&
                !etDescription.getText().toString().trim().isEmpty() &&
                !etPrice.getText().toString().trim().isEmpty() &&
                !etLocation.getText().toString().trim().isEmpty() &&
                selectedImageUri != null;

        btnPublish.setEnabled(!isLoading && isFormValid);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}