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
import com.example.newtrade.utils.AuthFlowManager;
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
    private boolean isAccountActivation; // ✅ Account activation từ login
    private boolean fromLogin; // ✅ Đến từ login
    private AuthFlowManager.AuthFlow currentFlow; // ✅ Track current flow
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

        // Update subtitle with email and context
        updateSubtitle();

        // Start resend timer
        startResendTimer();

        // Send OTP automatically
        sendOtp();

        Log.d(TAG, "OtpVerificationActivity created - Flow: " + currentFlow +
                ", Email: " + email +
                ", IsAccountActivation: " + isAccountActivation +
                ", FromLogin: " + fromLogin);
    }

    /**
     * ✅ Get intent data với support cho account activation
     */
    private void getIntentData() {
        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        fromRegister = intent.getBooleanExtra("fromRegister", true);
        isAccountActivation = intent.getBooleanExtra("isAccountActivation", false); // ✅ Account activation
        fromLogin = intent.getBooleanExtra("fromLogin", false); // ✅ Từ login

        // ✅ Determine current flow based on context
        if (AuthFlowManager.isAccountActivationFromLogin(isAccountActivation, fromLogin)) {
            // ✅ ACCOUNT ACTIVATION từ login
            currentFlow = AuthFlowManager.AuthFlow.REGISTER; // Use REGISTER flow for activation
            Log.d(TAG, "✅ Detected: Account activation from login");
        } else if (fromRegister && !fromLogin) {
            // ✅ REGISTER FLOW bình thường
            currentFlow = AuthFlowManager.AuthFlow.REGISTER;
            Log.d(TAG, "✅ Detected: Normal register flow");
        } else {
            // ✅ FORGOT PASSWORD FLOW
            currentFlow = AuthFlowManager.AuthFlow.FORGOT_PASSWORD;
            Log.d(TAG, "✅ Detected: Forgot password flow");
        }

        // ✅ Validate required data
        if (email == null || email.isEmpty()) {
            Log.e(TAG, "❌ Email not provided in intent");
            Toast.makeText(this, "Lỗi: Không có email", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (!AuthFlowManager.isValidFlowData(email, currentFlow)) {
            Log.e(TAG, "❌ Invalid flow data");
            Toast.makeText(this, "Lỗi: Dữ liệu flow không hợp lệ", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // ✅ Log flow information for debugging
        AuthFlowManager.logFlowInfo(currentFlow, email, false); // false vì đang cần verify

        Log.d(TAG, "✅ Flow Context:");
        Log.d(TAG, "✅ - Flow: " + AuthFlowManager.getFlowDescription(currentFlow));
        Log.d(TAG, "✅ - Email: " + email);
        Log.d(TAG, "✅ - FromRegister: " + fromRegister);
        Log.d(TAG, "✅ - IsAccountActivation: " + isAccountActivation);
        Log.d(TAG, "✅ - FromLogin: " + fromLogin);
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

    /**
     * ✅ Update subtitle theo flow và context
     */
    private void updateSubtitle() {
        if (tvSubtitle != null) {
            String subtitle;

            if (AuthFlowManager.isAccountActivationFromLogin(isAccountActivation, fromLogin)) {
                // ✅ ACCOUNT ACTIVATION từ login - FR-1.1.2
                subtitle = "🔐 Tài khoản chưa được kích hoạt!\n\n" +
                        "Chúng tôi đã gửi mã xác thực đến:\n" + email +
                        "\n\nVui lòng nhập mã để kích hoạt tài khoản và tiếp tục sử dụng app.";
            } else {
                // ✅ CÁC FLOW KHÁC
                switch (currentFlow) {
                    case REGISTER:
                        subtitle = "📧 Xác thực email để kích hoạt tài khoản\n\n" +
                                "Chúng tôi đã gửi mã xác thực đến:\n" + email +
                                "\n\nVui lòng nhập mã để kích hoạt tài khoản của bạn.";
                        break;

                    case FORGOT_PASSWORD:
                        subtitle = "🔑 Khôi phục mật khẩu\n\n" +
                                "Chúng tôi đã gửi mã xác thực đến:\n" + email +
                                "\n\nVui lòng nhập mã để tiếp tục đặt lại mật khẩu.";
                        break;

                    default:
                        subtitle = "Chúng tôi đã gửi mã xác thực đến:\n" + email;
                        break;
                }
            }
            tvSubtitle.setText(subtitle);
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
        Log.d(TAG, "🔍 Sending OTP for " + currentFlow + " to: " + email);

        if (isAccountActivation && fromLogin) {
            Log.d(TAG, "🔍 Context: Account activation from login (FR-1.1.2)");
        }

        Map<String, String> request = new HashMap<>();
        request.put("email", email);

        ApiClient.getAuthService().sendOtp(request).enqueue(new Callback<StandardResponse<Map<String, String>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, String>>> call,
                                   @NonNull Response<StandardResponse<Map<String, String>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<Map<String, String>> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Log.d(TAG, "✅ OTP sent successfully for " + currentFlow);
                    } else {
                        Log.e(TAG, "❌ Failed to send OTP: " + apiResponse.getMessage());
                        showError("Gửi mã OTP thất bại: " + apiResponse.getMessage());
                    }
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
        Log.d(TAG, "🔍 Verifying OTP for " + currentFlow + ": " + email);

        if (isAccountActivation && fromLogin) {
            Log.d(TAG, "🔍 Context: Account activation verification (FR-1.1.2)");
        }

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
        Log.d(TAG, "📤 Resending OTP for " + currentFlow);

        if (isAccountActivation && fromLogin) {
            Log.d(TAG, "📤 Context: Resending OTP for account activation");
        }

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

    /**
     * ✅ Handle OTP verification success với support cho account activation
     */
    private void handleOtpVerificationSuccess(Map<String, Object> data) {
        Log.d(TAG, "✅ OTP verification successful for " + currentFlow +
                " (AccountActivation: " + isAccountActivation +
                ", FromLogin: " + fromLogin + ")");

        switch (currentFlow) {
            case REGISTER:
                // ✅ Update email verification status in local storage
                prefsManager.setEmailVerified(true);

                if (AuthFlowManager.isAccountActivationFromLogin(isAccountActivation, fromLogin)) {
                    // ✅ ACCOUNT ACTIVATION từ login - FR-1.1.2 compliance
                    Log.d(TAG, "✅ FR-1.1.2: Account activation completed successfully");
                    Log.d(TAG, "✅ User can now access the app");

                    Toast.makeText(this,
                            "🎉 Tài khoản đã được kích hoạt thành công!\n\n" +
                                    "Chào mừng bạn đến với TradeUp. Bạn có thể sử dụng đầy đủ tính năng của app.",
                            Toast.LENGTH_LONG).show();
                } else {
                    // ✅ REGISTER FLOW bình thường
                    Log.d(TAG, "✅ REGISTER FLOW: Email verified, account activated");

                    Toast.makeText(this,
                            "✅ Xác thực email thành công!\n" +
                                    "Tài khoản đã được kích hoạt. Chào mừng bạn đến với TradeUp!",
                            Toast.LENGTH_LONG).show();
                }

                navigateToMain();
                break;

            case FORGOT_PASSWORD:
                // ✅ FORGOT PASSWORD FLOW
                Log.d(TAG, "✅ FORGOT PASSWORD FLOW: OTP verified, navigating to ResetPasswordActivity");

                Toast.makeText(this,
                        "✅ Xác thực OTP thành công!\nVui lòng tạo mật khẩu mới.",
                        Toast.LENGTH_SHORT).show();

                navigateToResetPassword();
                break;

            default:
                Log.w(TAG, "⚠️ Unknown flow: " + currentFlow + ", navigating to login");
                navigateToLogin();
                break;
        }
    }

    private void navigateToResetPassword() {
        Intent intent = new Intent(this, ResetPasswordActivity.class);
        intent.putExtra("email", email);
        intent.putExtra("fromOtpVerification", true);
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

        if (show) {
            btnVerify.setText("Đang xác thực...");
        } else {
            if (isAccountActivation && fromLogin) {
                btnVerify.setText("Kích hoạt tài khoản");
            } else if (currentFlow == AuthFlowManager.AuthFlow.REGISTER) {
                btnVerify.setText("Xác thực");
            } else {
                btnVerify.setText("Xác thực");
            }
        }

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

    /**
     * ✅ Back behavior theo flow và context
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (AuthFlowManager.isAccountActivationFromLogin(isAccountActivation, fromLogin)) {
            // ✅ Account activation từ login → về login để thử lại
            Log.d(TAG, "🔙 Back from ACCOUNT ACTIVATION → Login");
            Toast.makeText(this,
                    "Tài khoản chưa được kích hoạt.\nVui lòng xác thực email để tiếp tục sử dụng app.",
                    Toast.LENGTH_SHORT).show();
            navigateToLogin();
        } else {
            // ✅ CÁC FLOW KHÁC
            switch (currentFlow) {
                case REGISTER:
                    Log.d(TAG, "🔙 Back from REGISTER flow → Login");
                    Toast.makeText(this,
                            "Đăng ký chưa hoàn tất. Vui lòng xác thực email để kích hoạt tài khoản.",
                            Toast.LENGTH_SHORT).show();
                    navigateToLogin();
                    break;

                case FORGOT_PASSWORD:
                    Log.d(TAG, "🔙 Back from FORGOT_PASSWORD flow → Login");
                    navigateToLogin();
                    break;

                default:
                    Log.d(TAG, "🔙 Back from unknown flow → Login");
                    navigateToLogin();
                    break;
            }
        }
    }
}