package com.example.waviapp;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.waviapp.databinding.ActivityForgotPasswordBinding;
import com.example.waviapp.firebase.FirebaseAuthHelper;
import com.google.firebase.auth.FirebaseUser;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;
    private FirebaseAuthHelper authHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authHelper = new FirebaseAuthHelper();

        binding.btnSendReset.setOnClickListener(v -> {
            String email = binding.etEmailReset.getText().toString().trim();

            if (email.isEmpty()) {
                binding.etEmailReset.setError("Vui lòng nhập Email");
                return;
            }

            // Hiện trạng thái chờ
            binding.btnSendReset.setEnabled(false);
            binding.btnSendReset.setText("Đang gửi...");

            authHelper.sendPasswordResetEmail(email, new FirebaseAuthHelper.AuthCallback() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    Toast.makeText(ForgotPasswordActivity.this,
                            "Link đặt lại mật khẩu đã được gửi vào Email của bạn!", Toast.LENGTH_LONG).show();
                    finish(); // Thành công thì quay lại màn hình Login
                }

                @Override
                public void onFailure(String errorMessage) {
                    binding.btnSendReset.setEnabled(true);
                    binding.btnSendReset.setText("Gửi yêu cầu");
                    Toast.makeText(ForgotPasswordActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Nút quay lại
        binding.ivBack.setOnClickListener(v -> finish());
        // Quay lại Đăng nhập
        binding.tvBackToLogin.setOnClickListener(v -> {
            finish();
        });
    }
}