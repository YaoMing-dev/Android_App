// app/src/main/java/com/example/newtrade/ui/auth/OtpVerificationActivity.java
package com.example.newtrade.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
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
import com.example.newtrade.utils.ValidationUtils;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtpVerificationActivity extends AppCompatActivity {

    private static final String TAG = "OtpVerificationActivity";
    private static final int RESEND_TIMER_SECONDS = Constants.OTP_RESEND_TIME_SECONDS;

    // UI Components
    private TextView tvSubtitle, tvTimer, tvResend;
    private EditText etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6;
    private Button btnVerify;
    private LinearLayout llBack;

    // Data
    private String email;
    private boolean fromRegister; // ✅ TRUE = register, FALSE = forgot password
    private CountDownTimer resendTimer;
    private boolean isLoading = false;

    // Utils
    private SharedPrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        // Get data from intent
        getIntentData();

        initViews();
        initUtils();
        setupListeners();
        setupOtpInputs();

        // Update subtitle with email
        updateSubtitle();

        // Start resend timer
        startResendTimer();

        // Send OTP automatically
        sendOtp();

        Log.d(TAG, "OtpVerificationActivity created - fromRegister: " + fromRegister + ", email: " + email);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        fromRegister = intent.getBooleanExtra("fromRegister", true); // Default true

        if (email == null || email.isEmpty()) {
            Log.e(TAG, "❌ Email not provided in intent");
            Toast.makeText(this, "Lỗi: Không có email", Toast.LENGTH_LONG).show();
            finish();
        }
        Log.d(TAG, "Email: " + email + ", fromRegister: " + fromRegister);
    }

    private void initViews() {
        tvSubtitle = findViewById(R.id.tv_subtitle);
        tvTimer = findViewById(R.id.tv_timer);
        tvResend = findViewById(R.id.tv_resend);
        etOtp1 = findViewById(R.id.et_otp_1);
        etOtp2 = findViewById(R.id.et_otp_2);
        etOtp3 = findViewById(R.id.et_otp_3);
        etOtp4 = findViewById(R.id.et_otp_4);
        etOtp5 = findViewById(R.id.et_otp_5);
        etOtp6 = findViewById(R.id.et_otp_6);
        btnVerify = findViewById(R.id.btn_verify);
        llBack = findViewById(R.id.ll_back);

        // Initially disable verify button
        btnVerify.setEnabled(false);

        // Check if views are found
        if (etOtp1 == null || etOtp2 == null || etOtp3 == null ||
                etOtp4 == null || etOtp5 == null || etOtp6 == null || btnVerify == null) {
            Log.e(TAG, "❌ Required OTP views not found in layout");
            Toast.makeText(this, "Layout error - missing OTP fields", Toast.LENGTH_LONG).show();
        }
    }

    private void initUtils() {
        prefsManager = SharedPrefsManager.getInstance(this);
    }

    private void setupListeners() {
        btnVerify.setOnClickListener(v -> attemptOtpVerification());
        tvResend.setOnClickListener(v -> resendOtp());

        if (llBack != null) {
            llBack.setOnClickListener(v -> onBackPressed());
        }
    }

    private void setupOtpInputs() {
        EditText[] otpInputs = {etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6};

        for (int i = 0; i < otpInputs.length; i++) {
            final int index = i;
            EditText currentInput = otpInputs[i];

            currentInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1) {
                        // Move to next input
                        if (index < otpInputs.length - 1) {
                            otpInputs[index + 1].requestFocus();
                        }
                    }
                    validateOtp();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            // Handle backspace
            currentInput.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (currentInput.getText().toString().isEmpty() && index > 0) {
                        otpInputs[index - 1].requestFocus();
                    }
                }
                return false;
            });
        }
    }

    private void updateSubtitle() {
        if (tvSubtitle != null) {
            if (fromRegister) {
                tvSubtitle.setText("Chúng tôi đã gửi mã xác thực đến\n" + email + "\nđể xác thực tài khoản của bạn");
            } else {
                // ✅ FORGOT PASSWORD MESSAGE
                tvSubtitle.setText("Chúng tôi đã gửi mã xác thực đến\n" + email + "\nđể khôi phục mật khẩu của bạn");
            }
        }
    }

    private void validateOtp() {
        String otp = getOtpCode();
        btnVerify.setEnabled(!isLoading && ValidationUtils.isValidOtp(otp));
    }

    private String getOtpCode() {
        return etOtp1.getText().toString() +
                etOtp2.getText().toString() +
                etOtp3.getText().toString() +
                etOtp4.getText().toString() +
                etOtp5.getText().toString() +
                etOtp6.getText().toString();
    }

    private void sendOtp() {
        Log.d(TAG, "🔍 Sending OTP to: " + email);

        Map<String, String> request = new HashMap<>();
        request.put("email", email);

        ApiClient.getAuthService().sendOtp(request).enqueue(new Callback<StandardResponse<Map<String, String>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, String>>> call,
                                   @NonNull Response<StandardResponse<Map<String, String>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Map<String, String>> apiResponse = response.body();
                    Log.d(TAG, "✅ OTP sent successfully");
                } else {
                    Log.e(TAG, "❌ Failed to send OTP - Response code: " + response.code());
                    showError("Gửi mã OTP thất bại");
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, String>>> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Send OTP network error", t);
                showError("Lỗi mạng khi gửi OTP");
            }
        });
    }

    private void attemptOtpVerification() {
        String otp = getOtpCode();

        // Validate OTP
        String otpError = ValidationUtils.getOtpError(otp);
        if (otpError != null) {
            showError(otpError);
            return;
        }

        performOtpVerification(otp);
    }

    private void performOtpVerification(String otp) {
        Log.d(TAG, "🔍 Verifying OTP for: " + email);

        showLoading(true);

        Map<String, String> request = new HashMap<>();
        request.put("email", email);
        request.put("otpCode", otp);

        ApiClient.getAuthService().verifyOtp(request).enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                showLoading(false);

                Log.d(TAG, "🔍 OTP verification response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Map<String, Object>> apiResponse = response.body();
                    Log.d(TAG, "🔍 OTP verification response: " + new Gson().toJson(apiResponse));

                    if (apiResponse.isSuccess() && apiResponse.hasData()) {
                        handleOtpVerificationSuccess(apiResponse.getData());
                    } else {
                        showError(apiResponse.getMessage() != null ? apiResponse.getMessage() : "Xác thực OTP thất bại");
                    }
                } else {
                    Log.e(TAG, "❌ OTP verification failed - Response code: " + response.code());
                    showError("Mã OTP không chính xác hoặc đã hết hạn");
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e(TAG, "❌ OTP verification network error", t);
                showError("Lỗi mạng khi xác thực OTP");
            }
        });
    }

    private void resendOtp() {
        Log.d(TAG, "Resending OTP");

        Map<String, String> request = new HashMap<>();
        request.put("email", email);

        ApiClient.getAuthService().resendOtp(request).enqueue(new Callback<StandardResponse<Map<String, String>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, String>>> call,
                                   @NonNull Response<StandardResponse<Map<String, String>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Map<String, String>> apiResponse = response.body();

                    if (apiResponse.isSuccess()) {
                        Toast.makeText(OtpVerificationActivity.this, "Mã OTP đã được gửi lại", Toast.LENGTH_SHORT).show();
                        startResendTimer();
                        clearOtpInputs();
                    } else {
                        showError(apiResponse.getMessage() != null ? apiResponse.getMessage() : "Gửi lại OTP thất bại");
                    }
                } else {
                    showError("Gửi lại OTP thất bại");
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, String>>> call, @NonNull Throwable t) {
                Log.e(TAG, "Resend OTP network error", t);
                showError("Lỗi mạng khi gửi lại OTP");
            }
        });
    }

    private void handleOtpVerificationSuccess(Map<String, Object> data) {
        Log.d(TAG, "✅ OTP verification successful");

        if (fromRegister) {
            // ✅ REGISTER FLOW: OTP verify → MainActivity
            Toast.makeText(this, "Xác thực email thành công! Chào mừng bạn đến với TradeUp.", Toast.LENGTH_LONG).show();
            navigateToMain();
        } else {
            // ✅ FORGOT PASSWORD FLOW: OTP verify → ResetPasswordActivity
            Toast.makeText(this, "Xác thực OTP thành công! Vui lòng tạo mật khẩu mới.", Toast.LENGTH_SHORT).show();
            navigateToResetPassword();
        }
    }

    // ✅ NAVIGATE TO RESET PASSWORD (không cần token)
    private void navigateToResetPassword() {
        Intent intent = new Intent(this, ResetPasswordActivity.class);
        intent.putExtra("email", email);
        intent.putExtra("fromOtpVerification", true); // ✅ Đánh dấu từ OTP verification
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

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void startResendTimer() {
        if (resendTimer != null) {
            resendTimer.cancel();
        }

        tvResend.setEnabled(false);
        tvResend.setText("Gửi lại (" + RESEND_TIMER_SECONDS + "s)");

        resendTimer = new CountDownTimer(RESEND_TIMER_SECONDS * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                tvResend.setText("Gửi lại (" + seconds + "s)");
            }

            @Override
            public void onFinish() {
                tvResend.setEnabled(true);
                tvResend.setText("Gửi lại mã");
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

    private void showLoading(boolean show) {
        isLoading = show;
        btnVerify.setEnabled(!show && ValidationUtils.isValidOtp(getOtpCode()));
        btnVerify.setText(show ? "Đang xác thực..." : "Xác thực");

        // Disable OTP inputs during loading
        etOtp1.setEnabled(!show);
        etOtp2.setEnabled(!show);
        etOtp3.setEnabled(!show);
        etOtp4.setEnabled(!show);
        etOtp5.setEnabled(!show);
        etOtp6.setEnabled(!show);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Error: " + message);
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
            // From register → go back to login
            navigateToLogin();
        } else {
            // From forgot password → go back to login
            navigateToLogin();
        }
    }
}