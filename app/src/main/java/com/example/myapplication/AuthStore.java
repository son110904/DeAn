package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

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
        getPrefs(context).edit().putString(KEY_TOKEN, token).apply();
    }

    public static String getToken(Context context) {
        return getPrefs(context).getString(KEY_TOKEN, "");
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
}
