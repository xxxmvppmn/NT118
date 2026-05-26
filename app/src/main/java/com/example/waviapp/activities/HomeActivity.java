package com.example.waviapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.waviapp.R;
import com.example.waviapp.adapters.BannerAdapter;
import com.example.waviapp.databinding.ActivityHomeBinding;
import com.example.waviapp.managers.UserSessionManager;
import com.example.waviapp.models.Banner;
import com.example.waviapp.models.TaiKhoan;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends BaseActivity {

    private ActivityHomeBinding binding;
    private Handler bannerHandler = new Handler(Looper.getMainLooper());
    private Runnable bannerRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        UserSessionManager.getInstance().getUserLiveData().observe(this, this::updateUI);

        setupBanners();
        setupClickListeners();
        setupBottomNavigation();
    }

    private void setupBanners() {
        List<Banner> banners = new ArrayList<>();
        banners.add(new Banner("Gia nhập Cộng đồng WAVI", "Cùng nhau học tập và chia sẻ kinh nghiệm chinh phục TOEIC", R.drawable.banner_facebook_group, "FACEBOOK"));
        banners.add(new Banner("Nâng cấp Premium", "Mở khóa kho đề thi ETS 2024 mới nhất và giải chi tiết", R.drawable.banner_premium_upgrade, "PREMIUM"));
        banners.add(new Banner("Thi thử Online", "Trải nghiệm áp lực phòng thi thật với đồng hồ bấm giờ", R.drawable.banner_online_exam, "ONLINE_EXAM"));

        BannerAdapter adapter = new BannerAdapter(banners, banner -> {
            switch (banner.getActionType()) {
                case "FACEBOOK":
                    String fbUrl = "https://www.facebook.com/groups/waviapp";
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(fbUrl)));
                    break;
                case "PREMIUM":
                    startActivity(new Intent(this, PremiumActivity.class));
                    break;
                case "ONLINE_EXAM":
                    startActivity(new Intent(this, OnlineExamActivity.class));
                    break;
            }
        });

        binding.vpBanners.setAdapter(adapter);
        setupIndicators(banners.size());
        binding.vpBanners.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateIndicators(position);
            }
        });

        bannerRunnable = () -> {
            if (binding.vpBanners != null && banners.size() > 0) {
                int current = binding.vpBanners.getCurrentItem();
                int next = (current + 1) % banners.size();
                binding.vpBanners.setCurrentItem(next, true);
                bannerHandler.postDelayed(bannerRunnable, 4000);
            }
        };
        bannerHandler.postDelayed(bannerRunnable, 4000);
    }

    private void setupIndicators(int count) {
        binding.layoutIndicators.removeAllViews();
        for (int i = 0; i < count; i++) {
            ImageView dot = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 0, 8, 0);
            dot.setLayoutParams(params);
            dot.setImageResource(R.drawable.banner_dot_unselected);
            binding.layoutIndicators.addView(dot);
        }
        updateIndicators(0);
    }

    private void updateIndicators(int position) {
        for (int i = 0; i < binding.layoutIndicators.getChildCount(); i++) {
            ImageView dot = (ImageView) binding.layoutIndicators.getChildAt(i);
            dot.setImageResource(i == position ? R.drawable.banner_dot_selected : R.drawable.banner_dot_unselected);
        }
    }

    private void setupClickListeners() {
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

        binding.imgAvatarHome.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, UserInfoActivity.class);
            startActivity(intent);
        });

        // Mở màn hình Chat AI khi bấm vào bong bóng nổi
        binding.fabChatAI.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ChatAIActivity.class));
        });
    }

    private void updateUI(TaiKhoan user) {
        if (user == null) return;
        
        if (binding.tvHomeUserGreeting != null) {
            String fullName = user.getHoTen();
            String firstName = (fullName != null && fullName.contains(" ")) 
                    ? fullName.substring(fullName.lastIndexOf(" ") + 1) 
                    : (fullName != null ? fullName : "bạn");
            binding.tvHomeUserGreeting.setText("Chào " + firstName);
        }

        if (binding.tvStreakCount != null) {
            binding.tvStreakCount.setText(String.valueOf(user.getChuoiNgayHoc()));
        }

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
            } catch (Exception ignored) {}
        }

        // Cập nhật mục tiêu điểm TOEIC động từ lộ trình
        if (binding.tvTargetScore != null) {
            binding.tvTargetScore.setText(getString(R.string.auto_m_c_ti_u_toeic) + " " + user.getMucTieuDiem() + "đ");
        }

        // Cập nhật tiến độ học tập hàng ngày thực tế
        int targetMinutes = user.getMucTieuHangNgay();
        if (targetMinutes <= 0) targetMinutes = 10;

        int todaySeconds = com.example.waviapp.utils.StudyTimeTracker.getTodayStudyTime(this);
        int progressPercent = (int) (((double) todaySeconds / (targetMinutes * 60)) * 100);
        if (progressPercent > 100) progressPercent = 100;

        if (binding.progressStudy != null) {
            binding.progressStudy.setProgress(progressPercent);
        }
        if (binding.tvProgressPercent != null) {
            binding.tvProgressPercent.setText(progressPercent + "%");
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

    @Override
    protected void onPause() {
        super.onPause();
        bannerHandler.removeCallbacks(bannerRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bannerRunnable != null) {
            bannerHandler.postDelayed(bannerRunnable, 4000);
        }

        // Cập nhật lại UI thời gian thực khi user quay lại màn hình Home
        TaiKhoan currentUser = UserSessionManager.getInstance().getUserData();
        if (currentUser != null) {
            updateUI(currentUser);
        }
    }
}