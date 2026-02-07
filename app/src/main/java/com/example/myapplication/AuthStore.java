package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

public final class AuthStore {
    private static final String PREFS_NAME = "auth_store";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_NAME = "name";
    private static final String KEY_TOKEN = "token";

    private AuthStore() {
    }

    public static boolean isLoggedIn(Context context) {
        return getPrefs(context).getBoolean(KEY_LOGGED_IN, false) && !getToken(context).isEmpty();
    }

    public static void setLoggedIn(Context context, boolean loggedIn) {
        getPrefs(context).edit().putBoolean(KEY_LOGGED_IN, loggedIn).apply();
    }

    public static void saveProfile(Context context, String name, String email) {
        getPrefs(context)
                .edit()
                .putString(KEY_NAME, name)
                .putString(KEY_EMAIL, email)
                .apply();
    }

    public static void saveToken(Context context, String token) {
        getPrefs(context).edit().putString(KEY_TOKEN, sanitizeToken(token)).apply();
    }

    public static String getToken(Context context) {
        String token = getPrefs(context).getString(KEY_TOKEN, "");
        return sanitizeToken(token);
    }

    public static String getName(Context context) {
        return getPrefs(context).getString(KEY_NAME, context.getString(R.string.profile_default_name));
    }

    public static String getEmail(Context context) {
        return getPrefs(context).getString(KEY_EMAIL, context.getString(R.string.profile_default_email));
    }

    public static void clear(Context context) {
        getPrefs(context).edit().clear().apply();
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static String sanitizeToken(String token) {
        if (token == null) {
            return "";
        }

        String normalized = token.trim();

        if ((normalized.startsWith("\"") && normalized.endsWith("\""))
                || (normalized.startsWith("'") && normalized.endsWith("'"))) {
            normalized = normalized.substring(1, normalized.length() - 1).trim();
        }

        String lower = normalized.toLowerCase(Locale.US);
        if (lower.startsWith("bearer ")) {
            normalized = normalized.substring(7).trim();
        }

        return normalized;
    }
}
