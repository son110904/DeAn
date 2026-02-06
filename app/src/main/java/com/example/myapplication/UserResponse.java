package com.example.myapplication;

import com.google.gson.annotations.SerializedName;

public class UserResponse {
    private int id;
    private String name;
    @SerializedName("email")
    private String email;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
