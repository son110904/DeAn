package com.example.myapplication;

import android.content.Context;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit;
    private static String activeBaseUrl = "";

    private RetrofitClient() {
    }

    public static synchronized Retrofit getInstance(Context context) {
        Context appContext = context.getApplicationContext();
        String baseUrl = ApiConfigStore.getBaseUrl(appContext);

        if (retrofit == null || !baseUrl.equals(activeBaseUrl)) {
            Interceptor authInterceptor = chain -> {
                Request original = chain.request();
                String token = AuthStore.getToken(appContext);
                if (token.isEmpty()) {
                    return chain.proceed(original);
                }

                Request authorized = original.newBuilder()
                        .header("Authorization", "Bearer " + token)
                        .build();
                return chain.proceed(authorized);
            };

            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(httpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            activeBaseUrl = baseUrl;
        }

        return retrofit;
    }
}
