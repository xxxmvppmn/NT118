package com.example.waviapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.content.res.Configuration;
import com.example.waviapp.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Check current mode to set switch state
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        binding.switchDarkMode.setChecked(currentNightMode == Configuration.UI_MODE_NIGHT_YES);

        // Dark mode switch handler
        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        // Logout handler
        binding.tvLogout.setOnClickListener(v -> {
            Toast.makeText(this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Click handlers for setting items
        View.OnClickListener itemClickListener = v -> {
            Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
        };
        binding.llEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, UserInfoActivity.class);
            startActivity(intent);
        });
        binding.llGuide.setOnClickListener(itemClickListener);
        binding.llLanguage.setOnClickListener(itemClickListener);
        binding.llAnswerInterface.setOnClickListener(itemClickListener);
        binding.llDisplay.setOnClickListener(itemClickListener);
        binding.llCommunity.setOnClickListener(itemClickListener);
        binding.llShare.setOnClickListener(itemClickListener);
        binding.llDownloads.setOnClickListener(itemClickListener);
        binding.llReminder.setOnClickListener(itemClickListener);
        binding.llFeedback.setOnClickListener(itemClickListener);

        // Bottom Navigation
        binding.bottomNav.setSelectedItemId(R.id.nav_setting);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent intent = new Intent(SettingsActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            } else if (id == R.id.nav_exam) {
                Toast.makeText(this, "Chuyển sang màn hình Thi", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_pencil) {
                Toast.makeText(this, "Luyện tập hàng ngày", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_profile) {
                Toast.makeText(this, "Hồ sơ cá nhân", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_setting) {
                // Đang ở Settings
            }
            return true;
        });
    }
}
