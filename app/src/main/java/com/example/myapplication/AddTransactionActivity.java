package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AddTransactionActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "finance_prefs";
    private static final String KEY_INCOME_TOTAL = "income_total";
    private static final String KEY_EXPENSE_TOTAL = "expense_total";

    EditText edtAmount;
    RadioGroup rgType;
    Button btnSave;
    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        edtAmount = findViewById(R.id.edtAmount);
        rgType = findViewById(R.id.rgType);
        btnSave = findViewById(R.id.btnSave);
        bottomNav = findViewById(R.id.bottomNav);

        // Đánh dấu tab hiện tại
        bottomNav.setSelectedItemId(R.id.menu_add);

        // Xử lý điều hướng
        bottomNav.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.menu_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;

            } else if (id == R.id.menu_add) {
                return true;

            } else if (id == R.id.menu_stats) {
                startActivity(new Intent(this, StatisticsActivity.class));
                return true;

            } else if (id == R.id.menu_forecast) {
                startActivity(new Intent(this, ForecastActivity.class));
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

            long value;
            try {
                value = Long.parseLong(amount);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            if (value <= 0) {
                Toast.makeText(this, "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton rb = findViewById(checkedId);
            String type = rb.getText().toString();

            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            long currentIncome = prefs.getLong(KEY_INCOME_TOTAL, 0);
            long currentExpense = prefs.getLong(KEY_EXPENSE_TOTAL, 0);

            if (checkedId == R.id.rbIncome) {
                currentIncome += value;
            } else if (checkedId == R.id.rbExpense) {
                currentExpense += value;
            }

            prefs.edit()
                    .putLong(KEY_INCOME_TOTAL, currentIncome)
                    .putLong(KEY_EXPENSE_TOTAL, currentExpense)
                    .apply();

            Toast.makeText(this,
                    "Đã lưu: " + type + " - " + amount + "đ",
                    Toast.LENGTH_LONG).show();

            edtAmount.setText("");
            rgType.clearCheck();
        });
    }
}
