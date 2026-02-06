package com.example.myapplication;

public class AuthRequest {
    private final String email;
    private final String password;

    public AuthRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
