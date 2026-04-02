package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

public final class ApiConfigStore {
    private static final String PREFS_NAME = "api_config";
    private static final String KEY_BASE_URL = "base_url";
    private static final String DEFAULT_BASE_URL = "";

    private ApiConfigStore() {
    }

    public static String getBaseUrl(Context context) {
        String value = getPrefs(context).getString(KEY_BASE_URL, DEFAULT_BASE_URL);
        return normalize(value);
    }

    public static void saveBaseUrl(Context context, String value) {
        getPrefs(context)
                .edit()
                .putString(KEY_BASE_URL, normalize(value))
                .apply();
    }

    public static String normalize(String value) {
        if (value == null) {
            return DEFAULT_BASE_URL;
        }

        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return DEFAULT_BASE_URL;
        }

        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "http://" + normalized;
        }

        if (!normalized.endsWith("/")) {
            normalized += "/";
        }

        return normalized;
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
