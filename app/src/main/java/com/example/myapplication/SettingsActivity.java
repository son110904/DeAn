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
                Toast.makeText(this, "Đang chuyển ngôn ngữ...", Toast.LENGTH_SHORT).show()
        );

        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) ->
                Toast.makeText(this,
                        isChecked ? "Bật chế độ tối" : "Bật chế độ sáng",
                        Toast.LENGTH_SHORT).show()
        );

        btnClearData.setOnClickListener(v -> {
            TransactionStore.clearAll(this);
            Toast.makeText(this, "Đã xóa dữ liệu cục bộ", Toast.LENGTH_SHORT).show();
        });
    }
}
