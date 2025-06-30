// app/src/main/java/com/example/newtrade/api/ApiClient.java
package com.example.newtrade.api;

import android.content.Context;
import com.example.newtrade.utils.Constants;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    private static Retrofit retrofit;
    private static AuthService authService;
    private static ProductService productService;
    private static ChatService chatService;

    public static void init(Context context) {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(logging);

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
}