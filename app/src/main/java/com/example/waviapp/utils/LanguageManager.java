package com.example.waviapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

public class LanguageManager {

    private static final String PREFS_NAME = "WaviLanguagePrefs";
    private static final String KEY_LANGUAGE = "AppLanguage";

    public static void setLanguage(Context context, String languageCode) {
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply();
    }

    public static String getLanguage(Context context) {
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Default to Vietnamese since the app is originally in Vietnamese
        return prefs.getString(KEY_LANGUAGE, "vi");
    }

    public static Context setLocale(Context context) {
        return updateResources(context, getLanguage(context));
    }

    /**
     * Apply locale to the Application context as well.
     * Call this from WaviApp.onCreate() to ensure app-level resources are localized.
     */
    public static void applyToApp(Context appContext) {
        updateResources(appContext, getLanguage(appContext));
    }

    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        config.setLocale(locale);

        // Also update the resources configuration directly so menus/XML references
        // in the same process pick up the new locale
        res.updateConfiguration(config, res.getDisplayMetrics());

        return context.createConfigurationContext(config);
    }
}
