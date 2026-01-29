package com.example.myapplication;

public class TransactionCreateRequest {
    private final int amount;
    private final String category;
    private final String type;
    private final String note;

    public TransactionCreateRequest(int amount, String category, String type, String note) {
        this.amount = amount;
        this.category = category;
        this.type = type;
        this.note = note;
    }
}
