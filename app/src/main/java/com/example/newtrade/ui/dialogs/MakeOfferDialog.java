package com.example.newtrade.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.Product;
import com.example.newtrade.models.StandardResponse;
import com.google.gson.Gson;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * FR-5.1.1: Dialog for buyers to make offers on products
 */
public class MakeOfferDialog extends DialogFragment {

    private static final String TAG = "MakeOfferDialog";
    private static final String ARG_PRODUCT = "product";

    // UI Components
    private TextView tvProductTitle, tvOriginalPrice, tvOfferHelp;
    private EditText etOfferAmount, etMessage;
    private Button btnMakeOffer, btnCancel;

    // Data
    private Product product;
    private OfferCallback callback;
    private boolean isLoading = false;

    // Interface for callback
    public interface OfferCallback {
        void onOfferMade(boolean success, String message);
    }

    public static MakeOfferDialog newInstance(Product product) {
        MakeOfferDialog dialog = new MakeOfferDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PRODUCT, product);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            product = (Product) getArguments().getSerializable(ARG_PRODUCT);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_make_offer, null);

        initViews(view);
        setupUI();
        setupListeners();

        builder.setView(view);
        return builder.create();
    }

    private void initViews(View view) {
        tvProductTitle = view.findViewById(R.id.tv_product_title);
        tvOriginalPrice = view.findViewById(R.id.tv_original_price);
        tvOfferHelp = view.findViewById(R.id.tv_offer_help);
        etOfferAmount = view.findViewById(R.id.et_offer_amount);
        etMessage = view.findViewById(R.id.et_message);
        btnMakeOffer = view.findViewById(R.id.btn_make_offer);
        btnCancel = view.findViewById(R.id.btn_cancel);
    }

    private void setupUI() {
        if (product != null) {
            tvProductTitle.setText(product.getTitle());

            // Format original price
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            String formattedPrice = formatter.format(product.getPrice());
            tvOriginalPrice.setText("Original Price: " + formattedPrice);

            // Set helpful suggestion
            double suggestedOffer = product.getPrice().doubleValue() * 0.85; // 15% discount
            String suggestion = formatter.format(suggestedOffer);
            tvOfferHelp.setText("Suggested offer: " + suggestion + " (15% off)");
        }

        // Initially disable make offer button
        btnMakeOffer.setEnabled(false);
    }

    private void setupListeners() {
        etOfferAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateOffer();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnMakeOffer.setOnClickListener(v -> makeOffer());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void validateOffer() {
        String offerText = etOfferAmount.getText().toString().trim();

        if (offerText.isEmpty()) {
            btnMakeOffer.setEnabled(false);
            return;
        }

        try {
            double offerAmount = Double.parseDouble(offerText);
            double originalPrice = product.getPrice().doubleValue();

            // Validate reasonable offer (between 10% and 100% of original price)
            boolean isValid = offerAmount >= (originalPrice * 0.1) && offerAmount <= originalPrice;
            btnMakeOffer.setEnabled(isValid && !isLoading);

            if (offerAmount > originalPrice) {
                etOfferAmount.setError("Offer cannot exceed original price");
            } else if (offerAmount < (originalPrice * 0.1)) {
                etOfferAmount.setError("Offer too low (minimum 10% of original price)");
            } else {
                etOfferAmount.setError(null);
            }
        } catch (NumberFormatException e) {
            btnMakeOffer.setEnabled(false);
            etOfferAmount.setError("Invalid amount");
        }
    }

    private void makeOffer() {
        if (isLoading) return;

        String offerText = etOfferAmount.getText().toString().trim();
        String message = etMessage.getText().toString().trim();

        try {
            double offerAmount = Double.parseDouble(offerText);

            setLoading(true);

            // Prepare offer request
            Map<String, Object> offerRequest = new HashMap<>();
            offerRequest.put("productId", product.getId());
            offerRequest.put("offerAmount", offerAmount);
            if (!message.isEmpty()) {
                offerRequest.put("message", message);
            }

            Log.d(TAG, "Making offer: " + new Gson().toJson(offerRequest));

            // Make API call to create offer
            ApiClient.getApiService().createOffer(offerRequest)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                           @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                        setLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, Object>> apiResponse = response.body();

                            if (apiResponse.isSuccess()) {
                                Log.d(TAG, "✅ Offer made successfully");
                                if (callback != null) {
                                    callback.onOfferMade(true, "Offer sent successfully!");
                                }
                                dismiss();
                            } else {
                                String errorMsg = apiResponse.getMessage() != null ?
                                    apiResponse.getMessage() : "Failed to make offer";
                                showError(errorMsg);
                                if (callback != null) {
                                    callback.onOfferMade(false, errorMsg);
                                }
                            }
                        } else {
                            showError("Failed to make offer. Please try again.");
                            if (callback != null) {
                                callback.onOfferMade(false, "Network error");
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                        setLoading(false);
                        String errorMsg = "Network error: " + t.getMessage();
                        showError(errorMsg);
                        Log.e(TAG, "Failed to make offer", t);
                        if (callback != null) {
                            callback.onOfferMade(false, errorMsg);
                        }
                    }
                });

        } catch (NumberFormatException e) {
            showError("Invalid offer amount");
        }
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        btnMakeOffer.setEnabled(!loading && !etOfferAmount.getText().toString().trim().isEmpty());
        btnMakeOffer.setText(loading ? "Making Offer..." : "Make Offer");
        etOfferAmount.setEnabled(!loading);
        etMessage.setEnabled(!loading);
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }

    public void setOfferCallback(OfferCallback callback) {
        this.callback = callback;
    }
}
