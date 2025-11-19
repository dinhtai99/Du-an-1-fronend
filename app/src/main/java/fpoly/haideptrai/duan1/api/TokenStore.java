package fpoly.haideptrai.duan1.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class TokenStore {
    private static final String TAG = "TokenStore";
    private static final String PREF_NAME = "token_prefs";
    private static final String KEY_TOKEN = "auth_token";
    private static volatile String token;
    private static Context appContext;

    public static void initialize(Context context) {
        appContext = context.getApplicationContext();
        loadToken();
    }

    public static void setToken(String t) {
        token = t;
        Log.d(TAG, "Setting token: " + (t != null && !t.isEmpty() ? "Token exists (" + t.length() + " chars)" : "null/empty"));
        if (appContext != null) {
            SharedPreferences prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            if (t != null && !t.isEmpty()) {
                prefs.edit().putString(KEY_TOKEN, t).apply();
                Log.d(TAG, "Token saved to SharedPreferences");
            } else {
                prefs.edit().remove(KEY_TOKEN).apply();
                Log.d(TAG, "Token removed from SharedPreferences");
            }
        } else {
            Log.w(TAG, "appContext is null, cannot save token to SharedPreferences");
        }
    }

    public static String getToken() {
        if (token == null && appContext != null) {
            loadToken();
        }
        Log.d(TAG, "Getting token: " + (token != null && !token.isEmpty() ? "Token exists" : "null/empty"));
        return token;
    }

    private static void loadToken() {
        if (appContext != null) {
            SharedPreferences prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            token = prefs.getString(KEY_TOKEN, null);
            Log.d(TAG, "Token loaded from SharedPreferences: " + (token != null ? "exists" : "null"));
        }
    }

    public static void clearToken() {
        token = null;
        if (appContext != null) {
            SharedPreferences prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().remove(KEY_TOKEN).apply();
            Log.d(TAG, "Token cleared");
        }
    }
}
