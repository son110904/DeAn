package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button btnClearData = findViewById(R.id.btnClearData);
        EditText etServerUrl = findViewById(R.id.etServerUrl);
        Button btnSaveServer = findViewById(R.id.btnSaveServer);

        etServerUrl.setText(ApiConfigStore.getBaseUrl(this));

        btnSaveServer.setOnClickListener(v -> {
            String enteredValue = etServerUrl.getText().toString();
            String normalized = ApiConfigStore.normalize(enteredValue);
            ApiConfigStore.saveBaseUrl(this, normalized);
            etServerUrl.setText(normalized);
            Toast.makeText(this, getString(R.string.toast_server_saved, normalized), Toast.LENGTH_LONG).show();
        });

        btnClearData.setOnClickListener(v -> {
            TransactionStore.clearAll(this);
            Toast.makeText(this, getString(R.string.toast_clear_data), Toast.LENGTH_SHORT).show();
        });
    }
}
