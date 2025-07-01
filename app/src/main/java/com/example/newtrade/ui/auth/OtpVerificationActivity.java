// app/src/main/java/com/example/newtrade/ui/auth/OtpVerificationActivity.java
package com.example.newtrade.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.newtrade.MainActivity;
import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.NetworkUtils;
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
    private Toolbar toolbar;
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
        setupToolbar();
        setupListeners();
        updateUI();
        startCountdown();

        Log.d(TAG, "OtpVerificationActivity initialized for email: " + email);
    }

    private void getIntentData() {
        email = getIntent().getStringExtra("email");
        fromRegistration = getIntent().getBooleanExtra("fromRegistration", false);
        fromLogin = getIntent().getBooleanExtra("fromLogin", false);
        fromForgotPassword = getIntent().getBooleanExtra("fromForgotPassword", false);

        if (email == null) {
            Toast.makeText(this, "Invalid verification request", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvTitle = findViewById(R.id.tv_title);
        tvSubtitle = findViewById(R.id.tv_subtitle);
        tvResendOTP = findViewById(R.id.tv_resend_otp);
        tvCountdown = findViewById(R.id.tv_countdown);
        tilOTP = findViewById(R.id.til_otp);
        etOTP = findViewById(R.id.et_otp);
        btnVerify = findViewById(R.id.btn_verify);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Verify Account");
        }
    }

    private void setupListeners() {
        etOTP.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilOTP.setError(null);
                updateVerifyButtonState();

                // Auto-verify when OTP is complete
                if (s.length() == Constants.OTP_LENGTH) {
                    verifyOTP();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnVerify.setOnClickListener(v -> verifyOTP());
        tvResendOTP.setOnClickListener(v -> resendOTP());
    }

    private void updateUI() {
        if (fromRegistration) {
            tvTitle.setText("Verify Your Email");
            tvSubtitle.setText("We've sent a 6-digit code to\n" + email + "\n\nEnter the code to activate your account");
        } else if (fromLogin) {
            tvTitle.setText("Account Verification Required");
            tvSubtitle.setText("Please verify your email address\n" + email + "\n\nEnter the 6-digit code sent to your email");
        } else if (fromForgotPassword) {
            tvTitle.setText("Reset Password");
            tvSubtitle.setText("Enter the 6-digit code sent to\n" + email + "\n\nThis will allow you to reset your password");
        }
    }

    private void updateVerifyButtonState() {
        String otp = etOTP.getText().toString().trim();
        btnVerify.setEnabled(ValidationUtils.isValidOTP(otp) && !isLoading);
    }

    private void startCountdown() {
        canResend = false;
        tvResendOTP.setEnabled(false);
        tvResendOTP.setAlpha(0.5f);

        countDownTimer = new CountDownTimer(COUNTDOWN_TIME, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                tvCountdown.setText(String.format("Resend code in %02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                canResend = true;
                tvCountdown.setText("Didn't receive the code?");
                tvResendOTP.setEnabled(true);
                tvResendOTP.setAlpha(1.0f);
            }
        };

        countDownTimer.start();
    }

    private void verifyOTP() {
        String otpCode = etOTP.getText().toString().trim();

        if (!ValidationUtils.isValidOTP(otpCode)) {
            tilOTP.setError("Please enter the 6-digit code");
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            showError("No internet connection");
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
            public void onFailure(@NonNull Call<StandardResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                setLoading(false);
                Log.e(TAG, "OTP verification request failed", t);
                showError(NetworkUtils.getNetworkErrorMessage(t));
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void handleVerificationResponse(Response<StandardResponse<Map<String, Object>>> response) {
        try {
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                Toast.makeText(this, "Verification successful!", Toast.LENGTH_SHORT).show();

                if (fromRegistration || fromLogin) {
                    // Save user data if provided
                    Map<String, Object> data = response.body().getData();
                    if (data != null && data.get("user") != null) {
                        Map<String, Object> userData = (Map<String, Object>) data.get("user");
                        prefsManager.saveLoginData(userData);
                    }

                    // Navigate to main activity
                    navigateToMain();
                } else if (fromForgotPassword) {
                    // Navigate to reset password activity
                    navigateToResetPassword();
                }
            } else {
                String message = response.body() != null ? response.body().getMessage() : "Verification failed";
                handleVerificationError(message, response.code());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing verification response", e);
            showError("Verification failed. Please try again.");
        }
    }

    private void handleVerificationError(String message, int responseCode) {
        if (responseCode == 400 || message.toLowerCase().contains("invalid") || message.toLowerCase().contains("expired")) {
            tilOTP.setError("Invalid or expired code");
        } else if (responseCode == 429 || message.toLowerCase().contains("attempts")) {
            tilOTP.setError("Too many attempts. Please try again later");
        } else {
            showError(message);
        }
    }

    private void resendOTP() {
        if (!canResend || isLoading) {
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            showError("No internet connection");
            return;
        }

        setLoading(true);

        Map<String, String> resendData = new HashMap<>();
        resendData.put("email", email);

        Call<StandardResponse<Map<String, String>>> call = ApiClient.getAuthService().sendOtp(resendData);
        call.enqueue(new Callback<StandardResponse<Map<String, String>>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<Map<String, String>>> call,
                                   @NonNull Response<StandardResponse<Map<String, String>>> response) {
                setLoading(false);
                handleResendResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<Map<String, String>>> call, @NonNull Throwable t) {
                setLoading(false);
                Log.e(TAG, "Resend OTP failed", t);
                showError(NetworkUtils.getNetworkErrorMessage(t));
            }
        });
    }

    private void handleResendResponse(Response<StandardResponse<Map<String, String>>> response) {
        try {
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                Toast.makeText(this, "Verification code sent!", Toast.LENGTH_SHORT).show();
                etOTP.setText(""); // Clear previous code
                startCountdown(); // Restart countdown
            } else {
                String message = response.body() != null ? response.body().getMessage() : "Failed to resend code";
                showError(message);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing resend response", e);
            showError("Failed to resend code");
        }
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnVerify.setEnabled(!loading);
        btnVerify.setText(loading ? "Verifying..." : "Verify");
        tvResendOTP.setEnabled(!loading && canResend);
        updateVerifyButtonState();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToResetPassword() {
        // TODO: Create ResetPasswordActivity
        Toast.makeText(this, "Please check your email for password reset link", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}