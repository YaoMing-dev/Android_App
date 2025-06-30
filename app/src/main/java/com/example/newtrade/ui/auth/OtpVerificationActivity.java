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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.newtrade.MainActivity;
import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.SharedPrefsManager;
import com.example.newtrade.utils.ValidationUtils;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtpVerificationActivity extends AppCompatActivity {
    private static final String TAG = "OtpVerificationActivity";
    private static final long COUNTDOWN_TIME = 300000; // 5 minutes

    // UI Components
    private TextView tvTitle, tvSubtitle, tvResendOTP, tvCountdown;
    private TextInputLayout tilOTP;
    private EditText etOTP;
    private Button btnVerify;
    private ProgressBar progressBar;

    // Data
    private String email;
    private boolean fromRegistration = false;
    private boolean fromLogin = false;
    private boolean fromForgotPassword = false;
    private CountDownTimer countDownTimer;
    private boolean canResend = false;
    private boolean isLoading = false;

    // Utils
    private SharedPrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        ApiClient.init(this);
        prefsManager = new SharedPrefsManager(this);

        getIntentData();
        initViews();
        setupListeners();
        startCountdown();

        Log.d(TAG, "OtpVerificationActivity initialized for email: " + email);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        fromRegistration = intent.getBooleanExtra("fromRegistration", false);
        fromLogin = intent.getBooleanExtra("fromLogin", false);
        fromForgotPassword = intent.getBooleanExtra("fromForgotPassword", false);

        if (email == null) {
            showError("Invalid email address");
            finish();
        }
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_title);
        tvSubtitle = findViewById(R.id.tv_subtitle);
        tvResendOTP = findViewById(R.id.tv_resend_otp);
        tvCountdown = findViewById(R.id.tv_countdown);
        tilOTP = findViewById(R.id.til_otp);
        etOTP = findViewById(R.id.et_otp);
        btnVerify = findViewById(R.id.btn_verify);
        progressBar = findViewById(R.id.progress_bar);

        // Set initial state
        btnVerify.setEnabled(false);
        tvResendOTP.setEnabled(false);

        // Set content based on source
        if (fromRegistration) {
            tvTitle.setText("Verify Your Email");
            tvSubtitle.setText("We've sent a verification code to " + email);
        } else if (fromForgotPassword) {
            tvTitle.setText("Reset Your Password");
            tvSubtitle.setText("We've sent a reset code to " + email);
        } else {
            tvTitle.setText("Verify Your Account");
            tvSubtitle.setText("We've sent a verification code to " + email);
        }
    }

    private void setupListeners() {
        // OTP text watcher
        etOTP.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilOTP.setError(null);
                btnVerify.setEnabled(s.length() == Constants.OTP_LENGTH && !isLoading);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Verify button
        btnVerify.setOnClickListener(v -> verifyOTP());

        // Resend OTP
        tvResendOTP.setOnClickListener(v -> {
            if (canResend) {
                resendOTP();
            }
        });
    }

    private void startCountdown() {
        canResend = false;
        tvResendOTP.setEnabled(false);
        tvResendOTP.setText("Resend OTP");

        countDownTimer = new CountDownTimer(COUNTDOWN_TIME, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                tvCountdown.setText(String.format("Resend available in %02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                canResend = true;
                tvResendOTP.setEnabled(true);
                tvResendOTP.setText("Resend OTP");
                tvCountdown.setText("Didn't receive code?");
            }
        };

        countDownTimer.start();
    }

    private void verifyOTP() {
        String otpCode = etOTP.getText().toString().trim();

        if (otpCode.length() != Constants.OTP_LENGTH) {
            tilOTP.setError("Please enter the 6-digit code");
            return;
        }

        setLoading(true);

        Map<String, String> otpData = new HashMap<>();
        otpData.put("email", email);
        otpData.put("otpCode", otpCode);

        Call<StandardResponse<Map<String, Object>>> call = ApiClient.getAuthService().verifyOtp(otpData);
        call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                   @NonNull Response<StandardResponse<Map<String, Object>>> response) {
                setLoading(false);
                handleVerificationResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call,
                                  @NonNull Throwable t) {
                setLoading(false);
                Log.e(TAG, "OTP verification request failed", t);
                showError("Verification failed: " + t.getMessage());
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void handleVerificationResponse(Response<StandardResponse<Map<String, Object>>> response) {
        try {
            if (response.isSuccessful() && response.body() != null) {
                StandardResponse<Map<String, Object>> apiResponse = response.body();

                if (apiResponse.isSuccess()) {
                    Toast.makeText(this, "Verification successful!", Toast.LENGTH_SHORT).show();

                    if (fromRegistration || fromLogin) {
                        // Save user data if provided
                        Map<String, Object> data = apiResponse.getData();
                        if (data != null && data.get("user") != null) {
                            Map<String, Object> userData = (Map<String, Object>) data.get("user");
                            prefsManager.saveLoginData(userData);
                        }

                        // Navigate to main activity
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else if (fromForgotPassword) {
                        // Navigate to reset password activity
                        // TODO: Create ResetPasswordActivity
                        Toast.makeText(this, "Please check your email for password reset link",
                                Toast.LENGTH_LONG).show();
                        finish();
                    }
                } else {
                    showError(apiResponse.getMessage());
                }
            } else {
                String errorMsg = "Verification failed";
                if (response.code() == 400) {
                    errorMsg = "Invalid or expired code";
                }
                showError(errorMsg);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing verification response", e);
            showError("Verification failed");
        }
    }

    private void resendOTP() {
        setLoading(true);

        Map<String, String> resendData = new HashMap<>();
        resendData.put("email", email);

        Call<StandardResponse<Map<String, String>>> call = ApiClient.getAuthService().sendOtp(resendData);
        call.enqueue(new Callback<StandardResponse<Map<String, String>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, String>>> call,
                                   @NonNull Response<StandardResponse<Map<String, String>>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(OtpVerificationActivity.this, "OTP sent successfully",
                            Toast.LENGTH_SHORT).show();
                    etOTP.setText("");
                    startCountdown();
                } else {
                    showError("Failed to resend OTP");
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, String>>> call,
                                  @NonNull Throwable t) {
                setLoading(false);
                Log.e(TAG, "Resend OTP request failed", t);
                showError("Failed to resend OTP");
            }
        });
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        btnVerify.setEnabled(!loading && etOTP.getText().length() == Constants.OTP_LENGTH);
        btnVerify.setText(loading ? "Verifying..." : "Verify");
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        Log.e(TAG, "Showing error: " + message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}