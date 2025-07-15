// File: app/src/main/java/com/example/newtrade/MainActivity.java
package com.example.newtrade;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.newtrade.activities.NotificationActivity;
import com.example.newtrade.api.ApiClient;
import com.example.newtrade.api.ApiService;
import com.example.newtrade.api.NotificationService;
import com.example.newtrade.api.UserService;
import com.example.newtrade.models.StandardResponse;
import com.example.newtrade.ui.auth.LoginActivity;
import com.example.newtrade.utils.PromotionNotificationHelper;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;

    // ✅ NOTIFICATION CHANNELS
    private static final String CHANNEL_ID_MESSAGES = "tradeup_messages";
    private static final String CHANNEL_ID_OFFERS = "tradeup_offers";
    private static final String CHANNEL_ID_GENERAL = "tradeup_general";

    // UI Components
    private BottomNavigationView bottomNavigation;
    private NavController navController;
    private FloatingActionButton testFab;

    // Utils
    private SharedPrefsManager prefsManager;
    private int fabClickCount = 0;
    private Toast currentToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize API client first
        ApiClient.init(this);

        // Initialize SharedPrefs
        prefsManager = SharedPrefsManager.getInstance(this);

        // Check authentication
        if (!prefsManager.isLoggedIn()) {
            Log.d(TAG, "User not logged in, redirecting to login");
            navigateToLogin();
            return;
        }

        Log.d(TAG, "User logged in: " + prefsManager.getUserName() + " (ID: " + prefsManager.getUserId() + ")");

        setContentView(R.layout.activity_main);

        initViews();

        // ✅ CREATE NOTIFICATION CHANNELS FIRST
        createNotificationChannels();

        // ✅ THÊM FLOATING ACTION BUTTON ĐỂ TEST
        addTestFloatingActionButton();

        // Delay navigation setup để đảm bảo Fragment container được khởi tạo
        new Handler(Looper.getMainLooper()).postDelayed(() -> setupNavigation(), 100);

        // Setup notifications
        checkNotificationPermissions();
        setupFirebaseMessaging();
        setupPromotionScheduler();

        Log.d(TAG, "MainActivity created successfully");
    }

    private void setupPromotionScheduler() {
        // ✅ Bắt đầu schedule promotion mỗi 5 phút
        PromotionNotificationHelper.startRandomPromotionScheduling(this);
        Log.d(TAG, "✅ Promotion scheduler started - every 5 minutes");
        showSingleToast("🎁 Random promotions will arrive every 5 minutes!");
    }


    private void initViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation);

        if (bottomNavigation == null) {
            Log.e(TAG, "❌ Bottom navigation not found in layout");
            showSingleToast("Layout error - bottom navigation missing");
            return;
        }
    }

    // ✅ CREATE NOTIFICATION CHANNELS
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            // Messages Channel - High Priority
            NotificationChannel messagesChannel = new NotificationChannel(
                    CHANNEL_ID_MESSAGES,
                    "New Messages",
                    NotificationManager.IMPORTANCE_HIGH
            );
            messagesChannel.setDescription("Notifications for new messages");
            messagesChannel.enableLights(true);
            messagesChannel.enableVibration(true);
            messagesChannel.setShowBadge(true);

            // Offers Channel - Default Priority
            NotificationChannel offersChannel = new NotificationChannel(
                    CHANNEL_ID_OFFERS,
                    "Price Offers",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            offersChannel.setDescription("Notifications for price offers");
            offersChannel.enableLights(true);
            offersChannel.enableVibration(true);

            // General Channel - Default Priority
            NotificationChannel generalChannel = new NotificationChannel(
                    CHANNEL_ID_GENERAL,
                    "General Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            generalChannel.setDescription("General app notifications");

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(messagesChannel);
                notificationManager.createNotificationChannel(offersChannel);
                notificationManager.createNotificationChannel(generalChannel);
                Log.d(TAG, "✅ Notification channels created");
            }
        }
    }

    // ✅ THÊM FLOATING ACTION BUTTON ĐỂ TEST
    private void addTestFloatingActionButton() {
        try {
            CoordinatorLayout coordinatorLayout = findViewById(R.id.main);

            if (coordinatorLayout != null) {
                testFab = new FloatingActionButton(this);
                testFab.setImageResource(android.R.drawable.ic_dialog_email);
                testFab.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.holo_orange_dark));

                CoordinatorLayout.LayoutParams fabParams = new CoordinatorLayout.LayoutParams(
                        CoordinatorLayout.LayoutParams.WRAP_CONTENT,
                        CoordinatorLayout.LayoutParams.WRAP_CONTENT
                );
                fabParams.gravity = android.view.Gravity.END | android.view.Gravity.BOTTOM;
                fabParams.setMargins(0, 0, 48, 120);

                testFab.setLayoutParams(fabParams);
                testFab.setOnClickListener(v -> handleFabClick());
                coordinatorLayout.addView(testFab);

                Log.d(TAG, "✅ Test FAB added successfully");
                showSingleToast("🔔 Orange FAB = Test REAL Push Notifications");
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error adding test FAB", e);
        }
    }

    // ✅ HANDLE FAB CLICKS - REAL NOTIFICATION FLOW
    private void handleFabClick() {
        fabClickCount++;

        switch (fabClickCount % 4) {
            case 1:
                createTestProductAndNotification();
                testFab.setImageResource(android.R.drawable.ic_menu_add);
                break;

            case 2:
                showOfferNotification();
                testFab.setImageResource(android.R.drawable.ic_menu_view);
                break;

            case 3:
                showGeneralNotification();
                testFab.setImageResource(android.R.drawable.ic_menu_info_details);
                break;

            case 0:
                openNotificationActivity();
                testFab.setImageResource(android.R.drawable.ic_dialog_email);
                break;
        }
    }

    // ✅ STEP 1: CREATE PRODUCT + SHOW MESSAGE NOTIFICATION
    private void createTestProductAndNotification() {
        Log.d(TAG, "🔨 Creating test product and message notification...");

        try {
            ApiService apiService = ApiClient.getApiService();

            Map<String, Object> productData = new HashMap<>();
            productData.put("title", "Test Product for Notification");
            productData.put("description", "This is a test product created to trigger real notifications");
            productData.put("price", 100.0);
            productData.put("categoryId", 1L);
            productData.put("condition", "NEW");
            productData.put("location", "Ho Chi Minh City");
            productData.put("isNegotiable", true);

            Call<StandardResponse<Map<String, Object>>> call = apiService.createProduct(productData);

            call.enqueue(new Callback<StandardResponse<Map<String, Object>>>() {
                @Override
                public void onResponse(Call<StandardResponse<Map<String, Object>>> call,
                                       Response<StandardResponse<Map<String, Object>>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        StandardResponse<Map<String, Object>> standardResponse = response.body();

                        if (standardResponse.isSuccess()) {
                            Map<String, Object> productResult = standardResponse.getData();
                            Long productId = extractProductId(productResult);

                            if (productId != null) {
                                Log.d(TAG, "✅ Test product created with ID: " + productId);
                                prefsManager.saveTestProductId(productId);

                                // ✅ SHOW MESSAGE NOTIFICATION POPUP
                                showMessageNotification("New Product Created",
                                        "Your product '" + productData.get("title") + "' has been listed successfully!");
                            }
                        } else {
                            showMessageNotification("Product Creation Failed",
                                    "Failed to create test product");
                        }
                    } else {
                        showMessageNotification("API Error",
                                "Product API returned error: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<StandardResponse<Map<String, Object>>> call, Throwable t) {
                    showMessageNotification("Connection Error",
                            "Failed to connect to server");
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "❌ Error in createTestProductAndNotification", e);
            showMessageNotification("Error", "Exception occurred: " + e.getMessage());
        }
    }

    // ✅ SHOW MESSAGE NOTIFICATION (như Messenger)
    private void showMessageNotification(String title, String message) {
        Intent intent = new Intent(this, NotificationActivity.class);
        intent.putExtra("notification_type", "MESSAGE");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID_MESSAGES)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .setShowWhen(true)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .addAction(R.drawable.ic_notification, "View", pendingIntent)
                .addAction(R.drawable.ic_notification, "Reply", pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1001, builder.build());
            Log.d(TAG, "✅ MESSAGE notification displayed");
        }
    }

    // ✅ SHOW OFFER NOTIFICATION
    private void showOfferNotification() {
        Log.d(TAG, "🎯 Showing offer notification...");

        Intent intent = new Intent(this, NotificationActivity.class);
        intent.putExtra("notification_type", "OFFER");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID_OFFERS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("New Price Offer")
                .setContentText("Someone offered $150 for your iPhone 13")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_EMAIL)
                .setAutoCancel(true)
                .setShowWhen(true)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Someone offered $150 for your iPhone 13. Tap to view details and respond."))
                .addAction(R.drawable.ic_notification, "Accept", pendingIntent)
                .addAction(R.drawable.ic_notification, "Decline", pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1002, builder.build());
            Log.d(TAG, "✅ OFFER notification displayed");
        }
    }

    // ✅ SHOW GENERAL NOTIFICATION
    private void showGeneralNotification() {
        Log.d(TAG, "📢 Showing general notification...");

        Intent intent = new Intent(this, NotificationActivity.class);
        intent.putExtra("notification_type", "GENERAL");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID_GENERAL)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("TradeUp Promotion")
                .setContentText("Special offer: Get 20% off your next listing fee!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_PROMO)
                .setAutoCancel(true)
                .setShowWhen(true)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Special offer: Get 20% off your next listing fee! Limited time offer, expires in 24 hours."))
                .addAction(R.drawable.ic_notification, "Claim Offer", pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1003, builder.build());
            Log.d(TAG, "✅ GENERAL notification displayed");
        }
    }

    // ✅ HELPER METHODS
    private Long extractProductId(Map<String, Object> data) {
        try {
            Object idObj = data.get("id");
            if (idObj instanceof Number) {
                return ((Number) idObj).longValue();
            } else if (idObj instanceof String) {
                return Long.parseLong((String) idObj);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting product ID", e);
        }
        return null;
    }

    private void showSingleToast(String message) {
        if (currentToast != null) {
            currentToast.cancel();
        }
        currentToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        currentToast.show();
    }

    private void openNotificationActivity() {
        try {
            Intent intent = new Intent(this, NotificationActivity.class);
            startActivity(intent);
            Log.d(TAG, "📱 Opening notifications screen");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error opening NotificationActivity", e);
            showSingleToast("❌ NotificationActivity not found");
        }
    }

    // ✅ SETUP NOTIFICATION PERMISSIONS
    private void checkNotificationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                Log.w(TAG, "⚠️ Notification permission not granted, requesting...");

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION);
            } else {
                Log.d(TAG, "✅ Notification permission already granted");
            }
        } else {
            Log.d(TAG, "✅ Notification permission not required (API < 33)");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "✅ Notification permission granted by user");
                showSingleToast("🔔 Notifications enabled!");
            } else {
                Log.w(TAG, "❌ Notification permission denied by user");
                showSingleToast("❌ Notifications disabled");
            }
        }
    }

    // ✅ SETUP FIREBASE MESSAGING
    private void setupFirebaseMessaging() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "❌ Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    String token = task.getResult();
                    Log.d(TAG, "🎯 FCM Token: " + token);
                    prefsManager.saveFcmToken(token);
                    sendTokenToServer(token);
                });
    }

    // ✅ FIXED: sendTokenToServer() method
    private void sendTokenToServer(String token) {
        try {
            // ✅ FIXED: Dùng UserService.updateFcmToken() thay vì updateUserProfile()
            UserService userService = ApiClient.getUserService();

            Map<String, String> tokenRequest = new HashMap<>();
            tokenRequest.put("fcmToken", token);

            Call<StandardResponse<String>> call = userService.updateFcmToken(tokenRequest);

            call.enqueue(new Callback<StandardResponse<String>>() {
                @Override
                public void onResponse(Call<StandardResponse<String>> call,
                                       Response<StandardResponse<String>> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "✅ FCM token sent to server successfully");
                        // ✅ THÊM: Show notification confirmation
                        showGeneralNotificationForTokenSuccess();
                    } else {
                        Log.e(TAG, "❌ Failed to send FCM token: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<StandardResponse<String>> call, Throwable t) {
                    Log.e(TAG, "❌ Error sending FCM token", t);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "❌ Error in sendTokenToServer", e);
        }
    }

    // ✅ THÊM: Show notification confirmation when FCM token is registered
    private void showGeneralNotificationForTokenSuccess() {
        try {
            // ✅ THÊM: Also send notification via backend API
            NotificationService notificationService = ApiClient.getNotificationService();

            Map<String, Object> notificationRequest = new HashMap<>();
            notificationRequest.put("title", "🔔 Notifications Enabled");
            notificationRequest.put("message", "Push notifications are now active for your account");
            notificationRequest.put("userId", prefsManager.getUserId());

            Call<StandardResponse<String>> call = notificationService.sendGeneralNotification(notificationRequest);

            call.enqueue(new Callback<StandardResponse<String>>() {
                @Override
                public void onResponse(Call<StandardResponse<String>> call,
                                       Response<StandardResponse<String>> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "✅ Backend notification sent for FCM token success");
                    } else {
                        Log.e(TAG, "❌ Failed to send backend notification: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<StandardResponse<String>> call, Throwable t) {
                    Log.e(TAG, "❌ Error sending backend notification", t);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "❌ Error in showGeneralNotificationForTokenSuccess", e);
        }
    }

    // Rest of existing methods...
    private void setupNavigation() {
        try {
            navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            NavigationUI.setupWithNavController(bottomNavigation, navController);
            Log.d(TAG, "✅ Navigation setup completed");
        } catch (Exception e) {
            Log.e(TAG, "❌ Navigation setup failed", e);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    navController = Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment);
                    NavigationUI.setupWithNavController(bottomNavigation, navController);
                    Log.d(TAG, "✅ Navigation setup completed on retry");
                } catch (Exception ex) {
                    Log.e(TAG, "❌ Navigation setup failed on retry", ex);
                    showSingleToast("Navigation error");
                }
            }, 200);
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!prefsManager.isLoggedIn()) {
            Log.d(TAG, "Session lost, redirecting to login");
            navigateToLogin();
        }
    }

    @Override
    public void onBackPressed() {
        try {
            if (navController != null && navController.getCurrentDestination() != null &&
                    navController.getCurrentDestination().getId() == R.id.nav_home) {
                super.onBackPressed();
            } else if (bottomNavigation != null) {
                bottomNavigation.setSelectedItemId(R.id.nav_home);
            } else {
                super.onBackPressed();
            }
        } catch (Exception e) {
            Log.e(TAG, "Back button error", e);
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ✅ Dừng hoàn toàn khi app close
        PromotionNotificationHelper.stopRandomPromotionScheduling();
        Log.d(TAG, "MainActivity destroyed");
    }
}