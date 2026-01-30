package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);

        TextView emptyState = findViewById(R.id.tvEmptyState);
        RecyclerView recyclerView = findViewById(R.id.recyclerTransactions);
        TransactionAdapter adapter = new TransactionAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        Button btnAddTransaction = findViewById(R.id.btnAddTransaction);
        btnAddTransaction.setOnClickListener(v ->
                startActivity(new Intent(this, AddTransactionActivity.class))
        );

        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
        apiService.getTransactions().enqueue(new Callback<List<TransactionResponse>>() {
            @Override
            public void onResponse(Call<List<TransactionResponse>> call, Response<List<TransactionResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TransactionResponse> transactions = response.body();
                    adapter.submitList(transactions);
                    emptyState.setVisibility(transactions.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    Toast.makeText(TransactionsActivity.this,
                            "Không lấy được dữ liệu giao dịch",
                            Toast.LENGTH_SHORT).show();
                    adapter.submitList(null);
                    emptyState.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<TransactionResponse>> call, Throwable t) {
                Toast.makeText(TransactionsActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                adapter.submitList(null);
                emptyState.setVisibility(View.VISIBLE);
            }
        });
    }
}
