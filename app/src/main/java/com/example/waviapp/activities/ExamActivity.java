package com.example.waviapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.waviapp.R;
import com.example.waviapp.databinding.ActivityExamBinding;
import com.example.waviapp.firebase.DatabaseHelper;
import com.example.waviapp.managers.UserSessionManager;
import com.example.waviapp.models.BaiKiemTra;

import java.util.List;

public class ExamActivity extends BaseActivity {

    private ActivityExamBinding binding;
    private DatabaseHelper dbHelper;
    private boolean isUserPremium = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExamBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new DatabaseHelper();

        // Section 1: ETS 2023
        binding.tvSeeMore1.setOnClickListener(v -> openTestList(TestListActivity.CAT_FULLTEST, ""));
        
        // Section 2: ETS 2024 (Fulltest 2)
        binding.tvSeeMore2.setOnClickListener(v -> openTestList(TestListActivity.CAT_FULLTEST_2, "ets2024"));
        
        // Section 3: Speaking & Writing
        binding.tvSeeMore3.setOnClickListener(v -> openTestList(TestListActivity.CAT_SPEAKING, ""));

        // Tự động cập nhật khi Premium được kích hoạt
        UserSessionManager.getInstance().getUserLiveData().observe(this, user -> {
            if (user != null) {
                isUserPremium = user.isPremium();
                fetchTestData(); 
            }
        });

