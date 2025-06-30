// app/src/main/java/com/example/newtrade/api/ApiClient.java
package com.example.newtrade.api;

import android.content.Context;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.SharedPrefsManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    private static Retrofit retrofit;
    private static Context appContext;

    // Service instances
    private static AuthService authService;
    private static ProductService productService;
    private static ChatService chatService;
    private static UserService userService;
    private static OfferService offerService;

    public static void init(Context context) {
        appContext = context.getApplicationContext();

        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(logging)
                    .addInterceptor(chain -> {
                        // Add User-ID header if available
                        SharedPrefsManager prefs = new SharedPrefsManager(appContext);
                        Request original = chain.request();
                        Request.Builder requestBuilder = original.newBuilder();

                        // Add User-ID header for authenticated requests
                        if (prefs.isLoggedIn()) {
                            Long userId = prefs.getUserId();
                            if (userId != null) {
                                requestBuilder.header("User-ID", String.valueOf(userId));
                            }
                        }

                        Request request = requestBuilder.build();
                        return chain.proceed(request);
                    });

            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
    }

    public static AuthService getAuthService() {
        if (authService == null) {
            authService = retrofit.create(AuthService.class);
        }
        return authService;
    }

    public static ProductService getProductService() {
        if (productService == null) {
            productService = retrofit.create(ProductService.class);
        }
        return productService;
    }

    public static ChatService getChatService() {
        if (chatService == null) {
            chatService = retrofit.create(ChatService.class);
        }
        return chatService;
    }

    public static UserService getUserService() {
        if (userService == null) {
            userService = retrofit.create(UserService.class);
        }
        return userService;
    }

    public static OfferService getOfferService() {
        if (offerService == null) {
            offerService = retrofit.create(OfferService.class);
        }
        return offerService;
    }

    // Helper method to get Retrofit instance
    public static Retrofit getRetrofit() {
        return retrofit;
    }

    // Clear service instances on logout
    public static void clearServices() {
        authService = null;
        productService = null;
        chatService = null;
        userService = null;
        offerService = null;
    }
}