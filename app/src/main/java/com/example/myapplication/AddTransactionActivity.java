package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AddTransactionActivity extends AppCompatActivity {

    EditText edtAmount;
    RadioGroup rgType;
    Button btnSave;
    BottomNavigationView bottomNav;
    Spinner spinnerCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        edtAmount = findViewById(R.id.edtAmount);
        rgType = findViewById(R.id.rgType);
        btnSave = findViewById(R.id.btnSave);
        bottomNav = findViewById(R.id.bottomNav);
        spinnerCategory = findViewById(R.id.spinnerCategory);

        // Thiết lập spinner categories
        setupCategorySpinner();

        // Đánh dấu tab hiện tại
        bottomNav.setSelectedItemId(R.id.menu_add);

        // Xử lý điều hướng
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.menu_home) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.menu_add) {
                return true;
            } else if (id == R.id.menu_stats) {
                startActivity(new Intent(this, StatisticsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.menu_forecast) {
                startActivity(new Intent(this, ForecastActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });

        // Xử lý lưu giao dịch
        btnSave.setOnClickListener(v -> {
            String amount = edtAmount.getText().toString();
            int checkedId = rgType.getCheckedRadioButtonId();

            if (amount.isEmpty() || checkedId == -1) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton rb = findViewById(checkedId);
            String account = rb.getText().toString();
            String category = spinnerCategory.getSelectedItem().toString();

            Toast.makeText(this,
                    "Đã lưu: " + amount + " euro\n" +
                            "Tài khoản: " + account + "\n" +
                            "Danh mục: " + category,
                    Toast.LENGTH_LONG).show();

            // Reset form
            edtAmount.setText("");
            rgType.clearCheck();
            spinnerCategory.setSelection(0);
        });
    }

    private void setupCategorySpinner() {
        // Danh sách categories
        String[] categories = {
                "Chọn danh mục",
                "🍜 Ăn uống",
                "🚗 Giao thông",
                "🏠 Nhà ở",
                "🎮 Giải trí",
                "🛒 Mua sắm",
                "💊 Y tế",
                "📚 Giáo dục",
                "💰 Khác"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }
}