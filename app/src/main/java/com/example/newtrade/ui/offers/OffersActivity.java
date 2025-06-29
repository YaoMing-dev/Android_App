package com.example.newtrade.ui.offers;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtrade.R;
import com.example.newtrade.adapters.OfferAdapter;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Offer;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.chat.ChatActivity;
import com.example.newtrade.utils.NavigationUtils;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OffersActivity extends AppCompatActivity implements OfferAdapter.OnOfferActionListener {

    private static final String TAG = "OffersActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private RecyclerView rvOffers;
    private LinearLayout llEmptyState;

    // Data
    private OfferAdapter offerAdapter;
    private List<Offer> receivedOffers = new ArrayList<>(); // Offers I received (as seller)
    private List<Offer> sentOffers = new ArrayList<>();     // Offers I sent (as buyer)
    private List<Offer> currentOffers = new ArrayList<>();
    private SharedPrefsManager prefsManager;
    private boolean isShowingReceived = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offers);

        prefsManager = SharedPrefsManager.getInstance(this);

        initViews();
        setupToolbar();
        setupTabs();
        setupRecyclerView();
        loadOffers();

        Log.d(TAG, "OffersActivity created");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tab_layout);
        rvOffers = findViewById(R.id.rv_offers);
        llEmptyState = findViewById(R.id.ll_empty_state);
    }

    private void setupToolbar() {
        NavigationUtils.setupToolbarWithBackButton(this, toolbar, "My Offers");
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Received"));
        tabLayout.addTab(tabLayout.newTab().setText("Sent"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                isShowingReceived = tab.getPosition() == 0;
                updateOffersList();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvOffers.setLayoutManager(layoutManager);

        offerAdapter = new OfferAdapter(currentOffers, this, isShowingReceived);
        rvOffers.setAdapter(offerAdapter);
    }

    private void loadOffers() {
        loadReceivedOffers();
        loadSentOffers();
    }

    private void loadReceivedOffers() {
        Log.d(TAG, "Loading received offers");

        ApiClient.getApiService().getReceivedOffers()
            .enqueue(new Callback<StandardResponse<List<Offer>>>() {
                @Override
                public void onResponse(@NonNull Call<StandardResponse<List<Offer>>> call,
                                       @NonNull Response<StandardResponse<List<Offer>>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<List<Offer>> apiResponse = response.body();

                        if (apiResponse.isSuccess()) {
                            List<Offer> offers = apiResponse.getData();

                            receivedOffers.clear();
                            if (offers != null) {
                                receivedOffers.addAll(offers);
                            }

                            if (isShowingReceived) {
                                updateOffersList();
                            }

                            Log.d(TAG, "✅ Received offers loaded: " + receivedOffers.size());
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<StandardResponse<List<Offer>>> call, @NonNull Throwable t) {
                    Log.e(TAG, "Failed to load received offers", t);
                    showError("Failed to load received offers");
                }
            });
    }

    private void loadSentOffers() {
        Log.d(TAG, "Loading sent offers");

        ApiClient.getApiService().getSentOffers()
            .enqueue(new Callback<StandardResponse<List<Offer>>>() {
                @Override
                public void onResponse(@NonNull Call<StandardResponse<List<Offer>>> call,
                                       @NonNull Response<StandardResponse<List<Offer>>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<List<Offer>> apiResponse = response.body();

                        if (apiResponse.isSuccess()) {
                            List<Offer> offers = apiResponse.getData();

                            sentOffers.clear();
                            if (offers != null) {
                                sentOffers.addAll(offers);
                            }

                            if (!isShowingReceived) {
                                updateOffersList();
                            }

                            Log.d(TAG, "✅ Sent offers loaded: " + sentOffers.size());
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<StandardResponse<List<Offer>>> call, @NonNull Throwable t) {
                    Log.e(TAG, "Failed to load sent offers", t);
                    showError("Failed to load sent offers");
                }
            });
    }

    private void updateOffersList() {
        currentOffers.clear();

        if (isShowingReceived) {
            currentOffers.addAll(receivedOffers);
        } else {
            currentOffers.addAll(sentOffers);
        }

        // Update adapter
        offerAdapter = new OfferAdapter(currentOffers, this, isShowingReceived);
        rvOffers.setAdapter(offerAdapter);

        // Show/hide empty state
        if (currentOffers.isEmpty()) {
            llEmptyState.setVisibility(View.VISIBLE);
            rvOffers.setVisibility(View.GONE);
        } else {
            llEmptyState.setVisibility(View.GONE);
            rvOffers.setVisibility(View.VISIBLE);
        }
    }

    // OfferAdapter.OnOfferActionListener implementation
    @Override
    public void onAcceptOffer(Offer offer) {
        new AlertDialog.Builder(this)
            .setTitle("Accept Offer")
            .setMessage("Are you sure you want to accept this offer of $" +
                String.format("%.2f", offer.getOfferAmount()) + "?")
            .setPositiveButton("Accept", (dialog, which) -> acceptOffer(offer))
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onRejectOffer(Offer offer) {
        new AlertDialog.Builder(this)
            .setTitle("Reject Offer")
            .setMessage("Are you sure you want to reject this offer?")
            .setPositiveButton("Reject", (dialog, which) -> rejectOffer(offer))
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onCounterOffer(Offer offer) {
        showCounterOfferDialog(offer);
    }

    @Override
    public void onViewOffer(Offer offer) {
        // Open offer detail activity or expand inline
        Intent intent = new Intent(this, OfferDetailActivity.class);
        intent.putExtra("offer_id", offer.getId());
        startActivity(intent);
    }

    @Override
    public void onContactUser(Offer offer) {
        // Open chat with the other user
        Long otherUserId = isShowingReceived ? offer.getBuyerId() : offer.getSellerId();
        String otherUserName = isShowingReceived ? offer.getBuyerName() : offer.getSellerName();

        Intent chatIntent = new Intent(this, ChatActivity.class);
        chatIntent.putExtra("other_user_id", otherUserId);
        chatIntent.putExtra("other_user_name", otherUserName);
        chatIntent.putExtra("product_id", offer.getProductId());
        chatIntent.putExtra("product_title", offer.getProductTitle());
        startActivity(chatIntent);
    }

    private void acceptOffer(Offer offer) {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("action", "accept");

        ApiClient.getApiService().updateOfferStatus(offer.getId(), requestData)
            .enqueue(new Callback<StandardResponse<Offer>>() {
                @Override
                public void onResponse(@NonNull Call<StandardResponse<Offer>> call,
                                       @NonNull Response<StandardResponse<Offer>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<Offer> apiResponse = response.body();

                        if (apiResponse.isSuccess()) {
                            Toast.makeText(OffersActivity.this, "Offer accepted!", Toast.LENGTH_SHORT).show();

                            // Update offer status locally
                            offer.setStatus("accepted");
                            offerAdapter.notifyDataSetChanged();

                            Log.d(TAG, "✅ Offer accepted: " + offer.getId());
                        } else {
                            showError("Failed to accept offer");
                        }
                    } else {
                        showError("Failed to accept offer");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<StandardResponse<Offer>> call, @NonNull Throwable t) {
                    Log.e(TAG, "❌ Accept offer API error", t);
                    showError("Network error: " + t.getMessage());
                }
            });
    }

    private void rejectOffer(Offer offer) {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("action", "reject");

        ApiClient.getApiService().updateOfferStatus(offer.getId(), requestData)
            .enqueue(new Callback<StandardResponse<Offer>>() {
                @Override
                public void onResponse(@NonNull Call<StandardResponse<Offer>> call,
                                       @NonNull Response<StandardResponse<Offer>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<Offer> apiResponse = response.body();

                        if (apiResponse.isSuccess()) {
                            Toast.makeText(OffersActivity.this, "Offer rejected", Toast.LENGTH_SHORT).show();

                            // Update offer status locally
                            offer.setStatus("rejected");
                            offerAdapter.notifyDataSetChanged();

                            Log.d(TAG, "✅ Offer rejected: " + offer.getId());
                        } else {
                            showError("Failed to reject offer");
                        }
                    } else {
                        showError("Failed to reject offer");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<StandardResponse<Offer>> call, @NonNull Throwable t) {
                    Log.e(TAG, "❌ Reject offer API error", t);
                    showError("Network error: " + t.getMessage());
                }
            });
    }

    private void showCounterOfferDialog(Offer offer) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_counter_offer, null);

        TextInputEditText etCounterAmount = dialogView.findViewById(R.id.et_counter_amount);
        TextInputEditText etCounterMessage = dialogView.findViewById(R.id.et_counter_message);

        // Pre-fill with current offer amount
        etCounterAmount.setText(String.valueOf(offer.getOfferAmount()));

        new AlertDialog.Builder(this)
            .setTitle("Counter Offer")
            .setView(dialogView)
            .setPositiveButton("Send Counter", (dialog, which) -> {
                String amountStr = etCounterAmount.getText().toString().trim();
                String message = etCounterMessage.getText().toString().trim();

                if (amountStr.isEmpty()) {
                    Toast.makeText(this, "Please enter counter amount", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    double counterAmount = Double.parseDouble(amountStr);
                    sendCounterOffer(offer, counterAmount, message);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid amount format", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void sendCounterOffer(Offer offer, double counterAmount, String message) {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("action", "counter");
        requestData.put("counter_amount", counterAmount);
        requestData.put("counter_message", message);

        ApiClient.getApiService().updateOfferStatus(offer.getId(), requestData)
            .enqueue(new Callback<StandardResponse<Offer>>() {
                @Override
                public void onResponse(@NonNull Call<StandardResponse<Offer>> call,
                                       @NonNull Response<StandardResponse<Offer>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<Offer> apiResponse = response.body();

                        if (apiResponse.isSuccess()) {
                            Toast.makeText(OffersActivity.this, "Counter offer sent!", Toast.LENGTH_SHORT).show();

                            // Update offer status locally
                            offer.setStatus("countered");
                            offer.setCounterAmount(counterAmount);
                            offer.setCounterMessage(message);
                            offerAdapter.notifyDataSetChanged();

                            Log.d(TAG, "✅ Counter offer sent: " + offer.getId());
                        } else {
                            showError("Failed to send counter offer");
                        }
                    } else {
                        showError("Failed to send counter offer");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<StandardResponse<Offer>> call, @NonNull Throwable t) {
                    Log.e(TAG, "❌ Counter offer API error", t);
                    showError("Network error: " + t.getMessage());
                }
            });
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
