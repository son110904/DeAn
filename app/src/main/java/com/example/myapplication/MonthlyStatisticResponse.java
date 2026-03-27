package com.example.myapplication;

import com.google.gson.annotations.SerializedName;

public class MonthlyStatisticResponse {
    @SerializedName("month")
    private String month;

    @SerializedName("income")
    private int income;

    @SerializedName("expense")
    private int expense;

    public MonthlyStatisticResponse() {}

    public MonthlyStatisticResponse(String month, int income, int expense) {
        this.month = month;
        this.income = income;
        this.expense = expense;
    }

    public String getMonth() { return month; }
    public int getIncome()   { return income; }
    public int getExpense()  { return expense; }
}
