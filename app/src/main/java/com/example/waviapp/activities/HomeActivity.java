package com.example.waviapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.example.waviapp.R;
import com.example.waviapp.utils.DataImporter;
import com.example.waviapp.databinding.ActivityHomeBinding;
import com.google.firebase.auth.FirebaseUser;
import com.example.waviapp.firebase.FirebaseAuthHelper;
import com.example.waviapp.firebase.DatabaseHelper;
import com.example.waviapp.models.TaiKhoan;

public class HomeActivity extends BaseActivity {

    private ActivityHomeBinding binding;
    private FirebaseAuthHelper authHelper;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authHelper = new FirebaseAuthHelper();
        dbHelper = new DatabaseHelper();

        // Load thông tin user từ Firebase
        loadUserStats();

        // Import data (triggering once or as needed)
        DataImporter.importVocab(this);
        DataImporter.importGrammar(this);

        // Xử lý icon thông báo
        binding.icNotification.setOnClickListener(v ->
                Toast.makeText(this, getString(R.string.no_notification), Toast.LENGTH_SHORT).show()
        );

        // Xử lý click các kỹ năng
        binding.llNghe.setOnClickListener(v -> openSkillPractice(SkillPracticeActivity.CAT_LISTEN));
        binding.llDoc.setOnClickListener(v -> openSkillPractice(SkillPracticeActivity.CAT_READ));
        binding.llNoi.setOnClickListener(v -> openSkillPractice(SkillPracticeActivity.CAT_SPEAK));
        binding.llViet.setOnClickListener(v -> openSkillPractice(SkillPracticeActivity.CAT_WRITE));

        // 1. Click Thi Online
        binding.llThiOnline.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, OnlineExamActivity.class));
        });

        // 2. Click Thi Thử
        binding.llThiThu.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ExamActivity.class);
            startActivity(intent);
        });

        // 3. Click Lý Thuyết
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

    /**
     * Load thống kê user từ Firebase và hiển thị trên dashboard
     */
    private void loadUserStats() {
        FirebaseUser firebaseUser = authHelper.getCurrentUser();
        if (firebaseUser == null) return;

        dbHelper.getUser(firebaseUser.getUid(), new DatabaseHelper.UserCallback() {
            @Override
            public void onSuccess(TaiKhoan user) {
                // Cập nhật streak
                if (binding.tvStreakCount != null) {
                    binding.tvStreakCount.setText(String.valueOf(user.getChuoiNgayHoc()));
                }
            }

            @Override
            public void onFailure(String error) {
                // Giữ giá trị mặc định
            }
        });
    }

    private void openSkillPractice(String category) {
        Intent intent = new Intent(HomeActivity.this, SkillPracticeActivity.class);
        intent.putExtra(SkillPracticeActivity.EXTRA_SKILL_CATEGORY, category);
        startActivity(intent);
    }

    private void updateNotificationBadge() {
        String currentUserId = authHelper.getCurrentUser().getUid();

        dbHelper.getUnreadNotificationCount(currentUserId, new DatabaseHelper.CountCallback() {
            @Override
            public void onSuccess(int count) {
                if (count > 0) {
                    binding.tvNotificationCount.setVisibility(View.VISIBLE);
                    binding.tvNotificationCount.setText(String.valueOf(count));
                } else {
                    binding.tvNotificationCount.setVisibility(View.GONE);
                }
            }
            @Override
            public void onFailure(String error) { /* Xử lý lỗi */ }
        });
    }
}

