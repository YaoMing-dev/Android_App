// app/src/main/java/com/example/newtrade/ui/payment/PaymentMethodSelectionActivity.java
package com.example.newtrade.ui.payment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.newtrade.R;
import com.example.newtrade.models.PaymentMethod;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class PaymentMethodSelectionActivity extends AppCompatActivity {

    private static final String TAG = "PaymentMethodSelection";

    // UI Components
    private MaterialToolbar toolbar;
    private LinearLayout layoutPaymentMethods;
    private MaterialButton btnConfirm;

    // Data
    private PaymentMethod selectedPaymentMethod;
    private PaymentMethod currentPaymentMethod;
    private SharedPrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_method_selection);

        initializeComponents();
        setupToolbar();
        getIntentData();
        setupPaymentMethods();
        setupListeners();

        Log.d(TAG, "PaymentMethodSelectionActivity created for user: " + prefsManager.getUserName());
    }

    private void initializeComponents() {
        // Initialize SharedPrefsManager
        prefsManager = new SharedPrefsManager(this);

        // UI components
        toolbar = findViewById(R.id.toolbar);
        layoutPaymentMethods = findViewById(R.id.layout_payment_methods);
        btnConfirm = findViewById(R.id.btn_confirm);

        Log.d(TAG, "Components initialized, user ID: " + prefsManager.getUserId());
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Select Payment Method");
        }
    }

    private void getIntentData() {
        Intent intent = getIntent();
        currentPaymentMethod = (PaymentMethod) intent.getSerializableExtra("current_method");

        // Set current method as selected initially
        selectedPaymentMethod = currentPaymentMethod;

        Log.d(TAG, "Current payment method: " + currentPaymentMethod);
    }

    private void setupPaymentMethods() {
        layoutPaymentMethods.removeAllViews();

        // Add payment method options
        addPaymentMethodOption(PaymentMethod.CARD,
                "Credit/Debit Card",
                "Visa, MasterCard, American Express",
                R.drawable.ic_credit_card,
                true);

        addPaymentMethodOption(PaymentMethod.DIGITAL_WALLET,
                "Digital Wallet",
                "Apple Pay, Google Pay, Samsung Pay",
                R.drawable.ic_wallet,
                true);

        addPaymentMethodOption(PaymentMethod.BANK_TRANSFER,
                "Bank Transfer",
                "Direct bank account transfer",
                R.drawable.ic_bank,
                true);

        addPaymentMethodOption(PaymentMethod.CASH,
                "Cash Payment",
                "Pay with cash on delivery/pickup",
                R.drawable.ic_cash,
                true);

        // Update selection visual
        updatePaymentMethodSelection();
    }

    private void addPaymentMethodOption(PaymentMethod method, String title, String description,
                                        int iconRes, boolean enabled) {
        View methodView = getLayoutInflater().inflate(R.layout.item_payment_method_selection,
                layoutPaymentMethods, false);

        MaterialCardView card = methodView.findViewById(R.id.card_payment_method);
        android.widget.ImageView icon = methodView.findViewById(R.id.iv_payment_icon);
        android.widget.TextView titleText = methodView.findViewById(R.id.tv_payment_title);
        android.widget.TextView descText = methodView.findViewById(R.id.tv_payment_description);
        android.widget.ImageView checkIcon = methodView.findViewById(R.id.iv_check);

        // Set content
        icon.setImageResource(iconRes);
        titleText.setText(title);
        descText.setText(description);

        // Set enabled state
        card.setEnabled(enabled);
        card.setAlpha(enabled ? 1.0f : 0.5f);

        // Set click listener
        if (enabled) {
            methodView.setOnClickListener(v -> selectPaymentMethod(method));
        }

        // Tag for selection update
        methodView.setTag(method);

        layoutPaymentMethods.addView(methodView);

        Log.d(TAG, "Added payment method: " + method + " (enabled: " + enabled + ")");
    }

    private void selectPaymentMethod(PaymentMethod method) {
        selectedPaymentMethod = method;
        updatePaymentMethodSelection();
        btnConfirm.setEnabled(true);

        Log.d(TAG, "Selected payment method: " + method);

        // Save user preference for future use
        savePaymentMethodPreference(method);
    }

    private void updatePaymentMethodSelection() {
        for (int i = 0; i < layoutPaymentMethods.getChildCount(); i++) {
            View child = layoutPaymentMethods.getChildAt(i);
            MaterialCardView card = child.findViewById(R.id.card_payment_method);
            android.widget.ImageView checkIcon = child.findViewById(R.id.iv_check);
            PaymentMethod method = (PaymentMethod) child.getTag();

            if (method == selectedPaymentMethod) {
                card.setCardBackgroundColor(getResources().getColor(R.color.primary_light, null));
                card.setStrokeColor(getResources().getColor(R.color.primary, null));
                card.setStrokeWidth(4);
                checkIcon.setVisibility(View.VISIBLE);
            } else {
                card.setCardBackgroundColor(getResources().getColor(R.color.surface, null));
                card.setStrokeColor(getResources().getColor(R.color.outline, null));
                card.setStrokeWidth(2);
                checkIcon.setVisibility(View.GONE);
            }
        }
    }

    private void setupListeners() {
        btnConfirm.setOnClickListener(v -> confirmSelection());
    }

    private void confirmSelection() {
        if (selectedPaymentMethod == null) {
            Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
            return;
        }

        // Return selected method to calling activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("selected_method", selectedPaymentMethod);
        setResult(RESULT_OK, resultIntent);

        Log.d(TAG, "Confirmed payment method: " + selectedPaymentMethod);
        finish();
    }

    private void savePaymentMethodPreference(PaymentMethod method) {
        // Save user's preferred payment method for future use
        prefsManager.saveString("preferred_payment_method", method.name());
        Log.d(TAG, "Saved payment method preference: " + method);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(RESULT_CANCELED);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}