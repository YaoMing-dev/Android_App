package com.example.newtrade.ui.offers;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Offer;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.adapters.OfferAdapter;
import com.example.newtrade.utils.NavigationUtils;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * FR-5.1.2: Activity for sellers to manage offers (accept/reject/counter)
 * FR-9.1.2: Also shows offer history for sellers
 */
public class OfferManagementActivity extends AppCompatActivity implements OfferAdapter.OfferActionListener {

    private static final String TAG = "OfferManagementActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView rvOffers;
    private TextView tvEmptyState;

    // Data & Adapters
    private OfferAdapter offerAdapter;
    private final List<Offer> offers = new ArrayList<>();
    private String currentTab = "received"; // received, sent, history

    // Utils
    private SharedPrefsManager prefsManager;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_management);

        initViews();
        initUtils();
        setupToolbar();
        setupTabs();
        setupRecyclerView();
        setupSwipeRefresh();

        // Load initial data
        loadOffers();

        Log.d(TAG, "✅ OfferManagementActivity created");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tab_layout);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        rvOffers = findViewById(R.id.rv_offers);
        tvEmptyState = findViewById(R.id.tv_empty_state);
    }

    private void initUtils() {
        prefsManager = SharedPrefsManager.getInstance(this);
    }

    private void setupToolbar() {
        NavigationUtils.setupToolbarWithBackButton(this, toolbar, "Manage Offers");
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Received").setTag("received"));
        tabLayout.addTab(tabLayout.newTab().setText("Sent").setTag("sent"));
        tabLayout.addTab(tabLayout.newTab().setText("History").setTag("history"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = (String) tab.getTag();
                loadOffers();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        offerAdapter = new OfferAdapter(offers, this, currentTab);
        rvOffers.setLayoutManager(new LinearLayoutManager(this));
        rvOffers.setAdapter(offerAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadOffers);
    }

    private void loadOffers() {
        if (isLoading) return;

        Log.d(TAG, "Loading offers for tab: " + currentTab);

        isLoading = true;
        if (!swipeRefreshLayout.isRefreshing()) {
            // Show loading state
        }

        Call<StandardResponse<List<Map<String, Object>>>> call;

        switch (currentTab) {
            case "received":
                call = ApiClient.getApiService().getReceivedOffers();
                break;
            case "sent":
                call = ApiClient.getApiService().getSentOffers();
                break;
            case "history":
                call = ApiClient.getApiService().getOfferHistory();
                break;
            default:
                call = ApiClient.getApiService().getReceivedOffers();
        }

        call.enqueue(new Callback<StandardResponse<List<Map<String, Object>>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<List<Map<String, Object>>>> call,
                                   @NonNull Response<StandardResponse<List<Map<String, Object>>>> response) {
                isLoading = false;
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<List<Map<String, Object>>> apiResponse = response.body();

                    if (apiResponse.isSuccess()) {
                        List<Map<String, Object>> offerMaps = apiResponse.getData();

                        offers.clear();
                        if (offerMaps != null) {
                            for (Map<String, Object> offerMap : offerMaps) {
                                try {
                                    Offer offer = Offer.fromMap(offerMap);
                                    offers.add(offer);
                                } catch (Exception e) {
                                    Log.w(TAG, "Error parsing offer", e);
                                }
                            }
                        }

                        offerAdapter.updateTab(currentTab);
                        offerAdapter.notifyDataSetChanged();
                        updateEmptyState();

                        Log.d(TAG, "✅ Offers loaded: " + offers.size());
                    } else {
                        showError(apiResponse.getMessage() != null ?
                            apiResponse.getMessage() : "Failed to load offers");
                    }
                } else {
                    showError("Failed to load offers");
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<List<Map<String, Object>>>> call, @NonNull Throwable t) {
                isLoading = false;
                swipeRefreshLayout.setRefreshing(false);
                showError("Network error: " + t.getMessage());
                Log.e(TAG, "Failed to load offers", t);
            }
        });
    }

    // FR-5.1.2: OfferActionListener implementation
    @Override
    public void onAcceptOffer(Offer offer) {
        Log.d(TAG, "Accepting offer: " + offer.getId());
        updateOfferStatus(offer.getId(), "ACCEPTED");
    }

    @Override
    public void onRejectOffer(Offer offer) {
        Log.d(TAG, "Rejecting offer: " + offer.getId());
        updateOfferStatus(offer.getId(), "REJECTED");
    }

    @Override
    public void onCounterOffer(Offer offer, double newAmount, String message) {
        Log.d(TAG, "Counter offer: " + offer.getId() + " with amount: " + newAmount);

        Map<String, Object> counterRequest = new HashMap<>();
        counterRequest.put("offerId", offer.getId());
        counterRequest.put("counterAmount", newAmount);
        if (message != null && !message.trim().isEmpty()) {
            counterRequest.put("message", message);
        }

        ApiClient.getApiService().counterOffer(counterRequest)
            .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                @Override
                public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                       @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<Map<String, Object>> apiResponse = response.body();
                        if (apiResponse.isSuccess()) {
                            Toast.makeText(OfferManagementActivity.this,
                                "Counter offer sent!", Toast.LENGTH_SHORT).show();
                            loadOffers(); // Refresh list
                        } else {
                            showError(apiResponse.getMessage());
                        }
                    } else {
                        showError("Failed to send counter offer");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                    showError("Network error: " + t.getMessage());
                }
            });
    }

    @Override
    public void onViewOfferDetails(Offer offer) {
        // Open offer details dialog or activity
        OfferDetailsDialog dialog = OfferDetailsDialog.newInstance(offer);
        dialog.show(getSupportFragmentManager(), "offer_details");
    }

    private void updateOfferStatus(Long offerId, String status) {
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("offerId", offerId);
        updateRequest.put("status", status);

        ApiClient.getApiService().updateOfferStatus(updateRequest)
            .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                @Override
                public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                       @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<Map<String, Object>> apiResponse = response.body();
                        if (apiResponse.isSuccess()) {
                            String message = status.equals("ACCEPTED") ?
                                "Offer accepted!" : "Offer rejected!";
                            Toast.makeText(OfferManagementActivity.this, message, Toast.LENGTH_SHORT).show();
                            loadOffers(); // Refresh list
                        } else {
                            showError(apiResponse.getMessage());
                        }
                    } else {
                        showError("Failed to update offer");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                    showError("Network error: " + t.getMessage());
                }
            });
    }

    private void updateEmptyState() {
        if (offers.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvOffers.setVisibility(View.GONE);

            String emptyMessage;
            switch (currentTab) {
                case "received":
                    emptyMessage = "No offers received yet";
                    break;
                case "sent":
                    emptyMessage = "No offers sent yet";
                    break;
                case "history":
                    emptyMessage = "No offer history";
                    break;
                default:
                    emptyMessage = "No offers found";
            }
            tvEmptyState.setText(emptyMessage);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvOffers.setVisibility(View.VISIBLE);
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Error: " + message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "OfferManagementActivity destroyed");
    }
}
