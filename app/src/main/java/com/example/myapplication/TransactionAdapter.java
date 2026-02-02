package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
    private final List<TransactionResponse> items = new ArrayList<>();

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        TransactionResponse item = items.get(position);
        boolean isExpense = "expense".equalsIgnoreCase(item.getType())
                || "chi".equalsIgnoreCase(item.getType());
        String category = item.getCategory() != null && !item.getCategory().isEmpty()
                ? item.getCategory()
                : holder.itemView.getContext().getString(R.string.transaction_unknown_category);
        String note = item.getNote();
        String dateLabel = formatDate(item.getCreatedAt());

        holder.title.setText(category);
        String amountValue = TransactionStore.formatCurrency(item.getAmount());
        String amountLabel = isExpense
                ? holder.itemView.getContext().getString(R.string.amount_prefix_expense, amountValue)
                : holder.itemView.getContext().getString(R.string.amount_prefix_income, amountValue);
        holder.amount.setText(amountLabel);
        int amountColor = isExpense
                ? holder.itemView.getContext().getColor(R.color.accent_red)
                : holder.itemView.getContext().getColor(R.color.accent_green);
        holder.amount.setTextColor(amountColor);

        String label = isExpense
                ? holder.itemView.getContext().getString(R.string.transaction_expense_label)
                : holder.itemView.getContext().getString(R.string.transaction_income_label);
        String separator = holder.itemView.getContext().getString(R.string.separator_dot);
        StringBuilder subtitle = new StringBuilder(label);
        if (note != null && !note.isEmpty()) {
            subtitle.append(separator).append(note);
        }
        if (!dateLabel.isEmpty()) {
            subtitle.append(separator).append(dateLabel);
        }
        holder.subtitle.setText(subtitle.toString());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void submitList(List<TransactionResponse> transactions) {
        items.clear();
        if (transactions != null) {
            items.addAll(transactions);
        }
        notifyDataSetChanged();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView amount;
        final TextView subtitle;

        TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTransactionTitle);
            amount = itemView.findViewById(R.id.tvTransactionAmount);
            subtitle = itemView.findViewById(R.id.tvTransactionMeta);
        }
    }

    private String formatDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) {
            return "";
        }
        String[] parts = rawDate.split("T");
        if (parts.length > 0 && !parts[0].isEmpty()) {
            return parts[0];
        }
        return rawDate;
    }
}
