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
import android.text.InputType;
import com.example.newtrade.MainActivity;
import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.AuthFlowManager;
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

        // Initialize API client with callback
        ApiClient.init(this, new ApiClient.InitCallback() {
            @Override
            public void onSuccess(String baseUrl) {
                Log.d(TAG, "✅ Backend connected: " + baseUrl);
                Toast.makeText(LoginActivity.this, "Connected to: " + baseUrl, Toast.LENGTH_SHORT).show();

                // Continue with normal initialization
                initViews();
                initGoogleSignIn();
                initUtils();
                setupListeners();

                // ✅ Check prefill data từ ResetPasswordActivity
                checkPrefillData();

                // Check if already logged in
                if (prefsManager.isLoggedIn()) {
                    Log.d(TAG, "User already logged in, redirecting to main");
                    navigateToMain();
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "❌ Backend connection failed: " + error);
                showIPConfigDialog(error);
            }
        });
    }

    /**
     * ✅ Check prefill data từ ResetPasswordActivity
     */
    private void checkPrefillData() {
        Intent intent = getIntent();
        String prefillEmail = intent.getStringExtra("prefill_email");
        String prefillPassword = intent.getStringExtra("prefill_password");

        if (prefillEmail != null && etEmail != null) {
            etEmail.setText(prefillEmail);
            Log.d(TAG, "✅ Prefilled email: " + prefillEmail);
        }

        if (prefillPassword != null && etPassword != null) {
            etPassword.setText(prefillPassword);
            Log.d(TAG, "✅ Prefilled password");
        }

        if (prefillEmail != null && prefillPassword != null) {
            Toast.makeText(this, "Mật khẩu đã được đặt lại thành công! Bạn có thể đăng nhập ngay.", Toast.LENGTH_LONG).show();
        }
    }

    private void showIPConfigDialog(String error) {
        new AlertDialog.Builder(this)
                .setTitle("Backend Connection Failed")
                .setMessage("Could not connect to backend server.\n\nError: " + error + "\n\nWould you like to:")
                .setPositiveButton("Auto-detect IP", (dialog, which) -> {
                    ApiClient.autoDetectIP(this, new ApiClient.InitCallback() {
                        @Override
                        public void onSuccess(String baseUrl) {
                            Toast.makeText(LoginActivity.this, "✅ Connected: " + baseUrl, Toast.LENGTH_SHORT).show();
                            recreate(); // Restart activity
                        }

                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(LoginActivity.this, "❌ Still failed: " + error, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNeutralButton("Set Custom IP", (dialog, which) -> {
                    showCustomIPDialog();
                })
                .setNegativeButton("Retry", (dialog, which) -> {
                    recreate(); // Restart activity
                })
                .setCancelable(false)
                .show();
    }

    private void showCustomIPDialog() {
        EditText editText = new EditText(this);
        editText.setHint("Enter IP address (e.g., 192.168.1.100)");
        editText.setInputType(InputType.TYPE_CLASS_TEXT);

        new AlertDialog.Builder(this)
                .setTitle("Enter Backend IP")
                .setMessage("Enter the IP address of your backend server:")
                .setView(editText)
                .setPositiveButton("Connect", (dialog, which) -> {
                    String ip = editText.getText().toString().trim();
                    if (!ip.isEmpty()) {
                        ApiClient.setCustomIP(this, ip, new ApiClient.InitCallback() {
                            @Override
                            public void onSuccess(String baseUrl) {
                                Toast.makeText(LoginActivity.this, "✅ Connected: " + baseUrl, Toast.LENGTH_SHORT).show();
                                recreate(); // Restart activity
                            }

                            @Override
                            public void onFailure(String error) {
                                Toast.makeText(LoginActivity.this, "❌ Failed to connect to " + ip + ": " + error, Toast.LENGTH_LONG).show();
                                showIPConfigDialog(error); // Show dialog again
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    showIPConfigDialog("Custom IP cancelled");
                })
                .show();
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

        // ✅ DEBUG THÔNG TIN QUAN TRỌNG
        Log.d(TAG, "🔍 App Package: " + getPackageName());
        Log.d(TAG, "🔍 Client ID: " + Constants.GOOGLE_CLIENT_ID);
        Log.d(TAG, "🔍 Backend URL: " + Constants.BASE_URL);

        // ✅ KIỂM TRA SHA-1 HIỆN TẠI
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA1");
                md.update(signature.toByteArray());
                byte[] digest = md.digest();
                StringBuilder sha1 = new StringBuilder();
                for (byte b : digest) {
                    sha1.append(String.format("%02X:", b & 0xFF));
                }
                if (sha1.length() > 0) {
                    sha1.deleteCharAt(sha1.length() - 1); // Remove last ':'
                }
                Log.d(TAG, "🔍 Current SHA-1: " + sha1.toString());
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error getting SHA-1", e);
        }

        Log.d(TAG, "=== END DEBUG INFO ===");

        // ✅ CLEAR PREVIOUS SIGN-IN STATE
        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Log.d(TAG, "✅ Previous Google sign-in state cleared");

            // Create new GoogleSignInOptions after clearing
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(Constants.GOOGLE_CLIENT_ID)
                    .requestEmail()
                    .requestProfile()
                    .build();

            googleSignInClient = GoogleSignIn.getClient(this, gso);
            Log.d(TAG, "✅ Google Sign-In client created successfully");
        });
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

        Log.d(TAG, "🔍 Starting Google Sign-In...");
        Log.d(TAG, "🔍 App Package: " + getPackageName());
        Log.d(TAG, "🔍 Using Client ID: " + Constants.GOOGLE_CLIENT_ID);

        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, Constants.RC_GOOGLE_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "🔍 onActivityResult - requestCode: " + requestCode + ", resultCode: " + resultCode);

        if (requestCode == Constants.RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "✅ Google sign-in successful!");
                Log.d(TAG, "🔍 Email: " + account.getEmail());
                Log.d(TAG, "🔍 Name: " + account.getDisplayName());
                Log.d(TAG, "🔍 ID Token length: " + (account.getIdToken() != null ? account.getIdToken().length() : 0));

                if (account.getIdToken() != null) {
                    performGoogleSignIn(account.getIdToken());
                } else {
                    Log.e(TAG, "❌ ID Token is null!");
                    showError("Google đăng nhập thất bại - không nhận được ID token");
                }
            } catch (ApiException e) {
                Log.e(TAG, "❌ Google sign-in failed with code: " + e.getStatusCode(), e);
                Log.e(TAG, "❌ Status message: " + e.getStatus());
                Log.e(TAG, "❌ Status code meaning: " + getStatusCodeMeaning(e.getStatusCode()));
                showError("Google đăng nhập thất bại: " + getStatusCodeMeaning(e.getStatusCode()));
            }
        }
    }

    private String getStatusCodeMeaning(int statusCode) {
        switch (statusCode) {
            case 10: return "DEVELOPER_ERROR - Cấu hình OAuth không đúng";
            case 12: return "CANCELLED - Người dùng hủy đăng nhập";
            case 7: return "NETWORK_ERROR - Lỗi mạng";
            case 8: return "INTERNAL_ERROR - Lỗi nội bộ";
            default: return "Error code: " + statusCode;
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
        Log.d(TAG, "🔍 ID Token preview: " + idToken.substring(0, Math.min(50, idToken.length())) + "...");

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
                    showError("Google đăng nhập thất bại - Server error");
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

    /**
     * ✅ MANDATORY EMAIL VERIFICATION - FR-1.1.2 Implementation
     * Handle login success với kiểm tra account activation
     */
    private void handleLoginSuccess(Map<String, Object> data) {
        try {
            Log.d(TAG, "=== HANDLE LOGIN SUCCESS - FR-1.1.2 MANDATORY VERIFICATION ===");
            Log.d(TAG, "🔍 FULL BACKEND RESPONSE: " + new Gson().toJson(data));

            // Parse user data from response
            Object userObj = data.get("user");
            if (userObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> userMap = (Map<String, Object>) userObj;

                // Extract user info
                Long userId = extractUserId(userMap);
                String email = (String) userMap.get("email");
                String displayName = (String) userMap.get("displayName");
                Boolean isEmailVerified = (Boolean) userMap.get("isEmailVerified");

                Log.d(TAG, "🔍 BACKEND RETURNED:");
                Log.d(TAG, "🔍 User ID: " + userId);
                Log.d(TAG, "🔍 Email: " + email);
                Log.d(TAG, "🔍 Display Name: " + displayName);
                Log.d(TAG, "🔍 Email Verified: " + isEmailVerified);

                // ✅ VALIDATION
                if (userId != null && email != null && displayName != null) {
                    // Save user session
                    prefsManager.saveUserSession(userId, email, displayName,
                            isEmailVerified != null ? isEmailVerified : false);

                    Log.d(TAG, "✅ User session saved successfully");

                    // ✅ FR-1.1.2: MANDATORY EMAIL VERIFICATION FOR ACCOUNT ACTIVATION
                    boolean emailVerified = Boolean.TRUE.equals(isEmailVerified);

                    // Log flow information
                    AuthFlowManager.logFlowInfo(AuthFlowManager.AuthFlow.LOGIN, email, emailVerified);

                    if (AuthFlowManager.needsAccountActivation(emailVerified)) {
                        // ❌ ACCOUNT NOT ACTIVATED - FORCE EMAIL VERIFICATION
                        Log.d(TAG, "⚠️ LOGIN: Account not activated - email verification required");
                        Log.d(TAG, "⚠️ FR-1.1.2: Enforcing mandatory email verification for account activation");

                        String activationMessage = AuthFlowManager.getActivationMessage(
                                AuthFlowManager.AuthFlow.REGISTER, true);

                        Toast.makeText(this, activationMessage, Toast.LENGTH_LONG).show();

                        // ✅ REDIRECT TO ACCOUNT ACTIVATION
                        navigateToAccountActivation(email);

                    } else {
                        // ✅ ACCOUNT ACTIVATED - ACCESS GRANTED
                        Log.d(TAG, "✅ LOGIN: Account activated, email verified - access granted");
                        Log.d(TAG, "✅ FR-1.1.2: Account activation verified, allowing app access");

                        Toast.makeText(this,
                                "Đăng nhập thành công! Chào mừng " + displayName,
                                Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    }

                } else {
                    Log.e(TAG, "❌ Incomplete user data received");
                    Log.e(TAG, "❌ UserID: " + userId + ", Email: " + email + ", DisplayName: " + displayName);
                    showError("Dữ liệu người dùng không đầy đủ");
                }
            } else {
                Log.e(TAG, "❌ Invalid user data format");
                showError("Định dạng dữ liệu không hợp lệ");
            }
            Log.d(TAG, "=== END HANDLE LOGIN SUCCESS - FR-1.1.2 ===");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error processing login success", e);
            showError("Lỗi xử lý đăng nhập: " + e.getMessage());
        }
    }

    /**
     * ✅ Navigate to account activation (email verification từ login)
     */
    private void navigateToAccountActivation(String email) {
        Intent intent = AuthFlowManager.createAccountActivationIntent(this, email);
        startActivity(intent);
        finish(); // ✅ Clear login activity from stack
    }

    /**
     * ✅ Extract user ID helper method
     */
    private Long extractUserId(Map<String, Object> userMap) {
        Object idObj = userMap.get("id");
        if (idObj instanceof Number) {
            return ((Number) idObj).longValue();
        } else if (idObj instanceof String) {
            try {
                return Long.parseLong((String) idObj);
            } catch (NumberFormatException e) {
                Log.e(TAG, "❌ Cannot parse user ID: " + idObj);
            }
        }
        return null;
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
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    private void navigateToForgotPassword() {
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
    }
}