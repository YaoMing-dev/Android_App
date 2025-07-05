// app/src/main/java/com/example/newtrade/ui/auth/OtpVerificationActivity.java
package com.example.newtrade.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.newtrade.MainActivity;
import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.utils.SharedPrefsManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtpVerificationActivity extends AppCompatActivity {

    private static final String TAG = "OtpVerificationActivity";
    private static final int OTP_LENGTH = 6;
    private static final long COUNTDOWN_INTERVAL = 1000; // 1 second
    private static final long COUNTDOWN_TOTAL = 60000; // 60 seconds

    // UI Components
    private LinearLayout llBack;
    private TextView tvSubtitle, tvTimer, tvResend;
    private EditText etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6;
    private Button btnVerify;
    private ProgressBar progressBar;

    // Data
    private String email;
    private boolean isRegistration;
    private SharedPrefsManager prefsManager;

    // State
    private boolean isLoading = false;
    private CountDownTimer countDownTimer;
    private boolean canResend = false;

    // OTP EditText array for easy access
    private EditText[] otpEditTexts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        // Initialize API client
        ApiClient.init(this);

        // Get intent data
        getIntentData();

        initViews();
        initUtils();
        setupListeners();
        setupOtpEditTexts();
        startCountdown();

        Log.d(TAG, "✅ OtpVerificationActivity created successfully for: " + email);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        isRegistration = intent.getBooleanExtra("isRegistration", false);

        if (email == null || email.isEmpty()) {
            Log.e(TAG, "❌ Email not provided in intent");
            finish();
            return;
        }
    }

    private void initViews() {
        llBack = findViewById(R.id.ll_back);
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
        progressBar = findViewById(R.id.progress_bar);

        // Initialize OTP EditText array
        otpEditTexts = new EditText[]{etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6};

        // Set subtitle
        tvSubtitle.setText("Enter the 6-digit code sent to " + email);

        // Initially disable verify button
        btnVerify.setEnabled(false);

        // Hide progress bar
        progressBar.setVisibility(View.GONE);

        // Initially disable resend
        tvResend.setEnabled(false);
        tvResend.setAlpha(0.5f);

        Log.d(TAG, "✅ All views initialized successfully");
    }

    private void initUtils() {
        prefsManager = SharedPrefsManager.getInstance(this);
    }

    private void setupListeners() {
        // Back button click
        llBack.setOnClickListener(v -> finish());

        // Verify button click
        btnVerify.setOnClickListener(v -> performOtpVerification());

        // Resend link click
        tvResend.setOnClickListener(v -> {
            if (canResend) {
                resendOtp();
            }
        });

        Log.d(TAG, "✅ All listeners set up successfully");
    }

    private void setupOtpEditTexts() {
        for (int i = 0; i < otpEditTexts.length; i++) {
            final int index = i;
            EditText editText = otpEditTexts[i];

            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1) {
                        // Move to next EditText if not the last one
                        if (index < otpEditTexts.length - 1) {
                            otpEditTexts[index + 1].requestFocus();
                        }
                    }
                    updateVerifyButtonState();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            editText.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (editText.getText().toString().isEmpty() && index > 0) {
                        // Move to previous EditText if current is empty
                        otpEditTexts[index - 1].requestFocus();
                    }
                }
                return false;
            });
        }

        Log.d(TAG, "✅ OTP EditTexts set up successfully");
    }

    private void updateVerifyButtonState() {
        String otp = getOtpFromEditTexts();
        boolean isFormValid = otp.length() == OTP_LENGTH && !isLoading;
        btnVerify.setEnabled(isFormValid);
    }

    private String getOtpFromEditTexts() {
        StringBuilder otp = new StringBuilder();
        for (EditText editText : otpEditTexts) {
            otp.append(editText.getText().toString().trim());
        }
        return otp.toString();
    }

    private void clearOtpEditTexts() {
        for (EditText editText : otpEditTexts) {
            editText.setText("");
        }
        otpEditTexts[0].requestFocus();
    }

    private void performOtpVerification() {
        if (isLoading) return;

        String otpCode = getOtpFromEditTexts();

        if (otpCode.length() != OTP_LENGTH) {
            showError("Please enter the complete 6-digit code");
            return;
        }

        setLoading(true);
        Log.d(TAG, "🔐 Performing OTP verification for: " + email);

        Map<String, Object> otpVerificationRequest = new HashMap<>();
        otpVerificationRequest.put("email", email);
        otpVerificationRequest.put("otpCode", otpCode);

        ApiClient.getAuthService().verifyOtp(otpVerificationRequest)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        setLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, Object>> apiResponse = response.body();

                            if (apiResponse.isSuccess()) {
                                Log.d(TAG, "✅ OTP verification successful");
                                handleOtpVerificationSuccess(apiResponse.getData());
                            } else {
                                Log.w(TAG, "❌ OTP verification failed: " + apiResponse.getMessage());
                                showError(apiResponse.getMessage());
                                clearOtpEditTexts();
                            }
                        } else {
                            Log.e(TAG, "❌ OTP verification request failed with code: " + response.code());
                            showError("OTP verification failed. Please try again.");
                            clearOtpEditTexts();
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        setLoading(false);
                        Log.e(TAG, "❌ OTP verification request failed", t);
                        showError("Network error. Please check your connection.");
                        clearOtpEditTexts();
                    }
                });
    }

    private void handleOtpVerificationSuccess(Map<String, Object> responseData) {
        try {
            if (isRegistration) {
                // For registration, just show success and go to login
                Toast.makeText(this, "Email verified successfully! You can now login.", Toast.LENGTH_LONG).show();
                navigateToLogin();
            } else {
                // For login, extract user data and save it
                @SuppressWarnings("unchecked")
                Map<String, Object> userData = (Map<String, Object>) responseData.get("user");

                if (userData != null) {
                    Long userId = getLongValue(userData, "id");
                    String displayName = (String) userData.get("displayName");
                    String userEmail = (String) userData.get("email");
                    String profilePicture = (String) userData.get("profilePicture");

                    // Save user data and navigate to main
                    prefsManager.saveUserData(userId, displayName, userEmail, profilePicture);
                    Log.d(TAG, "✅ User data saved successfully");

                    Toast.makeText(this, "Welcome, " + displayName + "!", Toast.LENGTH_SHORT).show();
                    navigateToMain();
                } else {
                    Log.w(TAG, "❌ User data is null in OTP verification response");
                    showError("Verification successful but user data is missing");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error processing OTP verification response", e);
            showError("Verification successful but error processing response");
        }
    }

    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        } else if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Double) {
            return ((Double) value).longValue();
        }
        return null;
    }

    private void resendOtp() {
        if (isLoading || !canResend) return;

        setLoading(true);
        Log.d(TAG, "🔐 Resending OTP for: " + email);

        Map<String, Object> resendOtpRequest = new HashMap<>();
        resendOtpRequest.put("email", email);

        ApiClient.getAuthService().resendOtp(resendOtpRequest)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        setLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, Object>> apiResponse = response.body();

                            if (apiResponse.isSuccess()) {
                                Log.d(TAG, "✅ OTP resend successful");
                                Toast.makeText(OtpVerificationActivity.this, "OTP resent successfully", Toast.LENGTH_SHORT).show();
                                clearOtpEditTexts();
                                startCountdown();
                            } else {
                                Log.w(TAG, "❌ OTP resend failed: " + apiResponse.getMessage());
                                showError(apiResponse.getMessage());
                            }
                        } else {
                            Log.e(TAG, "❌ OTP resend request failed with code: " + response.code());
                            showError("Failed to resend OTP. Please try again.");
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        setLoading(false);
                        Log.e(TAG, "❌ OTP resend request failed", t);
                        showError("Network error. Please check your connection.");
                    }
                });
    }

    private void startCountdown() {
        canResend = false;
        tvResend.setEnabled(false);
        tvResend.setAlpha(0.5f);

        countDownTimer = new CountDownTimer(COUNTDOWN_TOTAL, COUNTDOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                tvTimer.setText("Resend code in " + seconds + "s");
            }

            @Override
            public void onFinish() {
                canResend = true;
                tvResend.setEnabled(true);
                tvResend.setAlpha(1.0f);
                tvTimer.setText("Didn't receive code?");
            }
        };

        countDownTimer.start();
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

    private void setLoading(boolean loading) {
        isLoading = loading;

        // Update UI based on loading state
        btnVerify.setEnabled(!loading && getOtpFromEditTexts().length() == OTP_LENGTH);
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);

        // Update button text
        if (loading) {
            btnVerify.setText("Verifying...");
        } else {
            btnVerify.setText("Verify Code");
        }

        Log.d(TAG, "Loading state set to: " + loading);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.w(TAG, "Error shown to user: " + message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        Log.d(TAG, "OtpVerificationActivity destroyed");
    }
}