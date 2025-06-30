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
                clearErrors();
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
        btnRegister.setOnClickListener(v -> performRegistration());

        // Login link
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void clearErrors() {
        tilFullName.setError(null);
        tilDisplayName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
    }

    private void updateRegisterButtonState() {
        boolean allFieldsFilled = !etFullName.getText().toString().trim().isEmpty() &&
                !etDisplayName.getText().toString().trim().isEmpty() &&
                !etEmail.getText().toString().trim().isEmpty() &&
                !etPassword.getText().toString().trim().isEmpty() &&
                !etConfirmPassword.getText().toString().trim().isEmpty() &&
                cbTerms.isChecked();

        btnRegister.setEnabled(allFieldsFilled && !isLoading);
    }

    private void performRegistration() {
        String fullName = etFullName.getText().toString().trim();
        String displayName = etDisplayName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate inputs
        if (!ValidationUtils.isValidName(fullName)) {
            tilFullName.setError("Full name must be at least 2 characters");
            return;
        }

        if (!ValidationUtils.isValidName(displayName)) {
            tilDisplayName.setError("Display name must be at least 2 characters");
            return;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            tilEmail.setError("Please enter a valid email address");
            return;
        }

        if (!ValidationUtils.isValidPassword(password)) {
            tilPassword.setError("Password must be at least 6 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Passwords do not match");
            return;
        }

        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Please accept the terms and conditions", Toast.LENGTH_SHORT).show();
            return;
        }

        // Perform registration
        setLoading(true);

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
                handleRegistrationResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                  @NonNull Throwable t) {
                setLoading(false);
                Log.e(TAG, "Registration request failed", t);
                showError("Registration failed: " + t.getMessage());
            }
        });
    }

    private void handleRegistrationResponse(Response<StandardResponse<Map<String, Object>>> response) {
        try {
            if (response.isSuccessful() && response.body() != null) {
                StandardResponse<Map<String, Object>> apiResponse = response.body();

                if (apiResponse.isSuccess()) {
                    Toast.makeText(this, "Registration successful! Please verify your email.",
                            Toast.LENGTH_LONG).show();

                    // Navigate to OTP verification
                    Intent intent = new Intent(this, OtpVerificationActivity.class);
                    intent.putExtra("email", etEmail.getText().toString().trim());
                    intent.putExtra("fromRegistration", true);
                    startActivity(intent);
                    finish();
                } else {
                    showError(apiResponse.getMessage());
                }
            } else {
                String errorMsg = "Registration failed";
                if (response.code() == 400) {
                    errorMsg = "Invalid registration data";
                } else if (response.code() == 409) {
                    errorMsg = "Email already exists";
                }
                showError(errorMsg);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing registration response", e);
            showError("Registration failed");
        }
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        updateRegisterButtonState();
        btnRegister.setText(loading ? "Creating Account..." : "Create Account");
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        Log.e(TAG, "Showing error: " + message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}