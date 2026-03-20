package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddTransactionActivity extends AppCompatActivity {

    private static final int QR_SCAN_REQUEST_CODE = 200;
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
    EditText edtNote;
    EditText edtDate;
    ImageButton btnScanQR;

    boolean isExpense = true;
    private String defaultCategory;

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
        edtNote = findViewById(R.id.edtNote);
        edtDate = findViewById(R.id.edtDate);
        btnScanQR = findViewById(R.id.btnScanQR);
        defaultCategory = getString(R.string.category_default);

        setupCategorySpinner();

        btnNewIncome.setOnClickListener(v -> selectTransactionType(false));
        btnNewExpense.setOnClickListener(v -> selectTransactionType(true));
        selectTransactionType(true);

        btnScanQR.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScannerActivity.class);
            startActivityForResult(intent, QR_SCAN_REQUEST_CODE);
        });

        bottomNav.setSelectedItemId(R.id.menu_add);
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
            } else if (id == R.id.menu_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        btnSave.setOnClickListener(v -> {
            saveTransaction();
        });
    }

    private void saveTransaction() {
        String amountText = edtAmount.getText().toString();
        long amount = parseAmount(amountText);
        int checkedId = rgType.getCheckedRadioButtonId();

        if (amount <= 0 || checkedId == -1) {
            Toast.makeText(this, getString(R.string.toast_add_missing), Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton rb = findViewById(checkedId);
        String account = rb.getText().toString();
        String category = spinnerCategory.getSelectedItem().toString();
        String note = edtNote.getText().toString().trim();
        String date = edtDate.getText().toString().trim();
        String notePayload = buildNotePayload(note, date, account);

        if (isExpense && getString(R.string.transaction_category_placeholder).equals(category)) {
            Toast.makeText(this, getString(R.string.toast_add_missing_category), Toast.LENGTH_SHORT).show();
            return;
        }

        String type = isExpense ? "expense" : "income";
        String requestCategory = isExpense ? category : getString(R.string.transaction_income_default);
        TransactionRequest request = new TransactionRequest(
                (int) amount,
                requestCategory,
                type,
                notePayload,
                date
        );

        ApiService apiService = RetrofitClient.getInstance(this).create(ApiService.class);
        apiService.createTransaction(request).enqueue(new Callback<TransactionResponse>() {
            @Override
            public void onResponse(Call<TransactionResponse> call, Response<TransactionResponse> response) {
                if (response.isSuccessful()) {
                    if (isExpense) {
                        String categoryKey = TransactionStore.normalizeCategory(AddTransactionActivity.this, category);
                        TransactionStore.addExpense(AddTransactionActivity.this, amount, notePayload, categoryKey);
                    } else {
                        TransactionStore.addIncome(AddTransactionActivity.this, amount, notePayload);
                    }
                    Toast.makeText(AddTransactionActivity.this, getString(R.string.toast_add_success), Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddTransactionActivity.this, getString(R.string.toast_add_failed), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TransactionResponse> call, Throwable t) {
                Toast.makeText(AddTransactionActivity.this, getString(R.string.toast_connection_error, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == QR_SCAN_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String qrResult = data.getStringExtra("QR_RESULT");
            parseQrData(qrResult);
        }
    }

    private void parseQrData(String data) {
        try {
            // Support both ";" or newline as delimiters, and both "=" or ":" as key-value separators
            String[] parts = data.split("[;\n]");
            for (String part : parts) {
                String[] kv = part.split("[=:]");
                if (kv.length < 2) continue;

                String key = kv[0].trim().toLowerCase();
                String value = kv[1].trim();

                switch (key) {
                    case "amount":
                        edtAmount.setText(value);
                        break;
                    case "note":
                        edtNote.setText(value);
                        break;
                    case "category":
                    case "cat":
                        ArrayAdapter adapter = (ArrayAdapter) spinnerCategory.getAdapter();
                        for (int i = 0; i < adapter.getCount(); i++) {
                            if (adapter.getItem(i).toString().toLowerCase().contains(value.toLowerCase())) {
                                spinnerCategory.setSelection(i);
                                break;
                            }
                        }
                        break;
                    case "type":
                        selectTransactionType(value.equalsIgnoreCase("expense"));
                        break;
                }
            }
            Toast.makeText(this, "Đã nhập dữ liệu từ QR", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Định dạng QR không hợp lệ", Toast.LENGTH_SHORT).show();
            edtNote.setText(data); // Fallback: put raw content in note
        }
    }

    private void setupCategorySpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.transaction_categories,
                android.R.layout.simple_spinner_item
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
            tvAccountLabel.setText(getString(R.string.add_transaction_account_from));
            categorySection.setVisibility(View.VISIBLE);
            rbCredit.setVisibility(View.VISIBLE);
        } else {
            btnNewIncome.setBackgroundResource(R.drawable.bg_segmented_selected);
            btnNewIncome.setTextColor(getColor(R.color.primary_blue));
            btnNewExpense.setBackgroundResource(android.R.color.transparent);
            btnNewExpense.setTextColor(getColor(R.color.text_secondary));
            tvAccountLabel.setText(getString(R.string.add_transaction_account_to));
            categorySection.setVisibility(View.GONE);
            rbCredit.setVisibility(View.GONE);
            if (rbCredit.isChecked()) rgType.clearCheck();
        }
    }

    private long parseAmount(String raw) {
        if (raw == null) return 0L;
        String digits = raw.replaceAll("[^\\d]", "");
        if (digits.isEmpty()) return 0L;
        return Long.parseLong(digits);
    }

    private String buildNotePayload(String note, String date, String account) {
        StringBuilder builder = new StringBuilder();
        if (note != null && !note.isEmpty()) builder.append(note);
        if (date != null && !date.isEmpty()) {
            if (builder.length() > 0) builder.append(getString(R.string.separator_dot));
            builder.append(date);
        }
        if (account != null && !account.isEmpty()) {
            if (builder.length() > 0) builder.append(getString(R.string.separator_dot));
            builder.append(account);
        }
        return builder.toString();
    }
}
