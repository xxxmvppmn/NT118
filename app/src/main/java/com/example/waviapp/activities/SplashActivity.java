package com.example.waviapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.waviapp.R;
import com.example.waviapp.firebase.FirebaseAuthHelper;
import com.example.waviapp.managers.UserSessionManager;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        FirebaseAuthHelper authHelper = new FirebaseAuthHelper();
        FirebaseUser currentUser = authHelper.getCurrentUser();

        if (currentUser != null) {
            // User already logged in, fetch data before going to Home
            UserSessionManager.getInstance().fetchUserData(currentUser.getUid(), success -> {
                goToHome();
            });
        } else {
            // No user, go to Login after a delay
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            }, 1500);
        }
    }

    private void goToHome() {
        startActivity(new Intent(SplashActivity.this, HomeActivity.class));
        finish();
    }
}