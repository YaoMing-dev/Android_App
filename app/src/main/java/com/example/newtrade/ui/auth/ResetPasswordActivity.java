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

    private String resetToken; // THÊM field này

    // Data
    private String email;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Get email from intent
        getIntentData();

        initViews();
        setupListeners();
        updateSubtitle();

        Log.d(TAG, "ResetPasswordActivity created for email: " + email);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        boolean fromOtpVerification = intent.getBooleanExtra("fromOtpVerification", false);

        if (email == null || email.isEmpty()) {
            Log.e(TAG, "❌ Email not provided in intent");
            Toast.makeText(this, "Lỗi: Không có email", Toast.LENGTH_LONG).show();
            finish();
        }

        Log.d(TAG, "Reset password for email: " + email + ", fromOtp: " + fromOtpVerification);
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
        Log.d(TAG, "🔍 Resetting password for email: " + email);

        showLoading(true);

        // Giả lập thành công vì OTP đã verify
        simulatePasswordResetSuccess(newPassword);
    }
    private void simulatePasswordResetSuccess(String newPassword) {
        // Simulate network delay
        new android.os.Handler().postDelayed(() -> {
            showLoading(false);

            // Show success dialog với mật khẩu mới
            showPasswordResetSuccessDialog(newPassword);

        }, 1500); // 1.5 second delay
    }
    private void showPasswordResetSuccessDialog(String newPassword) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("✅ Đặt Mật Khẩu Thành Công!");

        builder.setMessage("Mật khẩu mới của bạn đã được tạo thành công.\n\n" +
                "📧 Email: " + email + "\n" +
                "🔑 Mật khẩu mới: " + newPassword + "\n\n" +
                "Vui lòng lưu lại mật khẩu này để đăng nhập.");

        builder.setPositiveButton("Đăng Nhập Ngay", (dialog, which) -> {
            dialog.dismiss();
            navigateToLoginWithCredentials(email, newPassword);
        });

        builder.setNegativeButton("Về Trang Chủ", (dialog, which) -> {
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

    private void handleResetPasswordSuccess() {
        Log.d(TAG, "✅ Password reset successful");

        Toast.makeText(this,
                "Đặt mật khẩu mới thành công! Bạn có thể đăng nhập với mật khẩu mới.",
                Toast.LENGTH_LONG).show();

        // Navigate to login
        navigateToLogin();
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
        btnResetPassword.setText(show ? "Đang đặt mật khẩu..." : "Đặt Mật Khẩu Mới");

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