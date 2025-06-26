// app/src/main/java/com/example/newtrade/ui/profile/OfferHistoryActivity.java
package com.example.newtrade.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

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
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OfferHistoryActivity extends AppCompatActivity {

    private static final String TAG = "OfferHistoryActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private RecyclerView rvOffers;
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvEmptyState;

    // Data
    private OfferAdapter offerAdapter;
    private List<Offer> offers = new ArrayList<>();
    private SharedPrefsManager prefsManager;
    private String currentTab = "SENT"; // SENT or RECEIVED

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_history);

        initViews();
        setupToolbar();
        setupTabs();
        setupRecyclerView();
        setupListeners();
        loadOffers();

        Log.d(TAG, "OfferHistoryActivity created");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tab_layout);
        rvOffers = findViewById(R.id.rv_offers);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        tvEmptyState = findViewById(R.id.tv_empty_state);

        prefsManager = SharedPrefsManager.getInstance(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Offer History");
        }
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Sent Offers"));
        tabLayout.addTab(tabLayout.newTab().setText("Received Offers"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition() == 0 ? "SENT" : "RECEIVED";
                loadOffers();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        offerAdapter = new OfferAdapter(offers, this::openProductDetail);
        rvOffers.setLayoutManager(new LinearLayoutManager(this));
        rvOffers.setAdapter(offerAdapter);
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(this::loadOffers);
    }

    private void loadOffers() {
        swipeRefresh.setRefreshing(true);

        // Create mock data based on tab
        if ("SENT".equals(currentTab)) {
            createMockSentOffers();
        } else {
            createMockReceivedOffers();
        }

        // TODO: Load from API when OfferService is available
        // loadOffersFromAPI();
    }

    private void createMockSentOffers() {
        offers.clear();

        // Mock sent offer 1
        Offer offer1 = new Offer();
        offer1.setId(1L);
        offer1.setProductTitle("iPhone 13 Pro Max");
        offer1.setOfferAmount(new BigDecimal("17000000"));
        offer1.setOriginalPrice(new BigDecimal("18500000"));
        offer1.setStatus(Offer.OfferStatus.PENDING);
        offer1.setMessage("Can you consider 17M?");
        offer1.setCreatedAt("2 hours ago");
        offers.add(offer1);

        // Mock sent offer 2
        Offer offer2 = new Offer();
        offer2.setId(2L);
        offer2.setProductTitle("MacBook Air M1");
        offer2.setOfferAmount(new BigDecimal("20000000"));
        offer2.setOriginalPrice(new BigDecimal("22000000"));
        offer2.setStatus(Offer.OfferStatus.REJECTED);
        offer2.setMessage("Best price?");
        offer2.setCreatedAt("1 day ago");
        offers.add(offer2);

        updateUI();
    }

    private void createMockReceivedOffers() {
        offers.clear();

        // Mock received offer 1
        Offer offer1 = new Offer();
        offer1.setId(3L);
        offer1.setProductTitle("Samsung Galaxy Tab S8");
        offer1.setOfferAmount(new BigDecimal("12000000"));
        offer1.setOriginalPrice(new BigDecimal("13500000"));
        offer1.setStatus(Offer.OfferStatus.PENDING);
        offer1.setBuyerName("Nguyễn Văn A");
        offer1.setMessage("Is 12M acceptable?");
        offer1.setCreatedAt("3 hours ago");
        offers.add(offer1);

        // Mock received offer 2
        Offer offer2 = new Offer();
        offer2.setId(4L);
        offer2.setProductTitle("Nintendo Switch");
        offer2.setOfferAmount(new BigDecimal("5500000"));
        offer2.setOriginalPrice(new BigDecimal("6000000"));
        offer2.setStatus(Offer.OfferStatus.ACCEPTED);
        offer2.setBuyerName("Trần Thị B");
        offer2.setMessage("Good price for quick sale");
        offer2.setCreatedAt("2 days ago");
        offers.add(offer2);

        updateUI();
    }

    private void updateUI() {
        swipeRefresh.setRefreshing(false);
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

    private void openProductDetail(Offer offer) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product_id", offer.getProductId());
        intent.putExtra("product_title", offer.getProductTitle());
        intent.putExtra("product_price", offer.getOriginalPrice().toString());
        startActivity(intent);
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