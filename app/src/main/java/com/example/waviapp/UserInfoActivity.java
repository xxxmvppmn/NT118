package com.example.waviapp;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.waviapp.databinding.ActivityUserInfoBinding;

public class UserInfoActivity extends AppCompatActivity {

    private ActivityUserInfoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Back button
        binding.ivBack.setOnClickListener(v -> finish());

        // Avatar edit button
        binding.ivEditAvatar.setOnClickListener(v -> {
            Toast.makeText(this, "Thay đổi ảnh đại diện", Toast.LENGTH_SHORT).show();
        });

        // Underline change password text
        binding.tvChangePassword.setPaintFlags(binding.tvChangePassword.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        // Edit button
        binding.btnEdit.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng chỉnh sửa đang phát triển", Toast.LENGTH_SHORT).show();
        });

        // Change password click
        binding.tvChangePassword.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng đổi mật khẩu đang phát triển", Toast.LENGTH_SHORT).show();
        });

        // Delete account click
        binding.tvDeleteAccount.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng xoá tài khoản", Toast.LENGTH_SHORT).show();
        });

        // Bottom Navigation
        binding.bottomNav.setSelectedItemId(R.id.nav_profile); // Assuming nav_profile is the one for profile/user info
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(UserInfoActivity.this, HomeActivity.class));
                finish();
            } else if (id == R.id.nav_exam) {
                Toast.makeText(this, "Chuyển sang màn hình Thi", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_pencil) {
                Toast.makeText(this, "Luyện tập hàng ngày", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_profile) {
                // Currently in Profile
            } else if (id == R.id.nav_setting) {
                startActivity(new Intent(UserInfoActivity.this, SettingsActivity.class));
                finish();
            }
            return true;
        });
    }
}
