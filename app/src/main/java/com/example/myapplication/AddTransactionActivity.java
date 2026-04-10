package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddTransactionActivity extends AppCompatActivity {

    private static final int QR_SCAN_REQUEST_CODE = 200;
    EditText edtAmount;
    RadioGroup rgType;
    Button btnSave;
    Button btnDelete;
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
    private boolean isEditMode = false;
    private int transactionId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        edtAmount = findViewById(R.id.edtAmount);
        rgType = findViewById(R.id.rgType);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);
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

        setupCategorySpinner();

        isEditMode = getIntent().getBooleanExtra("isEdit", false);
        if (isEditMode) {
            setupEditMode();
        } else {
            btnDelete.setVisibility(View.GONE);
            selectTransactionType(true);
            setDefaultDate();
        }

        btnNewIncome.setOnClickListener(v -> selectTransactionType(false));
        btnNewExpense.setOnClickListener(v -> selectTransactionType(true));

        btnScanQR.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScannerActivity.class);
            startActivityForResult(intent, QR_SCAN_REQUEST_CODE);
        });

        setupBottomNav();

        btnSave.setOnClickListener(v -> saveTransaction());
        btnDelete.setOnClickListener(v -> showDeleteConfirmation());
    }

    private void setupEditMode() {
        transactionId = getIntent().getIntExtra("transactionId", -1);
        long amount = getIntent().getLongExtra("amount", 0);
        String category = getIntent().getStringExtra("category");
        String type = getIntent().getStringExtra("type");
        String note = getIntent().getStringExtra("note");
        String date = getIntent().getStringExtra("date");

        edtAmount.setText(String.valueOf(amount));
        edtNote.setText(note);
        edtDate.setText(date);
        
        selectTransactionType("expense".equalsIgnoreCase(type));
        
        if (isExpense) {
            selectCategoryInSpinner(category);
        }

        btnSave.setText("Cập nhật");
        btnDelete.setVisibility(View.VISIBLE);
        setTitle("Sửa giao dịch");
    }

    private void setDefaultDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = dateFormat.format(calendar.getTime());
        edtDate.setText(currentDate);
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.menu_add);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (id == R.id.menu_budget) {
                startActivity(new Intent(this, BudgetActivity.class));
                return true;
            } else if (id == R.id.menu_add) {
                return true;
            } else if (id == R.id.menu_stats) {
                startActivity(new Intent(this, StatisticsActivity.class));
                return true;
            } else if (id == R.id.menu_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
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

        String category = spinnerCategory.getSelectedItem().toString();
        String note = edtNote.getText().toString().trim();
        String date = edtDate.getText().toString().trim();
        String type = isExpense ? "expense" : "income";
        String requestCategory = isExpense ? category : getString(R.string.transaction_income_default);

        TransactionRequest request = new TransactionRequest((int) amount, requestCategory, type, note, date);
        ApiService apiService = RetrofitClient.getInstance(this).create(ApiService.class);

        if (isEditMode) {
            apiService.updateTransaction(transactionId, request).enqueue(new Callback<TransactionResponse>() {
                @Override
                public void onResponse(Call<TransactionResponse> call, Response<TransactionResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AddTransactionActivity.this, "Đã cập nhật giao dịch", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
                @Override
                public void onFailure(Call<TransactionResponse> call, Throwable t) {
                    Toast.makeText(AddTransactionActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            apiService.createTransaction(request).enqueue(new Callback<TransactionResponse>() {
                @Override
                public void onResponse(Call<TransactionResponse> call, Response<TransactionResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AddTransactionActivity.this, getString(R.string.toast_add_success), Toast.LENGTH_SHORT).show();
                        resetFormForNewTransaction();
                    }
                }
                @Override
                public void onFailure(Call<TransactionResponse> call, Throwable t) {
                    Toast.makeText(AddTransactionActivity.this, getString(R.string.toast_connection_error, t.getMessage()), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void resetFormForNewTransaction() {
        edtAmount.setText("");
        edtNote.setText("");
        setDefaultDate();
        selectTransactionType(true);
        spinnerCategory.setSelection(0);
        edtAmount.requestFocus();
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa giao dịch")
                .setMessage("Bạn có chắc chắn muốn xóa giao dịch này không?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteTransaction())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteTransaction() {
        ApiService apiService = RetrofitClient.getInstance(this).create(ApiService.class);
        apiService.deleteTransaction(transactionId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddTransactionActivity.this, "Đã xóa giao dịch", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(AddTransactionActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == QR_SCAN_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            parseQrData(data.getStringExtra("QR_RESULT"));
        }
    }

    private void parseQrData(String data) {
        if (data == null || data.isEmpty()) return;
        try {
            if (data.startsWith("000201")) {
                parseVietQr(data);
            } else {
                String[] lines = data.split("[;\n|,]");
                for (String line : lines) {
                    String[] kv = line.split("[=:]");
                    if (kv.length < 2) continue;
                    String key = kv[0].trim().toLowerCase();
                    String value = kv[1].trim();

                    if (key.contains("amount") || key.contains("tiền")) {
                        edtAmount.setText(extractDigits(value));
                    } else if (key.contains("note") || key.contains("ghi chú")) {
                        edtNote.setText(value);
                    } else if (key.contains("category") || key.contains("cat") || key.contains("loại")) {
                        selectCategoryInSpinner(value);
                    }
                }
            }
            Toast.makeText(this, "Đã nhập dữ liệu từ QR", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            edtNote.setText(data);
        }
    }

    private void parseVietQr(String data) {
        Pattern amountPattern = Pattern.compile("54(\\d{2})(\\d+)");
        Matcher matcher = amountPattern.matcher(data);
        if (matcher.find()) {
            int length = Integer.parseInt(matcher.group(1));
            String amount = matcher.group(2);
            edtAmount.setText(amount.substring(0, Math.min(amount.length(), length)));
        }
        // Thử đoán category từ nội dung nếu có
        if (data.contains("AN UONG") || data.contains("PHO") || data.contains("COFFEE")) {
            selectCategoryInSpinner("Ăn uống");
        }
    }

    private void selectCategoryInSpinner(String value) {
        if (value == null) return;
        ArrayAdapter adapter = (ArrayAdapter) spinnerCategory.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().toLowerCase().contains(value.toLowerCase())) {
                spinnerCategory.setSelection(i);
                break;
            }
        }
    }

    private String extractDigits(String input) {
        return input.replaceAll("[^\\d]", "");
    }

    private void setupCategorySpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.transaction_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void selectTransactionType(boolean expenseSelected) {
        isExpense = expenseSelected;
        btnNewExpense.setBackgroundResource(expenseSelected ? R.drawable.bg_segmented_selected : android.R.color.transparent);
        btnNewExpense.setTextColor(getColor(expenseSelected ? R.color.primary_blue : R.color.text_secondary));
        btnNewIncome.setBackgroundResource(!expenseSelected ? R.drawable.bg_segmented_selected : android.R.color.transparent);
        btnNewIncome.setTextColor(getColor(!expenseSelected ? R.color.primary_blue : R.color.text_secondary));
        categorySection.setVisibility(expenseSelected ? View.VISIBLE : View.GONE);
    }

    private long parseAmount(String raw) {
        if (raw == null) return 0L;
        String digits = extractDigits(raw);
        return digits.isEmpty() ? 0L : Long.parseLong(digits);
    }
}
