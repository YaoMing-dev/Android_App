// app/src/main/java/com/example/newtrade/ui/product/ProductPreviewActivity.java
package com.example.newtrade.ui.product;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.example.newtrade.ui.product.adapter.PreviewImageAdapter;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;

public class ProductPreviewActivity extends AppCompatActivity {

    // UI Components
    private Toolbar toolbar;
    private RecyclerView rvImages;
    private TextView tvTitle, tvPrice, tvDescription, tvLocation;
    private Chip chipCategory, chipCondition;
    private Button btnEdit, btnPublish;

    // Data
    private ArrayList<String> imagePaths;
    private String title, description, price, location, category, condition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_preview);

        getIntentData();
        initViews();
        setupToolbar();
        setupData();
        setupListeners();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        description = intent.getStringExtra("description");
        price = intent.getStringExtra("price");
        location = intent.getStringExtra("location");
        category = intent.getStringExtra("category");
        condition = intent.getStringExtra("condition");
        imagePaths = intent.getStringArrayListExtra("images");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvImages = findViewById(R.id.rv_images);
        tvTitle = findViewById(R.id.tv_title);
        tvPrice = findViewById(R.id.tv_price);
        tvDescription = findViewById(R.id.tv_description);
        tvLocation = findViewById(R.id.tv_location);
        chipCategory = findViewById(R.id.chip_category);
        chipCondition = findViewById(R.id.chip_condition);
        btnEdit = findViewById(R.id.btn_edit);
        btnPublish = findViewById(R.id.btn_publish);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Preview");
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupData() {
        // Setup image preview
        PreviewImageAdapter imageAdapter = new PreviewImageAdapter(imagePaths);
        rvImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvImages.setAdapter(imageAdapter);

        // Set text data
        tvTitle.setText(title);
        tvPrice.setText("₫" + String.format("%,.0f", Double.parseDouble(price)));
        tvDescription.setText(description);
        tvLocation.setText(location);
        chipCategory.setText(category);
        chipCondition.setText(condition);
    }

    private void setupListeners() {
        btnEdit.setOnClickListener(v -> finish());

        btnPublish.setOnClickListener(v -> {
            // Return to AddProductActivity to publish
            setResult(RESULT_OK);
            finish();
        });
    }
}