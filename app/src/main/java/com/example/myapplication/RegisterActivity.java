package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    EditText edtRegisterName;
    EditText edtRegisterEmail;
    EditText edtRegisterPassword;
    EditText edtRegisterConfirm;
    Button btnRegister;
    TextView tvGoLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtRegisterName = findViewById(R.id.edtRegisterName);
        edtRegisterEmail = findViewById(R.id.edtRegisterEmail);
        edtRegisterPassword = findViewById(R.id.edtRegisterPassword);
        edtRegisterConfirm = findViewById(R.id.edtRegisterConfirm);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoLogin = findViewById(R.id.tvGoLogin);

        btnRegister.setOnClickListener(v -> {
            String name = edtRegisterName.getText().toString().trim();
            String email = edtRegisterEmail.getText().toString().trim();
            String password = edtRegisterPassword.getText().toString().trim();
            String confirm = edtRegisterConfirm.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, getString(R.string.toast_register_missing), Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirm)) {
                Toast.makeText(this, getString(R.string.toast_register_mismatch), Toast.LENGTH_SHORT).show();
                return;
            }

            AuthStore.saveProfile(this, name, email);
            Toast.makeText(this, getString(R.string.toast_register_success), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        tvGoLogin.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class))
        );
    }
}
