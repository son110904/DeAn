package com.example.myapplication;

import android.content.Context;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "http://10.0.2.2:8000/";
    private static Retrofit retrofit;

    private RetrofitClient() {
    }

    public static Retrofit getInstance(Context context) {
        if (retrofit == null) {
            Context appContext = context.getApplicationContext();
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
                    .baseUrl(BASE_URL)
                    .client(httpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
