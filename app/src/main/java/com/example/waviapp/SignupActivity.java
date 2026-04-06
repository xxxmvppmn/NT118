package com.example.waviapp;

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
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.waviapp.databinding.ActivitySignupBinding;
import com.example.waviapp.firebase.DatabaseHelper;
import com.example.waviapp.firebase.FirebaseAuthHelper;
import com.example.waviapp.models.TaiKhoan;
import com.google.firebase.auth.FirebaseUser;

public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    private FirebaseAuthHelper authHelper;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authHelper = new FirebaseAuthHelper();
        dbHelper = new DatabaseHelper();

        // 1. Khởi tạo View Binding
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 2. Thiết lập Hyperlink cho dòng "Đăng nhập"
        setupLoginHyperlink();

        // 3. Thiết lập Hyperlink cho dòng "Điều khoản & Chính sách"
        setupTermsHyperlink();

        // 4. Xử lý sự kiện nút Đăng ký
        binding.btnRegister.setOnClickListener(v -> performRegister());

        // 5. Xử lý sự kiện nút Google
        binding.btnGoogle.setOnClickListener(v ->
                Toast.makeText(this, "Vui lòng sử dụng Google Sign-In ở màn hình Đăng nhập", Toast.LENGTH_SHORT).show()
        );
    }

    private void setupLoginHyperlink() {
        String loginText = "Bạn đã có tài khoản? Đăng nhập";
        SpannableString ssLogin = new SpannableString(loginText);

        ClickableSpan clickableLogin = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
                ds.setFakeBoldText(true);
            }
        };

        int start = loginText.indexOf("Đăng nhập");
        int end = start + "Đăng nhập".length();

        if (start != -1) {
            ssLogin.setSpan(clickableLogin, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssLogin.setSpan(new ForegroundColorSpan(Color.parseColor("#1A73E8")), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        binding.txtLoginLink.setText(ssLogin);
        binding.txtLoginLink.setMovementMethod(LinkMovementMethod.getInstance());
        binding.txtLoginLink.setHighlightColor(Color.TRANSPARENT);
    }

    private void setupTermsHyperlink() {
        String termsText = "Bằng việc đăng ký, bạn đồng ý với Điều khoản và Chính sách bảo mật của chúng tôi.";
        SpannableString ssTerms = new SpannableString(termsText);

        ClickableSpan clickableTerms = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Toast.makeText(SignupActivity.this, "Đang mở Điều khoản...", Toast.LENGTH_SHORT).show();
            }
        };

        ssTerms.setSpan(clickableTerms, 34, 66, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssTerms.setSpan(new ForegroundColorSpan(Color.BLACK), 33, 66, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.txtTerms.setText(ssTerms);
        binding.txtTerms.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void performRegister() {
        String name = binding.edtFullName.getText().toString().trim();
        String email = binding.edtEmail.getText().toString().trim();
        String password = binding.edtPassword.getText().toString().trim();
        String confirmPass = binding.edtConfirmPassword.getText().toString().trim();

        // Validation
        if (name.isEmpty()) {
            binding.edtFullName.setError("Vui lòng nhập họ tên");
            return;
        }

        if (email.isEmpty()) {
            binding.edtEmail.setError("Vui lòng nhập Email");
            return;
        }

        if (password.length() < 6) {
            binding.edtPassword.setError("Mật khẩu ít nhất 6 ký tự");
            return;
        }

        if (!password.equals(confirmPass)) {
            binding.edtConfirmPassword.setError("Mật khẩu xác nhận không khớp!");
            return;
        }

        if (!binding.cbTerms.isChecked()) {
            Toast.makeText(this, "Vui lòng đồng ý với Điều khoản sử dụng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hiện loading
        binding.btnRegister.setEnabled(false);
        binding.btnRegister.setText("Đang đăng ký...");

        // Đăng ký với Firebase Auth
        authHelper.register(email, password, new FirebaseAuthHelper.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser firebaseUser) {
                // Lưu thông tin user vào Realtime Database
                TaiKhoan user = new TaiKhoan(firebaseUser.getUid(), name, email);
                dbHelper.saveUser(firebaseUser.getUid(), user, new DatabaseHelper.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        authHelper.logout(); // Đăng xuất để yêu cầu người dùng tự đăng nhập lại
                        Toast.makeText(SignupActivity.this, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_LONG).show();
                        // Chuyển sang LoginActivity
                        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFailure(String error) {
                        authHelper.logout();
                        Toast.makeText(SignupActivity.this, "Đăng ký thành công! (Lỗi profile: " + error + "). Vui lòng đăng nhập.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                binding.btnRegister.setEnabled(true);
                binding.btnRegister.setText("Đăng ký");
                Toast.makeText(SignupActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}