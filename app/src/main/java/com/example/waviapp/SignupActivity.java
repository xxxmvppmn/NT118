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

public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Khởi tạo View Binding
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 2. Thiết lập Hyperlink cho dòng "Đăng nhập"
        setupLoginHyperlink();

        // 3. Thiết lập Hyperlink cho dòng "Điều khoản & Chính sách"
        setupTermsHyperlink();

        // 4. Xử lý sự kiện nút Đăng ký
        binding.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performRegister();
            }
        });

        // 5. Xử lý sự kiện nút Google
        binding.btnGoogle.setOnClickListener(v ->
                Toast.makeText(this, "Đang kết nối Google...", Toast.LENGTH_SHORT).show()
        );
    }

    private void setupLoginHyperlink() {
        String loginText = "Bạn đã có tài khoản? Đăng nhập";
        SpannableString ssLogin = new SpannableString(loginText);

        ClickableSpan clickableLogin = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Từ Signup sang Login
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

        // Gán link vào chữ "Đăng nhập" (vị trí từ 21 đến 30)
        int start = loginText.indexOf("Đăng nhập");
        int end = start + "Đăng nhập".length();

        if (start != -1) {
            ssLogin.setSpan(clickableLogin, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            // Đổi màu xanh cho chữ Đăng nhập giống bên Login
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

        // Gán link vào "Điều khoản" và "Chính sách bảo mật"
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

        // Kiểm tra logic
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

        // Kiểm tra khớp mật khẩu
        if (!password.equals(confirmPass)) {
            binding.edtConfirmPassword.setError("Mật khẩu xác nhận không khớp!");
            return;
        }
        if (!binding.cbTerms.isChecked()) {
            Toast.makeText(this, "Vui lòng đồng ý với Điều khoản sử dụng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Thành công
        Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_LONG).show();
    }
}