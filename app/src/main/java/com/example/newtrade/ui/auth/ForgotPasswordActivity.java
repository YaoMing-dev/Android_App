// ===== SỬA ForgotPasswordActivity.java =====
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

public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ForgotPasswordActivity";

    // UI Components
    private EditText etEmail;
    private Button btnReset;
    private TextView tvLogin;
    private LinearLayout llBack;

    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initViews();
        setupListeners();

        Log.d(TAG, "ForgotPasswordActivity created");
    }

    private void initViews() {
        etEmail = findViewById(R.id.et_email);
        btnReset = findViewById(R.id.btn_reset);
        tvLogin = findViewById(R.id.tv_login);
        llBack = findViewById(R.id.ll_back);

        // Initially disable reset button
        btnReset.setEnabled(false);
    }

    private void setupListeners() {
        // Email text change listener
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateForm();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // ===== SỬA: Gọi sendOtp thay vì password-reset-request =====
        btnReset.setOnClickListener(v -> attemptSendOtpForForgotPassword());
        tvLogin.setOnClickListener(v -> navigateToLogin());

        if (llBack != null) {
            llBack.setOnClickListener(v -> onBackPressed());
        }
    }

    private void validateForm() {
        String email = etEmail.getText().toString().trim();
        btnReset.setEnabled(ValidationUtils.isValidEmail(email) && !isLoading);
    }

    // ===== SỬA METHOD NÀY =====
    private void attemptPasswordResetRequest() {
        String email = etEmail.getText().toString().trim();

        // Validate email
        String emailError = ValidationUtils.getEmailError(email);
        if (emailError != null) {
            etEmail.setError(emailError);
            return;
        }

        // Clear error
        etEmail.setError(null);

        sendPasswordResetRequest(email);
    }

    // ===== SỬA METHOD NÀY =====
    private void sendPasswordResetRequest(String email) {
        Log.d(TAG, "🔍 Sending password reset request: " + email);

        showLoading(true);

        // ===== SỬA: Dùng email parameter trực tiếp thay vì Map =====
        ApiClient.getAuthService().passwordResetRequest(email).enqueue(new Callback<StandardResponse<Map<String, String>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, String>>> call,
                                   @NonNull Response<StandardResponse<Map<String, String>>> response) {
                showLoading(false);

                Log.d(TAG, "🔍 Password reset request response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Map<String, String>> apiResponse = response.body();
                    Log.d(TAG, "🔍 Password reset request response: " + new Gson().toJson(apiResponse));

                    if (apiResponse.isSuccess() && apiResponse.hasData()) {
                        // Lấy token từ response
                        Map<String, String> responseData = apiResponse.getData();
                        String resetToken = responseData.get("token");

                        if (resetToken != null && !resetToken.isEmpty()) {
                            handlePasswordResetRequestSuccess(email, resetToken);
                        } else {
                            Log.e(TAG, "No token in password reset response");
                            Log.d(TAG, "Available keys in response: " + responseData.keySet());
                            showError("Không nhận được token reset. Thử lại sau.");
                        }
                    } else {
                        showError(apiResponse.getMessage() != null ?
                                apiResponse.getMessage() : "Gửi yêu cầu reset thất bại");
                    }
                } else {
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "❌ Password reset request error response: " + errorBody);

                            StandardResponse<?> errorResponse = new Gson().fromJson(errorBody, StandardResponse.class);
                            if (errorResponse != null && errorResponse.getMessage() != null) {
                                showError(errorResponse.getMessage());
                            } else {
                                showError("Email không tồn tại trong hệ thống");
                            }
                        } else {
                            showError("Email không tồn tại trong hệ thống");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error response", e);
                        showError("Gửi yêu cầu reset thất bại. Thử lại sau.");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, String>>> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e(TAG, "❌ Password reset request network error", t);

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

    // ===== THÊM METHOD MỚI =====
    private void handlePasswordResetRequestSuccess(String email, String resetToken) {
        Log.d(TAG, "✅ Password reset request successful, token received: " + resetToken);

        Toast.makeText(this,
                "Yêu cầu reset mật khẩu thành công! Bạn có thể tạo mật khẩu mới.",
                Toast.LENGTH_LONG).show();

        // Navigate directly to reset password với token
        navigateToResetPassword(email, resetToken);
    }

    // ===== THÊM METHOD MỚI =====
    private void navigateToResetPassword(String email, String resetToken) {
        Intent intent = new Intent(this, ResetPasswordActivity.class);
        intent.putExtra("email", email);
        intent.putExtra("resetToken", resetToken);
        startActivity(intent);
        finish();
    }


    private void sendOtpForForgotPassword(String email) {
        Log.d(TAG, "🔍 Sending OTP for forgot password: " + email);

        showLoading(true);

        Map<String, String> request = new HashMap<>();
        request.put("email", email);

        // ===== QUAN TRỌNG: Dùng sendOtp thay vì passwordResetRequest =====
        ApiClient.getAuthService().sendOtp(request).enqueue(new Callback<StandardResponse<Map<String, String>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, String>>> call,
                                   @NonNull Response<StandardResponse<Map<String, String>>> response) {
                showLoading(false);

                Log.d(TAG, "🔍 Send OTP response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Map<String, String>> apiResponse = response.body();
                    Log.d(TAG, "🔍 Send OTP response: " + new Gson().toJson(apiResponse));

                    if (apiResponse.isSuccess()) {
                        handleOtpSentSuccess(email);
                    } else {
                        showError(apiResponse.getMessage() != null ?
                                apiResponse.getMessage() : "Gửi mã OTP thất bại");
                    }
                } else {
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "❌ Send OTP error response: " + errorBody);

                            StandardResponse<?> errorResponse = new Gson().fromJson(errorBody, StandardResponse.class);
                            if (errorResponse != null && errorResponse.getMessage() != null) {
                                showError(errorResponse.getMessage());
                            } else {
                                showError("Email không tồn tại trong hệ thống");
                            }
                        } else {
                            showError("Email không tồn tại trong hệ thống");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error response", e);
                        showError("Gửi mã OTP thất bại. Thử lại sau.");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, String>>> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e(TAG, "❌ Send OTP network error", t);

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

    private void handleOtpSentSuccess(String email) {
        Log.d(TAG, "✅ OTP sent successfully for forgot password");

        Toast.makeText(this,
                "Mã OTP đã được gửi đến email " + email + ". Vui lòng kiểm tra hộp thư.",
                Toast.LENGTH_LONG).show();

        // Navigate to OTP verification for forgot password
        navigateToOtpVerification(email);
    }

    private void navigateToOtpVerification(String email) {
        Intent intent = new Intent(this, OtpVerificationActivity.class);
        intent.putExtra("email", email);
        intent.putExtra("fromRegister", false); // FALSE vì đây là forgot password
        startActivity(intent);
        finish();
    }

    // ===== CÁC METHOD KHÁC GIỮ NGUYÊN =====
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void attemptSendOtpForForgotPassword() {
        String email = etEmail.getText().toString().trim();

        // Validate email
        String emailError = ValidationUtils.getEmailError(email);
        if (emailError != null) {
            etEmail.setError(emailError);
            return;
        }

        // Clear error
        etEmail.setError(null);

        sendOtpForForgotPassword(email);
    }

    private void showLoading(boolean show) {
        isLoading = show;
        btnReset.setEnabled(!show && ValidationUtils.isValidEmail(etEmail.getText().toString().trim()));
        btnReset.setText(show ? "Đang gửi..." : "Reset Mật Khẩu"); // SỬA text

        // Disable input during loading
        etEmail.setEnabled(!show);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Error: " + message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "ForgotPasswordActivity destroyed");
    }
}