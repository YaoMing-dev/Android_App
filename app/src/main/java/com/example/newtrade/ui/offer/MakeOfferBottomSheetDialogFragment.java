// app/src/main/java/com/example/newtrade/ui/offer/MakeOfferBottomSheetDialogFragment.java
package com.example.newtrade.ui.offer;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.OfferRequest;
import com.example.newtrade.models.StandardResponse;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MakeOfferBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private static final String TAG = "MakeOfferDialog";
    private static final String ARG_PRODUCT_ID = "productId";
    private static final String ARG_CURRENT_PRICE = "currentPrice";

    private Long productId;
    private String currentPrice;
    private TextInputEditText etOfferAmount, etOfferMessage;
    private Button btnSubmitOffer;

    // Callback interface
    public interface OnOfferSubmittedListener {
        void onOfferSubmitted(boolean success, String message);
    }

    private OnOfferSubmittedListener listener;

    public static MakeOfferBottomSheetDialogFragment newInstance(Long productId, String currentPrice) {
        MakeOfferBottomSheetDialogFragment fragment = new MakeOfferBottomSheetDialogFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_PRODUCT_ID, productId);
        args.putString(ARG_CURRENT_PRICE, currentPrice);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productId = getArguments().getLong(ARG_PRODUCT_ID);
            currentPrice = getArguments().getString(ARG_CURRENT_PRICE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_make_offer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupListeners();
    }

    private void initViews(View view) {
        etOfferAmount = view.findViewById(R.id.et_offer_amount);
        etOfferMessage = view.findViewById(R.id.et_offer_message);
        btnSubmitOffer = view.findViewById(R.id.btn_submit_offer);
    }

    private void setupListeners() {
        btnSubmitOffer.setOnClickListener(v -> submitOffer());
    }

    private void submitOffer() {
        String amount = etOfferAmount.getText().toString().trim();
        String message = etOfferMessage.getText().toString().trim();

        if (amount.isEmpty()) {
            Toast.makeText(getContext(), "Please enter offer amount", Toast.LENGTH_SHORT).show();
            etOfferAmount.requestFocus();
            return;
        }

        try {
            double offerAmount = Double.parseDouble(amount);

            if (offerAmount <= 0) {
                Toast.makeText(getContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                etOfferAmount.requestFocus();
                return;
            }

            // ✅ THÊM: Validation với current price
            if (currentPrice != null && !currentPrice.isEmpty()) {
                try {
                    double originalPrice = Double.parseDouble(currentPrice);
                    double minOffer = originalPrice * 0.1; // 10% minimum như backend

                    if (offerAmount < minOffer) {
                        Toast.makeText(getContext(),
                                "Offer too low. Minimum: " + formatPrice(minOffer) + " VNĐ",
                                Toast.LENGTH_LONG).show();
                        etOfferAmount.requestFocus();
                        return;
                    }

                    if (offerAmount > originalPrice * 1.5) {
                        Toast.makeText(getContext(),
                                "Offer too high. Consider negotiating closer to original price.",
                                Toast.LENGTH_LONG).show();
                        etOfferAmount.requestFocus();
                        return;
                    }

                } catch (Exception e) {
                    Log.w(TAG, "Could not parse current price for validation: " + currentPrice);
                }
            }

            Log.d(TAG, "✅ Submitting offer: " + offerAmount);

            setLoading(true);
            submitOfferToAPI(offerAmount, message);

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Please enter numbers only", Toast.LENGTH_SHORT).show();
            etOfferAmount.requestFocus();
        }
    }

    // ✅ HOÀN CHỈNH API CALL
    private void submitOfferToAPI(double amount, String message) {
        // ✅ CREATE PROPER REQUEST OBJECT matching backend OfferRequest
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("productId", productId);
        requestData.put("offerAmount", amount);  // Backend DTO expects this

        if (!message.isEmpty()) {
            requestData.put("message", message);
        }

        Log.d(TAG, "🔄 API request: " + requestData);

        ApiClient.getOfferService().createOffer(requestData)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        setLoading(false);

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            // ✅ SUCCESS
                            Log.d(TAG, "✅ Offer submitted successfully");
                            Toast.makeText(getContext(), "Offer submitted successfully!", Toast.LENGTH_SHORT).show();

                            if (listener != null) {
                                listener.onOfferSubmitted(true, "Offer submitted successfully");
                            }
                            dismiss();

                        } else {
                            // ✅ HANDLE ERROR
                            handleApiError(response);
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        setLoading(false);
                        Log.e(TAG, "❌ Network error", t);

                        String errorMsg = "Network error. Please check your connection and try again.";
                        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();

                        if (listener != null) {
                            listener.onOfferSubmitted(false, errorMsg);
                        }
                    }
                });
    }

    private void submitOfferToAPITypeSafe(double amount, String message) {
        try {
            BigDecimal offerAmount = BigDecimal.valueOf(amount);
            OfferRequest request = new OfferRequest(productId, offerAmount, message);

            Log.d(TAG, "🔄 Type-safe API request: productId=" + productId + ", amount=" + offerAmount);

            ApiClient.getOfferService().createOfferTypeSafe(request)
                    .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                        @Override
                        public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                               Response<StandardResponse<Map<String, Object>>> response) {
                            setLoading(false);

                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                Log.d(TAG, "✅ Type-safe offer submitted successfully");
                                Toast.makeText(getContext(), "Offer submitted successfully!", Toast.LENGTH_SHORT).show();

                                if (listener != null) {
                                    listener.onOfferSubmitted(true, "Offer submitted successfully");
                                }
                                dismiss();
                            } else {
                                handleApiError(response);
                            }
                        }

                        @Override
                        public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                            setLoading(false);
                            Log.e(TAG, "❌ Network error", t);
                            Toast.makeText(getContext(), "Network error. Please try again.", Toast.LENGTH_SHORT).show();

                            if (listener != null) {
                                listener.onOfferSubmitted(false, "Network error");
                            }
                        }
                    });

        } catch (Exception e) {
            setLoading(false);
            Log.e(TAG, "❌ Error creating offer request", e);
            Toast.makeText(getContext(), "Error creating offer", Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ IMPROVED ERROR HANDLING
    private void handleApiError(Response<StandardResponse<Map<String, Object>>> response) {
        String errorMsg = "Failed to submit offer";

        try {
            if (response.body() != null) {
                String backendMessage = response.body().getMessage();

                // ✅ PARSE backend errors và translate cho user
                if (backendMessage.contains("Offer amount too low")) {
                    errorMsg = "Your offer is too low. Please offer at least 10% of the original price.";
                } else if (backendMessage.contains("Product not found")) {
                    errorMsg = "Product is no longer available.";
                } else if (backendMessage.contains("Product is not available")) {
                    errorMsg = "This product is no longer available for offers.";
                } else if (backendMessage.contains("Cannot make offer on your own product")) {
                    errorMsg = "You cannot make offers on your own products.";
                } else if (backendMessage.contains("offered_price") || backendMessage.contains("offer_amount")) {
                    errorMsg = "Server configuration issue. Please try again later.";
                } else {
                    errorMsg = backendMessage; // Show backend message if it's user-friendly
                }

            } else if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                Log.e(TAG, "❌ Error body: " + errorBody);

                // Try to extract message from error body
                if (errorBody.contains("Offer amount too low")) {
                    errorMsg = "Your offer is too low. Please offer at least 10% of the original price.";
                } else if (errorBody.contains("message")) {
                    errorMsg = "Server error occurred. Please try again.";
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing error response", e);
            errorMsg = "Unexpected error occurred";
        }

        Log.e(TAG, "❌ API error: " + response.code() + " - " + errorMsg);
        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();

        if (listener != null) {
            listener.onOfferSubmitted(false, errorMsg);
        }
    }
    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
        return formatter.format(price);
    }

    private void setLoading(boolean loading) {
        btnSubmitOffer.setEnabled(!loading);
        btnSubmitOffer.setText(loading ? "Submitting..." : "Submit Offer");
        etOfferAmount.setEnabled(!loading);
        etOfferMessage.setEnabled(!loading);
    }

    public void setOnOfferSubmittedListener(OnOfferSubmittedListener listener) {
        this.listener = listener;
    }
}