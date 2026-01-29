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
                : "Khác";
        String note = item.getNote();

        holder.title.setText(category);
        String prefix = isExpense ? "- " : "+ ";
        holder.amount.setText(prefix + TransactionStore.formatCurrency(item.getAmount()));
        int amountColor = isExpense
                ? holder.itemView.getContext().getColor(R.color.accent_red)
                : holder.itemView.getContext().getColor(R.color.accent_green);
        holder.amount.setTextColor(amountColor);

        String subtitle;
        if (note != null && !note.isEmpty()) {
            subtitle = (isExpense ? "Chi" : "Thu") + " • " + note;
        } else {
            subtitle = isExpense ? "Chi" : "Thu";
        }
        holder.subtitle.setText(subtitle);
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
            subtitle = itemView.findViewById(R.id.tvTransactionSubtitle);
        }
    }
}
