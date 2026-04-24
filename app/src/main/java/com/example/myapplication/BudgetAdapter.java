package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private final List<BudgetResponse> budgetList;
    private final Context context;
    private final OnBudgetClickListener clickListener;

    public interface OnBudgetClickListener {
        void onBudgetClick(BudgetResponse budget);
    }

    public BudgetAdapter(Context context, List<BudgetResponse> budgetList) {
        this(context, budgetList, null);
    }

    public BudgetAdapter(Context context, List<BudgetResponse> budgetList, OnBudgetClickListener clickListener) {
        this.context = context;
        this.budgetList = budgetList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int i) {
        BudgetResponse budget = budgetList.get(i);
        holder.tvCategory.setText(budget.getCategory());
        holder.tvLimit.setText("Hạn mức: " + TransactionStore.formatCurrency(budget.getLimitAmount()));
        holder.tvSpent.setText("Đã dùng: " + TransactionStore.formatCurrency(budget.getCurrentSpent()));

        int percent = 0;
        if (budget.getLimitAmount() > 0) {
            percent = (int) ((budget.getCurrentSpent() * 100) / budget.getLimitAmount());
        }
        holder.pbBudget.setProgress(Math.min(percent, 100));
        holder.tvPercent.setText(percent + "%");

        if (percent >= 100) {
            holder.pbBudget.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.progress_budget_red));
            holder.tvWarning.setVisibility(View.VISIBLE);
            holder.tvWarning.setText("Cảnh báo: Đã vượt hạn mức!");
        } else if (percent >= 80) {
            holder.pbBudget.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.progress_budget_yellow));
            holder.tvWarning.setVisibility(View.VISIBLE);
            holder.tvWarning.setText("Cảnh báo: Sắp chạm hạn mức!");
        } else {
            holder.pbBudget.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.progress_budget));
            holder.tvWarning.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onBudgetClick(budget);
            }
        });
    }

    @Override
    public int getItemCount() {
        return budgetList.size();
    }

    static class BudgetViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvLimit, tvSpent, tvPercent, tvWarning;
        ProgressBar pbBudget;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvBudgetCategory);
            tvLimit = itemView.findViewById(R.id.tvLimitAmount);
            tvSpent = itemView.findViewById(R.id.tvSpentAmount);
            tvPercent = itemView.findViewById(R.id.tvBudgetPercent);
            tvWarning = itemView.findViewById(R.id.tvWarning);
            pbBudget = itemView.findViewById(R.id.pbBudget);
        }
    }
}
