// app/src/main/java/com/example/newtrade/ui/auth/OtpVerificationActivity.java
package com.example.newtrade.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtpVerificationActivity extends AppCompatActivity {

    private static final String TAG = "OtpVerificationActivity";

    // UI Components - ✅ FIXED IDs to match layout
    private TextView tvSubtitle, tvTimer, tvResend;
    private EditText etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6;
    private Button btnVerify;
    private LinearLayout llBack;
    // ❌ REMOVED: progressBar (not in layout)

    // Data
    private String email;
    private boolean fromRegister;
    private CountDownTimer resendTimer;
    private boolean isLoading = false;

    // Utils
    private SharedPrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        // Get data from intent
        email = getIntent().getStringExtra("email");
        fromRegister = getIntent().getBooleanExtra("from_register", false);

        if (email == null || email.isEmpty()) {
            showError("Email không hợp lệ");
            finish();
            return;
        }

        initViews();
        initUtils();
        setupListeners();
        setupOtpInputs();
        startResendTimer();

        Log.d(TAG, "OtpVerificationActivity created for email: " + email);
    }

    private void initViews() {
        tvSubtitle = findViewById(R.id.tv_subtitle); // ✅ FIXED: was tv_email
        tvTimer = findViewById(R.id.tv_timer);       // ✅ FIXED: was tv_resend_timer
        tvResend = findViewById(R.id.tv_resend);
        etOtp1 = findViewById(R.id.et_otp_1);
        etOtp2 = findViewById(R.id.et_otp_2);
        etOtp3 = findViewById(R.id.et_otp_3);
        etOtp4 = findViewById(R.id.et_otp_4);
        etOtp5 = findViewById(R.id.et_otp_5);
        etOtp6 = findViewById(R.id.et_otp_6);
        btnVerify = findViewById(R.id.btn_verify);
        llBack = findViewById(R.id.ll_back);

        // Set email in subtitle
        tvSubtitle.setText("We've sent a 6-digit code to\n" + email);

        // Initially disable verify button
        btnVerify.setEnabled(false);
    }

    private void initUtils() {
        prefsManager = SharedPrefsManager.getInstance(this);
    }

    private void setupListeners() {
        btnVerify.setOnClickListener(v -> attemptVerifyOtp());
        tvResend.setOnClickListener(v -> resendOtp());
        llBack.setOnClickListener(v -> onBackPressed());
    }

    private void setupOtpInputs() {
        EditText[] otpInputs = {etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6};

        for (int i = 0; i < otpInputs.length; i++) {
            final int index = i;
            otpInputs[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1) {
                        // Move to next input
                        if (index < otpInputs.length - 1) {
                            otpInputs[index + 1].requestFocus();
                        }
                    } else if (s.length() == 0) {
                        // Move to previous input
                        if (index > 0) {
                            otpInputs[index - 1].requestFocus();
                        }
                    }

                    validateOtp();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void validateOtp() {
        String otp = getOtpCode();
        btnVerify.setEnabled(otp.length() == Constants.OTP_LENGTH && !isLoading);
    }

    private String getOtpCode() {
        return etOtp1.getText().toString() +
                etOtp2.getText().toString() +
                etOtp3.getText().toString() +
                etOtp4.getText().toString() +
                etOtp5.getText().toString() +
                etOtp6.getText().toString();
    }

    private void attemptVerifyOtp() {
        String otpCode = getOtpCode();

        if (otpCode.length() != Constants.OTP_LENGTH) {
            showError("Vui lòng nhập đầy đủ mã OTP");
            return;
        }

        performVerifyOtp(otpCode);
    }

    private void performVerifyOtp(String otpCode) {
        showLoading(true);

        Map<String, String> request = new HashMap<>();
        request.put("email", email);
        request.put("otpCode", otpCode);

        ApiClient.getAuthService().verifyOtp(request).enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Map<String, Object>> apiResponse = response.body();

                    if (apiResponse.isSuccess()) {
                        handleVerificationSuccess(apiResponse.getData());
                    } else {
                        showError(apiResponse.getMessage());
                        clearOtpInputs();
                    }
                } else {
                    showError("Xác thực OTP thất bại");
                    clearOtpInputs();
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e(TAG, "OTP verification failed", t);
                showError(ApiClient.getErrorMessage(t));
                clearOtpInputs();
            }
        });
    }

    private void handleVerificationSuccess(Map<String, Object> data) {
        Log.d(TAG, "OTP verification successful for email: " + email);

        // Mark email as verified
        prefsManager.setEmailVerified(true);

        Toast.makeText(this, "Xác thực email thành công!", Toast.LENGTH_SHORT).show();

        if (fromRegister) {
            // If coming from registration, navigate to login
            navigateToLogin();
        } else {
            // If coming from login, navigate to main
            navigateToMain();
        }
    }

    private void resendOtp() {
        Map<String, String> request = new HashMap<>();
        request.put("email", email);

        ApiClient.getAuthService().resendOtp(request).enqueue(new Callback<StandardResponse<String>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<String>> call,
                                   @NonNull Response<StandardResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<String> apiResponse = response.body();

                    if (apiResponse.isSuccess()) {
                        showError("Mã OTP mới đã được gửi");
                        startResendTimer();
                        clearOtpInputs();
                    } else {
                        showError(apiResponse.getMessage());
                    }
                } else {
                    showError("Không thể gửi lại OTP");
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<String>> call, @NonNull Throwable t) {
                Log.e(TAG, "Resend OTP failed", t);
                showError("Không thể gửi lại OTP");
            }
        });
    }

    private void startResendTimer() {
        tvResend.setVisibility(View.GONE);
        tvTimer.setVisibility(View.VISIBLE);

        if (resendTimer != null) {
            resendTimer.cancel();
        }

        resendTimer = new CountDownTimer(Constants.OTP_RESEND_TIME_SECONDS * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                tvTimer.setText("Resend code in " + String.format("%02d:%02d", seconds / 60, seconds % 60));
            }

            @Override
            public void onFinish() {
                tvTimer.setVisibility(View.GONE);
                tvResend.setVisibility(View.VISIBLE);
            }
        };

        resendTimer.start();
    }

    private void clearOtpInputs() {
        etOtp1.setText("");
        etOtp2.setText("");
        etOtp3.setText("");
        etOtp4.setText("");
        etOtp5.setText("");
        etOtp6.setText("");
        etOtp1.requestFocus();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ✅ FIXED: No more progressBar - use button state instead
    private void showLoading(boolean show) {
        isLoading = show;
        btnVerify.setEnabled(!show && getOtpCode().length() == Constants.OTP_LENGTH);
        btnVerify.setText(show ? "Đang xác thực..." : "Verify Code");

        // Disable all OTP inputs during loading
        etOtp1.setEnabled(!show);
        etOtp2.setEnabled(!show);
        etOtp3.setEnabled(!show);
        etOtp4.setEnabled(!show);
        etOtp5.setEnabled(!show);
        etOtp6.setEnabled(!show);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (resendTimer != null) {
            resendTimer.cancel();
        }
        Log.d(TAG, "OtpVerificationActivity destroyed");
    }

    @Override
    public void onBackPressed() {
        if (fromRegister) {
            navigateToLogin();
        } else {
            super.onBackPressed();
        }
    }
}