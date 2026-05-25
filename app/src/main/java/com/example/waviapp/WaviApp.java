package com.example.waviapp;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.example.waviapp.firebase.SeedDataHelper;
import com.example.waviapp.utils.LanguageManager;

public class WaviApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Apply saved language preference before anything else
        LanguageManager.applyToApp(this);

        // Đổ dữ liệu mẫu nếu chưa có
        SeedDataHelper.seedAllIfNeeded();
    }
}