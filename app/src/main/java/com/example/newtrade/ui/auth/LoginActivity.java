// app/src/main/java/com/example/newtrade/ui/auth/LoginActivity.java
package com.example.newtrade.ui.auth;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.newtrade.MainActivity;
import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.SharedPrefsManager;
import com.example.newtrade.utils.ValidationUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    // UI Components
    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoogleSignIn;
    private TextView tvRegister, tvForgotPassword;

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
        Log.d(TAG, "Backend URL: " + Constants.BASE_URL);
        Log.d(TAG, "Google Client ID: " + Constants.GOOGLE_CLIENT_ID);
    }

    private void initViews() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnGoogleSignIn = findViewById(R.id.btn_google_signin);
        tvRegister = findViewById(R.id.tv_register);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);

        // Initially disable login button
        btnLogin.setEnabled(false);

        // Check if views are found
        if (etEmail == null || etPassword == null || btnLogin == null) {
            Log.e(TAG, "❌ Required views not found in layout");
            Toast.makeText(this, "Layout error - missing required fields", Toast.LENGTH_LONG).show();
        } else {
            Log.d(TAG, "✅ All views initialized successfully");
        }
    }

    private void initGoogleSignIn() {
        Log.d(TAG, "=== GOOGLE SIGN-IN DEBUG INFO ===");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(Constants.GOOGLE_CLIENT_ID)
                .requestEmail()
                .requestProfile()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        Log.d(TAG, "✅ Google Sign-In configured");
    }

    private void initUtils() {
        prefsManager = SharedPrefsManager.getInstance(this);
    }

    private void setupListeners() {
        // Text watchers for enabling/disabling login button
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateLoginButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etEmail.addTextChangedListener(textWatcher);
        etPassword.addTextChangedListener(textWatcher);

        // Button listeners
        btnLogin.setOnClickListener(v -> performLogin());
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
        tvRegister.setOnClickListener(v -> navigateToRegister());
        tvForgotPassword.setOnClickListener(v -> navigateToForgotPassword());
    }

    private void updateLoginButtonState() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        boolean isFormValid = !email.isEmpty() && !password.isEmpty() && !isLoading;
        btnLogin.setEnabled(isFormValid);
    }

    private void performLogin() {
        if (isLoading) return;

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validateLoginForm(email, password)) {
            return;
        }

        setLoading(true);

        // ✅ FIX: Use Map<String, String> instead of Map<String, Object>
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", email);
        loginRequest.put("password", password);

        ApiClient.getAuthService().login(loginRequest)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        setLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, Object>> apiResponse = response.body();

                            if (apiResponse.isSuccess()) {
                                handleLoginSuccess(apiResponse.getData());
                            } else {
                                showError(apiResponse.getMessage());
                            }
                        } else {
                            showError("Login failed. Please check your credentials.");
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        setLoading(false);
                        String errorMessage = getNetworkErrorMessage(t);
                        showError(errorMessage);
                        Log.e(TAG, "Login request failed", t);
                    }
                });
    }


    private void signInWithGoogle() {
        if (isLoading) return;

        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 1001);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                handleGoogleSignIn(account);
            } catch (ApiException e) {
                String errorMessage = getNetworkErrorMessage(e);
                showError("Google Sign-In failed: " + errorMessage);
                Log.e(TAG, "Google Sign-In failed", e);
            }
        }
    }

    private void handleGoogleSignIn(GoogleSignInAccount account) {
        if (account == null) return;

        setLoading(true);

        // ✅ FIX: Use Map<String, String> instead of Map<String, Object>
        Map<String, String> googleRequest = new HashMap<>();
        googleRequest.put("idToken", account.getIdToken());
        googleRequest.put("email", account.getEmail());
        googleRequest.put("displayName", account.getDisplayName());

        ApiClient.getAuthService().googleSignIn(googleRequest)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        setLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, Object>> apiResponse = response.body();

                            if (apiResponse.isSuccess()) {
                                handleLoginSuccess(apiResponse.getData());
                            } else {
                                showError(apiResponse.getMessage());
                            }
                        } else {
                            showError("Google Sign-In failed. Please try again.");
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        setLoading(false);
                        String errorMessage = getNetworkErrorMessage(t);
                        showError("Google Sign-In failed: " + errorMessage);
                        Log.e(TAG, "Google Sign-In request failed", t);
                    }
                });
    }

    private boolean validateLoginForm(String email, String password) {
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    // ✅ FIX: Handle login success with proper Map casting
    private void handleLoginSuccess(Map<String, Object> userData) {
        try {
            // ✅ FIX: Safe casting with proper type checking
            Object userObject = userData.get("user");
            if (userObject instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> user = (Map<String, Object>) userObject;

                if (user != null) {
                    Long userId = Long.valueOf(user.get("id").toString());
                    String email = (String) user.get("email");
                    String name = (String) user.get("displayName");
                    Boolean isEmailVerifiedObj = (Boolean) user.get("isEmailVerified");
                    boolean isEmailVerified = isEmailVerifiedObj != null ? isEmailVerifiedObj : false;

                    // Save user session
                    prefsManager.saveUserSession(userId, email, name, isEmailVerified);

                    Log.d(TAG, "✅ Login successful for user: " + email);
                    Toast.makeText(this, "Welcome back, " + name + "!", Toast.LENGTH_SHORT).show();
                    navigateToMain();
                }
            } else {
                throw new ClassCastException("User data is not a Map");
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
    }

    // ✅ FIX: Add missing getNetworkErrorMessage method
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
        intent.putExtra("fromRegister", false);
        startActivity(intent);
    }
}