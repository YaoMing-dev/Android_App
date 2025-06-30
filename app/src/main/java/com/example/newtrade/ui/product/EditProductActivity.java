// app/src/main/java/com/example/newtrade/ui/product/EditProductActivity.java
package com.example.newtrade.ui.product;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.ImageUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
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

public class EditProductActivity extends AddProductActivity {
    private static final String TAG = "EditProductActivity";

    private Long productId;
    private Product existingProduct;
    private List<String> originalImageUrls = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get product ID from intent
        productId = getIntent().getLongExtra("product_id", -1);
        if (productId == -1) {
            Toast.makeText(this, "Invalid product ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Update toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Edit Product");
        }

        // Update publish button text
        if (btnPublish != null) {
            btnPublish.setText("Update Product");
        }

        // Load existing product data
        loadProductData();
    }

    private void loadProductData() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        ApiClient.getProductService().getProductById(productId)
                .enqueue(new Callback<StandardResponse<Product>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Product>> call,
                                           Response<StandardResponse<Product>> response) {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }

                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Product> apiResponse = response.body();

                            if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                                existingProduct = apiResponse.getData();
                                populateFields();
                            } else {
                                Toast.makeText(EditProductActivity.this,
                                        apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } else {
                            Toast.makeText(EditProductActivity.this,
                                    "Failed to load product data", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Product>> call, Throwable t) {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        Log.e(TAG, "Failed to load product", t);
                        Toast.makeText(EditProductActivity.this,
                                "Network error", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void populateFields() {
        if (existingProduct == null) return;

        // Populate text fields
        if (etTitle != null) etTitle.setText(existingProduct.getTitle());
        if (etDescription != null) etDescription.setText(existingProduct.getDescription());
        if (etPrice != null && existingProduct.getPrice() != null) {
            etPrice.setText(existingProduct.getPrice().toString());
        }
        if (etLocation != null) etLocation.setText(existingProduct.getLocation());

        // Set GPS coordinates
        currentLatitude = existingProduct.getLatitude();
        currentLongitude = existingProduct.getLongitude();

        // Set category
        if (existingProduct.getCategory() != null) {
            selectedCategory = existingProduct.getCategory();
            if (actvCategory != null) {
                actvCategory.setText(selectedCategory.getName());
            }
        }

        // Set condition
        if (existingProduct.getCondition() != null) {
            selectedCondition = existingProduct.getCondition();
            if (actvCondition != null) {
                actvCondition.setText(selectedCondition.getDisplayName());
            }
        }

        // Load existing images
        if (existingProduct.getImages() != null && !existingProduct.getImages().isEmpty()) {
            selectedImagePaths.clear();
            originalImageUrls.clear();

            for (com.example.newtrade.models.ProductImage image : existingProduct.getImages()) {
                selectedImagePaths.add(image.getImageUrl());
                originalImageUrls.add(image.getImageUrl());
            }

            if (imageAdapter != null) {
                imageAdapter.notifyDataSetChanged();
            }
        }

        // Enable buttons
        updateButtonStates();
    }

    @Override
    protected void publishProduct() {
        if (!validateForm()) {
            return;
        }

        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        // Check if any new images need to be uploaded
        List<String> newImagePaths = new ArrayList<>();
        for (String imagePath : selectedImagePaths) {
            if (!originalImageUrls.contains(imagePath)) {
                newImagePaths.add(imagePath);
            }
        }

        if (!newImagePaths.isEmpty()) {
            // Upload new images first
            uploadNewImagesAndUpdate(newImagePaths);
        } else {
            // No new images, update product directly
            updateProductWithExistingImages();
        }
    }

    private void uploadNewImagesAndUpdate(List<String> newImagePaths) {
        List<String> uploadedImageUrls = new ArrayList<>(originalImageUrls);
        uploadNextNewImage(0, newImagePaths, uploadedImageUrls);
    }

    private void uploadNextNewImage(int index, List<String> newImagePaths, List<String> allImageUrls) {
        if (index >= newImagePaths.size()) {
            // All new images uploaded, now update product
            updateProductWithImages(allImageUrls);
            return;
        }

        String imagePath = newImagePaths.get(index);

        // Check if it's a local file path (new image) or URL (existing image)
        if (imagePath.startsWith("http")) {
            // It's an existing image URL, skip upload
            uploadNextNewImage(index + 1, newImagePaths, allImageUrls);
            return;
        }

        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            uploadNextNewImage(index + 1, newImagePaths, allImageUrls);
            return;
        }

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);

        ApiClient.getProductService().uploadProductImage(imagePart)
                .enqueue(new Callback<StandardResponse<Map<String, String>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, String>>> call,
                                           Response<StandardResponse<Map<String, String>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, String>> apiResponse = response.body();

                            if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                                String imageUrl = apiResponse.getData().get("imageUrl");
                                if (imageUrl != null) {
                                    allImageUrls.add(imageUrl);
                                }

                                // Upload next image
                                uploadNextNewImage(index + 1, newImagePaths, allImageUrls);
                            } else {
                                if (progressBar != null) {
                                    progressBar.setVisibility(View.GONE);
                                }
                                Toast.makeText(EditProductActivity.this,
                                        "Failed to upload image: " + apiResponse.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            if (progressBar != null) {
                                progressBar.setVisibility(View.GONE);
                            }
                            Toast.makeText(EditProductActivity.this,
                                    "Failed to upload image", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, String>>> call, Throwable t) {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        Log.e(TAG, "Image upload failed", t);
                        Toast.makeText(EditProductActivity.this,
                                "Network error during image upload", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateProductWithExistingImages() {
        updateProductWithImages(selectedImagePaths);
    }

    private void updateProductWithImages(List<String> imageUrls) {
        Map<String, Object> productRequest = new HashMap<>();

        if (etTitle != null) {
            productRequest.put("title", etTitle.getText().toString().trim());
        }
        if (etDescription != null) {
            productRequest.put("description", etDescription.getText().toString().trim());
        }
        if (etPrice != null) {
            try {
                productRequest.put("price", new BigDecimal(etPrice.getText().toString().trim()));
            } catch (NumberFormatException e) {
                productRequest.put("price", BigDecimal.ZERO);
            }
        }
        if (selectedCondition != null) {
            productRequest.put("condition", selectedCondition.name());
        }
        if (etLocation != null) {
            productRequest.put("location", etLocation.getText().toString().trim());
        }
        if (selectedCategory != null) {
            productRequest.put("categoryId", selectedCategory.getId());
        }

        productRequest.put("imageUrls", imageUrls);

        if (currentLatitude != null && currentLongitude != null) {
            productRequest.put("latitude", currentLatitude);
            productRequest.put("longitude", currentLongitude);
            productRequest.put("locationRadius", com.example.newtrade.utils.Constants.DEFAULT_LOCATION_RADIUS);
        }

        Long userId = prefsManager.getUserId();

        ApiClient.getProductService().updateProduct(productId, productRequest, userId)
                .enqueue(new Callback<StandardResponse<Product>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Product>> call,
                                           Response<StandardResponse<Product>> response) {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }

                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Product> apiResponse = response.body();

                            if (apiResponse.isSuccess()) {
                                Toast.makeText(EditProductActivity.this,
                                        "Product updated successfully!", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(EditProductActivity.this,
                                        apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(EditProductActivity.this,
                                    "Failed to update product", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Product>> call, Throwable t) {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        Log.e(TAG, "Update product failed", t);
                        Toast.makeText(EditProductActivity.this,
                                "Network error during product update", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void updateButtonStates() {
        if (btnPreview == null || btnPublish == null) return;

        boolean isFormComplete =
                etTitle != null && !etTitle.getText().toString().trim().isEmpty() &&
                        etDescription != null && !etDescription.getText().toString().trim().isEmpty() &&
                        etPrice != null && !etPrice.getText().toString().trim().isEmpty() &&
                        etLocation != null && !etLocation.getText().toString().trim().isEmpty() &&
                        selectedCategory != null &&
                        selectedCondition != null &&
                        !selectedImagePaths.isEmpty();

        btnPreview.setEnabled(isFormComplete);
        btnPublish.setEnabled(isFormComplete);
    }
}