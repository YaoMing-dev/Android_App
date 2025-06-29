package com.example.newtrade.ui.product;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Product;
import com.example.newtrade.ui.adapters.ProductImagesAdapter;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddProductFragment extends Fragment {
    private TextInputEditText etTitle, etDescription, etPrice;
    private AutoCompleteTextView spinnerCategory, spinnerCondition;
    private Button btnAddImages, btnSubmit;
    private RecyclerView rvImages;
    private ProductImagesAdapter imagesAdapter;
    private List<Uri> selectedImages = new ArrayList<>();

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Uri imageUri = result.getData().getData();
                if (imageUri != null) {
                    selectedImages.add(imageUri);
                    imagesAdapter.notifyItemInserted(selectedImages.size() - 1);
                }
            }
        }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupSpinners();
        setupImageRecyclerView();
        setupListeners();
    }

    private void initializeViews(View view) {
        etTitle = view.findViewById(R.id.etTitle);
        etDescription = view.findViewById(R.id.etDescription);
        etPrice = view.findViewById(R.id.etPrice);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        spinnerCondition = view.findViewById(R.id.spinnerCondition);
        btnAddImages = view.findViewById(R.id.btnAddImages);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        rvImages = view.findViewById(R.id.rvImages);
    }

    private void setupSpinners() {
        String[] categories = {"Electronics", "Fashion", "Home", "Sports", "Books", "Others"};
        String[] conditions = {"New", "Like New", "Good", "Fair", "Poor"};

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            categories
        );
        spinnerCategory.setAdapter(categoryAdapter);

        ArrayAdapter<String> conditionAdapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            conditions
        );
        spinnerCondition.setAdapter(conditionAdapter);
    }

    private void setupImageRecyclerView() {
        imagesAdapter = new ProductImagesAdapter(selectedImages, position -> {
            selectedImages.remove(position);
            imagesAdapter.notifyItemRemoved(position);
        });
        rvImages.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvImages.setAdapter(imagesAdapter);
    }

    private void setupListeners() {
        btnAddImages.setOnClickListener(v -> checkPermissionAndPickImage());
        btnSubmit.setOnClickListener(v -> validateAndSubmitProduct());
    }

    private void checkPermissionAndPickImage() {
        Dexter.withContext(getContext())
            .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(new PermissionListener() {
                @Override
                public void onPermissionGranted(PermissionGrantedResponse response) {
                    openImagePicker();
                }

                @Override
                public void onPermissionDenied(PermissionDeniedResponse response) {
                    Snackbar.make(requireView(), "Permission required to select images", Snackbar.LENGTH_LONG).show();
                }

                @Override
                public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                    token.continuePermissionRequest();
                }
            }).check();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void validateAndSubmitProduct() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String category = spinnerCategory.getText().toString();
        String condition = spinnerCondition.getText().toString();

        if (title.isEmpty() || description.isEmpty() || priceStr.isEmpty()
            || category.isEmpty() || condition.isEmpty()) {
            showError("Please fill all fields");
            return;
        }

        if (selectedImages.isEmpty()) {
            showError("Please add at least one image");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            showError("Invalid price format");
            return;
        }

        submitProduct(title, description, price, category, condition);
    }

    private void submitProduct(String title, String description, double price,
                             String category, String condition) {
        btnSubmit.setEnabled(false);

        Product product = new Product();
        product.setTitle(title);
        product.setDescription(description);
        product.setPrice(price);
        product.setCategory(category);
        product.setCondition(condition);

        // ✅ FIX: Convert Product to Map<String,Object> for API
        Map<String, Object> productMap = new HashMap<>();
        productMap.put("title", product.getTitle());
        productMap.put("description", product.getDescription());
        productMap.put("price", product.getPrice());
        productMap.put("category", product.getCategory());
        productMap.put("condition", product.getCondition());
        productMap.put("location", "Default Location"); // You can add location picker later

        List<MultipartBody.Part> imageParts = new ArrayList<>();
        for (Uri imageUri : selectedImages) {
            File file = new File(imageUri.getPath());
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData(
                "images", file.getName(), requestFile);
            imageParts.add(imagePart);
        }

        ApiClient.getClient().createProduct(productMap, imageParts)
            .enqueue(new Callback<Product>() {
                @Override
                public void onResponse(@NonNull Call<Product> call, @NonNull Response<Product> response) {
                    btnSubmit.setEnabled(true);
                    if (response.isSuccessful() && response.body() != null) {
                        Snackbar.make(requireView(), "Product posted successfully", Snackbar.LENGTH_LONG).show();
                        clearForm();
                    } else {
                        showError("Failed to post product");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Product> call, @NonNull Throwable t) {
                    btnSubmit.setEnabled(true);
                    showError("Network error");
                }
            });
    }

    private void clearForm() {
        etTitle.setText("");
        etDescription.setText("");
        etPrice.setText("");
        spinnerCategory.setText("");
        spinnerCondition.setText("");
        selectedImages.clear();
        imagesAdapter.notifyDataSetChanged();
    }

    private void showError(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
        }
    }
}
