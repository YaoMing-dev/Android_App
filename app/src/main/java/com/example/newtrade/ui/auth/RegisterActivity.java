// app/src/main/java/com/example/newtrade/ui/auth/RegisterActivity.java
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
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    // UI Components
    private TextInputLayout tilDisplayName, tilEmail, tilPassword, tilConfirmPassword;
    private EditText etDisplayName, etEmail, etPassword, etConfirmPassword;
    private MaterialCheckBox cbTerms;
    private Button btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar;

    // State
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize API client
        ApiClient.init(this);

        initViews();
        setupListeners();

        Log.d(TAG, "✅ RegisterActivity created successfully");
    }

    private void initViews() {
        tilDisplayName = findViewById(R.id.til_display_name);
        tilEmail = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);

        etDisplayName = findViewById(R.id.et_display_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);

        cbTerms = findViewById(R.id.cb_terms);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);
        progressBar = findViewById(R.id.progress_bar);

        // Initially disable register button
        btnRegister.setEnabled(false);

        // Hide progress bar
        progressBar.setVisibility(View.GONE);

        Log.d(TAG, "✅ All views initialized successfully");
    }

    private void setupListeners() {
        // Text change listeners
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateRegisterButtonState();
                clearFieldErrors();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etDisplayName.addTextChangedListener(textWatcher);
        etEmail.addTextChangedListener(textWatcher);
        etPassword.addTextChangedListener(textWatcher);
        etConfirmPassword.addTextChangedListener(textWatcher);

        // Terms checkbox listener
        cbTerms.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateRegisterButtonState();
        });

        // Register button click
        btnRegister.setOnClickListener(v -> performRegistration());

        // Login link click
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        Log.d(TAG, "✅ All listeners set up successfully");
    }

    private void updateRegisterButtonState() {
        String displayName = etDisplayName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        boolean isFormValid = !displayName.isEmpty() && !email.isEmpty() &&
                !password.isEmpty() && !confirmPassword.isEmpty() &&
                cbTerms.isChecked() && !isLoading;

        btnRegister.setEnabled(isFormValid);
    }

    private void clearFieldErrors() {
        tilDisplayName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
    }

    private void performRegistration() {
        if (isLoading) return;

        String displayName = etDisplayName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (!validateRegistrationForm(displayName, email, password, confirmPassword)) {
            return;
        }

        setLoading(true);
        Log.d(TAG, "🔐 Performing registration for: " + email);

        Map<String, Object> registrationRequest = new HashMap<>();
        registrationRequest.put("displayName", displayName);
        registrationRequest.put("email", email);
        registrationRequest.put("password", password);

        ApiClient.getAuthService().register(registrationRequest)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        setLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, Object>> apiResponse = response.body();

                            if (apiResponse.isSuccess()) {
                                Log.d(TAG, "✅ Registration successful");
                                handleRegistrationSuccess(email);
                            } else {
                                Log.w(TAG, "❌ Registration failed: " + apiResponse.getMessage());
                                showError(apiResponse.getMessage());
                            }
                        } else {
                            Log.e(TAG, "❌ Registration request failed with code: " + response.code());
                            showError("Registration failed. Please try again.");
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        setLoading(false);
                        Log.e(TAG, "❌ Registration request failed", t);
                        showError("Network error. Please check your connection.");
                    }
                });
    }

    private void handleRegistrationSuccess(String email) {
        Toast.makeText(this, "Registration successful! Please check your email for verification.", Toast.LENGTH_LONG).show();

        // Navigate to OTP verification
        Intent intent = new Intent(this, OtpVerificationActivity.class);
        intent.putExtra("email", email);
        intent.putExtra("isRegistration", true);
        startActivity(intent);
        finish();
    }

    private boolean validateRegistrationForm(String displayName, String email, String password, String confirmPassword) {
        boolean isValid = true;

        // Display name validation
        if (displayName.isEmpty()) {
            tilDisplayName.setError("Display name is required");
            isValid = false;
        } else if (displayName.length() < 2) {
            tilDisplayName.setError("Display name must be at least 2 characters");
            isValid = false;
        } else if (displayName.length() > 50) {
            tilDisplayName.setError("Display name must be less than 50 characters");
            isValid = false;
        }

        // Email validation
        if (email.isEmpty()) {
            tilEmail.setError("Email is required");
            isValid = false;
        } else if (!ValidationUtils.isValidEmail(email)) {
            tilEmail.setError("Please enter a valid email address");
            isValid = false;
        }

        // Password validation
        if (password.isEmpty()) {
            tilPassword.setError("Password is required");
            isValid = false;
        } else if (!ValidationUtils.isValidPassword(password)) {
            tilPassword.setError("Password must be at least 8 characters with uppercase, lowercase, number, and special character");
            isValid = false;
        }

        // Confirm password validation
        if (confirmPassword.isEmpty()) {
            tilConfirmPassword.setError("Please confirm your password");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Passwords do not match");
            isValid = false;
        }

        // Terms checkbox validation
        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Please accept the terms and conditions", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    private void setLoading(boolean loading) {
        isLoading = loading;

        // Update UI based on loading state
        btnRegister.setEnabled(!loading && validateFormFields());
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);

        // Update button text
        if (loading) {
            btnRegister.setText("Creating Account...");
        } else {
            btnRegister.setText("Create Account");
        }

        Log.d(TAG, "Loading state set to: " + loading);
    }

    private boolean validateFormFields() {
        String displayName = etDisplayName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        return !displayName.isEmpty() && !email.isEmpty() && !password.isEmpty() &&
                !confirmPassword.isEmpty() && cbTerms.isChecked();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.w(TAG, "Error shown to user: " + message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "RegisterActivity destroyed");
    }
}