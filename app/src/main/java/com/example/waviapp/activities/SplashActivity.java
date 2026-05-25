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

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        FirebaseAuthHelper authHelper = new FirebaseAuthHelper();
        FirebaseUser currentUser = authHelper.getCurrentUser();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (currentUser != null) {
                UserSessionManager.getInstance().fetchUserData(currentUser.getUid(), success -> {
                    goToHome();
                });
            } else {
                boolean isDebug = (0 != (getApplicationContext().getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE));
                if (isDebug) {
                    // Mock a user session for offline/demo/testing purposes
                    com.example.waviapp.models.TaiKhoan mockUser = new com.example.waviapp.models.TaiKhoan("mock_user_id", "Người dùng Thử nghiệm", "demo@wavi.vn");
                    mockUser.setChuoiNgayHoc(5);
                    UserSessionManager.getInstance().updateUserDataLocally(mockUser);
                    goToHome();
                } else {
                    // Navigate to LoginActivity in production
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    finish();
                }
            }
        }, 1500);
    }

    private void goToHome() {
        startActivity(new Intent(SplashActivity.this, HomeActivity.class));
        finish();
    }
}