package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddTransactionActivity extends AppCompatActivity {

    private static final String DEFAULT_CATEGORY = "Chọn danh mục";

    EditText edtAmount;
    RadioGroup rgType;
    Button btnSave;
    BottomNavigationView bottomNav;
    Spinner spinnerCategory;
    TextView btnNewIncome;
    TextView btnNewExpense;
    TextView tvAccountLabel;
    LinearLayout categorySection;
    RadioButton rbBank;
    RadioButton rbCredit;
    RadioButton rbCash;

    boolean isExpense = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        edtAmount = findViewById(R.id.edtAmount);
        rgType = findViewById(R.id.rgType);
        btnSave = findViewById(R.id.btnSave);
        bottomNav = findViewById(R.id.bottomNav);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnNewIncome = findViewById(R.id.btnNewIncome);
        btnNewExpense = findViewById(R.id.btnNewExpense);
        tvAccountLabel = findViewById(R.id.tvAccountLabel);
        categorySection = findViewById(R.id.categorySection);
        rbBank = findViewById(R.id.rbBank);
        rbCredit = findViewById(R.id.rbCredit);
        rbCash = findViewById(R.id.rbCash);

        // Thiết lập spinner categories
        setupCategorySpinner();

        btnNewIncome.setOnClickListener(v -> selectTransactionType(false));
        btnNewExpense.setOnClickListener(v -> selectTransactionType(true));
        selectTransactionType(true);

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
            String amountText = edtAmount.getText().toString();
            long amount = parseAmount(amountText);
            int checkedId = rgType.getCheckedRadioButtonId();

            if (amount <= 0 || checkedId == -1) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton rb = findViewById(checkedId);
            String account = rb.getText().toString();
            String category = spinnerCategory.getSelectedItem().toString();

            if (isExpense && DEFAULT_CATEGORY.equals(category)) {
                Toast.makeText(this, "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean expenseSelected = isExpense;
            String categoryKey = expenseSelected ? TransactionStore.normalizeCategory(category) : "Thu nhập";
            String typeValue = expenseSelected ? "expense" : "income";
            String accountValue = account;
            long amountValue = amount;
            TransactionCreateRequest payload = new TransactionCreateRequest(
                    (int) amountValue,
                    categoryKey,
                    typeValue,
                    accountValue
            );

            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            apiService.createTransaction(payload).enqueue(new Callback<TransactionResponse>() {
                @Override
                public void onResponse(Call<TransactionResponse> call, Response<TransactionResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        if (expenseSelected) {
                            TransactionStore.addExpense(AddTransactionActivity.this, amountValue, accountValue, categoryKey);
                            Toast.makeText(AddTransactionActivity.this,
                                    "Đã lưu: Chi " + TransactionStore.formatCurrency(amountValue) + "\n" +
                                            "Tài khoản: " + accountValue + "\n" +
                                            "Danh mục: " + categoryKey,
                                    Toast.LENGTH_LONG).show();
                        } else {
                            TransactionStore.addIncome(AddTransactionActivity.this, amountValue, accountValue);
                            Toast.makeText(AddTransactionActivity.this,
                                    "Đã lưu: Thu " + TransactionStore.formatCurrency(amountValue) + "\n" +
                                            "Tài khoản nhận: " + accountValue,
                                    Toast.LENGTH_LONG).show();
                        }

                        // Reset form
                        edtAmount.setText("");
                        rgType.clearCheck();
                        spinnerCategory.setSelection(0);
                        selectTransactionType(isExpense);
                    } else {
                        Toast.makeText(AddTransactionActivity.this,
                                "Không thể lưu giao dịch. Vui lòng thử lại.",
                                Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<TransactionResponse> call, Throwable t) {
                    Toast.makeText(AddTransactionActivity.this,
                            "Lỗi kết nối: " + t.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void setupCategorySpinner() {
        // Danh sách categories
        String[] categories = {
                DEFAULT_CATEGORY,
                "🍜 Ăn uống",
                "🚗 Giao thông vận tải",
                "🏠 Nhà ở",
                "🎮 Giải trí",
                "🛒 Mua sắm",
                "💊 Y tế",
                "📚 Giáo dục",
                "🖼️ Sở thích",
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

    private void selectTransactionType(boolean expenseSelected) {
        isExpense = expenseSelected;

        if (expenseSelected) {
            btnNewExpense.setBackgroundResource(R.drawable.bg_segmented_selected);
            btnNewExpense.setTextColor(getColor(R.color.primary_blue));
            btnNewIncome.setBackgroundResource(android.R.color.transparent);
            btnNewIncome.setTextColor(getColor(R.color.text_secondary));
            tvAccountLabel.setText("Từ tài khoản");
            categorySection.setVisibility(View.VISIBLE);
            rbCredit.setVisibility(View.VISIBLE);
        } else {
            btnNewIncome.setBackgroundResource(R.drawable.bg_segmented_selected);
            btnNewIncome.setTextColor(getColor(R.color.primary_blue));
            btnNewExpense.setBackgroundResource(android.R.color.transparent);
            btnNewExpense.setTextColor(getColor(R.color.text_secondary));
            tvAccountLabel.setText("Thu về");
            categorySection.setVisibility(View.GONE);
            rbCredit.setVisibility(View.GONE);
            if (rbCredit.isChecked()) {
                rgType.clearCheck();
            }
        }
    }

    private long parseAmount(String raw) {
        if (raw == null) {
            return 0L;
        }
        String digits = raw.replaceAll("[^\\d]", "");
        if (digits.isEmpty()) {
            return 0L;
        }
        return Long.parseLong(digits);
    }
}
