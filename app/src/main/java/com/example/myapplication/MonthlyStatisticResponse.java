package com.example.myapplication;

public class MonthlyStatisticResponse {
    private String month;
    private int income;
    private int expense;

    public MonthlyStatisticResponse(String month, int income, int expense) {
    }

    public String getMonth() {
        return month;
    }

    public int getIncome() {
        return income;
    }

    public int getExpense() {
        return expense;
    }
}
