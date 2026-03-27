package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BudgetActivity extends AppCompatActivity {

    private RecyclerView rvBudgets;
    private BudgetAdapter adapter;
    private List<BudgetResponse> budgetList = new ArrayList<>();
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        rvBudgets = findViewById(R.id.rvBudgets);
        Button btnAddBudget = findViewById(R.id.btnAddBudget);
        bottomNav = findViewById(R.id.bottomNav);

        rvBudgets.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BudgetAdapter(this, budgetList);
        rvBudgets.setAdapter(adapter);

        btnAddBudget.setOnClickListener(v -> showAddBudgetDialog());

        bottomNav.setSelectedItemId(R.id.menu_budget);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (id == R.id.menu_budget) {
                return true;
            } else if (id == R.id.menu_add) {
                startActivity(new Intent(this, AddTransactionActivity.class));
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

        fetchBudgets();
    }

    private void fetchBudgets() {
        ApiService apiService = RetrofitClient.getInstance(this).create(ApiService.class);
        apiService.getBudgets().enqueue(new Callback<List<BudgetResponse>>() {
            @Override
            public void onResponse(Call<List<BudgetResponse>> call, Response<List<BudgetResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    budgetList.clear();
                    budgetList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onFailure(Call<List<BudgetResponse>> call, Throwable t) {
                Toast.makeText(BudgetActivity.this, "Lỗi tải ngân sách", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddBudgetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_budget, null);
        builder.setView(dialogView);

        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerBudgetCategory);
        EditText edtLimit = dialogView.findViewById(R.id.edtBudgetLimit);
        Button btnSave = dialogView.findViewById(R.id.btnSaveBudget);

        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                this, R.array.transaction_categories, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String category = spinnerCategory.getSelectedItem().toString();
            String limitStr = edtLimit.getText().toString().trim();
            String digits = limitStr.replaceAll("[^\\d]", "");
            if (digits.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                return;
            }

            long limit = Long.parseLong(digits);
            saveBudgetToServer(category, limit, dialog);
        });

        dialog.show();
    }

    private void saveBudgetToServer(String category, long limit, AlertDialog dialog) {
        BudgetResponse request = new BudgetResponse(category, limit);
        ApiService apiService = RetrofitClient.getInstance(this).create(ApiService.class);
        
        apiService.saveBudget(request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(BudgetActivity.this, "Đã thiết lập ngân sách", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    fetchBudgets();
                } else {
                    Toast.makeText(BudgetActivity.this, "Không thể lưu ngân sách", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(BudgetActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

