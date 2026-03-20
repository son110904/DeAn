package com.example.myapplication;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

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

    // Advanced Statistics
    @GET("summary/daily")
    Call<List<DailySpendingResponse>> getDailySummary(@Query("month") String month);

    // Budget features
    @GET("budgets")
    Call<List<BudgetResponse>> getBudgets();

    @POST("budgets")
    Call<ResponseBody> saveBudget(@Body BudgetResponse budget);

    // QR Scanning - analyze QR content if needed
    @POST("qr/analyze")
    Call<TransactionRequest> analyzeQr(@Body String qrContent);
}
