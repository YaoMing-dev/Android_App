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
    private TextInputLayout tilEmail;
    private EditText etEmail;
    private Button btnSendOTP;
    private TextView tvBackToLogin;
    private ProgressBar progressBar;

    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        ApiClient.init(this);

        initViews();
        setupListeners();

        Log.d(TAG, "ForgotPasswordActivity initialized");
    }

    private void initViews() {
        tilEmail = findViewById(R.id.til_email);
        etEmail = findViewById(R.id.et_email);
        btnSendOTP = findViewById(R.id.btn_send_otp);
        tvBackToLogin = findViewById(R.id.tv_back_to_login);
        progressBar = findViewById(R.id.progress_bar);

        btnSendOTP.setEnabled(false);
    }

    private void setupListeners() {
        // Email text watcher
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilEmail.setError(null);
                updateSendButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Send OTP button
        btnSendOTP.setOnClickListener(v -> sendPasswordResetOTP());

        // Back to login
        tvBackToLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void updateSendButtonState() {
        String email = etEmail.getText().toString().trim();
        btnSendOTP.setEnabled(!email.isEmpty() && !isLoading);
    }

    private void sendPasswordResetOTP() {
        if (isLoading) return;

        String email = etEmail.getText().toString().trim();

        // Validate email
        String emailError = ValidationUtils.getEmailValidationError(email);
        if (emailError != null) {
            tilEmail.setError(emailError);
            return;
        }

        setLoading(true);

        Map<String, String> forgotRequest = new HashMap<>();
        forgotRequest.put("email", email);

        ApiClient.getAuthService().forgotPassword(forgotRequest)
                .enqueue(new Callback<StandardResponse<Map<String, String>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, String>>> call,
                                           Response<StandardResponse<Map<String, String>>> response) {
                        setLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, String>> apiResponse = response.body();

                            if (apiResponse.isSuccess()) {
                                handleSendSuccess(email);
                            } else {
                                showError(apiResponse.getMessage());
                            }
                        } else {
                            showError("Failed to send reset email. Please try again.");
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, String>>> call, Throwable t) {
                        setLoading(false);
                        Log.e(TAG, "Forgot password API call failed", t);
                        showError("Network error. Please check your connection.");
                    }
                });
    }

    private void handleSendSuccess(String email) {
        Toast.makeText(this, "Password reset email sent! Please check your inbox.", Toast.LENGTH_LONG).show();

        // Navigate to OTP verification
        Intent intent = new Intent(this, OTPVerificationActivity.class);
        intent.putExtra("email", email);
        intent.putExtra("from_forgot_password", true);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSendOTP.setEnabled(!loading && !etEmail.getText().toString().trim().isEmpty());
        etEmail.setEnabled(!loading);
        tvBackToLogin.setEnabled(!loading);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}