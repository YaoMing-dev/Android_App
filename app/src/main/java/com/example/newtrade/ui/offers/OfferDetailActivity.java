package com.example.newtrade.ui.offers;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Offer;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.chat.ChatActivity;
import com.example.newtrade.ui.product.ProductDetailActivity;
import com.example.newtrade.utils.NavigationUtils;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OfferDetailActivity extends AppCompatActivity {

    private static final String TAG = "OfferDetailActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private ImageView ivProductImage;
    private TextView tvProductTitle;
    private CircleImageView ivUserAvatar;
    private TextView tvUserName;
    private TextView tvOfferAmount, tvOriginalPrice, tvDiscountPercent;
    private TextView tvOfferMessage, tvOfferDate;
    private TextView tvCounterAmount, tvCounterMessage;
    private Chip chipStatus;
    private MaterialButton btnAccept, btnReject, btnCounter, btnContact, btnViewProduct;

    // Data
    private SharedPrefsManager prefsManager;
    private Long offerId;
    private Offer offer;
    private boolean isSellerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_detail);

        prefsManager = SharedPrefsManager.getInstance(this);

        getOfferIdFromIntent();
        initViews();
        setupToolbar();
        setupListeners();
        loadOfferDetail();

        Log.d(TAG, "OfferDetailActivity created for offer: " + offerId);
    }

    private void getOfferIdFromIntent() {
        offerId = getIntent().getLongExtra("offer_id", -1L);

        if (offerId == -1L) {
            Log.e(TAG, "❌ No offer ID provided");
            Toast.makeText(this, "Invalid offer", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivProductImage = findViewById(R.id.iv_product_image);
        tvProductTitle = findViewById(R.id.tv_product_title);
        ivUserAvatar = findViewById(R.id.iv_user_avatar);
        tvUserName = findViewById(R.id.tv_user_name);
        tvOfferAmount = findViewById(R.id.tv_offer_amount);
        tvOriginalPrice = findViewById(R.id.tv_original_price);
        tvDiscountPercent = findViewById(R.id.tv_discount_percent);
        tvOfferMessage = findViewById(R.id.tv_offer_message);
        tvOfferDate = findViewById(R.id.tv_offer_date);
        tvCounterAmount = findViewById(R.id.tv_counter_amount);
        tvCounterMessage = findViewById(R.id.tv_counter_message);
        chipStatus = findViewById(R.id.chip_status);
        btnAccept = findViewById(R.id.btn_accept);
        btnReject = findViewById(R.id.btn_reject);
        btnCounter = findViewById(R.id.btn_counter);
        btnContact = findViewById(R.id.btn_contact);
        btnViewProduct = findViewById(R.id.btn_view_product);
    }

    private void setupToolbar() {
        NavigationUtils.setupToolbarWithBackButton(this, toolbar, "Offer Details");
    }

    private void setupListeners() {
        btnAccept.setOnClickListener(v -> acceptOffer());
        btnReject.setOnClickListener(v -> rejectOffer());
        btnCounter.setOnClickListener(v -> showCounterOfferDialog());
        btnContact.setOnClickListener(v -> contactUser());
        btnViewProduct.setOnClickListener(v -> viewProduct());
    }

    private void loadOfferDetail() {
        ApiClient.getApiService().getOfferById(offerId)
            .enqueue(new Callback<StandardResponse<Offer>>() {
                @Override
                public void onResponse(@NonNull Call<StandardResponse<Offer>> call,
                                       @NonNull Response<StandardResponse<Offer>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<Offer> apiResponse = response.body();

                        if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                            offer = apiResponse.getData();
                            updateUI();
                            Log.d(TAG, "✅ Offer detail loaded");
                        } else {
                            showError("Failed to load offer details");
                        }
                    } else {
                        showError("Offer not found");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<StandardResponse<Offer>> call, @NonNull Throwable t) {
                    Log.e(TAG, "❌ Failed to load offer detail", t);
                    showError("Network error: " + t.getMessage());
                }
            });
    }

    private void updateUI() {
        if (offer == null) return;

        // Determine if current user is seller or buyer
        Long currentUserId = prefsManager.getUserId();
        isSellerView = currentUserId.equals(offer.getSellerId());

        // Product info
        tvProductTitle.setText(offer.getProductTitle());
        if (offer.getProductImage() != null && !offer.getProductImage().isEmpty()) {
            Glide.with(this)
                .load(offer.getProductImage())
                .placeholder(R.drawable.placeholder_image)
                .into(ivProductImage);
        }

        // User info (buyer or seller based on view)
        String userName, userAvatar;
        if (isSellerView) {
            userName = offer.getBuyerName();
            userAvatar = offer.getBuyerAvatar();
        } else {
            userName = offer.getSellerName();
            userAvatar = offer.getSellerAvatar();
        }

        tvUserName.setText(userName != null ? userName : "Unknown User");
        if (userAvatar != null && !userAvatar.isEmpty()) {
            Glide.with(this)
                .load(userAvatar)
                .placeholder(R.drawable.ic_person_placeholder)
                .into(ivUserAvatar);
        }

        // Offer details
        tvOfferAmount.setText("$" + String.format("%.2f", offer.getOfferAmount()));

        if (offer.getOriginalPrice() != null) {
            tvOriginalPrice.setText("$" + String.format("%.2f", offer.getOriginalPrice()));
            tvOriginalPrice.setVisibility(View.VISIBLE);

            double discount = offer.getDiscountPercentage();
            if (discount > 0) {
                tvDiscountPercent.setText("-" + String.format("%.0f", discount) + "%");
                tvDiscountPercent.setVisibility(View.VISIBLE);
            } else {
                tvDiscountPercent.setVisibility(View.GONE);
            }
        } else {
            tvOriginalPrice.setVisibility(View.GONE);
            tvDiscountPercent.setVisibility(View.GONE);
        }

        // Message
        if (offer.getMessage() != null && !offer.getMessage().trim().isEmpty()) {
            tvOfferMessage.setText(offer.getMessage());
            tvOfferMessage.setVisibility(View.VISIBLE);
        } else {
            tvOfferMessage.setVisibility(View.GONE);
        }

        // Date
        tvOfferDate.setText(formatDate(offer.getCreatedAt()));

        // Counter offer details
        if (offer.isCountered() && offer.getCounterAmount() != null) {
            tvCounterAmount.setText("Counter: $" + String.format("%.2f", offer.getCounterAmount()));
            tvCounterAmount.setVisibility(View.VISIBLE);

            if (offer.getCounterMessage() != null && !offer.getCounterMessage().trim().isEmpty()) {
                tvCounterMessage.setText(offer.getCounterMessage());
                tvCounterMessage.setVisibility(View.VISIBLE);
            }
        } else {
            tvCounterAmount.setVisibility(View.GONE);
            tvCounterMessage.setVisibility(View.GONE);
        }

        // Status chip
        chipStatus.setText(offer.getStatusDisplayText());
        setStatusChipColor(offer.getStatus());

        // Action buttons
        setupActionButtons();
    }

    private void setStatusChipColor(String status) {
        int colorRes;
        switch (status) {
            case "pending":
                colorRes = R.color.status_pending;
                break;
            case "accepted":
                colorRes = R.color.status_accepted;
                break;
            case "rejected":
                colorRes = R.color.status_rejected;
                break;
            case "countered":
                colorRes = R.color.status_countered;
                break;
            case "expired":
                colorRes = R.color.status_expired;
                break;
            default:
                colorRes = R.color.text_secondary;
        }
        chipStatus.setChipBackgroundColorResource(colorRes);
    }

    private void setupActionButtons() {
        // Hide all buttons first
        btnAccept.setVisibility(View.GONE);
        btnReject.setVisibility(View.GONE);
        btnCounter.setVisibility(View.GONE);

        if (isSellerView && offer.isPending()) {
            // Seller can accept, reject, or counter pending offers
            btnAccept.setVisibility(View.VISIBLE);
            btnReject.setVisibility(View.VISIBLE);
            btnCounter.setVisibility(View.VISIBLE);
        } else if (!isSellerView && offer.isCountered()) {
            // Buyer can accept or reject counter offers
            btnAccept.setVisibility(View.VISIBLE);
            btnReject.setVisibility(View.VISIBLE);
        }
    }

    private void acceptOffer() {
        new AlertDialog.Builder(this)
            .setTitle("Accept Offer")
            .setMessage("Are you sure you want to accept this offer?")
            .setPositiveButton("Accept", (dialog, which) -> updateOfferStatus("accept"))
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void rejectOffer() {
        new AlertDialog.Builder(this)
            .setTitle("Reject Offer")
            .setMessage("Are you sure you want to reject this offer?")
            .setPositiveButton("Reject", (dialog, which) -> updateOfferStatus("reject"))
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showCounterOfferDialog() {
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
                    sendCounterOffer(counterAmount, message);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid amount format", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void sendCounterOffer(double counterAmount, String message) {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("action", "counter");
        requestData.put("counter_amount", counterAmount);
        requestData.put("counter_message", message);

        updateOfferStatusWithData(requestData);
    }

    private void updateOfferStatus(String action) {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("action", action);
        updateOfferStatusWithData(requestData);
    }

    private void updateOfferStatusWithData(Map<String, Object> requestData) {
        ApiClient.getApiService().updateOfferStatus(offerId, requestData)
            .enqueue(new Callback<StandardResponse<Offer>>() {
                @Override
                public void onResponse(@NonNull Call<StandardResponse<Offer>> call,
                                       @NonNull Response<StandardResponse<Offer>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<Offer> apiResponse = response.body();

                        if (apiResponse.isSuccess()) {
                            offer = apiResponse.getData();
                            updateUI();

                            String action = (String) requestData.get("action");
                            String message = "counter".equals(action) ? "Counter offer sent!" :
                                           "accept".equals(action) ? "Offer accepted!" : "Offer rejected";
                            Toast.makeText(OfferDetailActivity.this, message, Toast.LENGTH_SHORT).show();

                            Log.d(TAG, "✅ Offer status updated: " + action);
                        } else {
                            showError("Failed to update offer");
                        }
                    } else {
                        showError("Failed to update offer");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<StandardResponse<Offer>> call, @NonNull Throwable t) {
                    Log.e(TAG, "❌ Update offer status API error", t);
                    showError("Network error: " + t.getMessage());
                }
            });
    }

    private void contactUser() {
        if (offer == null) return;

        Long otherUserId = isSellerView ? offer.getBuyerId() : offer.getSellerId();
        String otherUserName = isSellerView ? offer.getBuyerName() : offer.getSellerName();

        Intent chatIntent = new Intent(this, ChatActivity.class);
        chatIntent.putExtra("other_user_id", otherUserId);
        chatIntent.putExtra("other_user_name", otherUserName);
        chatIntent.putExtra("product_id", offer.getProductId());
        chatIntent.putExtra("product_title", offer.getProductTitle());
        startActivity(chatIntent);
    }

    private void viewProduct() {
        if (offer == null) return;

        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product_id", offer.getProductId());
        startActivity(intent);
    }

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (Exception e) {
            return "Recently";
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
