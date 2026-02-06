package com.example.myapplication;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @POST("auth/login")
    Call<AuthResponse> login(@Body AuthRequest request);

    @POST("auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @GET("transactions")
    Call<List<TransactionResponse>> getTransactions();

    @POST("transactions")
    Call<TransactionResponse> createTransaction(@Body TransactionRequest request);

    @GET("statistics/monthly")
    Call<List<MonthlyStatisticResponse>> getMonthlyStatistics();
}
