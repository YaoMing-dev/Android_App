package com.example.newtrade.ui.product;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.example.newtrade.models.Product;
import com.example.newtrade.utils.NavigationUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ProductPreviewActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private RecyclerView rvImages;
    private TextView tvTitle, tvDescription, tvPrice, tvCategory, tvCondition, tvLocation;
    private MaterialButton btnEdit, btnPublish;

    private Product product;
    private List<String> imageUris;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_preview);

        getDataFromIntent();
        initViews();
        setupToolbar();
        populateData();
        setupListeners();
    }

    private void getDataFromIntent() {
        product = (Product) getIntent().getSerializableExtra("product");
        imageUris = getIntent().getStringArrayListExtra("image_uris");
        if (imageUris == null) imageUris = new ArrayList<>();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvImages = findViewById(R.id.rv_images);
        tvTitle = findViewById(R.id.tv_title);
        tvDescription = findViewById(R.id.tv_description);
        tvPrice = findViewById(R.id.tv_price);
        tvCategory = findViewById(R.id.tv_category);
        tvCondition = findViewById(R.id.tv_condition);
        tvLocation = findViewById(R.id.tv_location);
        btnEdit = findViewById(R.id.btn_edit);
        btnPublish = findViewById(R.id.btn_publish);
    }

    private void setupToolbar() {
        NavigationUtils.setupToolbarWithBackButton(this, toolbar, "Preview Product");
    }

    private void populateData() {
        if (product == null) return;

        tvTitle.setText(product.getTitle());
        tvDescription.setText(product.getDescription());
        tvPrice.setText("$" + String.format("%.2f", product.getPrice()));
        tvCategory.setText(product.getCategory());
        tvCondition.setText(product.getCondition());
        tvLocation.setText(product.getLocation());

        // Setup images RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvImages.setLayoutManager(layoutManager);

        // TODO: Create ImagePreviewAdapter to show selected images
    }

    private void setupListeners() {
        btnEdit.setOnClickListener(v -> {
            finish(); // Go back to edit
        });

        btnPublish.setOnClickListener(v -> {
            setResult(RESULT_OK);
            finish();
        });
    }
}
