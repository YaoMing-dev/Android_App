// app/src/main/java/com/example/newtrade/ui/auth/OTPVerificationActivity.java
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

import androidx.appcompat.app.AppCompatActivity;

import com.example.newtrade.MainActivity;
import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.SharedPrefsManager;
import com.example.newtrade.utils.ValidationUtils;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OTPVerificationActivity extends AppCompatActivity {
    private static final String TAG = "OTPVerificationActivity";
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

        Log.d(TAG, "OTPVerificationActivity initialized for email: " + email);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        fromRegistration = intent.getBooleanExtra("from_registration", false);
        fromLogin = intent.getBooleanExtra("from_login", false);
        fromForgotPassword = intent.getBooleanExtra("from_forgot_password", false);

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

        // Set subtitle with email
        tvSubtitle.setText("We've sent a verification code to\n" + email);

        // Initially disable verify button
        btnVerify.setEnabled(false);

        // Initially disable resend
        tvResendOTP.setEnabled(false);
        tvResendOTP.setAlpha(0.5f);
    }

    private void setupListeners() {
        // OTP text watcher
        etOTP.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilOTP.setError(null);
                updateVerifyButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Auto-verify when 6 digits entered
                if (s.length() == 6) {
                    performOTPVerification();
                }
            }
        });

        // Verify button
        btnVerify.setOnClickListener(v -> performOTPVerification());

        // Resend OTP
        tvResendOTP.setOnClickListener(v -> {
            if (canResend) {
                resendOTP();
            }
        });
    }

    private void updateVerifyButtonState() {
        String otp = etOTP.getText().toString().trim();
        btnVerify.setEnabled(otp.length() == 6 && !isLoading);
    }

    private void performOTPVerification() {
        if (isLoading) return;

        String otp = etOTP.getText().toString().trim();

        if (!ValidationUtils.isValidOTP(otp)) {
            tilOTP.setError("Please enter a valid 6-digit OTP");
            return;
        }

        setLoading(true);

        Map<String, String> otpRequest = new HashMap<>();
        otpRequest.put("email", email);
        otpRequest.put("otpCode", otp);

        ApiClient.getAuthService().verifyOTP(otpRequest)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        setLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, Object>> apiResponse = response.body();

                            if (apiResponse.isSuccess()) {
                                handleOTPVerificationSuccess(apiResponse.getData());
                            } else {
                                tilOTP.setError(apiResponse.getMessage());
                                etOTP.setText("");
                            }
                        } else {
                            tilOTP.setError("Invalid OTP. Please try again.");
                            etOTP.setText("");
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        setLoading(false);
                        Log.e(TAG, "OTP verification API call failed", t);
                        showError("Network error. Please check your connection.");
                    }
                });
    }

    private void resendOTP() {
        if (!canResend) return;

        setLoading(true);

        Map<String, String> resendRequest = new HashMap<>();
        resendRequest.put("email", email);

        ApiClient.getAuthService().resendOTP(resendRequest)
                .enqueue(new Callback<StandardResponse<Map<String, String>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, String>>> call,
                                           Response<StandardResponse<Map<String, String>>> response) {
                        setLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, String>> apiResponse = response.body();

                            if (apiResponse.isSuccess()) {
                                Toast.makeText(OTPVerificationActivity.this,
                                        "OTP resent successfully!", Toast.LENGTH_SHORT).show();
                                etOTP.setText("");
                                startCountdown();
                            } else {
                                showError(apiResponse.getMessage());
                            }
                        } else {
                            showError("Failed to resend OTP. Please try again.");
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, String>>> call, Throwable t) {
                        setLoading(false);
                        Log.e(TAG, "Resend OTP API call failed", t);
                        showError("Network error. Please check your connection.");
                    }
                });
    }

    private void handleOTPVerificationSuccess(Map<String, Object> data) {
        Toast.makeText(this, "Email verified successfully!", Toast.LENGTH_SHORT).show();

        if (fromRegistration || fromLogin) {
            // Extract user data and save session
            Map<String, Object> user = (Map<String, Object>) data.get("user");
            String token = (String) data.get("token");

            if (user != null && token != null) {
                Long userId = Long.valueOf(user.get("id").toString());
                String userEmail = (String) user.get("email");
                String displayName = (String) user.get("displayName");
                String avatarUrl = (String) user.get("profilePicture");

                prefsManager.saveUserLogin(userId, userEmail, displayName, avatarUrl, token);

                // Navigate to main activity
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        } else if (fromForgotPassword) {
            // Navigate to reset password activity
            Intent intent = new Intent(this, ResetPasswordActivity.class);
            intent.putExtra("email", email);
            intent.putExtra("verified", true);
            startActivity(intent);
            finish();
        } else {
            // Default case - go back to login
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void startCountdown() {
        canResend = false;
        tvResendOTP.setEnabled(false);
        tvResendOTP.setAlpha(0.5f);

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(COUNTDOWN_TIME, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                tvCountdown.setText(String.format("Resend OTP in %02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                canResend = true;
                tvResendOTP.setEnabled(true);
                tvResendOTP.setAlpha(1.0f);
                tvCountdown.setText("Didn't receive the code?");
            }
        };

        countDownTimer.start();
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnVerify.setEnabled(!loading && etOTP.getText().toString().trim().length() == 6);
        etOTP.setEnabled(!loading);
        tvResendOTP.setEnabled(!loading && canResend);
    }

    private void showError(String message) {
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