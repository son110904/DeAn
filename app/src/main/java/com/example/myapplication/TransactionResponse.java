package com.example.myapplication;

import com.google.gson.annotations.SerializedName;

public class TransactionResponse {
    private int id;
    private int amount;
    private String category;
    private String type;
    private String note;
    @SerializedName("created_at")
    private String createdAt;

    public int getId() {
        return id;
    }

    public int getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public String getType() {
        return type;
    }

    public String getNote() {
        return note;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
