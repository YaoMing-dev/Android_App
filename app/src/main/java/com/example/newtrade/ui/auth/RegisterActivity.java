// app/src/main/java/com/example/newtrade/ui/auth/RegisterActivity.java
package com.example.newtrade.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.models.User;
import com.example.newtrade.utils.SharedPrefsManager;
import com.example.newtrade.utils.ValidationUtils;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    // UI Components
    private EditText etDisplayName, etEmail, etPassword, etConfirmPassword;
    private CheckBox cbTerms;
    private Button btnRegister;
    private TextView tvLogin;

    // Utils
    private SharedPrefsManager prefsManager;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        initUtils();
        setupListeners();

        Log.d(TAG, "RegisterActivity created");
    }

    private void initViews() {
        etDisplayName = findViewById(R.id.et_display_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        cbTerms = findViewById(R.id.cb_terms);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);

        // Initially disable register button
        btnRegister.setEnabled(false);

        // Check if views are found
        if (etDisplayName == null || etEmail == null || etPassword == null ||
                etConfirmPassword == null || btnRegister == null) {
            Log.e(TAG, "❌ Required views not found in layout");
            Toast.makeText(this, "Layout error - missing required fields", Toast.LENGTH_LONG).show();
        }
    }

    private void initUtils() {
        prefsManager = SharedPrefsManager.getInstance(this);
    }

    private void setupListeners() {
        // Text change listeners for validation
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateForm();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etDisplayName.addTextChangedListener(textWatcher);
        etEmail.addTextChangedListener(textWatcher);
        etPassword.addTextChangedListener(textWatcher);
        etConfirmPassword.addTextChangedListener(textWatcher);

        // Terms checkbox listener
        cbTerms.setOnCheckedChangeListener((buttonView, isChecked) -> validateForm());

        // Click listeners
        btnRegister.setOnClickListener(v -> attemptRegister());
        tvLogin.setOnClickListener(v -> navigateToLogin());
    }

    private void validateForm() {
        String displayName = etDisplayName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        boolean isValid = ValidationUtils.isValidDisplayName(displayName) &&
                ValidationUtils.isValidEmail(email) &&
                ValidationUtils.isValidPassword(password) &&
                ValidationUtils.isPasswordMatch(password, confirmPassword) &&
                cbTerms.isChecked();

        btnRegister.setEnabled(isValid && !isLoading);
    }

    private void attemptRegister() {
        String displayName = etDisplayName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate display name
        String displayNameError = ValidationUtils.getDisplayNameError(displayName);
        if (displayNameError != null) {
            etDisplayName.setError(displayNameError);
            return;
        }

        // Validate email
        String emailError = ValidationUtils.getEmailError(email);
        if (emailError != null) {
            etEmail.setError(emailError);
            return;
        }

        // Validate password
        String passwordError = ValidationUtils.getPasswordError(password);
        if (passwordError != null) {
            etPassword.setError(passwordError);
            return;
        }

        // Validate confirm password
        String confirmPasswordError = ValidationUtils.getConfirmPasswordError(password, confirmPassword);
        if (confirmPasswordError != null) {
            etConfirmPassword.setError(confirmPasswordError);
            return;
        }

        // Check terms
        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Vui lòng đồng ý với điều khoản sử dụng", Toast.LENGTH_SHORT).show();
            return;
        }

        performRegister(displayName, email, password);
    }

    private void performRegister(String displayName, String email, String password) {
        Log.d(TAG, "🔍 Attempting registration for: " + email);

        showLoading(true);

        Map<String, String> request = new HashMap<>();
        request.put("displayName", displayName);
        request.put("email", email);
        request.put("password", password);

        ApiClient.getAuthService().register(request).enqueue(new Callback<StandardResponse<User>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<User>> call,
                                   @NonNull Response<StandardResponse<User>> response) {
                showLoading(false);

                Log.d(TAG, "🔍 Register response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<User> apiResponse = response.body();
                    Log.d(TAG, "🔍 Register response: " + new Gson().toJson(apiResponse));

                    if (apiResponse.isSuccess()) {
                        // Registration successful - now navigate to OTP verification
                        Log.d(TAG, "✅ Registration successful, navigating to OTP verification");
                        Toast.makeText(RegisterActivity.this,
                                "Registration successful! Please check your email for verification code.",
                                Toast.LENGTH_LONG).show();
                        navigateToOtpVerification(email);
                    } else {
                        showError(apiResponse.getMessage() != null ? apiResponse.getMessage() : "Đăng ký thất bại");
                    }
                } else {
                    // Handle error response
                    handleRegisterError(response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<User>> call, @NonNull Throwable t) {
                showLoading(false);
                String errorMessage = getNetworkErrorMessage(t);
                showError("Registration failed: " + errorMessage);
                Log.e(TAG, "❌ Registration request failed", t);
            }
        });
    }

    private void handleRegisterError(Response<StandardResponse<User>> response) {
        try {
            if (response.errorBody() != null) {
                String errorJson = response.errorBody().string();
                Log.e(TAG, "❌ Register error body: " + errorJson);

                // Try to parse error message
                StandardResponse<User> errorResponse = new Gson().fromJson(errorJson, StandardResponse.class);
                if (errorResponse != null && errorResponse.getMessage() != null) {
                    showError(errorResponse.getMessage());
                } else {
                    showError("Registration failed. Please try again.");
                }
            } else {
                showError("Registration failed. Please try again.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing error response", e);
            showError("Registration failed. Please try again.");
        }
    }

    private String getNetworkErrorMessage(Throwable throwable) {
        if (throwable == null) {
            return "Unknown error occurred";
        }

        String message = throwable.getMessage();
        if (message == null || message.isEmpty()) {
            return "Network error occurred";
        }

        // Handle specific error types
        if (message.contains("timeout")) {
            return "Connection timeout. Please try again.";
        } else if (message.contains("connection")) {
            return "Connection failed. Please check your internet.";
        } else if (message.contains("409")) {
            return "Email already exists. Please use a different email.";
        } else if (message.contains("400")) {
            return "Invalid registration data. Please check your input.";
        } else if (message.contains("500")) {
            return "Server error. Please try again later.";
        }

        return message;
    }

    private void navigateToOtpVerification(String email) {
        Intent intent = new Intent(this, OtpVerificationActivity.class);
        intent.putExtra("email", email);
        intent.putExtra("fromRegister", true);
        startActivity(intent);
        // Don't finish() here - user might want to go back if OTP fails
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        isLoading = show;
        btnRegister.setEnabled(!show && isFormValid());
        btnRegister.setText(show ? "Đang đăng ký..." : "Đăng ký");

        // Disable inputs during loading
        etDisplayName.setEnabled(!show);
        etEmail.setEnabled(!show);
        etPassword.setEnabled(!show);
        etConfirmPassword.setEnabled(!show);
        cbTerms.setEnabled(!show);
    }

    private boolean isFormValid() {
        String displayName = etDisplayName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        return ValidationUtils.isValidDisplayName(displayName) &&
                ValidationUtils.isValidEmail(email) &&
                ValidationUtils.isValidPassword(password) &&
                ValidationUtils.isPasswordMatch(password, confirmPassword) &&
                cbTerms.isChecked();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Error: " + message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "RegisterActivity destroyed");
    }
}