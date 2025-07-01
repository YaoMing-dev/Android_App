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
        btnRegister.setOnClickListener(v -> register());
        tvLogin.setOnClickListener(v -> navigateToLogin());

        // Real-time validation
        TextWatcher validationWatcher = new TextWatcher() {
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

        etFullName.addTextChangedListener(validationWatcher);
        etDisplayName.addTextChangedListener(validationWatcher);
        etEmail.addTextChangedListener(validationWatcher);
        etPassword.addTextChangedListener(validationWatcher);
        etConfirmPassword.addTextChangedListener(validationWatcher);
        cbTerms.setOnCheckedChangeListener((buttonView, isChecked) -> updateRegisterButtonState());
    }

    private void clearErrors() {
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

        btnRegister.setEnabled(!isLoading &&
                !fullName.isEmpty() &&
                !displayName.isEmpty() &&
                !email.isEmpty() &&
                !password.isEmpty() &&
                !confirmPassword.isEmpty() &&
                cbTerms.isChecked());
    }

    private void register() {
        if (isLoading) return;

        String fullName = etFullName.getText().toString().trim();
        String displayName = etDisplayName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (!validateInput(fullName, displayName, email, password, confirmPassword)) {
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            showError("No internet connection");
            return;
        }

        setLoading(true);

        Map<String, Object> request = new HashMap<>();
        request.put("fullName", fullName);
        request.put("displayName", displayName);
        request.put("email", email);
        request.put("password", password);

        Call<StandardResponse<Map<String, Object>>> call = ApiClient.getAuthService().register(request);
        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                setLoading(false);
                handleRegisterResponse(response, email);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                setLoading(false);
                Log.e(TAG, "Registration failed", t);
                showError("Registration failed. Please try again.");
            }
        });
    }

    private void handleRegisterResponse(Response<StandardResponse<Map<String, Object>>> response, String email) {
        try {
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                // Registration successful, navigate to OTP verification
                navigateToOtpVerification(email);
            } else {
                String message = response.body() != null ? response.body().getMessage() : "Registration failed";
                showError(message);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing register response", e);
            showError("Registration failed. Please try again.");
        }
    }

    private boolean validateInput(String fullName, String displayName, String email, String password, String confirmPassword) {
        boolean isValid = true;

        if (!ValidationUtils.isValidName(fullName)) {
            tilFullName.setError("Full name must be 2-50 characters");
            isValid = false;
        }

        if (!ValidationUtils.isValidName(displayName)) {
            tilDisplayName.setError("Display name must be 2-50 characters");
            isValid = false;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            tilEmail.setError("Please enter a valid email");
            isValid = false;
        }

        if (!ValidationUtils.isValidPassword(password)) {
            tilPassword.setError("Password must be at least 6 characters");
            isValid = false;
        }

        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Passwords do not match");
            isValid = false;
        }

        if (!cbTerms.isChecked()) {
            showError("Please accept Terms and Conditions");
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

    private void navigateToOtpVerification(String email) {
        Intent intent = new Intent(this, OtpVerificationActivity.class);
        intent.putExtra("email", email);
        intent.putExtra("fromRegister", true);
        startActivity(intent);
        finish();
    }
}