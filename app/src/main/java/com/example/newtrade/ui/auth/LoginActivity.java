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

        // Initialize
        initViews();
        initGoogleSignIn();
        initUtils();
        setupListeners();

        // Check if already logged in
        if (prefsManager.isLoggedIn()) {
            navigateToMain();
            return;
        }

        Log.d(TAG, "LoginActivity created");
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
    }

    private void initGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(Constants.GOOGLE_CLIENT_ID)
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
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

        etEmail.addTextChangedListener(textWatcher);
        etPassword.addTextChangedListener(textWatcher);

        // Click listeners
        btnLogin.setOnClickListener(v -> attemptLogin());
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
        tvRegister.setOnClickListener(v -> navigateToRegister());
        tvForgotPassword.setOnClickListener(v -> navigateToForgotPassword());
    }

    private void validateForm() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        boolean isValid = ValidationUtils.isValidEmail(email)
                && ValidationUtils.isValidPassword(password);

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
        showLoading(true);

        Map<String, String> request = new HashMap<>();
        request.put("email", email);
        request.put("password", password);

        ApiClient.getAuthService().login(request).enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Map<String, Object>> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.hasData()) {
                        Map<String, Object> data = apiResponse.getData();

                        // Parse user data (assuming it's in the response)
                        if (data.containsKey("user")) {
                            // Handle successful login
                            handleLoginSuccess(data);
                        } else {
                            showError("Dữ liệu người dùng không hợp lệ");
                        }
                    } else {
                        showError(apiResponse.getMessage());
                    }
                } else {
                    try {
                        String errorMessage = "Đăng nhập thất bại";
                        if (response.errorBody() != null) {
                            // Try to parse error response
                            errorMessage = "Email hoặc mật khẩu không đúng";
                        }
                        showError(errorMessage);
                    } catch (Exception e) {
                        showError("Đăng nhập thất bại");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e(TAG, "Login failed", t);
                showError(ApiClient.getErrorMessage(t));
            }
        });
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, Constants.RC_GOOGLE_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
        }
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken();

            if (idToken != null) {
                performGoogleSignIn(idToken);
            } else {
                showError("Không thể lấy Google ID token");
            }
        } catch (ApiException e) {
            Log.w(TAG, "Google sign in failed", e);
            showError("Google đăng nhập thất bại: " + e.getMessage());
        }
    }

    private void performGoogleSignIn(String idToken) {
        showLoading(true);

        Map<String, String> request = new HashMap<>();
        request.put("idToken", idToken);

        ApiClient.getAuthService().googleSignIn(request).enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Map<String, Object>> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.hasData()) {
                        handleLoginSuccess(apiResponse.getData());
                    } else {
                        showError(apiResponse.getMessage());
                    }
                } else {
                    showError("Google đăng nhập thất bại");
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e(TAG, "Google sign in failed", t);
                showError(ApiClient.getErrorMessage(t));
            }
        });
    }

    private void handleLoginSuccess(Map<String, Object> data) {
        try {
            // Extract user data from response
            Map<String, Object> userData = (Map<String, Object>) data.get("user");

            if (userData != null) {
                Long userId = ((Number) userData.get("id")).longValue();
                String email = (String) userData.get("email");
                String displayName = (String) userData.get("displayName");
                Boolean isEmailVerified = (Boolean) userData.get("isEmailVerified");

                // Save user session
                prefsManager.saveUserSession(userId, email, displayName,
                        isEmailVerified != null ? isEmailVerified : false);

                Log.d(TAG, "Login successful for user: " + displayName);

                // Check if email verification is required
                Boolean requiresOtp = (Boolean) data.get("requiresOtp");
                if (requiresOtp != null && requiresOtp) {
                    navigateToOtpVerification(email);
                } else {
                    navigateToMain();
                }
            } else {
                showError("Dữ liệu người dùng không hợp lệ");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing login response", e);
            showError("Lỗi xử lý dữ liệu đăng nhập");
        }
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
        startActivity(intent);
    }

    private void showLoading(boolean show) {
        isLoading = show;
        btnLogin.setEnabled(!show && isFormValid());
        btnLogin.setText(show ? "Đang đăng nhập..." : "Sign In");
        btnGoogleSignIn.setEnabled(!show);

        // Disable inputs during loading
        etEmail.setEnabled(!show);
        etPassword.setEnabled(!show);
    }

    private boolean isFormValid() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        return ValidationUtils.isValidEmail(email)
                && ValidationUtils.isValidPassword(password);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "LoginActivity destroyed");
    }
}