// app/src/main/java/com/example/newtrade/ui/auth/RegisterActivity.java
package com.example.newtrade.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    // UI Components
    private TextInputLayout tilFullName, tilDisplayName, tilEmail, tilPassword, tilConfirmPassword;
    private EditText etFullName, etDisplayName, etEmail, etPassword, etConfirmPassword;
    private CheckBox cbTerms;
    private Button btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar;

    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ApiClient.init(this);

        initViews();
        setupListeners();

        Log.d(TAG, "RegisterActivity initialized");
    }

    private void initViews() {
        tilFullName = findViewById(R.id.til_full_name);
        tilDisplayName = findViewById(R.id.til_display_name);
        tilEmail = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);
        etFullName = findViewById(R.id.et_full_name);
        etDisplayName = findViewById(R.id.et_display_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        cbTerms = findViewById(R.id.cb_terms);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupListeners() {
        // Register button
        btnRegister.setOnClickListener(v -> performRegister());

        // Login link
        tvLogin.setOnClickListener(v -> navigateToLogin());

        // Terms checkbox
        cbTerms.setOnCheckedChangeListener((buttonView, isChecked) -> updateRegisterButtonState());

        // Text validation listeners
        etFullName.addTextChangedListener(new ValidationTextWatcher(tilFullName));
        etDisplayName.addTextChangedListener(new ValidationTextWatcher(tilDisplayName));
        etEmail.addTextChangedListener(new ValidationTextWatcher(tilEmail));
        etPassword.addTextChangedListener(new ValidationTextWatcher(tilPassword));
        etConfirmPassword.addTextChangedListener(new ValidationTextWatcher(tilConfirmPassword));
    }

    private class ValidationTextWatcher implements TextWatcher {
        private TextInputLayout textInputLayout;

        ValidationTextWatcher(TextInputLayout textInputLayout) {
            this.textInputLayout = textInputLayout;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            textInputLayout.setError(null);
            updateRegisterButtonState();
        }

        @Override
        public void afterTextChanged(Editable s) {}
    }

    private void updateRegisterButtonState() {
        String fullName = etFullName.getText().toString().trim();
        String displayName = etDisplayName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        boolean isFormValid = ValidationUtils.isValidName(fullName) &&
                ValidationUtils.isValidDisplayName(displayName) &&
                ValidationUtils.isValidEmail(email) &&
                ValidationUtils.isValidPassword(password) &&
                password.equals(confirmPassword) &&
                cbTerms.isChecked();

        btnRegister.setEnabled(isFormValid && !isLoading);
    }

    // FR-1.1.1: Users register via email/password with format validation
    private void performRegister() {
        if (!validateForm()) {
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            showError("No internet connection");
            return;
        }

        setLoading(true);

        String fullName = etFullName.getText().toString().trim();
        String displayName = etDisplayName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        Map<String, Object> registerData = new HashMap<>();
        registerData.put("fullName", fullName);
        registerData.put("displayName", displayName);
        registerData.put("email", email);
        registerData.put("password", password);

        Call<StandardResponse<Map<String, Object>>> call = ApiClient.getAuthService().register(registerData);
        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                setLoading(false);
                handleRegisterResponse(response, email);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                  @NonNull Throwable t) {
                setLoading(false);
                Log.e(TAG, "Registration request failed", t);
                showError(NetworkUtils.getNetworkErrorMessage(t));
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void handleRegisterResponse(Response<StandardResponse<Map<String, Object>>> response, String email) {
        try {
            if (response.isSuccessful() && response.body() != null) {
                StandardResponse<Map<String, Object>> apiResponse = response.body();

                if (apiResponse.isSuccess()) {
                    Toast.makeText(this, "Registration successful! Please verify your email.", Toast.LENGTH_LONG).show();

                    // FR-1.1.2: Email verification required for account activation
                    Intent intent = new Intent(this, OtpVerificationActivity.class);
                    intent.putExtra("email", email);
                    intent.putExtra("fromRegistration", true);
                    startActivity(intent);
                    finish();
                } else {
                    showError(apiResponse.getMessage());
                }
            } else {
                String errorMsg = "Registration failed";
                if (response.code() == 409) {
                    errorMsg = "Email already exists";
                } else if (response.code() == 400) {
                    errorMsg = "Invalid registration data";
                }
                showError(errorMsg);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing registration response", e);
            showError("Registration failed");
        }
    }

    private boolean validateForm() {
        boolean isValid = true;

        String fullName = etFullName.getText().toString().trim();
        if (!ValidationUtils.isValidName(fullName)) {
            tilFullName.setError("Full name must be between 2-50 characters");
            isValid = false;
        }

        String displayName = etDisplayName.getText().toString().trim();
        if (!ValidationUtils.isValidDisplayName(displayName)) {
            tilDisplayName.setError("Display name must be between 2-50 characters");
            isValid = false;
        }

        String email = etEmail.getText().toString().trim();
        if (!ValidationUtils.isValidEmail(email)) {
            tilEmail.setError("Please enter a valid email address");
            isValid = false;
        }

        String password = etPassword.getText().toString().trim();
        if (!ValidationUtils.isValidPassword(password)) {
            tilPassword.setError("Password must be at least 6 characters");
            isValid = false;
        }

        String confirmPassword = etConfirmPassword.getText().toString().trim();
        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Passwords do not match");
            isValid = false;
        }

        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Please accept the terms and conditions", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        updateRegisterButtonState();
        btnRegister.setText(loading ? "Creating Account..." : "Create Account");
    }

    private void showError(String message) {
        Log.e(TAG, "Showing error: " + message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}