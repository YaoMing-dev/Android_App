// app/src/main/java/com/example/newtrade/ui/auth/ResetPasswordActivity.java
package com.example.newtrade.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.ValidationUtils;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ResetPasswordActivity";

    // UI Components
    private TextView tvSubtitle;
    private EditText etNewPassword, etConfirmPassword;
    private Button btnResetPassword;
    private TextView tvLogin;
    private LinearLayout llBack;

    // Data
    private String email;
    private boolean fromOtpVerification;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Get data from intent
        getIntentData();

        initViews();
        setupListeners();
        updateSubtitle();

        Log.d(TAG, "ResetPasswordActivity created for email: " + email);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        fromOtpVerification = intent.getBooleanExtra("fromOtpVerification", false);

        // ✅ CHỈ CẦN EMAIL (không cần token nữa vì đã verify OTP)
        if (email == null || email.isEmpty()) {
            Log.e(TAG, "❌ Email not provided in intent");
            Toast.makeText(this, "Lỗi: Không có thông tin email", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Log.d(TAG, "Reset password for email: " + email + ", fromOtpVerification: " + fromOtpVerification);
    }

    private void initViews() {
        tvSubtitle = findViewById(R.id.tv_subtitle);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnResetPassword = findViewById(R.id.btn_reset_password);
        tvLogin = findViewById(R.id.tv_login);
        llBack = findViewById(R.id.ll_back);

        // Initially disable reset button
        btnResetPassword.setEnabled(false);

        // Check if views are found
        if (etNewPassword == null || etConfirmPassword == null || btnResetPassword == null) {
            Log.e(TAG, "❌ Required views not found in layout");
            Toast.makeText(this, "Layout error - missing required fields", Toast.LENGTH_LONG).show();
        }
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

        etNewPassword.addTextChangedListener(textWatcher);
        etConfirmPassword.addTextChangedListener(textWatcher);

        // Click listeners
        btnResetPassword.setOnClickListener(v -> attemptResetPassword());
        tvLogin.setOnClickListener(v -> navigateToLogin());

        if (llBack != null) {
            llBack.setOnClickListener(v -> onBackPressed());
        }
    }

    private void updateSubtitle() {
        if (tvSubtitle != null) {
            tvSubtitle.setText("Nhập mật khẩu mới cho tài khoản:\n" + email);
        }
    }

    private void validateForm() {
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        boolean isValid = ValidationUtils.isValidPassword(newPassword) &&
                ValidationUtils.isPasswordMatch(newPassword, confirmPassword);

        btnResetPassword.setEnabled(isValid && !isLoading);
    }

    private void attemptResetPassword() {
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate new password
        String passwordError = ValidationUtils.getPasswordError(newPassword);
        if (passwordError != null) {
            etNewPassword.setError(passwordError);
            return;
        }

        // Validate confirm password
        String confirmPasswordError = ValidationUtils.getConfirmPasswordError(newPassword, confirmPassword);
        if (confirmPasswordError != null) {
            etConfirmPassword.setError(confirmPasswordError);
            return;
        }

        // Clear errors
        etNewPassword.setError(null);
        etConfirmPassword.setError(null);

        performResetPassword(newPassword);
    }

    private void performResetPassword(String newPassword) {
        Log.d(TAG, "🔍 Performing password reset with email (after OTP verification)");
        showLoading(true);

        Map<String, String> request = new HashMap<>();
        request.put("email", email);
        request.put("newPassword", newPassword);

        // ✅ GỌI ĐÚNG ENDPOINT: /api/auth/reset-password-with-email
        ApiClient.getAuthService().resetPasswordWithEmail(request)
                .enqueue(new Callback<StandardResponse<Map<String, String>>>() {
                    @Override
                    public void onResponse(@NonNull Call<StandardResponse<Map<String, String>>> call,
                                           @NonNull Response<StandardResponse<Map<String, String>>> response) {
                        showLoading(false);

                        Log.d(TAG, "🔍 Reset password with email response code: " + response.code());

                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, String>> apiResponse = response.body();
                            Log.d(TAG, "🔍 Reset password with email response: " + new Gson().toJson(apiResponse));

                            if (apiResponse.isSuccess()) {
                                Log.d(TAG, "✅ Password reset with email successful");
                                handleResetPasswordSuccess(newPassword);
                            } else {
                                Log.e(TAG, "❌ Password reset with email failed: " + apiResponse.getMessage());
                                showError(apiResponse.getMessage() != null ?
                                        apiResponse.getMessage() : "Đặt lại mật khẩu thất bại");
                            }
                        } else {
                            try {
                                if (response.errorBody() != null) {
                                    String errorBody = response.errorBody().string();
                                    Log.e(TAG, "❌ Reset password with email error response: " + errorBody);

                                    StandardResponse<?> errorResponse = new Gson().fromJson(errorBody, StandardResponse.class);
                                    if (errorResponse != null && errorResponse.getMessage() != null) {
                                        showError(errorResponse.getMessage());
                                    } else {
                                        showError("Đặt lại mật khẩu thất bại");
                                    }
                                } else {
                                    showError("Đặt lại mật khẩu thất bại");
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing error response", e);
                                showError("Đặt lại mật khẩu thất bại. Vui lòng thử lại.");
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<StandardResponse<Map<String, String>>> call, @NonNull Throwable t) {
                        showLoading(false);
                        Log.e(TAG, "❌ Reset password with email network error", t);

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

    private void handleResetPasswordSuccess(String newPassword) {
        Log.d(TAG, "✅ Password reset successful");

        // Show success dialog
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("✅ Đặt Lại Mật Khẩu Thành Công!");

        builder.setMessage("Mật khẩu của bạn đã được đặt lại thành công!\n\n" +
                "📧 Email: " + email + "\n" +
                "🔑 Mật khẩu mới: " + newPassword + "\n\n" +
                "Bạn có thể đăng nhập ngay với mật khẩu mới.");

        builder.setPositiveButton("Đăng Nhập Ngay", (dialog, which) -> {
            dialog.dismiss();
            navigateToLoginWithCredentials(email, newPassword);
        });

        builder.setNegativeButton("Về Đăng Nhập", (dialog, which) -> {
            dialog.dismiss();
            navigateToLogin();
        });

        builder.setCancelable(false);
        builder.show();
    }

    private void navigateToLoginWithCredentials(String email, String password) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("prefill_email", email);
        intent.putExtra("prefill_password", password);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        isLoading = show;
        btnResetPassword.setEnabled(!show && isFormValid());
        btnResetPassword.setText(show ? "Đang đặt lại..." : "Đặt Lại Mật Khẩu");

        // Disable inputs during loading
        etNewPassword.setEnabled(!show);
        etConfirmPassword.setEnabled(!show);
    }

    private boolean isFormValid() {
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        return ValidationUtils.isValidPassword(newPassword) &&
                ValidationUtils.isPasswordMatch(newPassword, confirmPassword);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Error: " + message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "ResetPasswordActivity destroyed");
    }
}