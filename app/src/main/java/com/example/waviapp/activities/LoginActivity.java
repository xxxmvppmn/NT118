package com.example.waviapp.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import android.util.Log;

import com.example.waviapp.R;
import com.example.waviapp.databinding.ActivityLoginBinding;
import com.example.waviapp.firebase.DatabaseHelper;
import com.example.waviapp.firebase.FirebaseAuthHelper;
import com.example.waviapp.managers.UserSessionManager;
import com.example.waviapp.models.TaiKhoan;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity";

    private ActivityLoginBinding binding;
    private FirebaseAuthHelper authHelper;
    private DatabaseHelper dbHelper;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    private com.google.firebase.auth.FirebaseAuth.AuthStateListener authStateListener;
    private boolean isHandlingGoogleSignIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authHelper = new FirebaseAuthHelper();
        dbHelper = new DatabaseHelper();

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    isHandlingGoogleSignIn = false;
                    handleGoogleSignInActivityResult(result);
                }
        );

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRegisterHyperlink();
        setupForgotPassword();

        authHelper.initGoogleSignIn(this, getString(R.string.default_web_client_id));

        binding.btnLogin.setOnClickListener(v -> performLogin());

        binding.btnGoogleLogin.setOnClickListener(v -> {
            Intent signInIntent = authHelper.getGoogleSignInIntent();
            if (signInIntent != null) {
                isHandlingGoogleSignIn = true;
                googleSignInLauncher.launch(signInIntent);
            }
        });

        authStateListener = firebaseAuth -> {
            if (!isHandlingGoogleSignIn && firebaseAuth.getCurrentUser() != null) {
                Log.d(TAG, "AuthStateListener: user đã đăng nhập, chuyển Home");
                // Fetch data before going to Home
                UserSessionManager.getInstance().fetchUserData(firebaseAuth.getCurrentUser().getUid(), success -> goToHome());
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        com.google.firebase.auth.FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        com.google.firebase.auth.FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
    }

    private void performLogin() {
        String email = binding.edtLoginEmail.getText().toString().trim();
        String password = binding.edtLoginPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đủ Email và Mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnLogin.setEnabled(false);
        binding.btnLogin.setText("Đang đăng nhập...");

        authHelper.login(email, password, new FirebaseAuthHelper.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                handleUserSession(user.getUid());
            }

            @Override
            public void onFailure(String errorMessage) {
                binding.btnLogin.setEnabled(true);
                binding.btnLogin.setText("Đăng nhập");
                new android.app.AlertDialog.Builder(LoginActivity.this)
                        .setTitle("Lỗi Đăng Nhập")
                        .setMessage(errorMessage)
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    private void handleUserSession(String uid) {
        dbHelper.getUser(uid, new DatabaseHelper.UserCallback() {
            @Override
            public void onSuccess(TaiKhoan user) {
                int currentStreak = user.getChuoiNgayHoc();
                Timestamp lastLoginTs = user.getLastLogin();
                Timestamp now = Timestamp.now();

                int newStreak = currentStreak;

                if (lastLoginTs != null) {
                    Calendar calLast = Calendar.getInstance();
                    calLast.setTime(lastLoginTs.toDate());
                    setAtStartOfDay(calLast);

                    Calendar calNow = Calendar.getInstance();
                    calNow.setTime(now.toDate());
                    setAtStartOfDay(calNow);

                    long diffMillis = calNow.getTimeInMillis() - calLast.getTimeInMillis();
                    long diffDays = diffMillis / (24 * 60 * 60 * 1000);

                    if (diffDays == 1) {
                        newStreak++;
                    } else if (diffDays > 1) {
                        newStreak = 1;
                    }
                } else {
                    newStreak = 1;
                }

                Map<String, Object> updates = new HashMap<>();
                updates.put("lastLogin", now);
                updates.put("chuoiNgayHoc", newStreak);

                final int streakToSave = newStreak;
                dbHelper.updateUser(uid, updates, new DatabaseHelper.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(LoginActivity.this, "Chào mừng trở lại!", Toast.LENGTH_SHORT).show();
                        // Update local cache
                        user.setLastLogin(now);
                        user.setChuoiNgayHoc(streakToSave);
                        UserSessionManager.getInstance().updateUserDataLocally(user);
                        goToHome();
                    }

                    @Override
                    public void onFailure(String error) {
                        UserSessionManager.getInstance().updateUserDataLocally(user);
                        goToHome();
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                goToHome();
            }
        });
    }

    private void setAtStartOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private void saveGoogleUserIfNew(FirebaseUser firebaseUser) {
        dbHelper.getUser(firebaseUser.getUid(), new DatabaseHelper.UserCallback() {
            @Override
            public void onSuccess(TaiKhoan user) {
                handleUserSession(firebaseUser.getUid());
            }

            @Override
            public void onFailure(String error) {
                String name = firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "User";
                String email = firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "";
                TaiKhoan newUser = new TaiKhoan(firebaseUser.getUid(), name, email);

                newUser.setLastLogin(Timestamp.now());
                newUser.setChuoiNgayHoc(1);

                dbHelper.saveUser(firebaseUser.getUid(), newUser, new DatabaseHelper.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        UserSessionManager.getInstance().updateUserDataLocally(newUser);
                        goToHome();
                    }
                    @Override
                    public void onFailure(String error) { goToHome(); }
                });
            }
        });
    }

    private void handleGoogleSignInActivityResult(ActivityResult result) {
        if (result.getData() == null) return;
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
        authHelper.handleGoogleSignInResult(task, new FirebaseAuthHelper.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) { saveGoogleUserIfNew(user); }
            @Override
            public void onFailure(String errorMessage) {
                new android.app.AlertDialog.Builder(LoginActivity.this)
                        .setTitle("Lỗi Google Sign-In")
                        .setMessage(errorMessage)
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    private void setupRegisterHyperlink() {
        String text = "Bạn chưa có tài khoản? Tạo tài khoản";
        SpannableString ss = new SpannableString(text);
        int start = text.indexOf("Tạo tài khoản");
        int end = start + "Tạo tài khoản".length();

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
                ds.setFakeBoldText(true);
            }
        };

        if (start != -1) {
            ss.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(new ForegroundColorSpan(Color.parseColor("#1A73E8")), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        binding.txtRegisterLink.setText(ss);
        binding.txtRegisterLink.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void goToHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void setupForgotPassword() {
        binding.txtForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }
}