package com.example.newtrade.api;

import com.example.newtrade.utils.Constants;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class ApiClient {
    private static Retrofit retrofit = null;
    private static ApiService apiService = null;

    public static ApiService getApiService() {
        if (apiService == null) {
            apiService = getClient().create(ApiService.class);
        }
        return apiService;
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Create logging interceptor
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Create OkHttp client with timeouts and logging
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .connectTimeout(Constants.NETWORK_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(Constants.NETWORK_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(Constants.NETWORK_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(logging);

            // Add auth interceptor
            httpClient.addInterceptor(chain -> {
                okhttp3.Request original = chain.request();
                okhttp3.Request.Builder requestBuilder = original.newBuilder();

                // Add auth header if available
                // String token = SharedPrefsManager.getInstance().getToken();
                // if (token != null) {
                //     requestBuilder.header("Authorization", "Bearer " + token);
                // }

                requestBuilder.header("Content-Type", "application/json");
                requestBuilder.method(original.method(), original.body());

                return chain.proceed(requestBuilder.build());
            });

            retrofit = new Retrofit.Builder()
                .baseUrl(Constants.API_BASE_URL + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        }
        return retrofit;
    }

    public static void clearClient() {
        retrofit = null;
        apiService = null;
    }
}
