package com.example.waviapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StudyTimeTracker {
    private static final String PREF_NAME = "StudyTimePrefs";
    private static final String KEY_TODAY_TIME = "study_time_";
    private static final String KEY_TODAY_DATE = "study_date";
    
    private static long sessionStartTime = 0;
    
    public static void startSession() {
        sessionStartTime = System.currentTimeMillis();
    }
    
    public static void endSession(Context context) {
        if (context == null || sessionStartTime == 0) return;
        long elapsedMillis = System.currentTimeMillis() - sessionStartTime;
        sessionStartTime = 0;
        
        int elapsedSeconds = (int) (elapsedMillis / 1000);
        if (elapsedSeconds > 0) {
            addStudyTime(context, elapsedSeconds);
        }
    }
    
    private static String getTodayDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }
    
    public static int getTodayStudyTime(Context context) {
        if (context == null) return 0;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String today = getTodayDateString();
        String savedDate = prefs.getString(KEY_TODAY_DATE, "");
        
        if (!today.equals(savedDate)) {
            // Ngày mới - reset thời gian
            prefs.edit()
                .putString(KEY_TODAY_DATE, today)
                .putInt(KEY_TODAY_TIME + today, 0)
                .apply();
            return 0;
        }
        
        return prefs.getInt(KEY_TODAY_TIME + today, 0);
    }
    
    public static void addStudyTime(Context context, int seconds) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String today = getTodayDateString();
        
        int current = getTodayStudyTime(context);
        prefs.edit()
            .putString(KEY_TODAY_DATE, today)
            .putInt(KEY_TODAY_TIME + today, current + seconds)
            .apply();
    }
}
