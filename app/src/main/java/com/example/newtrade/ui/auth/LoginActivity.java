// app/src/main/java/com/example/newtrade/ui/auth/LoginActivity.java
package com.example.newtrade.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
        Log.d(TAG, "Initializing Google Sign-In...");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(Constants.GOOGLE_CLIENT_ID)
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
        Log.d(TAG, "✅ Google Sign-In client created successfully");
    }

    private void initUtils() {
        prefsManager = SharedPrefsManager.getInstance(this);

        // Test backend connectivity
        Constants.checkNetworkAndLog(this);
        Constants.testBackendConnectivity(this);

        Log.d(TAG, "✅ Utils initialized successfully");
    }

    private void setupListeners() {
        // Text change listeners for validation
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateFormValidation();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etEmail.addTextChangedListener(textWatcher);
        etPassword.addTextChangedListener(textWatcher);

        // Click listeners
        btnLogin.setOnClickListener(v -> attemptLogin());
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
        tvRegister.setOnClickListener(v -> navigateToRegister());
        tvForgotPassword.setOnClickListener(v -> navigateToForgotPassword());

        Log.d(TAG, "✅ Event listeners set up successfully");
    }

    private void updateFormValidation() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        boolean isValid = ValidationUtils.isValidEmail(email) && ValidationUtils.isValidPassword(password);
        btnLogin.setEnabled(isValid && !isLoading);
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

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

        performLogin(email, password);
    }

    private void performLogin(String email, String password) {
        Log.d(TAG, "🔍 Attempting login for: " + email);
        Log.d(TAG, "🔍 Backend URL: " + Constants.BASE_URL);

        showLoading(true);

        Map<String, String> request = new HashMap<>();
        request.put("email", email);
        request.put("password", password);

        ApiClient.getAuthService().login(request).enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                showLoading(false);

                Log.d(TAG, "🔍 Login response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Map<String, Object>> apiResponse = response.body();
                    Log.d(TAG, "🔍 Login response: " + new Gson().toJson(apiResponse));

                    if (apiResponse.isSuccess() && apiResponse.hasData()) {
                        handleLoginSuccess(apiResponse.getData());
                    } else {
                        showError(apiResponse.getMessage() != null ? apiResponse.getMessage() : "Đăng nhập thất bại");
                    }
                } else {
                    Log.e(TAG, "❌ Login failed - Response code: " + response.code());
                    handleErrorResponse(response);
                    showError("Đăng nhập thất bại. Kiểm tra email và mật khẩu.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e(TAG, "❌ Login network error", t);
                Log.e(TAG, "❌ Error type: " + t.getClass().getSimpleName());
                Log.e(TAG, "❌ Error message: " + t.getMessage());

                showError("Lỗi mạng: " + Constants.getNetworkErrorMessage(t));
            }
        });
    }

    private void signInWithGoogle() {
        if (isLoading) return;

        Log.d(TAG, "🔍 Starting Google Sign-In");
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, Constants.RC_GOOGLE_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "✅ Google sign-in successful: " + account.getEmail());
                Log.d(TAG, "🔍 Google ID Token length: " + (account.getIdToken() != null ? account.getIdToken().length() : 0));
                performGoogleSignIn(account.getIdToken());
            } catch (ApiException e) {
                Log.e(TAG, "❌ Google sign-in failed with code: " + e.getStatusCode(), e);
                showError("Google đăng nhập thất bại: " + e.getMessage());
            }
        }
    }

    private void performGoogleSignIn(String idToken) {
        if (idToken == null || idToken.isEmpty()) {
            Log.e(TAG, "❌ Google ID Token is null or empty");
            showError("Google đăng nhập thất bại - không nhận được token");
            return;
        }

        Log.d(TAG, "🔍 Sending Google Sign-In request to backend");
        Log.d(TAG, "🔍 Backend URL: " + Constants.BASE_URL + "api/auth/google-signin");
        Log.d(TAG, "🔍 ID Token length: " + idToken.length());

        showLoading(true);

        Map<String, String> request = new HashMap<>();
        request.put("idToken", idToken);

        ApiClient.getAuthService().googleSignIn(request).enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                showLoading(false);

                Log.d(TAG, "🔍 Google Sign-In response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Map<String, Object>> apiResponse = response.body();
                    Log.d(TAG, "🔍 Google Sign-In response: " + new Gson().toJson(apiResponse));

                    if (apiResponse.isSuccess() && apiResponse.hasData()) {
                        handleLoginSuccess(apiResponse.getData());
                    } else {
                        showError(apiResponse.getMessage() != null ?
                                apiResponse.getMessage() : "Google đăng nhập thất bại");
                    }
                } else {
                    Log.e(TAG, "❌ Google Sign-In failed - Response code: " + response.code());
                    handleErrorResponse(response);
                    showError("Google đăng nhập thất bại");
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e(TAG, "❌ Google Sign-In network error", t);
                Log.e(TAG, "❌ Error type: " + t.getClass().getSimpleName());
                Log.e(TAG, "❌ Error message: " + t.getMessage());
                showError("Lỗi mạng khi đăng nhập Google: " + Constants.getNetworkErrorMessage(t));
            }
        });
    }

    private void handleLoginSuccess(Map<String, Object> data) {
        try {
            Log.d(TAG, "🔍 Processing login success data: " + new Gson().toJson(data));

            // Parse user data from response
            Object userObj = data.get("user");
            if (userObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> userMap = (Map<String, Object>) userObj;

                // Extract user info
                Long userId = null;
                Object idObj = userMap.get("id");
                if (idObj instanceof Number) {
                    userId = ((Number) idObj).longValue();
                } else if (idObj instanceof String) {
                    try {
                        userId = Long.parseLong((String) idObj);
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "❌ Cannot parse user ID: " + idObj);
                    }
                }

                String email = (String) userMap.get("email");
                String displayName = (String) userMap.get("displayName");
                Boolean isEmailVerified = (Boolean) userMap.get("isEmailVerified");

                if (userId != null && email != null && displayName != null) {
                    // Save user session
                    prefsManager.saveUserSession(userId, email, displayName,
                            isEmailVerified != null ? isEmailVerified : false);

                    Log.d(TAG, "✅ User session saved successfully");

                    // Check if OTP verification is required
                    Boolean requiresOtp = (Boolean) data.get("requiresOtp");
                    if (requiresOtp != null && requiresOtp && !Boolean.TRUE.equals(isEmailVerified)) {
                        Log.d(TAG, "🔍 OTP verification required");
                        navigateToOtpVerification(email);
                    } else {
                        Log.d(TAG, "🔍 Login complete, navigating to main");
                        navigateToMain();
                    }
                } else {
                    Log.e(TAG, "❌ Incomplete user data received");
                    showError("Dữ liệu người dùng không đầy đủ");
                }
            } else {
                Log.e(TAG, "❌ Invalid user data format");
                showError("Định dạng dữ liệu không hợp lệ");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error processing login success", e);
            showError("Lỗi xử lý đăng nhập: " + e.getMessage());
        }
    }

    private void handleErrorResponse(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                Log.e(TAG, "❌ Error body: " + errorBody);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Cannot read error body", e);
        }
    }

    private void showLoading(boolean loading) {
        isLoading = loading;
        updateFormValidation();
        btnGoogleSignIn.setEnabled(!loading);

        if (loading) {
            btnLogin.setText("Đang đăng nhập...");
            btnGoogleSignIn.setText("Đang xử lý...");
        } else {
            btnLogin.setText("Sign In");
            btnGoogleSignIn.setText("Continue with Google");
        }
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
        // TODO: Implement RegisterActivity navigation
        showError("Chức năng đăng ký đang được phát triển");
    }

    private void navigateToForgotPassword() {
        // TODO: Implement ForgotPasswordActivity navigation
        showError("Chức năng quên mật khẩu đang được phát triển");
    }

    private void navigateToOtpVerification(String email) {
        // TODO: Implement OtpVerificationActivity navigation
        showError("OTP verification sẽ được thêm sau");
        navigateToMain(); // Temporary - skip OTP for now
    }
}