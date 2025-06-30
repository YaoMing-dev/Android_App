// app/src/main/java/com/example/newtrade/ui/auth/LoginActivity.java
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.newtrade.MainActivity;
import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.models.User;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.SharedPrefsManager;
import com.example.newtrade.utils.ValidationUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;

    // UI Components
    private TextInputLayout tilEmail, tilPassword;
    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoogleSignIn;
    private TextView tvRegister, tvForgotPassword;
    private ProgressBar progressBar;

    // Google Sign-In
    private GoogleSignInClient googleSignInClient;

    // Utils
    private SharedPrefsManager prefsManager;

    // State
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize API client
        ApiClient.init(this);

        // Initialize views and utilities
        initViews();
        initGoogleSignIn();
        initUtils();
        setupListeners();

        // Check if already logged in
        if (prefsManager.isLoggedIn()) {
            Log.d(TAG, "User already logged in, redirecting to main");
            navigateToMain();
            return;
        }

        Log.d(TAG, "LoginActivity created successfully");
    }

    private void initViews() {
        tilEmail = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnGoogleSignIn = findViewById(R.id.btn_google_signin);
        tvRegister = findViewById(R.id.tv_register);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        progressBar = findViewById(R.id.progress_bar);

        // Initially disable login button
        btnLogin.setEnabled(false);

        // Check if views are found
        if (etEmail == null || etPassword == null || btnLogin == null) {
            Log.e(TAG, "❌ Required views not found in layout");
            Toast.makeText(this, "Layout error", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Log.d(TAG, "✅ Views initialized successfully");
    }

    private void initGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(Constants.GOOGLE_CLIENT_ID)
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void initUtils() {
        prefsManager = new SharedPrefsManager(this);
    }

    private void setupListeners() {
        // Email text watcher
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilEmail.setError(null);
                updateLoginButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Password text watcher
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilPassword.setError(null);
                updateLoginButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Login button
        btnLogin.setOnClickListener(v -> performLogin());

        // Google Sign-In button
        btnGoogleSignIn.setOnClickListener(v -> performGoogleSignIn());

        // Register link
        tvRegister.setOnClickListener(v -> navigateToRegister());

        // Forgot password link
        tvForgotPassword.setOnClickListener(v -> navigateToForgotPassword());
    }

    private void updateLoginButtonState() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        btnLogin.setEnabled(!email.isEmpty() && !password.isEmpty() && !isLoading);
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate inputs
        if (!ValidationUtils.isValidEmail(email)) {
            tilEmail.setError("Please enter a valid email address");
            return;
        }

        if (!ValidationUtils.isValidPassword(password)) {
            tilPassword.setError("Password must be at least 6 characters");
            return;
        }

        // Perform login
        setLoading(true);

        Map<String, String> loginData = new HashMap<>();
        loginData.put("email", email);
        loginData.put("password", password);

        Call<StandardResponse<Map<String, Object>>> call = ApiClient.getAuthService().login(loginData);
        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                setLoading(false);
                handleLoginResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                  @NonNull Throwable t) {
                setLoading(false);
                Log.e(TAG, "Login request failed", t);
                showError(getNetworkErrorMessage(t));
            }
        });
    }

    private void performGoogleSignIn() {
        setLoading(true);
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                handleGoogleSignInResult(account);
            } catch (ApiException e) {
                setLoading(false);
                Log.e(TAG, "Google sign in failed", e);
                showError("Google sign in failed: " + e.getMessage());
            }
        }
    }

    private void handleGoogleSignInResult(GoogleSignInAccount account) {
        String idToken = account.getIdToken();
        if (idToken == null) {
            setLoading(false);
            showError("Failed to get Google ID token");
            return;
        }

        Map<String, String> googleData = new HashMap<>();
        googleData.put("idToken", idToken);

        Call<StandardResponse<Map<String, Object>>> call = ApiClient.getAuthService().googleSignIn(googleData);
        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                setLoading(false);
                handleLoginResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                  @NonNull Throwable t) {
                setLoading(false);
                Log.e(TAG, "Google sign in request failed", t);
                showError(getNetworkErrorMessage(t));
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void handleLoginResponse(Response<StandardResponse<Map<String, Object>>> response) {
        try {
            if (response.isSuccessful() && response.body() != null) {
                StandardResponse<Map<String, Object>> apiResponse = response.body();

                if (apiResponse.isSuccess()) {
                    Map<String, Object> data = apiResponse.getData();

                    // Check if OTP is required
                    Boolean requiresOtp = (Boolean) data.get("requiresOtp");
                    if (requiresOtp != null && requiresOtp) {
                        String email = etEmail.getText().toString().trim();
                        navigateToOtpVerification(email);
                        return;
                    }

                    // Get user data
                    Map<String, Object> userData = (Map<String, Object>) data.get("user");
                    if (userData != null) {
                        // Save user data
                        prefsManager.saveLoginData(userData);

                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    } else {
                        showError("Invalid response data");
                    }
                } else {
                    showError(apiResponse.getMessage());
                }
            } else {
                String errorMsg = "Login failed";
                if (response.code() == 401) {
                    errorMsg = "Invalid email or password";
                } else if (response.code() == 400) {
                    errorMsg = "Invalid request. Please check your input.";
                }
                showError(errorMsg);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing login response", e);
            showError("Login successful but failed to save user data");
        }
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        btnLogin.setEnabled(!loading && !etEmail.getText().toString().trim().isEmpty()
                && !etPassword.getText().toString().trim().isEmpty());
        btnGoogleSignIn.setEnabled(!loading);
        btnLogin.setText(loading ? "Signing in..." : "Sign In");
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
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
        } else if (message.contains("401")) {
            return "Invalid email or password";
        } else if (message.contains("400")) {
            return "Invalid request. Please check your input.";
        } else if (message.contains("500")) {
            return "Server error. Please try again later.";
        }

        return message;
    }

    private void showError(String message) {
        Log.e(TAG, "Showing error: " + message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    private void navigateToForgotPassword() {
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
    }

    private void navigateToOtpVerification(String email) {
        Intent intent = new Intent(this, OtpVerificationActivity.class);
        intent.putExtra("email", email);
        intent.putExtra("fromLogin", true);
        startActivity(intent);
    }
}