package com.example.waviapp;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.waviapp.databinding.ActivityLoginBinding;
import com.example.waviapp.firebase.DatabaseHelper;
import com.example.waviapp.firebase.FirebaseAuthHelper;
import com.example.waviapp.models.TaiKhoan;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuthHelper authHelper;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authHelper = new FirebaseAuthHelper();
        dbHelper = new DatabaseHelper();

        // AUTO-LOGIN: Nếu đã đăng nhập rồi thì nhảy thẳng vào Home
        if (authHelper.isLoggedIn()) {
            goToHome();
            return;
        }

        // 1. Khởi tạo View Binding (Kết nối với activity_login.xml)
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 2. Thiết lập Hyperlink "Tạo tài khoản" và Quên mật khẩu
        setupRegisterHyperlink();
        setupForgotPassword();

        // 3. Khởi tạo Google Sign-In
        authHelper.initGoogleSignIn(this, getString(R.string.default_web_client_id));

        // 4. Xử lý nút Đăng nhập
        binding.btnLogin.setOnClickListener(v -> performLogin());

        // 5. Xử lý nút Google Sign-In
        binding.btnGoogleLogin.setOnClickListener(v -> {
            Intent signInIntent = authHelper.getGoogleSignInIntent();
            if (signInIntent != null) {
                startActivityForResult(signInIntent, FirebaseAuthHelper.RC_GOOGLE_SIGN_IN);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FirebaseAuthHelper.RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            authHelper.handleGoogleSignInResult(task, new FirebaseAuthHelper.AuthCallback() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    // Lưu thông tin user vào Database nếu là lần đầu
                    saveGoogleUserIfNew(user);
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void saveGoogleUserIfNew(FirebaseUser firebaseUser) {
        dbHelper.getUser(firebaseUser.getUid(), new DatabaseHelper.UserCallback() {
            @Override
            public void onSuccess(TaiKhoan user) {
                // User đã tồn tại, nhảy vào Home
                goToHome();
            }

            @Override
            public void onFailure(String error) {
                // User chưa tồn tại, tạo mới
                String name = firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "User";
                String email = firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "";
                TaiKhoan newUser = new TaiKhoan(firebaseUser.getUid(), name, email);
                dbHelper.saveUser(firebaseUser.getUid(), newUser, new DatabaseHelper.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        goToHome();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(LoginActivity.this, "Lỗi lưu thông tin: " + error, Toast.LENGTH_SHORT).show();
                        goToHome(); // Vẫn cho vào Home
                    }
                });
            }
        });
    }

    /**
     * Hàm xử lý nhảy sang màn hình Đăng ký
     */
    private void setupRegisterHyperlink() {
        String text = "Bạn chưa có tài khoản? Tạo tài khoản";
        SpannableString ss = new SpannableString(text);

        // Tìm vị trí của cụm từ "Tạo tài khoản"
        int start = text.indexOf("Tạo tài khoản");
        int end = start + "Tạo tài khoản".length();

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                //Từ Login sang Signup
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
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
        binding.txtRegisterLink.setHighlightColor(Color.TRANSPARENT);
    }

    private void setupForgotPassword() {
        binding.txtForgotPassword.setOnClickListener(v ->
                Toast.makeText(LoginActivity.this, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show()
        );
    }

    private void performLogin() {
        String email = binding.edtLoginEmail.getText().toString().trim();
        String password = binding.edtLoginPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đủ Email và Mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hiện loading
        binding.btnLogin.setEnabled(false);
        binding.btnLogin.setText("Đang đăng nhập...");

        authHelper.login(email, password, new FirebaseAuthHelper.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                goToHome();
            }

            @Override
            public void onFailure(String errorMessage) {
                binding.btnLogin.setEnabled(true);
                binding.btnLogin.setText("Đăng nhập");
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void goToHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}