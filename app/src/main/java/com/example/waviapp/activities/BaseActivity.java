package com.example.waviapp.activities;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import com.example.waviapp.utils.LanguageManager;
import com.example.waviapp.utils.StudyTimeTracker;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageManager.setLocale(newBase));
    }

    @Override
    protected void onResume() {
        super.onResume();
        StudyTimeTracker.startSession();
    }

    @Override
    protected void onPause() {
        super.onPause();
        StudyTimeTracker.endSession(this);
    }
}

