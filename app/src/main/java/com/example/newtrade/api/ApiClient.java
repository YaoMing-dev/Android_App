// app/src/main/java/com/example/newtrade/api/ApiClient.java
package com.example.newtrade.api;

import android.content.Context;
import android.util.Log;

import com.example.newtrade.utils.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String TAG = "ApiClient";

    private static Retrofit retrofit;
    private static Context context;

    // Service instances
    private static AuthService authService;
    private static ApiService apiService; // ✅ THÊM ApiService
    private static UserService userService;
    private static ProductService productService;
    private static ChatService chatService;
    private static OfferService offerService;
    private static TransactionService transactionService;
    private static NotificationService notificationService;
    private static SavedItemService savedItemService;
    private static ReviewService reviewService;
    private static AnalyticsService analyticsService;
    private static PaymentService paymentService;

    public static void init(Context appContext) {
        context = appContext.getApplicationContext();

        // Create HTTP logging interceptor
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Create OkHttp client with timeouts and logging
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .connectTimeout(Constants.NETWORK_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(Constants.NETWORK_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(Constants.NETWORK_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .addInterceptor(new AuthInterceptor(context))
                .retryOnConnectionFailure(true);

        // Create Gson with date format
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .setLenient()
                .create();

        // Create Retrofit instance
        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        Log.d(TAG, "✅ ApiClient initialized with base URL: " + Constants.BASE_URL);
    }

    private static Retrofit getRetrofit() {
        if (retrofit == null) {
            throw new IllegalStateException("ApiClient not initialized. Call ApiClient.init() first.");
        }
        return retrofit;
    }

    // =============================================
    // SERVICE GETTERS
    // =============================================

    public static AuthService getAuthService() {
        if (authService == null) {
            authService = getRetrofit().create(AuthService.class);
        }
        return authService;
    }

    // ✅ THÊM getApiService() để sửa lỗi compilation
    public static ApiService getApiService() {
        if (apiService == null) {
            apiService = getRetrofit().create(ApiService.class);
        }
        return apiService;
    }

    public static UserService getUserService() {
        if (userService == null) {
            userService = getRetrofit().create(UserService.class);
        }
        return userService;
    }

    public static ProductService getProductService() {
        if (productService == null) {
            productService = getRetrofit().create(ProductService.class);
        }
        return productService;
    }

    public static ChatService getChatService() {
        if (chatService == null) {
            chatService = getRetrofit().create(ChatService.class);
        }
        return chatService;
    }

    public static OfferService getOfferService() {
        if (offerService == null) {
            offerService = getRetrofit().create(OfferService.class);
        }
        return offerService;
    }

    public static TransactionService getTransactionService() {
        if (transactionService == null) {
            transactionService = getRetrofit().create(TransactionService.class);
        }
        return transactionService;
    }

    public static NotificationService getNotificationService() {
        if (notificationService == null) {
            notificationService = getRetrofit().create(NotificationService.class);
        }
        return notificationService;
    }

    public static SavedItemService getSavedItemService() {
        if (savedItemService == null) {
            savedItemService = getRetrofit().create(SavedItemService.class);
        }
        return savedItemService;
    }

    public static ReviewService getReviewService() {
        if (reviewService == null) {
            reviewService = getRetrofit().create(ReviewService.class);
        }
        return reviewService;
    }

    public static AnalyticsService getAnalyticsService() {
        if (analyticsService == null) {
            analyticsService = getRetrofit().create(AnalyticsService.class);
        }
        return analyticsService;
    }

    public static PaymentService getPaymentService() {
        if (paymentService == null) {
            paymentService = getRetrofit().create(PaymentService.class);
        }
        return paymentService;
    }
}