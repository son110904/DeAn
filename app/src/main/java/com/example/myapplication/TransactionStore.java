package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.NumberFormat;
import java.util.Locale;

public class TransactionStore {
    private static final String PREFS_NAME = "transaction_store";
    private static final String KEY_INCOME_TOTAL = "income_total";
    private static final String KEY_EXPENSE_TOTAL = "expense_total";
    private static final String KEY_LAST_TRANSACTION = "last_transaction";
    private static final String KEY_CATEGORY_PREFIX = "category_total_";

    private TransactionStore() {
    }

    public static long getIncomeTotal(Context context) {
        return getPrefs(context).getLong(KEY_INCOME_TOTAL, 0L);
    }

    public static long getExpenseTotal(Context context) {
        return getPrefs(context).getLong(KEY_EXPENSE_TOTAL, 0L);
    }

    public static String getLastTransaction(Context context) {
        return getPrefs(context).getString(KEY_LAST_TRANSACTION, "");
    }

    public static long getCategoryTotal(Context context, String categoryKey) {
        return getPrefs(context).getLong(KEY_CATEGORY_PREFIX + categoryKey, 0L);
    }

    public static void addIncome(Context context, long amount, String account) {
        SharedPreferences prefs = getPrefs(context);
        long incomeTotal = prefs.getLong(KEY_INCOME_TOTAL, 0L) + amount;

        prefs.edit()
                .putLong(KEY_INCOME_TOTAL, incomeTotal)
                .putString(KEY_LAST_TRANSACTION,
                        "Thu " + formatCurrency(amount) + " • " + account)
                .apply();
    }

    public static void addExpense(Context context, long amount, String account, String categoryKey) {
        SharedPreferences prefs = getPrefs(context);
        long expenseTotal = prefs.getLong(KEY_EXPENSE_TOTAL, 0L) + amount;
        long categoryTotal = prefs.getLong(KEY_CATEGORY_PREFIX + categoryKey, 0L) + amount;

        prefs.edit()
                .putLong(KEY_EXPENSE_TOTAL, expenseTotal)
                .putLong(KEY_CATEGORY_PREFIX + categoryKey, categoryTotal)
                .putString(KEY_LAST_TRANSACTION,
                        "Chi " + formatCurrency(amount) + " • " + categoryKey + " • " + account)
                .apply();
    }

    public static String normalizeCategory(String categoryLabel) {
        if (categoryLabel == null) {
            return "Khác";
        }
        return categoryLabel.replaceAll("^[^\\p{L}\\p{N}]+", "").trim();
    }

    public static String formatCurrency(long amount) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(amount) + "đ";
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
