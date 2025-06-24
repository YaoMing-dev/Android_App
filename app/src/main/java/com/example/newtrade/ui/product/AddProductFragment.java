// app/src/main/java/com/example/newtrade/ui/product/AddProductFragment.java
package com.example.newtrade.ui.product;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.newtrade.R;
import com.google.android.material.textfield.TextInputEditText;

public class AddProductFragment extends Fragment {

    private static final String TAG = "AddProductFragment";

    // UI Components
    private ImageView ivAddImage;
    private TextInputEditText etTitle;
    private TextInputEditText etDescription;
    private TextInputEditText etPrice;
    private TextInputEditText etLocation;
    private Spinner spinnerCategory;
    private Spinner spinnerCondition;
    private Button btnSelectLocation;
    private Button btnPreview;
    private Button btnPublish;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupListeners();

        Log.d(TAG, "AddProductFragment created successfully");
    }

    private void initViews(View view) {
        ivAddImage = view.findViewById(R.id.iv_add_image);
        etTitle = view.findViewById(R.id.et_title);
        etDescription = view.findViewById(R.id.et_description);
        etPrice = view.findViewById(R.id.et_price);
        etLocation = view.findViewById(R.id.et_location);
        spinnerCategory = view.findViewById(R.id.spinner_category);
        spinnerCondition = view.findViewById(R.id.spinner_condition);
        btnSelectLocation = view.findViewById(R.id.btn_select_location);
        btnPreview = view.findViewById(R.id.btn_preview);
        btnPublish = view.findViewById(R.id.btn_publish);
    }

    private void setupListeners() {
        if (ivAddImage != null) {
            ivAddImage.setOnClickListener(v -> {
                // TODO: Add image picker
            });
        }

        if (btnSelectLocation != null) {
            btnSelectLocation.setOnClickListener(v -> {
                // TODO: Navigate to location picker
            });
        }

        if (btnPreview != null) {
            btnPreview.setOnClickListener(v -> {
                // TODO: Navigate to product preview
            });
        }

        if (btnPublish != null) {
            btnPublish.setOnClickListener(v -> {
                // TODO: Publish product
            });
        }
    }
}