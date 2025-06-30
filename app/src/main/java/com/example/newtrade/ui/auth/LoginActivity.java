// app/src/main/java/com/example/newtrade/ui/auth/LoginActivity.java
package com.example.newtrade.ui.auth;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;

    // UI Components
    private TextInputLayout tilEmail, tilPassword;
    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoogleSignIn;
    private TextView tvRegister, tvForgotPassword;
    private ProgressBar progressBar;

    // Google Sign-In
    private GoogleSignInClient googleSignInClient;

    // Utils
    private SharedPrefsManager prefsManager;

    // State
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize API Client
        ApiClient.init(this);
        prefsManager = new SharedPrefsManager(this);

        // Check if already logged in
        if (prefsManager.isLoggedIn()) {
            navigateToMain();
            return;
        }

        initViews();
        setupGoogleSignIn();
        setupListeners();

        Log.d(TAG, "LoginActivity initialized");
    }

    private void initViews() {
        tilEmail = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnGoogleSignIn = findViewById(R.id.btn_google_signin);
        tvRegister = findViewById(R.id.tv_register);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(Constants.GOOGLE_CLIENT_ID)
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupListeners() {
        // Google Sign In
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());

        // Regular login
        btnLogin.setOnClickListener(v -> performLogin());

        // Navigation
        tvRegister.setOnClickListener(v -> navigateToRegister());
        tvForgotPassword.setOnClickListener(v -> navigateToForgotPassword());

        // Text validation
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilEmail.setError(null);
                updateLoginButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilPassword.setError(null);
                updateLoginButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // FR-1.1.4: Login button disabled unless both fields are filled correctly
    private void updateLoginButtonState() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        boolean isValid = ValidationUtils.isValidEmail(email) &&
                ValidationUtils.isValidPassword(password);

        btnLogin.setEnabled(isValid && !isLoading);
    }

    private void signInWithGoogle() {
        if (isLoading) return;

        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                handleGoogleSignInResult(account);
            } catch (ApiException e) {
                Log.e(TAG, "Google sign in failed", e);
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleGoogleSignInResult(GoogleSignInAccount account) {
        if (account == null) {
            Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        Map<String, String> request = new HashMap<>();
        request.put("idToken", account.getIdToken());

        ApiClient.getAuthService().googleSignIn(request).enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<StandardResponse<Map<String, Object>>> call, Response<StandardResponse<Map<String, Object>>> response) {
                setLoading(false);
                handleAuthResponse(response, null);
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                setLoading(false);
                Log.e(TAG, "Google login failed", t);
                showError("Network error. Please try again.");
            }
        });
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validateInput(email, password)) {
            return;
        }

        setLoading(true);

        Map<String, String> request = new HashMap<>();
        request.put("email", email);
        request.put("password", password);

        ApiClient.getAuthService().login(request).enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<StandardResponse<Map<String, Object>>> call, Response<StandardResponse<Map<String, Object>>> response) {
                setLoading(false);
                handleAuthResponse(response, email);
            }

            @Override
            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                setLoading(false);
                Log.e(TAG, "Login failed", t);
                showError("Network error. Please try again.");
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void handleAuthResponse(Response<StandardResponse<Map<String, Object>>> response, String email) {
        try {
            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                Map<String, Object> data = response.body().getData();
                Map<String, Object> user = (Map<String, Object>) data.get("user");
                boolean requiresOtp = (Boolean) data.getOrDefault("requiresOtp", false);

                if (requiresOtp) {
                    // FR-1.1.2: Email verification required
                    String userEmail = email != null ? email : (String) user.get("email");
                    navigateToOtpVerification(userEmail);
                } else {
                    prefsManager.saveLoginData(user);
                    navigateToMain();
                }
            } else {
                String message = response.body() != null ? response.body().getMessage() : "Login failed";
                showError(message);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing auth response", e);
            showError("Login failed. Please try again.");
        }
    }

    private boolean validateInput(String email, String password) {
        boolean isValid = true;

        if (!ValidationUtils.isValidEmail(email)) {
            tilEmail.setError("Please enter a valid email");
            isValid = false;
        }

        if (!ValidationUtils.isValidPassword(password)) {
            tilPassword.setError("Password must be at least 6 characters");
            isValid = false;
        }

        return isValid;
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        btnGoogleSignIn.setEnabled(!loading);
        updateLoginButtonState();
    }

    private void showError(String message) {
        Log.e(TAG, "Showing error: " + message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    private void navigateToForgotPassword() {
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
    }

    private void navigateToOtpVerification(String email) {
        Intent intent = new Intent(this, OtpVerificationActivity.class);
        intent.putExtra("email", email);
        intent.putExtra("fromLogin", true);
        startActivity(intent);
    }
}