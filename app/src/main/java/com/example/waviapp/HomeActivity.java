package com.example.waviapp;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.waviapp.databinding.ActivityHomeBinding;
import com.google.android.material.navigation.NavigationBarView;
import android.view.MenuItem;


public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Xử lý icon thông báo
        binding.icNotification.setOnClickListener(v ->
                Toast.makeText(this, "Bạn chưa có thông báo mới", Toast.LENGTH_SHORT).show()
        );

        // Xử lý click các kỹ năng
        binding.llNghe.setOnClickListener(v -> openSkillPractice(SkillPracticeActivity.CAT_LISTEN));
        binding.llDoc.setOnClickListener(v -> openSkillPractice(SkillPracticeActivity.CAT_READ));
        binding.llNoi.setOnClickListener(v -> openSkillPractice(SkillPracticeActivity.CAT_SPEAK));
        binding.llViet.setOnClickListener(v -> openSkillPractice(SkillPracticeActivity.CAT_WRITE));
// Trong hàm onCreate, dưới phần xử lý click các kỹ năng:

// 1. Click Thi Online
        binding.llThiOnline.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng Thi Online đang phát triển", Toast.LENGTH_SHORT).show();
        });

// 2. Click Thi Thử
        binding.llThiThu.setOnClickListener(v -> {
            // Nếu bạn có ExamActivity rồi thì mở nó ra
            Intent intent = new Intent(HomeActivity.this, ExamActivity.class);
            startActivity(intent);
        });

// 3. Click Lý Thuyết (Màn hình tím mình vừa làm)
        binding.llLyThuyet.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, TheoryActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });
        // Xử lý Bottom Navigation
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // Đã ở trang chủ rồi
            } else if (id == R.id.nav_exam) {
                Intent intent = new Intent(HomeActivity.this, ExamActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            } else if (id == R.id.nav_premium) {
                Intent intent = new Intent(HomeActivity.this, PremiumActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(HomeActivity.this, UserInfoActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_setting) {
                Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
            return true;
        });
    }

    private void openSkillPractice(String category) {
        Intent intent = new Intent(HomeActivity.this, SkillPracticeActivity.class);
        intent.putExtra(SkillPracticeActivity.EXTRA_SKILL_CATEGORY, category);
        startActivity(intent);
    }
}