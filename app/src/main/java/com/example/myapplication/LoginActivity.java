package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    EditText edtLoginEmail;
    EditText edtLoginPassword;
    Button btnLogin;
    TextView tvGoRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtLoginEmail = findViewById(R.id.edtLoginEmail);
        edtLoginPassword = findViewById(R.id.edtLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoRegister = findViewById(R.id.tvGoRegister);

        btnLogin.setOnClickListener(v -> {
            String email = edtLoginEmail.getText().toString().trim();
            String password = edtLoginPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, getString(R.string.toast_login_missing), Toast.LENGTH_SHORT).show();
                return;
            }

            ApiService apiService = RetrofitClient.getInstance(this).create(ApiService.class);
            apiService.login(new AuthRequest(email, password)).enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getUser() != null) {
                        AuthResponse authResponse = response.body();
                        AuthStore.saveToken(LoginActivity.this, authResponse.getToken());
                        AuthStore.setLoggedIn(LoginActivity.this, true);
                        AuthStore.saveProfile(
                                LoginActivity.this,
                                authResponse.getUser().getName(),
                                authResponse.getUser().getEmail()
                        );
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, getString(R.string.toast_login_failed), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<AuthResponse> call, Throwable t) {
                    Toast.makeText(LoginActivity.this,
                            getString(R.string.toast_connection_error, t.getMessage()),
                            Toast.LENGTH_SHORT).show();
                }
            });
        });

        tvGoRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }
}
