package com.example.stilo;

import android.content.Context;
import android.content.SharedPreferences;

public class ThemeManager {

    private static final String PREFS_NAME = "StiloThemePrefs";
    private static final String PREF_USER_TYPE = "userType";

    public static void applyTheme(Context context) {
        // Com a unificação dos temas, aplicamos sempre o tema principal.
        context.setTheme(R.style.Theme_Stilo);
    }

    public static void setUserType(Context context, String userType) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(PREF_USER_TYPE, userType);
        editor.apply();
    }

    public static String getUserType(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(PREF_USER_TYPE, null); // O valor padrão pode ser ajustado se necessário
    }

    public static void clearTheme(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.remove(PREF_USER_TYPE);
        editor.apply();
    }
}
