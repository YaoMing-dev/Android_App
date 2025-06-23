// app/src/main/java/com/example/newtrade/ui/auth/RegisterActivity.java
package com.example.newtrade.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.newtrade.R;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.models.User;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.SharedPrefsManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    // UI Components - ✅ FIXED IDs to match layout
    private EditText etDisplayName, etEmail, etPassword, etConfirmPassword;
    private CheckBox cbTerms;
    private Button btnRegister;
    private TextView tvLogin;
    // ❌ REMOVED: progressBar (not in layout)

    // Utils
    private SharedPrefsManager prefsManager;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        initUtils();
        setupListeners();

        Log.d(TAG, "RegisterActivity created");
    }

    private void initViews() {
        etDisplayName = findViewById(R.id.et_display_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        cbTerms = findViewById(R.id.cb_terms);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);

        // Initially disable register button
        btnRegister.setEnabled(false);
    }

    private void initUtils() {
        prefsManager = SharedPrefsManager.getInstance(this);
    }

    private void setupListeners() {
        // Text change listeners for validation
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateForm();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etDisplayName.addTextChangedListener(textWatcher);
        etEmail.addTextChangedListener(textWatcher);
        etPassword.addTextChangedListener(textWatcher);
        etConfirmPassword.addTextChangedListener(textWatcher);

        cbTerms.setOnCheckedChangeListener((buttonView, isChecked) -> validateForm());

        // Click listeners
        btnRegister.setOnClickListener(v -> attemptRegister());
        tvLogin.setOnClickListener(v -> navigateToLogin());

        // ❌ REMOVED: btn_back listener (not in layout)
    }

    private void validateForm() {
        String displayName = etDisplayName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        boolean isValid = !displayName.isEmpty()
                && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                && password.length() >= Constants.MIN_PASSWORD_LENGTH
                && password.equals(confirmPassword)
                && cbTerms.isChecked();

        btnRegister.setEnabled(isValid && !isLoading);
    }

    private void attemptRegister() {
        String displayName = etDisplayName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate display name
        if (displayName.isEmpty() || displayName.length() < 2) {
            etDisplayName.setError("Tên hiển thị phải có ít nhất 2 ký tự");
            return;
        }

        // Validate email
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            return;
        }

        // Validate password
        if (password.length() < Constants.MIN_PASSWORD_LENGTH) {
            etPassword.setError("Mật khẩu phải có ít nhất " + Constants.MIN_PASSWORD_LENGTH + " ký tự");
            return;
        }

        // Validate password confirmation
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            return;
        }

        // Validate terms
        if (!cbTerms.isChecked()) {
            showError("Bạn phải đồng ý với Điều khoản dịch vụ");
            return;
        }

        performRegister(displayName, email, password);
    }

    private void performRegister(String displayName, String email, String password) {
        showLoading(true);

        Map<String, String> request = new HashMap<>();
        request.put("displayName", displayName);
        request.put("email", email);
        request.put("password", password);

        ApiClient.getAuthService().register(request).enqueue(new Callback<StandardResponse<User>>() {
            @Override
            public void onResponse(@NonNull Call<StandardResponse<User>> call,
                                   @NonNull Response<StandardResponse<User>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    StandardResponse<User> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.hasData()) {
                        User user = apiResponse.getData();
                        handleRegisterSuccess(user);
                    } else {
                        showError(apiResponse.getMessage());
                    }
                } else {
                    showError("Đăng ký thất bại");
                }
            }

            @Override
            public void onFailure(@NonNull Call<StandardResponse<User>> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e(TAG, "Register failed", t);
                showError(ApiClient.getErrorMessage(t));
            }
        });
    }

    private void handleRegisterSuccess(User user) {
        Log.d(TAG, "Registration successful for user: " + user.getDisplayName());

        // Save basic user info (but don't mark as fully logged in until email is verified)
        prefsManager.saveString("pending_user_email", user.getEmail());
        prefsManager.saveString("pending_user_name", user.getDisplayName());

        // Show success message
        Toast.makeText(this, "Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản.",
                Toast.LENGTH_LONG).show();

        // Navigate to OTP verification
        navigateToOtpVerification(user.getEmail());
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToOtpVerification(String email) {
        Intent intent = new Intent(this, OtpVerificationActivity.class);
        intent.putExtra("email", email);
        intent.putExtra("from_register", true);
        startActivity(intent);
        finish();
    }

    // ✅ FIXED: No more progressBar - use button state instead
    private void showLoading(boolean show) {
        isLoading = show;
        btnRegister.setEnabled(!show && isFormValid());
        btnRegister.setText(show ? "Đang đăng ký..." : "Create Account");

        // Disable form inputs during loading
        etDisplayName.setEnabled(!show);
        etEmail.setEnabled(!show);
        etPassword.setEnabled(!show);
        etConfirmPassword.setEnabled(!show);
        cbTerms.setEnabled(!show);
    }

    private boolean isFormValid() {
        String displayName = etDisplayName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        return !displayName.isEmpty()
                && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                && password.length() >= Constants.MIN_PASSWORD_LENGTH
                && password.equals(confirmPassword)
                && cbTerms.isChecked();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "RegisterActivity destroyed");
    }
}