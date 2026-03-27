package com.example.myapplication;

import com.google.gson.annotations.SerializedName;

public class BudgetResponse {
    @SerializedName("id")
    private int id;
    
    @SerializedName("category")
    private String category;
    
    @SerializedName("limit_amount")
    private long limitAmount;
    
    @SerializedName("current_spent")
    private long currentSpent;

    public BudgetResponse() {}

    public BudgetResponse(String category, long limitAmount) {
        this.category = category;
        this.limitAmount = limitAmount;
    }

    public int getId() { return id; }
    public String getCategory() { return category; }
    public long getLimitAmount() { return limitAmount; }
    public long getCurrentSpent() { return currentSpent; }

    public void setCategory(String category) { this.category = category; }
    public void setLimitAmount(long limitAmount) { this.limitAmount = limitAmount; }
}
