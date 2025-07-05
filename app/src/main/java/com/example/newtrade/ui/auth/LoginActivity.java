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

        Log.d(TAG, "✅ LoginActivity created successfully");
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

        // Hide progress bar
        progressBar.setVisibility(View.GONE);

        Log.d(TAG, "✅ All views initialized successfully");
    }

    private void initGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(Constants.GOOGLE_CLIENT_ID)
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        Log.d(TAG, "✅ Google Sign-In initialized with client ID: " + Constants.GOOGLE_CLIENT_ID);
    }

    private void initUtils() {
        prefsManager = SharedPrefsManager.getInstance(this);
        Log.d(TAG, "✅ Utils initialized");
    }

    private void setupListeners() {
        // Email/Password text change listeners
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateLoginButtonState();
                clearFieldErrors();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etEmail.addTextChangedListener(textWatcher);
        etPassword.addTextChangedListener(textWatcher);

        // Login button click
        btnLogin.setOnClickListener(v -> performEmailPasswordLogin());

        // Google Sign-In button click
        btnGoogleSignIn.setOnClickListener(v -> performGoogleSignIn());

        // Register link click
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        // Forgot password link click
        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });

        Log.d(TAG, "✅ All listeners set up successfully");
    }

    private void updateLoginButtonState() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        boolean isFormValid = !email.isEmpty() && !password.isEmpty() && !isLoading;
        btnLogin.setEnabled(isFormValid);
    }

    private void clearFieldErrors() {
        tilEmail.setError(null);
        tilPassword.setError(null);
    }

    private void performEmailPasswordLogin() {
        if (isLoading) return;

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validateEmailPasswordForm(email, password)) {
            return;
        }

        setLoading(true);
        Log.d(TAG, "🔐 Performing email/password login for: " + email);

        Map<String, Object> loginRequest = new HashMap<>();
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
                                Log.d(TAG, "✅ Email/Password login successful");
                                handleLoginSuccess(apiResponse.getData());
                            } else {
                                Log.w(TAG, "❌ Login failed: " + apiResponse.getMessage());
                                showError(apiResponse.getMessage());
                            }
                        } else {
                            Log.e(TAG, "❌ Login request failed with code: " + response.code());
                            showError("Login failed. Please check your credentials.");
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        setLoading(false);
                        Log.e(TAG, "❌ Login request failed", t);
                        showError("Network error. Please check your connection.");
                    }
                });
    }

    private void performGoogleSignIn() {
        if (isLoading) return;

        Log.d(TAG, "🔐 Starting Google Sign-In");
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
        }
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            if (account != null) {
                Log.d(TAG, "✅ Google Sign-In successful for: " + account.getEmail());
                performGoogleSignInWithBackend(account.getIdToken());
            } else {
                Log.w(TAG, "❌ Google Sign-In account is null");
                showError("Google Sign-In failed. Please try again.");
            }
        } catch (ApiException e) {
            Log.e(TAG, "❌ Google Sign-In failed with code: " + e.getStatusCode(), e);
            showError("Google Sign-In failed. Please try again.");
        }
    }

    private void performGoogleSignInWithBackend(String idToken) {
        if (idToken == null) {
            showError("Google Sign-In failed. Please try again.");
            return;
        }

        setLoading(true);
        Log.d(TAG, "🔐 Authenticating with backend using Google ID token");

        Map<String, Object> googleSignInRequest = new HashMap<>();
        googleSignInRequest.put("idToken", idToken);

        ApiClient.getAuthService().googleSignIn(googleSignInRequest)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        setLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, Object>> apiResponse = response.body();

                            if (apiResponse.isSuccess()) {
                                Log.d(TAG, "✅ Google Sign-In backend authentication successful");
                                handleLoginSuccess(apiResponse.getData());
                            } else {
                                Log.w(TAG, "❌ Google Sign-In backend authentication failed: " + apiResponse.getMessage());
                                showError(apiResponse.getMessage());
                            }
                        } else {
                            Log.e(TAG, "❌ Google Sign-In backend request failed with code: " + response.code());
                            showError("Google Sign-In failed. Please try again.");
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        setLoading(false);
                        Log.e(TAG, "❌ Google Sign-In backend request failed", t);
                        showError("Network error. Please check your connection.");
                    }
                });
    }

    private void handleLoginSuccess(Map<String, Object> loginData) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> userData = (Map<String, Object>) loginData.get("user");
            Boolean requiresOtp = (Boolean) loginData.get("requiresOtp");

            if (userData != null) {
                // Extract user data
                Long userId = getLongValue(userData, "id");
                String displayName = (String) userData.get("displayName");
                String email = (String) userData.get("email");
                String profilePicture = (String) userData.get("profilePicture");
                Boolean isEmailVerified = (Boolean) userData.get("isEmailVerified");

                Log.d(TAG, "✅ User data extracted - ID: " + userId + ", Name: " + displayName + ", Email: " + email);

                // Check if OTP verification is required
                if (requiresOtp != null && requiresOtp) {
                    Log.d(TAG, "🔐 OTP verification required for: " + email);
                    navigateToOtpVerification(email);
                    return;
                }

                // Check if email verification is required
                if (isEmailVerified != null && !isEmailVerified) {
                    Log.d(TAG, "📧 Email verification required for: " + email);
                    Toast.makeText(this, "Please verify your email address", Toast.LENGTH_LONG).show();
                    // In a real app, you might want to navigate to email verification screen
                    return;
                }

                // Save user data and navigate to main
                prefsManager.saveUserData(userId, displayName, email, profilePicture);
                Log.d(TAG, "✅ User data saved successfully");

                Toast.makeText(this, "Welcome back, " + displayName + "!", Toast.LENGTH_SHORT).show();
                navigateToMain();
            } else {
                Log.w(TAG, "❌ User data is null in login response");
                showError("Login successful but user data is missing");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error processing login response", e);
            showError("Login successful but error processing response");
        }
    }

    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        } else if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Double) {
            return ((Double) value).longValue();
        }
        return null;
    }

    private void navigateToOtpVerification(String email) {
        Intent intent = new Intent(this, OtpVerificationActivity.class);
        intent.putExtra("email", email);
        startActivity(intent);
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private boolean validateEmailPasswordForm(String email, String password) {
        boolean isValid = true;

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
        } else if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            isValid = false;
        }

        return isValid;
    }

    private void setLoading(boolean loading) {
        isLoading = loading;

        // Update UI based on loading state
        btnLogin.setEnabled(!loading && !etEmail.getText().toString().trim().isEmpty() && !etPassword.getText().toString().trim().isEmpty());
        btnGoogleSignIn.setEnabled(!loading);
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);

        // Update button text
        if (loading) {
            btnLogin.setText("Signing in...");
            btnGoogleSignIn.setText("Signing in...");
        } else {
            btnLogin.setText("Sign In");
            btnGoogleSignIn.setText("Sign in with Google");
        }

        Log.d(TAG, "Loading state set to: " + loading);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.w(TAG, "Error shown to user: " + message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "LoginActivity destroyed");
    }
}