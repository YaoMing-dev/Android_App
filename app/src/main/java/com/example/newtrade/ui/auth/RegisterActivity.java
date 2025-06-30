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

        btnRegister.setEnabled(false);
    }

    private void setupListeners() {
        // Text watchers for validation
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearFieldErrors();
                updateRegisterButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etFullName.addTextChangedListener(textWatcher);
        etDisplayName.addTextChangedListener(textWatcher);
        etEmail.addTextChangedListener(textWatcher);
        etPassword.addTextChangedListener(textWatcher);
        etConfirmPassword.addTextChangedListener(textWatcher);

        // Terms checkbox
        cbTerms.setOnCheckedChangeListener((buttonView, isChecked) -> updateRegisterButtonState());

        // Register button
        btnRegister.setOnClickListener(v -> performRegister());

        // Login link
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void clearFieldErrors() {
        tilFullName.setError(null);
        tilDisplayName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
    }

    private void updateRegisterButtonState() {
        String fullName = etFullName.getText().toString().trim();
        String displayName = etDisplayName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        boolean isFormValid = !fullName.isEmpty() && !displayName.isEmpty() &&
                !email.isEmpty() && !password.isEmpty() && !confirmPassword.isEmpty() &&
                cbTerms.isChecked() && !isLoading;

        btnRegister.setEnabled(isFormValid);
    }

    private void performRegister() {
        if (isLoading) return;

        String fullName = etFullName.getText().toString().trim();
        String displayName = etDisplayName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (!validateRegistrationForm(fullName, displayName, email, password, confirmPassword)) {
            return;
        }

        setLoading(true);

        Map<String, Object> registerRequest = new HashMap<>();
        registerRequest.put("fullName", fullName);
        registerRequest.put("displayName", displayName);
        registerRequest.put("email", email);
        registerRequest.put("password", password);

        ApiClient.getAuthService().register(registerRequest)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        setLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, Object>> apiResponse = response.body();

                            if (apiResponse.isSuccess()) {
                                handleRegistrationSuccess(email);
                            } else {
                                showError(apiResponse.getMessage());
                            }
                        } else {
                            showError("Registration failed. Please try again.");
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        setLoading(false);
                        Log.e(TAG, "Registration API call failed", t);
                        showError("Network error. Please check your connection.");
                    }
                });
    }

    private boolean validateRegistrationForm(String fullName, String displayName,
                                             String email, String password, String confirmPassword) {
        boolean isValid = true;

        // Validate full name
        String fullNameError = ValidationUtils.getNameValidationError(fullName);
        if (fullNameError != null) {
            tilFullName.setError(fullNameError);
            isValid = false;
        }

        // Validate display name
        String displayNameError = ValidationUtils.getNameValidationError(displayName);
        if (displayNameError != null) {
            tilDisplayName.setError(displayNameError);
            isValid = false;
        }

        // Validate email
        String emailError = ValidationUtils.getEmailValidationError(email);
        if (emailError != null) {
            tilEmail.setError(emailError);
            isValid = false;
        }

        // Validate password
        String passwordError = ValidationUtils.getPasswordValidationError(password);
        if (passwordError != null) {
            tilPassword.setError(passwordError);
            isValid = false;
        }

        // Validate confirm password
        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Passwords don't match");
            isValid = false;
        }

        // Validate terms
        if (!cbTerms.isChecked()) {
            showError("Please accept the Terms of Service and Privacy Policy");
            isValid = false;
        }

        return isValid;
    }

    private void handleRegistrationSuccess(String email) {
        Toast.makeText(this, "Registration successful! Please check your email for verification.", Toast.LENGTH_LONG).show();

        // Navigate to OTP verification
        Intent intent = new Intent(this, OTPVerificationActivity.class);
        intent.putExtra("email", email);
        intent.putExtra("from_registration", true);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!loading && areFieldsValid() && cbTerms.isChecked());

        etFullName.setEnabled(!loading);
        etDisplayName.setEnabled(!loading);
        etEmail.setEnabled(!loading);
        etPassword.setEnabled(!loading);
        etConfirmPassword.setEnabled(!loading);
        cbTerms.setEnabled(!loading);
        tvLogin.setEnabled(!loading);
    }

    private boolean areFieldsValid() {
        return !etFullName.getText().toString().trim().isEmpty() &&
                !etDisplayName.getText().toString().trim().isEmpty() &&
                !etEmail.getText().toString().trim().isEmpty() &&
                !etPassword.getText().toString().trim().isEmpty() &&
                !etConfirmPassword.getText().toString().trim().isEmpty();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}