        setupBottomNavigation();
    }

    private void fetchTestData() {
        java.util.Comparator<BaiKiemTra> testSorter = (t1, t2) -> {
            try {
                String num1 = t1.getMaBKT().replaceAll("\\D+", "");
                String num2 = t2.getMaBKT().replaceAll("\\D+", "");
                return Integer.compare(Integer.parseInt(num1.isEmpty() ? "0" : num1), 
                                      Integer.parseInt(num2.isEmpty() ? "0" : num2));
            } catch (Exception e) { return t1.getMaBKT().compareTo(t2.getMaBKT()); }
        };

        // 1. Load ETS 2023
        dbHelper.getTests("fulltest", new DatabaseHelper.TestCallback() {
            @Override
            public void onSuccess(List<BaiKiemTra> tests) {
                binding.llFulltest.removeAllViews();
                if (tests.isEmpty()) setupFulltestDefault();
                else {
                    java.util.Collections.sort(tests, testSorter);
                    for (int i = 0; i < Math.min(5, tests.size()); i++) {
                        addTestToLayout(binding.llFulltest, tests.get(i), R.drawable.ic_test, "#FFFDE7", "");
                    }
                }
            }
            @Override public void onFailure(String error) { setupFulltestDefault(); }
        });

        // 2. Load ETS 2024
        dbHelper.getTests("fulltest2", new DatabaseHelper.TestCallback() {
            @Override
            public void onSuccess(List<BaiKiemTra> tests) {
                binding.llFulltest2.removeAllViews();
                if (tests.isEmpty()) setupFulltest2Default();
                else {
                    java.util.Collections.sort(tests, testSorter);
                    for (int i = 0; i < Math.min(5, tests.size()); i++) {
                        addTestToLayout(binding.llFulltest2, tests.get(i), R.drawable.ic_test, "#E1F5FE", "ets2024");
                    }
                }
            }
            @Override public void onFailure(String error) { setupFulltest2Default(); }
        });

        // 3. Load Speaking
        dbHelper.getTests("speaking", new DatabaseHelper.TestCallback() {
            @Override
            public void onSuccess(List<BaiKiemTra> tests) {
                binding.llSpeaking.removeAllViews();
                if (tests.isEmpty()) setupSpeakingDefault();
                else {
                    java.util.Collections.sort(tests, testSorter);
                    for (int i = 0; i < Math.min(5, tests.size()); i++) {
                        addTestToLayout(binding.llSpeaking, tests.get(i), R.drawable.ic_speak, "#FCE4EC", "");
                    }
                }
            }
            @Override public void onFailure(String error) { setupSpeakingDefault(); }
        });
    }

    private void addTestToLayout(LinearLayout container, BaiKiemTra test, int iconRes, String bgColor, String folderPrefix) {
        View itemView = LayoutInflater.from(this).inflate(R.layout.item_test_card, container, false);
        TextView tvTestName = itemView.findViewById(R.id.tvTestName);
        TextView tvTestDesc = itemView.findViewById(R.id.tvTestDesc);
        ImageView ivIcon = itemView.findViewById(R.id.ivIcon);
        ImageView ivLock = itemView.findViewById(R.id.ivLock);
        com.google.android.material.card.MaterialCardView cardContainer = itemView.findViewById(R.id.cardContainer);

        ivIcon.setImageResource(iconRes);
        cardContainer.setCardBackgroundColor(android.graphics.Color.parseColor(bgColor));
        tvTestName.setText(test.getTenBKT());
        tvTestDesc.setText(test.getLoaiKiemTra());

        if (test.isLocked() && !isUserPremium) {
            ivLock.setVisibility(View.VISIBLE);
            itemView.setOnClickListener(v -> Toast.makeText(this, getString(R.string.upgrade_required), Toast.LENGTH_SHORT).show());
        } else {
            ivLock.setVisibility(View.GONE);
            itemView.setOnClickListener(v -> openTestDetail(test.getTenBKT(), test.getThoiGianLamBai(), test.getTongSoCau(), folderPrefix));
        }
        container.addView(itemView);
    }

    private void setupFulltestDefault() {
        binding.llFulltest.removeAllViews();
        for (int i = 1; i <= 5; i++) {
            BaiKiemTra test = new BaiKiemTra("ft_" + i, "cd_1", "Test " + i, "ETS 2023", 200, 120, i > 2);
            addTestToLayout(binding.llFulltest, test, R.drawable.ic_test, "#FFFDE7", "");
        }
    }

    private void setupFulltest2Default() {
        binding.llFulltest2.removeAllViews();
        for (int i = 1; i <= 5; i++) {
            BaiKiemTra test = new BaiKiemTra("ft2_" + i, "cd_2", "Test " + i, "ETS 2024", 200, 120, true);
            addTestToLayout(binding.llFulltest2, test, R.drawable.ic_test, "#E1F5FE", "ets2024");
        }
    }

    private void setupSpeakingDefault() {
        binding.llSpeaking.removeAllViews();
        for (int i = 1; i <= 5; i++) {
            BaiKiemTra test = new BaiKiemTra("sp_" + i, "cd_3", "Test " + i, "Speaking", 16, 70, i > 2);
            addTestToLayout(binding.llSpeaking, test, R.drawable.ic_speak, "#FCE4EC", "");
        }
    }

    private void openTestList(String category, String folderPrefix) {
        Intent intent = new Intent(this, TestListActivity.class);
        intent.putExtra(TestListActivity.EXTRA_CATEGORY, category);
        intent.putExtra(TestListActivity.EXTRA_FOLDER_PREFIX, folderPrefix);
        startActivity(intent);
    }

    private void openTestDetail(String title, int time, int questions, String folderPrefix) {
        Intent intent = new Intent(this, TestDetailActivity.class);
        intent.putExtra(TestDetailActivity.EXTRA_TITLE, title);
        intent.putExtra(TestDetailActivity.EXTRA_TIME, time);
        intent.putExtra(TestDetailActivity.EXTRA_QUESTIONS, questions);
        intent.putExtra(EtsTestActivity.EXTRA_FOLDER_PREFIX, folderPrefix);
        startActivity(intent);
    }

    private void setupBottomNavigation() {
        binding.bottomNav.setSelectedItemId(R.id.nav_exam);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_exam) return false;
            Intent intent = null;
            if (id == R.id.nav_home) intent = new Intent(this, HomeActivity.class);
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
