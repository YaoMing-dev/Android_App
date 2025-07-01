// app/src/main/java/com/example/newtrade/ui/auth/ForgotPasswordActivity.java
package com.example.newtrade.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.NetworkUtils;
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
    private Toolbar toolbar;
    private TextView tvInstructions;
    private TextInputLayout tilEmail;
    private EditText etEmail;
    private Button btnSendCode;
    private TextView tvBackToLogin;
    private ProgressBar progressBar;

    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        ApiClient.init(this);

        initViews();
        setupToolbar();
        setupListeners();

        Log.d(TAG, "ForgotPasswordActivity initialized");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvInstructions = findViewById(R.id.tv_instructions);
        tilEmail = findViewById(R.id.til_email);
        etEmail = findViewById(R.id.et_email);
        btnSendCode = findViewById(R.id.btn_send_code);
        tvBackToLogin = findViewById(R.id.tv_back_to_login);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Reset Password");
        }
    }

    private void setupListeners() {
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

        btnSendCode.setOnClickListener(v -> sendResetCode());
        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void updateSendButtonState() {
        String email = etEmail.getText().toString().trim();
        btnSendCode.setEnabled(ValidationUtils.isValidEmail(email) && !isLoading);
    }

    // FR-1.1.3: Password recovery through email
    private void sendResetCode() {
        String email = etEmail.getText().toString().trim();

        if (!ValidationUtils.isValidEmail(email)) {
            tilEmail.setError("Please enter a valid email address");
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            showError("No internet connection");
            return;
        }

        setLoading(true);

        Map<String, String> forgotData = new HashMap<>();
        forgotData.put("email", email);

        Call<StandardResponse<Map<String, String>>> call = ApiClient.getAuthService().forgotPassword(forgotData);
        call.enqueue(new Callback<StandardResponse<Map<String, String>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, String>>> call,
                                   @NonNull Response<StandardResponse<Map<String, String>>> response) {
                setLoading(false);
                handleForgotPasswordResponse(response, email);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, String>>> call,
                                  @NonNull Throwable t) {
                setLoading(false);
                Log.e(TAG, "Forgot password request failed", t);
                showError(NetworkUtils.getNetworkErrorMessage(t));
            }
        });
    }

    private void handleForgotPasswordResponse(Response<StandardResponse<Map<String, String>>> response, String email) {
        try {
            if (response.isSuccessful() && response.body() != null) {
                StandardResponse<Map<String, String>> apiResponse = response.body();

                if (apiResponse.isSuccess()) {
                    Toast.makeText(this, "Reset code sent to your email!", Toast.LENGTH_LONG).show();

                    // Navigate to OTP verification
                    Intent intent = new Intent(this, OtpVerificationActivity.class);
                    intent.putExtra("email", email);
                    intent.putExtra("fromForgotPassword", true);
                    startActivity(intent);
                    finish();
                } else {
                    String message = apiResponse.getMessage();
                    handleForgotPasswordError(message, response.code());
                }
            } else {
                handleForgotPasswordError("Failed to send reset code", response.code());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing forgot password response", e);
            showError("Request failed");
        }
    }

    private void handleForgotPasswordError(String message, int responseCode) {
        if (responseCode == 404 || message.toLowerCase().contains("not found") || message.toLowerCase().contains("email")) {
            tilEmail.setError("Email address not found");
        } else if (responseCode == 429 || message.toLowerCase().contains("limit") || message.toLowerCase().contains("attempts")) {
            showError("Too many requests. Please try again later.");
        } else {
            showError(message);
        }
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSendCode.setEnabled(!loading);
        btnSendCode.setText(loading ? "Sending..." : "Send Reset Code");
        updateSendButtonState();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}