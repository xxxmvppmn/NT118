package com.example.waviapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.waviapp.R;
import com.example.waviapp.databinding.ActivityHomeBinding;
import com.example.waviapp.managers.UserSessionManager;
import com.example.waviapp.models.TaiKhoan;

public class HomeActivity extends BaseActivity {

    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        UserSessionManager.getInstance().getUserLiveData().observe(this, this::updateUI);

        binding.icNotification.setOnClickListener(v ->
                Toast.makeText(this, getString(R.string.no_notification), Toast.LENGTH_SHORT).show()
        );

        binding.llNghe.setOnClickListener(v -> openSkillPractice(SkillPracticeActivity.CAT_LISTEN));
        binding.llDoc.setOnClickListener(v -> openSkillPractice(SkillPracticeActivity.CAT_READ));
        binding.llNoi.setOnClickListener(v -> openSkillPractice(SkillPracticeActivity.CAT_SPEAK));
        binding.llViet.setOnClickListener(v -> openSkillPractice(SkillPracticeActivity.CAT_WRITE));

        binding.llThiOnline.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, OnlineExamActivity.class)));
        binding.llThiThu.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ExamActivity.class)));
        binding.llLyThuyet.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, TheoryActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });
        binding.btnReviewVocab.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, FavoriteWordsActivity.class)));

        // Thêm sự kiện click vào avatar để mở profile
        binding.imgAvatarHome.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, UserInfoActivity.class);
            startActivity(intent);
        });

        setupBottomNavigation();
    }

    private void updateUI(TaiKhoan user) {
        if (user == null) return;
        
        // Cập nhật Streak
        if (binding.tvStreakCount != null) {
            binding.tvStreakCount.setText(String.valueOf(user.getChuoiNgayHoc()));
        }

        // Cập nhật Avatar từ Base64 (giống trong UserInfoActivity)
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(user.getAvatarUrl(), Base64.DEFAULT);
                Glide.with(this)
                        .asBitmap()
                        .load(decodedBytes)
                        .placeholder(R.drawable.ic_user_placeholder)
                        .error(R.drawable.ic_user_placeholder)
                        .circleCrop()
                        .into(binding.imgAvatarHome);
            } catch (Exception e) {
                binding.imgAvatarHome.setImageResource(R.drawable.ic_user_placeholder);
            }
        } else {
            binding.imgAvatarHome.setImageResource(R.drawable.ic_user_placeholder);
        }
    }

    private void openSkillPractice(String category) {
        Intent intent = new Intent(HomeActivity.this, SkillPracticeActivity.class);
        intent.putExtra(SkillPracticeActivity.EXTRA_SKILL_CATEGORY, category);
        startActivity(intent);
    }

    private void setupBottomNavigation() {
        binding.bottomNav.setSelectedItemId(R.id.nav_home);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return false;
            
            Intent intent = null;
            if (id == R.id.nav_exam) intent = new Intent(this, ExamActivity.class);
            else if (id == R.id.nav_premium) intent = new Intent(this, PremiumActivity.class);
            else if (id == R.id.nav_profile) intent = new Intent(this, UserInfoActivity.class);
            else if (id == R.id.nav_setting) intent = new Intent(this, SettingsActivity.class);

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }
}