package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class TransactionActionsBottomSheet extends BottomSheetDialogFragment {
    public interface TransactionActionsListener {
        void onRequestDeleteTransaction(int transactionId);
    }

    private static final String ARG_TRANSACTION_ID = "transactionId";
    private static final String ARG_AMOUNT = "amount";
    private static final String ARG_CATEGORY = "category";
    private static final String ARG_TYPE = "type";
    private static final String ARG_NOTE = "note";
    private static final String ARG_DATE = "date";

    private int transactionId;
    private long amount;
    private String category;
    private String type;
    private String note;
    private String date;

    public static TransactionActionsBottomSheet newInstance(TransactionResponse transaction) {
        TransactionActionsBottomSheet fragment = new TransactionActionsBottomSheet();
        Bundle args = new Bundle();
        args.putInt(ARG_TRANSACTION_ID, transaction.getId());
        args.putLong(ARG_AMOUNT, transaction.getAmount());
        args.putString(ARG_CATEGORY, transaction.getCategory());
        args.putString(ARG_TYPE, transaction.getType());
        args.putString(ARG_NOTE, transaction.getNote());
        args.putString(ARG_DATE, transaction.getDate());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            transactionId = getArguments().getInt(ARG_TRANSACTION_ID);
            amount = getArguments().getLong(ARG_AMOUNT);
            category = getArguments().getString(ARG_CATEGORY);
            type = getArguments().getString(ARG_TYPE);
            note = getArguments().getString(ARG_NOTE);
            date = getArguments().getString(ARG_DATE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_transaction_actions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnEdit = view.findViewById(R.id.btnEdit);
        Button btnDelete = view.findViewById(R.id.btnDelete);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        btnEdit.setOnClickListener(v -> {
            // Launch edit activity
            Intent intent = new Intent(getContext(), AddTransactionActivity.class);
            intent.putExtra("isEdit", true);
            intent.putExtra("transactionId", transactionId);
            intent.putExtra("amount", amount);
            intent.putExtra("category", category);
            intent.putExtra("type", type);
            intent.putExtra("note", note);
            intent.putExtra("date", date);
            startActivity(intent);
            dismiss();
        });

        btnDelete.setOnClickListener(v -> {
            if (getActivity() instanceof TransactionActionsListener) {
                ((TransactionActionsListener) getActivity()).onRequestDeleteTransaction(transactionId);
            }
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }
}