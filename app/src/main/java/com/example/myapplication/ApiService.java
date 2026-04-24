package com.example.myapplication;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
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

    @PUT("transactions/{id}")
    Call<TransactionResponse> updateTransaction(@Path("id") int id, @Body TransactionRequest request);

    @DELETE("transactions/{id}")
    Call<ResponseBody> deleteTransaction(@Path("id") int id);


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

    @PUT("budgets/{id}")
    Call<BudgetResponse> updateBudget(@Path("id") int id, @Body BudgetResponse budget);

    @DELETE("budgets/{id}")
    Call<ResponseBody> deleteBudget(@Path("id") int id);

    // QR Scanning - analyze QR content if needed
    @POST("qr/analyze")
    Call<TransactionRequest> analyzeQr(@Body String qrContent);
}
