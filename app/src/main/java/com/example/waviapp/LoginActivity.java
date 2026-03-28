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
import androidx.appcompat.app.AppCompatActivity;

import com.example.waviapp.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Khởi tạo View Binding (Kết nối với activity_login.xml)
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 2. Thiết lập Hyperlink "Tạo tài khoản" và Quên mật khẩu
        setupRegisterHyperlink();
        setupForgotPassword();

        // 3. Xử lý nút Đăng nhập
        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });
        binding.btnLogin.setOnClickListener(v -> {
            // Nhảy thẳng sang Home luôn để xem giao diện đã đẹp chưa
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
        // 4. Xử lý nút Google
        binding.btnGoogleLogin.setOnClickListener(v ->
                Toast.makeText(this, "Đang kết nối Google...", Toast.LENGTH_SHORT).show()
        );
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
                // Bạn có thể dùng finish() nếu không muốn quay lại Login bằng nút Back
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
                ds.setFakeBoldText(true);
            }
        };

        if (start != -1) {
            // Gán sự kiện click
            ss.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            // Gán màu xanh dương cho link
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

        // Sau này code kiểm tra tài khoản sẽ ở đây
        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
    }
}