// app/src/main/java/com/example/newtrade/ui/home/HomeFragment.java
package com.example.newtrade.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    // UI Components
    private RecyclerView rvCategories;
    private RecyclerView rvRecentProducts;
    private TextView tvViewAllCategories;
    private TextView tvViewAllProducts;
    private FloatingActionButton fabQuickAdd;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerViews();
        setupListeners();
        loadData();

        Log.d(TAG, "HomeFragment created successfully");
    }

    private void initViews(View view) {
        rvCategories = view.findViewById(R.id.rv_categories);
        rvRecentProducts = view.findViewById(R.id.rv_recent_products);
        tvViewAllCategories = view.findViewById(R.id.tv_view_all_categories);
        tvViewAllProducts = view.findViewById(R.id.tv_view_all_products);
        fabQuickAdd = view.findViewById(R.id.fab_quick_add);
    }

    private void setupRecyclerViews() {
        if (rvCategories != null) {
            rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        }

        if (rvRecentProducts != null) {
            rvRecentProducts.setLayoutManager(new LinearLayoutManager(getContext()));
            rvRecentProducts.setNestedScrollingEnabled(false);
        }
    }

    private void setupListeners() {
        if (fabQuickAdd != null) {
            fabQuickAdd.setOnClickListener(v -> {
                try {
                    Navigation.findNavController(v).navigate(R.id.nav_add_product);
                } catch (Exception e) {
                    Log.e(TAG, "Navigation failed", e);
                }
            });
        }

        if (tvViewAllCategories != null) {
            tvViewAllCategories.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Categories coming soon!", Toast.LENGTH_SHORT).show();
            });
        }

        if (tvViewAllProducts != null) {
            tvViewAllProducts.setOnClickListener(v -> {
                try {
                    Navigation.findNavController(v).navigate(R.id.nav_search);
                } catch (Exception e) {
                    Log.e(TAG, "Navigation failed", e);
                }
            });
        }
    }

    private void loadData() {
        loadCategories();
        loadRecentProducts();
    }

    private void loadCategories() {
        Log.d(TAG, "🔄 Loading categories...");

        try {
            Call<StandardResponse<List<Map<String, Object>>>> call = ApiClient.getApiService().getCategories();
            call.enqueue(new Callback<StandardResponse<List<Map<String, Object>>>>() {
                @Override
                public void onResponse(@NonNull Call<StandardResponse<List<Map<String, Object>>>> call,
                                       @NonNull Response<StandardResponse<List<Map<String, Object>>>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<List<Map<String, Object>>> apiResponse = response.body();
                        Log.d(TAG, "✅ Categories API response: " + apiResponse.getMessage());

                        if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                            List<Map<String, Object>> categories = apiResponse.getData();
                            Log.d(TAG, "✅ Categories loaded: " + categories.size() + " items");

                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Categories loaded: " + categories.size() + " items", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Log.w(TAG, "❌ Categories response failed: " + response.code());
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Failed to load categories", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<StandardResponse<List<Map<String, Object>>>> call, @NonNull Throwable t) {
                    Log.e(TAG, "❌ Categories API call failed", t);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "❌ Error calling categories API", e);
        }
    }

    private void loadRecentProducts() {
        Log.d(TAG, "🔄 Loading recent products...");

        try {
            Call<StandardResponse<Map<String, Object>>> call = ApiClient.getApiService().getProducts();
            call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                @Override
                public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                       @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<Map<String, Object>> apiResponse = response.body();
                        Log.d(TAG, "✅ Products API response: " + apiResponse.getMessage());

                        if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                            Map<String, Object> data = apiResponse.getData();
                            Log.d(TAG, "✅ Products data: " + data.toString());

                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Products loaded successfully!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Log.w(TAG, "❌ Products response failed: " + response.code());
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Failed to load products", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                    Log.e(TAG, "❌ Products API call failed", t);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "❌ Error calling products API", e);
        }
    }
}