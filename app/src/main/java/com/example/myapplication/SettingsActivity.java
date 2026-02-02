package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Switch switchTheme = findViewById(R.id.switchTheme);
        Button btnLanguage = findViewById(R.id.btnLanguage);
        Button btnClearData = findViewById(R.id.btnClearData);

        btnLanguage.setOnClickListener(v ->
                Toast.makeText(this, getString(R.string.toast_switching_language), Toast.LENGTH_SHORT).show()
        );

        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) ->
                Toast.makeText(this,
                        isChecked ? getString(R.string.toast_dark_mode_on) : getString(R.string.toast_dark_mode_off),
                        Toast.LENGTH_SHORT).show()
        );

        btnClearData.setOnClickListener(v -> {
            TransactionStore.clearAll(this);
            Toast.makeText(this, getString(R.string.toast_clear_data), Toast.LENGTH_SHORT).show();
        });
    }
}
