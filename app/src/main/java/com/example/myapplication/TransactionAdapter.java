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
    private OnTransactionClickListener listener;
    private OnTransactionLongClickListener longClickListener;

    public interface OnTransactionClickListener {
        void onTransactionClick(TransactionResponse transaction);
    }

    public interface OnTransactionLongClickListener {
        void onTransactionLongClick(TransactionResponse transaction);
    }

    public void setOnTransactionClickListener(OnTransactionClickListener listener) {
        this.listener = listener;
    }

    public void setOnTransactionLongClickListener(OnTransactionLongClickListener listener) {
        this.longClickListener = listener;
    }

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
        android.content.Context context = holder.itemView.getContext();
        
        boolean isExpense = "expense".equalsIgnoreCase(item.getType())
                || "chi".equalsIgnoreCase(item.getType());
                
        String category = item.getCategory() != null && !item.getCategory().isEmpty()
                ? item.getCategory()
                : context.getString(R.string.transaction_unknown_category);
                
        holder.title.setText(category);
        String amountValue = TransactionStore.formatCurrency(item.getAmount());
        String amountLabel = isExpense
                ? context.getString(R.string.amount_prefix_expense, amountValue)
                : context.getString(R.string.amount_prefix_income, amountValue);
        
        holder.amount.setText(amountLabel);
        int amountColor = isExpense
                ? context.getColor(R.color.accent_red)
                : context.getColor(R.color.accent_green);
        holder.amount.setTextColor(amountColor);

        String dateLabel = formatDate(item.getDate());
        String note = item.getNote();
        String label = isExpense
                ? context.getString(R.string.transaction_expense_label)
                : context.getString(R.string.transaction_income_label);
        String separator = context.getString(R.string.separator_dot);
        
        StringBuilder subtitle = new StringBuilder(label);
        if (note != null && !note.isEmpty()) {
            subtitle.append(separator).append(note);
        }
        if (!dateLabel.isEmpty()) {
            subtitle.append(separator).append(dateLabel);
        }
        holder.subtitle.setText(subtitle.toString());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTransactionClick(item);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onTransactionLongClick(item);
                return true;
            }
            return false;
        });
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
        if (rawDate == null || rawDate.isEmpty()) return "";
        String[] parts = rawDate.split("T");
        return parts.length > 0 ? parts[0] : rawDate;
    }
}
