package com.example.waviapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class ProgressManager {
    private static final String PREF_NAME = "WaviProgress";
    private static final String KEY_XP = "total_xp";
    private static final String KEY_SET_PROGRESS = "part5_set_progress_";
    private static final String KEY_SET_COMPLETED = "part5_set_completed_";

    private SharedPreferences prefs;

    public ProgressManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void addXP(int amount) {
        int currentXP = prefs.getInt(KEY_XP, 0);
        prefs.edit().putInt(KEY_XP, currentXP + amount).apply();
    }

    public int getTotalXP() {
        return prefs.getInt(KEY_XP, 0);
    }

    public void saveSetProgress(int setIndex, int lastIndex) {
        prefs.edit().putInt(KEY_SET_PROGRESS + setIndex, lastIndex).apply();
    }

    public int getSetProgress(int setIndex) {
        return prefs.getInt(KEY_SET_PROGRESS + setIndex, 0);
    }

    public void markSetCompleted(int setIndex) {
        prefs.edit().putBoolean(KEY_SET_COMPLETED + setIndex, true).apply();
    }

    public boolean isSetCompleted(int setIndex) {
        return prefs.getBoolean(KEY_SET_COMPLETED + setIndex, false);
    }

    public void resetSetProgress(int setIndex) {
        prefs.edit().putInt(KEY_SET_PROGRESS + setIndex, 0).apply();
    }
}
