package com.example.newtrade.ui.favorites;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.example.newtrade.adapters.ProductAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.product.ProductDetailActivity;
import com.example.newtrade.utils.NavigationUtils;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoritesActivity extends AppCompatActivity {

    private static final String TAG = "FavoritesActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private RecyclerView rvFavorites;
    private LinearLayout llEmptyState;

    // Data
    private ProductAdapter productAdapter;
    private List<Product> favoriteProducts = new ArrayList<>();
    private SharedPrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        prefsManager = SharedPrefsManager.getInstance(this);

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadFavorites();

        Log.d(TAG, "FavoritesActivity created");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvFavorites = findViewById(R.id.rv_favorites);
        llEmptyState = findViewById(R.id.ll_empty_state);
    }

    private void setupToolbar() {
        NavigationUtils.setupToolbarWithBackButton(this, toolbar, "My Favorites");
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        rvFavorites.setLayoutManager(layoutManager);

        productAdapter = new ProductAdapter(favoriteProducts, product -> {
            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        });
        rvFavorites.setAdapter(productAdapter);
    }

    private void loadFavorites() {
        Log.d(TAG, "Loading favorite products");

        ApiClient.getApiService().getFavorites()
            .enqueue(new Callback<StandardResponse<List<Product>>>() {
                @Override
                public void onResponse(@NonNull Call<StandardResponse<List<Product>>> call,
                                       @NonNull Response<StandardResponse<List<Product>>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<List<Product>> apiResponse = response.body();

                        if (apiResponse.isSuccess()) {
                            List<Product> products = apiResponse.getData();

                            favoriteProducts.clear();
                            if (products != null) {
                                favoriteProducts.addAll(products);
                            }

                            productAdapter.notifyDataSetChanged();
                            updateEmptyState();

                            Log.d(TAG, "✅ Favorites loaded: " + favoriteProducts.size());
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<StandardResponse<List<Product>>> call, @NonNull Throwable t) {
                    Log.e(TAG, "Failed to load favorites", t);
                    Toast.makeText(FavoritesActivity.this, "Failed to load favorites", Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                }
            });
    }

    private void updateEmptyState() {
        if (favoriteProducts.isEmpty()) {
            llEmptyState.setVisibility(View.VISIBLE);
            rvFavorites.setVisibility(View.GONE);
        } else {
            llEmptyState.setVisibility(View.GONE);
            rvFavorites.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh favorites when returning to this activity
        loadFavorites();
    }
}
