// app/src/main/java/com/example/newtrade/ui/profile/OfferHistoryActivity.java
package com.example.newtrade.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newtrade.R;
import com.example.newtrade.adapters.OfferAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Offer;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.product.ProductDetailActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OfferHistoryActivity extends AppCompatActivity implements OfferAdapter.OnOfferClickListener {

    private static final String TAG = "OfferHistoryActivity";
    private static final int PAGE_SIZE = 20;

    // UI Components
    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvOffers;
    private TextView tvEmptyState;

    // Data
    private List<Offer> offers = new ArrayList<>();
    private OfferAdapter offerAdapter;
    private String currentTab = "SENT"; // SENT or RECEIVED
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean hasMorePages = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_history);

        initViews();
        setupToolbar();
        setupTabs();
        setupRecyclerView();
        setupSwipeRefresh();

        // ✅ LOAD REAL DATA từ API
        loadOffers();

        Log.d(TAG, "✅ OfferHistoryActivity created");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tab_layout);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        rvOffers = findViewById(R.id.rv_offers);
        tvEmptyState = findViewById(R.id.tv_empty_state);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Offers");
        }
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("SENT"));
        tabLayout.addTab(tabLayout.newTab().setText("RECEIVED"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition() == 0 ? "SENT" : "RECEIVED";
                currentPage = 0;
                hasMorePages = true;

                // ✅ UPDATE ADAPTER TAB
                offerAdapter.updateTab(currentTab);
                refreshOffers();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        offerAdapter = new OfferAdapter(offers, currentTab, this);

        // ✅ SET ACTION LISTENER
        offerAdapter.setOnOfferActionListener(new OfferAdapter.OnOfferActionListener() {
            @Override
            public void onOfferAccepted(Offer offer) {
                respondToOffer(offer.getId(), "ACCEPTED", null, "Thank you! I accept your offer.");
            }

            @Override
            public void onOfferRejected(Offer offer) {
                respondToOffer(offer.getId(), "REJECTED", null, "Sorry, I cannot accept this offer.");
            }

            @Override
            public void onOfferCountered(Offer offer, double counterAmount, String message) {
                respondToOffer(offer.getId(), "COUNTERED", counterAmount, message);
            }

            @Override
            public void onOfferCancelled(Offer offer) {
                cancelOffer(offer.getId());
            }
        });

        rvOffers.setLayoutManager(new LinearLayoutManager(this));
        rvOffers.setAdapter(offerAdapter);

        // ✅ PAGINATION...
    }

    // ✅ THÊM methods cho API calls:
    private void respondToOffer(Long offerId, String status, Double counterAmount, String message) {
        Log.d(TAG, "🔄 Responding to offer: " + offerId + " with status: " + status +
                (counterAmount != null ? ", amount: " + counterAmount : ""));

        ApiClient.getOfferService().respondToOffer(offerId, status, counterAmount, message)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            String successMsg = "ACCEPTED".equals(status) ? "Offer accepted!" :
                                    "REJECTED".equals(status) ? "Offer rejected!" :
                                            "COUNTERED".equals(status) ? "Counter offer sent!" : "Response sent!";

                            Toast.makeText(OfferHistoryActivity.this, successMsg, Toast.LENGTH_SHORT).show();
                            refreshOffers(); // Reload data

                        } else {
                            // ✅ BETTER ERROR HANDLING
                            String error = "Failed to respond to offer";
                            if (response.body() != null) {
                                String backendError = response.body().getMessage();

                                if (backendError.contains("Offer not found")) {
                                    error = "Offer no longer exists";
                                } else if (backendError.contains("Only seller can respond")) {
                                    error = "You can only respond to offers on your products";
                                } else if (backendError.contains("no longer pending")) {
                                    error = "Offer has already been responded to";
                                } else if (backendError.contains("expired")) {
                                    error = "Offer has expired";
                                } else {
                                    error = backendError;
                                }
                            }

                            Log.e(TAG, "❌ Respond error: " + response.code() + " - " + error);
                            Toast.makeText(OfferHistoryActivity.this, error, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        Log.e(TAG, "❌ Network error responding to offer", t);
                        Toast.makeText(OfferHistoryActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void cancelOffer(Long offerId) {
        ApiClient.getOfferService().cancelOffer(offerId)
                .enqueue(new Callback<StandardResponse<String>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<String>> call,
                                           Response<StandardResponse<String>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(OfferHistoryActivity.this, "Offer cancelled", Toast.LENGTH_SHORT).show();
                            refreshOffers();
                        } else {
                            Toast.makeText(OfferHistoryActivity.this, "Failed to cancel offer", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<String>> call, Throwable t) {
                        Toast.makeText(OfferHistoryActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(this::refreshOffers);
        swipeRefresh.setColorSchemeResources(R.color.primary_color);
    }

    private void refreshOffers() {
        currentPage = 0;
        hasMorePages = true;
        loadOffers();
    }

    // ✅ REAL API CALLS - NO MOCK DATA
    private void loadOffers() {
        if (isLoading) return;

        isLoading = true;
        if (currentPage == 0) {
            swipeRefresh.setRefreshing(true);
        }

        Call<StandardResponse<Map<String, Object>>> call;

        if ("SENT".equals(currentTab)) {
            // ✅ BUYER OFFERS (offers tôi đã gửi)
            call = ApiClient.getOfferService().getBuyerOffers(currentPage, PAGE_SIZE);
            Log.d(TAG, "🔄 Loading buyer offers - page: " + currentPage);
        } else {
            // ✅ SELLER OFFERS (offers tôi nhận được)
            call = ApiClient.getOfferService().getSellerOffers(currentPage, PAGE_SIZE);
            Log.d(TAG, "🔄 Loading seller offers - page: " + currentPage);
        }

        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                   Response<StandardResponse<Map<String, Object>>> response) {
                isLoading = false;
                swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Map<String, Object> data = response.body().getData();

                    // ✅ PARSE PAGED RESPONSE
                    List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");
                    boolean isLastPage = (Boolean) data.get("last");

                    List<Offer> newOffers = parseOffersFromResponse(content);

                    if (currentPage == 0) {
                        offers.clear();
                    }
                    offers.addAll(newOffers);

                    hasMorePages = !isLastPage;
                    updateUI();

                    Log.d(TAG, "✅ Loaded " + newOffers.size() + " offers, total: " + offers.size() + ", hasMore: " + hasMorePages);

                } else {
                    String error = response.body() != null ? response.body().getMessage() : "Failed to load offers";
                    Log.e(TAG, "❌ API error: " + error);
                    Toast.makeText(OfferHistoryActivity.this, error, Toast.LENGTH_SHORT).show();

                    // ✅ NẾU EMPTY, VẪN UPDATE UI
                    if (currentPage == 0) {
                        offers.clear();
                        updateUI();
                    }
                }
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                isLoading = false;
                swipeRefresh.setRefreshing(false);
                Log.e(TAG, "❌ Network error", t);
                Toast.makeText(OfferHistoryActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();

                // ✅ NẾU EMPTY, VẪN UPDATE UI
                if (currentPage == 0) {
                    offers.clear();
                    updateUI();
                }
            }
        });
    }

    // ✅ PARSE THẬT TỪ BACKEND RESPONSE
    private List<Offer> parseOffersFromResponse(List<Map<String, Object>> content) {
        List<Offer> result = new ArrayList<>();

        if (content != null) {
            for (Map<String, Object> item : content) {
                try {
                    Offer offer = Offer.fromBackendResponse(item);
                    result.add(offer);
                    Log.d(TAG, "✅ Parsed offer: " + offer.getId() + " - " + offer.getProductTitle());
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error parsing offer: " + item, e);
                }
            }
        }

        return result;
    }

    private void updateUI() {
        offerAdapter.notifyDataSetChanged();

        if (offers.isEmpty()) {
            rvOffers.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("SENT".equals(currentTab) ?
                    "No offers sent yet" : "No offers received yet");
        } else {
            rvOffers.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
        }
    }

    @Override
    public void onOfferClick(Offer offer) {
        // Navigate to product detail
        if (offer.getProductId() != null) {
            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra("product_id", offer.getProductId());
            intent.putExtra("product_title", offer.getProductTitle());
            intent.putExtra("product_price", offer.getOriginalPrice().toString());
            startActivity(intent);
        }
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