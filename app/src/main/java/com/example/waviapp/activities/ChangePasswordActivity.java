package com.example.waviapp.activities;

import android.os.Bundle;
import android.widget.Toast;
import com.example.waviapp.databinding.ActivityChangePasswordBinding;
import com.example.waviapp.firebase.FirebaseAuthHelper;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends BaseActivity {

    private ActivityChangePasswordBinding binding;
    private FirebaseAuthHelper authHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authHelper = new FirebaseAuthHelper();

        binding.ivBack.setOnClickListener(v -> finish());

        binding.btnSubmit.setOnClickListener(v -> {
            String current = binding.etCurrentPassword.getText().toString();
            String newer = binding.etNewPassword.getText().toString();
            String confirm = binding.etConfirmPassword.getText().toString();

            if (current.isEmpty() || newer.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newer.length() < 6) {
                Toast.makeText(this, "Mật khẩu mới phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newer.equals(confirm)) {
                Toast.makeText(this, "Mật khẩu nhập lại không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            // Hiện loading
            binding.btnSubmit.setEnabled(false);
            binding.btnSubmit.setText("Đang xử lý...");

            authHelper.changePassword(current, newer, new FirebaseAuthHelper.AuthCallback() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    Toast.makeText(ChangePasswordActivity.this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onFailure(String errorMessage) {
                    binding.btnSubmit.setEnabled(true);
                    binding.btnSubmit.setText("Xác nhận");
                    Toast.makeText(ChangePasswordActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}

