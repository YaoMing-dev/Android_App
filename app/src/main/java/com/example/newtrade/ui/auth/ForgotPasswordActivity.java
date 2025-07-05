// app/src/main/java/com/example/newtrade/ui/auth/ForgotPasswordActivity.java
package com.example.newtrade.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.ValidationUtils;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ForgotPasswordActivity";

    // UI Components
    private LinearLayout llBack;
    private TextInputLayout tilEmail;
    private EditText etEmail;
    private Button btnReset;
    private TextView tvLogin;
    private ProgressBar progressBar;

    // State
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize API client
        ApiClient.init(this);

        initViews();
        setupListeners();

        Log.d(TAG, "✅ ForgotPasswordActivity created successfully");
    }

    private void initViews() {
        llBack = findViewById(R.id.ll_back);
        tilEmail = findViewById(R.id.til_email);
        etEmail = findViewById(R.id.et_email);
        btnReset = findViewById(R.id.btn_reset);
        tvLogin = findViewById(R.id.tv_login);
        progressBar = findViewById(R.id.progress_bar);

        // Initially disable reset button
        btnReset.setEnabled(false);

        // Hide progress bar
        progressBar.setVisibility(View.GONE);

        Log.d(TAG, "✅ All views initialized successfully");
    }

    private void setupListeners() {
        // Back button click
        llBack.setOnClickListener(v -> {
            finish();
        });

        // Email text change listener
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateResetButtonState();
                clearFieldErrors();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Reset button click
        btnReset.setOnClickListener(v -> performPasswordReset());

        // Login link click
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
            finish();
        });

        Log.d(TAG, "✅ All listeners set up successfully");
    }

    private void updateResetButtonState() {
        String email = etEmail.getText().toString().trim();
        boolean isFormValid = !email.isEmpty() && !isLoading;
        btnReset.setEnabled(isFormValid);
    }

    private void clearFieldErrors() {
        tilEmail.setError(null);
    }

    private void performPasswordReset() {
        if (isLoading) return;

        String email = etEmail.getText().toString().trim();

        if (!validatePasswordResetForm(email)) {
            return;
        }

        setLoading(true);
        Log.d(TAG, "🔐 Performing password reset for: " + email);

        Map<String, Object> passwordResetRequest = new HashMap<>();
        passwordResetRequest.put("email", email);

        ApiClient.getUserService().requestPasswordReset(passwordResetRequest)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        setLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, Object>> apiResponse = response.body();

                            if (apiResponse.isSuccess()) {
                                Log.d(TAG, "✅ Password reset request successful");
                                handlePasswordResetSuccess(email);
                            } else {
                                Log.w(TAG, "❌ Password reset request failed: " + apiResponse.getMessage());
                                showError(apiResponse.getMessage());
                            }
                        } else {
                            Log.e(TAG, "❌ Password reset request failed with code: " + response.code());
                            showError("Password reset failed. Please try again.");
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        setLoading(false);
                        Log.e(TAG, "❌ Password reset request failed", t);
                        showError("Network error. Please check your connection.");
                    }
                });
    }

    private void handlePasswordResetSuccess(String email) {
        Toast.makeText(this, "Password reset instructions sent to your email", Toast.LENGTH_LONG).show();

        // Navigate to login with success message
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("message", "Password reset instructions sent to " + email);
        startActivity(intent);
        finish();
    }

    private boolean validatePasswordResetForm(String email) {
        boolean isValid = true;

        // Email validation
        if (email.isEmpty()) {
            tilEmail.setError("Email is required");
            isValid = false;
        } else if (!ValidationUtils.isValidEmail(email)) {
            tilEmail.setError("Please enter a valid email address");
            isValid = false;
        }

        return isValid;
    }

    private void setLoading(boolean loading) {
        isLoading = loading;

        // Update UI based on loading state
        btnReset.setEnabled(!loading && !etEmail.getText().toString().trim().isEmpty());
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);

        // Update button text
        if (loading) {
            btnReset.setText("Sending...");
        } else {
            btnReset.setText("Send Reset Link");
        }

        Log.d(TAG, "Loading state set to: " + loading);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.w(TAG, "Error shown to user: " + message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "ForgotPasswordActivity destroyed");
    }
}