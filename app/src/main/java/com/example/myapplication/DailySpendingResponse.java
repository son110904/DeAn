package com.example.myapplication;

import com.google.gson.annotations.SerializedName;

public class DailySpendingResponse {
    @SerializedName("date")
    private String date;
    
    @SerializedName("amount")
    private long amount;

    public String getDate() { return date; }
    public long getAmount() { return amount; }
}
