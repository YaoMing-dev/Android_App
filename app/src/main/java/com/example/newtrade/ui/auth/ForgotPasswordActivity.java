// app/src/main/java/com/example/newtrade/ui/auth/ForgotPasswordActivity.java
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

        // ✅ GỬI OTP CHO FORGOT PASSWORD
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

    private void sendOtpForForgotPassword(String email) {
        Log.d(TAG, "🔍 Sending OTP for forgot password: " + email);
        showLoading(true);

        Map<String, String> request = new HashMap<>();
        request.put("email", email);

        // ✅ GỬI OTP CHO FORGOT PASSWORD
        ApiClient.getAuthService().sendOtp(request)
                .enqueue(new Callback<StandardResponse<Map<String, String>>>() {
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
                                showError("Gửi mã OTP thất bại. Vui lòng thử lại.");
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

        // ✅ Navigate to OTP verification cho FORGOT PASSWORD
        navigateToOtpVerification(email);
    }

    private void navigateToOtpVerification(String email) {
        Intent intent = new Intent(this, OtpVerificationActivity.class);
        intent.putExtra("email", email);
        intent.putExtra("fromRegister", false); // ✅ FALSE = forgot password flow
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
        btnReset.setEnabled(!show && ValidationUtils.isValidEmail(etEmail.getText().toString().trim()));
        btnReset.setText(show ? "Đang gửi OTP..." : "Gửi Mã OTP");

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