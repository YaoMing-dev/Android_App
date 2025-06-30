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
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize API client
        ApiClient.init(this);

        // Initialize components
        initViews();
        initGoogleSignIn();
        initUtils();
        setupListeners();

        // Check if already logged in
        if (prefsManager.isLoggedIn()) {
            navigateToMain();
            return;
        }

        Log.d(TAG, "LoginActivity initialized successfully");
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

        // Initially disable login button
        btnLogin.setEnabled(false);
    }

    private void initGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(Constants.GOOGLE_CLIENT_ID)
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void initUtils() {
        prefsManager = new SharedPrefsManager(this);
    }

    private void setupListeners() {
        // Email text watcher
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

        // Password text watcher
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

        // Login button
        btnLogin.setOnClickListener(v -> performLogin());

        // Google Sign-In button
        btnGoogleSignIn.setOnClickListener(v -> performGoogleSignIn());

        // Register link
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        // Forgot password link
        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgotPasswordActivity.class));
        });
    }

    private void updateLoginButtonState() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        boolean isFormValid = !email.isEmpty() && !password.isEmpty() && !isLoading;
        btnLogin.setEnabled(isFormValid);
    }

    private void performLogin() {
        if (isLoading) return;

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate input
        if (!validateLoginForm(email, password)) {
            return;
        }

        setLoading(true);

        Map<String, Object> loginRequest = new HashMap<>();
        loginRequest.put("email", email);
        loginRequest.put("password", password);

        ApiClient.getAuthService().login(loginRequest)
                .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                           Response<StandardResponse<Map<String, Object>>> response) {
                        setLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            StandardResponse<Map<String, Object>> apiResponse = response.body();

                            if (apiResponse.isSuccess()) {
                                handleLoginSuccess(apiResponse.getData());
                            } else {
                                showError(apiResponse.getMessage());
                            }
                        } else {
                            showError("Login failed. Please check your credentials.");
                        }
                    }

                    @Override
                    public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                        setLoading(false);
                        Log.e(TAG, "Login API call failed", t);
                        showError("Network error. Please check your connection.");
                    }
                });
    }

    private void performGoogleSignIn() {
        if (isLoading) return;

        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, Constants.GOOGLE_SIGN_IN_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.GOOGLE_SIGN_IN_REQUEST) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
        }
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            if (account != null && account.getIdToken() != null) {
                setLoading(true);

                Map<String, String> googleRequest = new HashMap<>();
                googleRequest.put("idToken", account.getIdToken());
                googleRequest.put("email", account.getEmail());
                googleRequest.put("name", account.getDisplayName());

                ApiClient.getAuthService().googleLogin(googleRequest)
                        .enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                            @Override
                            public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                                   Response<StandardResponse<Map<String, Object>>> response) {
                                setLoading(false);

                                if (response.isSuccessful() && response.body() != null) {
                                    StandardResponse<Map<String, Object>> apiResponse = response.body();

                                    if (apiResponse.isSuccess()) {
                                        handleLoginSuccess(apiResponse.getData());
                                    } else {
                                        showError(apiResponse.getMessage());
                                    }
                                } else {
                                    showError("Google sign-in failed. Please try again.");
                                }
                            }

                            @Override
                            public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                                setLoading(false);
                                Log.e(TAG, "Google login API call failed", t);
                                showError("Network error. Please try again.");
                            }
                        });
            }
        } catch (ApiException e) {
            Log.e(TAG, "Google sign-in failed", e);
            showError("Google sign-in was cancelled or failed.");
        }
    }

    private boolean validateLoginForm(String email, String password) {
        boolean isValid = true;

        // Validate email
        String emailError = ValidationUtils.getEmailValidationError(email);
        if (emailError != null) {
            tilEmail.setError(emailError);
            isValid = false;
        }

        // Validate password
        String passwordError = ValidationUtils.getPasswordValidationError(password);
        if (passwordError != null) {
            tilPassword.setError(passwordError);
            isValid = false;
        }

        return isValid;
    }

    private void handleLoginSuccess(Map<String, Object> userData) {
        try {
            // Extract user data
            Map<String, Object> user = (Map<String, Object>) userData.get("user");
            String token = (String) userData.get("token");

            if (user != null && token != null) {
                Long userId = Long.valueOf(user.get("id").toString());
                String email = (String) user.get("email");
                String displayName = (String) user.get("displayName");
                String avatarUrl = (String) user.get("profilePicture");
                Boolean isEmailVerified = (Boolean) user.get("isEmailVerified");

                // Save user session
                prefsManager.saveUserLogin(userId, email, displayName, avatarUrl, token);

                // Check if email is verified
                if (isEmailVerified != null && !isEmailVerified) {
                    // Redirect to OTP verification
                    Intent intent = new Intent(this, OTPVerificationActivity.class);
                    intent.putExtra("email", email);
                    intent.putExtra("from_login", true);
                    startActivity(intent);
                    finish();
                } else {
                    // Navigate to main activity
                    navigateToMain();
                }

                Toast.makeText(this, "Welcome back, " + displayName + "!", Toast.LENGTH_SHORT).show();
            } else {
                showError("Invalid response from server.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing login response", e);
            showError("Login successful but failed to process user data.");
        }
    }

    private void setLoading(boolean loading) {
        isLoading = loading;
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading && !etEmail.getText().toString().trim().isEmpty()
                && !etPassword.getText().toString().trim().isEmpty());
        btnGoogleSignIn.setEnabled(!loading);
        etEmail.setEnabled(!loading);
        etPassword.setEnabled(!loading);
        tvRegister.setEnabled(!loading);
        tvForgotPassword.setEnabled(!loading);
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
}