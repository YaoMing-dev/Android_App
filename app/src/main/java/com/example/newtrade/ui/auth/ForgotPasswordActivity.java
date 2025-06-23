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
        // Text change listener for email validation
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

        // Click listeners
        btnReset.setOnClickListener(v -> attemptPasswordReset());
        tvLogin.setOnClickListener(v -> navigateToLogin());
        llBack.setOnClickListener(v -> onBackPressed());
    }

    private void validateForm() {
        String email = etEmail.getText().toString().trim();
        boolean isValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
        btnReset.setEnabled(isValid && !isLoading);
    }

    private void attemptPasswordReset() {
        String email = etEmail.getText().toString().trim();

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            return;
        }

        performPasswordReset(email);
    }

    private void performPasswordReset(String email) {
        showLoading(true);

        Map<String, String> request = new HashMap<>();
        request.put("email", email);

        ApiClient.getAuthService().sendPasswordReset(request).enqueue(new Callback<StandardResponse<String>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<String>> call,
                                   @NonNull Response<StandardResponse<String>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<String> apiResponse = response.body();

                    if (apiResponse.isSuccess()) {
                        handleResetSuccess(email);
                    } else {
                        // For security, don't reveal if email exists or not
                        handleResetSuccess(email);
                    }
                } else {
                    // For security, don't reveal if email exists or not
                    handleResetSuccess(email);
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<String>> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e(TAG, "Password reset failed", t);
                // For security, don't reveal network errors
                handleResetSuccess(email);
            }
        });
    }

    private void handleResetSuccess(String email) {
        Log.d(TAG, "Password reset request completed for email: " + email);

        Toast.makeText(this,
                "Nếu email này tồn tại trong hệ thống, chúng tôi đã gửi link reset mật khẩu.",
                Toast.LENGTH_LONG).show();

        // Navigate back to login after showing message
        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        isLoading = show;
        btnReset.setEnabled(!show && isFormValid());
        btnReset.setText(show ? "Đang gửi..." : "Send Reset Link");
        etEmail.setEnabled(!show);
    }

    private boolean isFormValid() {
        String email = etEmail.getText().toString().trim();
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "ForgotPasswordActivity destroyed");
    }
}