package com.example.myapplication;

public class AuthResponse {
    private String token;
    private UserResponse user;

    public String getToken() {
        return token;
    }

    public UserResponse getUser() {
        return user;
    }
}
