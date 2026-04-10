package com.example.waviapp.activities;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import com.example.waviapp.utils.LanguageManager;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageManager.setLocale(newBase));
    }
}

