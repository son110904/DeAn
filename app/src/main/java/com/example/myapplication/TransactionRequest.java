package com.example.myapplication;

public class TransactionRequest {
    private final int amount;
    private final String category;
    private final String type;
    private final String note;
    private final String date;

    public TransactionRequest(int amount, String category, String type, String note, String date) {
        this.amount = amount;
        this.category = category;
        this.type = type;
        this.note = note;
        this.date = date;
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
}
