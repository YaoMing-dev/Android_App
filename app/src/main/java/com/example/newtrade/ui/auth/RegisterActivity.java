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

import com.example.newtrade.MainActivity;
import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.AuthFlowManager;
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

        Log.d(TAG, "RegisterActivity created - FR-1.1.2: Email verification required for account activation");
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

        // Check terms agreement
        if (!cbTerms.isChecked()) {
            showError("Vui lòng đồng ý với điều khoản sử dụng");
            return;
        }

        // Clear errors
        etDisplayName.setError(null);
        etEmail.setError(null);
        etPassword.setError(null);
        etConfirmPassword.setError(null);

        performRegister(displayName, email, password);
    }

    private void performRegister(String displayName, String email, String password) {
        Log.d(TAG, "🔍 Registering user: " + email);
        Log.d(TAG, "🔍 FR-1.1.2: Will require email verification for account activation");

        showLoading(true);

        Map<String, String> request = new HashMap<>();
        request.put("displayName", displayName);
        request.put("email", email);
        request.put("password", password);

        ApiClient.getAuthService().register(request).enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                showLoading(false);

                Log.d(TAG, "🔍 Register response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Map<String, Object>> apiResponse = response.body();
                    Log.d(TAG, "🔍 Register response: " + new Gson().toJson(apiResponse));

                    if (apiResponse.isSuccess() && apiResponse.hasData()) {
                        handleRegisterSuccess(apiResponse.getData(), email);
                    } else {
                        showError(apiResponse.getMessage() != null ?
                                apiResponse.getMessage() : "Đăng ký thất bại");
                    }
                } else {
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "❌ Register error response: " + errorBody);

                            StandardResponse<?> errorResponse = new Gson().fromJson(errorBody, StandardResponse.class);
                            if (errorResponse != null && errorResponse.getMessage() != null) {
                                showError(errorResponse.getMessage());
                            } else {
                                showError("Đăng ký thất bại. Thử lại sau.");
                            }
                        } else {
                            showError("Đăng ký thất bại. Thử lại sau.");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error response", e);
                        showError("Đăng ký thất bại. Thử lại sau.");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e(TAG, "❌ Register network error", t);

                if (t instanceof java.net.ConnectException) {
                    showError("Không thể kết nối đến server. Kiểm tra kết nối mạng.");
                } else if (t instanceof java.net.SocketTimeoutException) {
                    showError("Kết nối timeout. Thử lại sau.");
                } else {
                    showError("Lỗi mạng: " + t.getMessage());
                }
            }
        });
    }

    /**
     * ✅ Handle register success với FR-1.1.2 compliance
     */
    private void handleRegisterSuccess(Map<String, Object> responseData, String email) {
        Log.d(TAG, "✅ Registration successful - implementing FR-1.1.2 email verification requirement");

        try {
            // Parse user object từ response
            Object userObj = responseData.get("user");
            if (userObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> userMap = (Map<String, Object>) userObj;

                // Extract user info
                Long userId = extractUserId(userMap);
                String userEmail = (String) userMap.get("email");
                String displayName = (String) userMap.get("displayName");
                Boolean isEmailVerified = (Boolean) userMap.get("isEmailVerified");

                Log.d(TAG, "🔍 REGISTER RESPONSE PARSED:");
                Log.d(TAG, "🔍 User ID: " + userId);
                Log.d(TAG, "🔍 Email: " + userEmail);
                Log.d(TAG, "🔍 Display Name: " + displayName);
                Log.d(TAG, "🔍 Email Verified: " + isEmailVerified);

                if (userId != null && userEmail != null && displayName != null) {
                    // ✅ SAVE USER SESSION (but mark as not activated yet)
                    prefsManager.saveUserSession(userId, userEmail, displayName,
                            isEmailVerified != null ? isEmailVerified : false);

                    // ✅ FR-1.1.2: EMAIL VERIFICATION REQUIRED for account activation
                    boolean emailVerified = Boolean.TRUE.equals(isEmailVerified);

                    // Log flow information
                    AuthFlowManager.logFlowInfo(AuthFlowManager.AuthFlow.REGISTER, userEmail, emailVerified);

                    if (AuthFlowManager.requiresEmailVerification(AuthFlowManager.AuthFlow.REGISTER, emailVerified)) {
                        // ✅ ACCOUNT ACTIVATION REQUIRED - FR-1.1.2 compliance
                        Log.d(TAG, "✅ FR-1.1.2: Email verification required for account activation");
                        Log.d(TAG, "✅ Redirecting to email verification to activate account");

                        Toast.makeText(this,
                                "🎉 Đăng ký thành công!\n\n" +
                                        "📧 Vui lòng xác thực email để kích hoạt tài khoản và bắt đầu sử dụng TradeUp.",
                                Toast.LENGTH_LONG).show();

                        navigateToEmailVerification(userEmail);
                    } else {
                        // ✅ ALREADY VERIFIED (ít khi xảy ra trong register flow)
                        Log.d(TAG, "✅ Email already verified during registration, activating account");

                        Toast.makeText(this,
                                "🎉 Đăng ký và kích hoạt tài khoản thành công!\nChào mừng " + displayName + " đến với TradeUp!",
                                Toast.LENGTH_LONG).show();

                        navigateToMain();
                    }
                } else {
                    Log.e(TAG, "❌ Invalid user data in register response");
                    showError("Đăng ký thành công nhưng không thể lưu thông tin.\nVui lòng đăng nhập lại để tiếp tục.");
                    navigateToLogin();
                }
            } else {
                Log.e(TAG, "❌ User object not found in register response");
                showError("Đăng ký thành công nhưng không thể lấy thông tin user.\nVui lòng đăng nhập lại để tiếp tục.");
                navigateToLogin();
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error parsing register success data", e);
            showError("Đăng ký thành công nhưng có lỗi xử lý dữ liệu.\nVui lòng đăng nhập lại để tiếp tục.");
            navigateToLogin();
        }
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
                Log.e(TAG, "Cannot parse user ID: " + idObj);
            }
        }
        return null;
    }

    /**
     * ✅ Navigate to email verification cho register flow
     */
    private void navigateToEmailVerification(String email) {
        Intent intent = AuthFlowManager.createOtpIntent(this, email, AuthFlowManager.AuthFlow.REGISTER);
        startActivity(intent);
        finish();
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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