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
import com.example.newtrade.models.StandardResponse;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
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

        // Validation
        if (amount.isEmpty()) {
            etOfferAmount.setError("Please enter offer amount");
            return;
        }

        try {
            double offerAmount = Double.parseDouble(amount);
            if (offerAmount <= 0) {
                etOfferAmount.setError("Please enter valid amount");
                return;
            }

            // Show loading state
            setLoading(true);

            // Submit to API
            submitOfferToAPI(offerAmount, message);

        } catch (NumberFormatException e) {
            etOfferAmount.setError("Please enter valid number");
        }
    }

    private void submitOfferToAPI(double amount, String message) {
        Map<String, Object> offerData = new HashMap<>();
        offerData.put("productId", productId);
        offerData.put("offerAmount", amount);
        if (!message.isEmpty()) {
            offerData.put("message", message);
        }

        Log.d(TAG, "Submitting offer: " + offerData);

        ApiClient.getOfferService().createOffer(offerData)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        setLoading(false);

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            handleSuccess("Offer submitted successfully!");
                        } else {
                            String errorMsg = response.body() != null ? response.body().getMessage() : "Failed to submit offer";
                            handleError(errorMsg);
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        setLoading(false);
                        Log.e(TAG, "Network error submitting offer", t);
                        handleError("Network error. Please try again.");
                    }
                });
    }

    private void handleSuccess(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        if (listener != null) {
            listener.onOfferSubmitted(true, message);
        }
        dismiss();
    }

    private void handleError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        if (listener != null) {
            listener.onOfferSubmitted(false, message);
        }
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