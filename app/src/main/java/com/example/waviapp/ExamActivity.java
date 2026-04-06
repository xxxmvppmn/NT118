package com.example.waviapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.waviapp.databinding.ActivityExamBinding;
import com.example.waviapp.firebase.DatabaseHelper;
import com.example.waviapp.models.BaiKiemTra;
import com.example.waviapp.models.TaiKhoan;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class ExamActivity extends AppCompatActivity {

    private ActivityExamBinding binding;
    private DatabaseHelper dbHelper;
    private boolean isUserPremium = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExamBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new DatabaseHelper();

        // Xử lý sự kiện "Xem Tất cả" (tvSeeMore1, 2, 3)
        binding.tvSeeMore1.setOnClickListener(v -> openTestList(TestListActivity.CAT_FULLTEST));
        binding.tvSeeMore2.setOnClickListener(v -> openTestList(TestListActivity.CAT_MINITEST));
        binding.tvSeeMore3.setOnClickListener(v -> openTestList(TestListActivity.CAT_SPEAKING));

        // Tải dữ liệu Firebase (hoặc hiển thị mẫu)
        loadTestsFromFirebase();

        // Cấu hình thanh điều hướng bên dưới
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        binding.bottomNav.setSelectedItemId(R.id.nav_exam);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(ExamActivity.this, HomeActivity.class));
            } else if (id == R.id.nav_exam) {
                return true;
            } else if (id == R.id.nav_premium) {
                startActivity(new Intent(ExamActivity.this, PremiumActivity.class));
            } else if (id == R.id.nav_setting) {
                startActivity(new Intent(ExamActivity.this, SettingsActivity.class));
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(ExamActivity.this, UserInfoActivity.class));
            }
            overridePendingTransition(0, 0);
            finish();
            return true;
        });
    }

    private void openTestList(String category) {
        Intent intent = new Intent(ExamActivity.this, TestListActivity.class);
        intent.putExtra(TestListActivity.EXTRA_CATEGORY, category);
        startActivity(intent);
    }

    private void openTestDetail(String title, int time, int questions) {
        Intent intent = new Intent(ExamActivity.this, TestDetailActivity.class);
        intent.putExtra(TestDetailActivity.EXTRA_TITLE, title);
        intent.putExtra(TestDetailActivity.EXTRA_TIME, time);
        intent.putExtra(TestDetailActivity.EXTRA_QUESTIONS, questions);
        startActivity(intent);
    }

    private void loadTestsFromFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            dbHelper.getUser(user.getUid(), new DatabaseHelper.UserCallback() {
                @Override
                public void onSuccess(TaiKhoan taiKhoan) {
                    isUserPremium = taiKhoan.isPremium();
                    fetchTestData();
                }

                @Override
                public void onFailure(String error) {
                    isUserPremium = false;
                    fetchTestData();
                }
            });
        } else {
            isUserPremium = false;
            fetchTestData();
        }
    }

    private void fetchTestData() {
        java.util.Comparator<BaiKiemTra> testSorter = new java.util.Comparator<BaiKiemTra>() {
            @Override
            public int compare(BaiKiemTra t1, BaiKiemTra t2) {
                try {
                    String num1 = t1.getMaBKT().replaceAll("\\D+", "");
                    String num2 = t2.getMaBKT().replaceAll("\\D+", "");
                    int n1 = num1.isEmpty() ? 0 : Integer.parseInt(num1);
                    int n2 = num2.isEmpty() ? 0 : Integer.parseInt(num2);
                    return Integer.compare(n1, n2);
                } catch (Exception e) {
                    return t1.getMaBKT().compareTo(t2.getMaBKT());
                }
            }
        };

        // Full Tests
        dbHelper.getTests("fulltest", new DatabaseHelper.TestCallback() {
            @Override
            public void onSuccess(List<BaiKiemTra> tests) {
                if (tests.isEmpty()) {
                    setupFulltestDefault();
                } else {
                    binding.llFulltest.removeAllViews();
                    java.util.Collections.sort(tests, testSorter);
                    for (int i = 0; i < Math.min(5, tests.size()); i++) {
                        addTestToLayout(binding.llFulltest, tests.get(i), R.drawable.ic_test, "#FFFDE7");
                    }
                }
            }
            @Override
            public void onFailure(String error) { setupFulltestDefault(); }
        });

        // Mini Tests
        dbHelper.getTests("minitest", new DatabaseHelper.TestCallback() {
            @Override
            public void onSuccess(List<BaiKiemTra> tests) {
                if (tests.isEmpty()) {
                    setupMinitestDefault();
                } else {
                    binding.llMinitest.removeAllViews();
                    java.util.Collections.sort(tests, testSorter);
                    for (int i = 0; i < Math.min(5, tests.size()); i++) {
                        addTestToLayout(binding.llMinitest, tests.get(i), R.drawable.ic_listen, "#E1F5FE");
                    }
                }
            }
            @Override
            public void onFailure(String error) { setupMinitestDefault(); }
        });

        // Speaking Tests
        dbHelper.getTests("speaking", new DatabaseHelper.TestCallback() {
            @Override
            public void onSuccess(List<BaiKiemTra> tests) {
                if (tests.isEmpty()) {
                    setupSpeakingDefault();
                } else {
                    binding.llSpeaking.removeAllViews();
                    java.util.Collections.sort(tests, testSorter);
                    for (int i = 0; i < Math.min(5, tests.size()); i++) {
                        addTestToLayout(binding.llSpeaking, tests.get(i), R.drawable.ic_speak, "#FCE4EC");
                    }
                }
            }
            @Override
            public void onFailure(String error) { setupSpeakingDefault(); }
        });
    }

    private void addTestToLayout(android.widget.LinearLayout container, BaiKiemTra test, int iconRes, String bgColor) {
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
            itemView.setOnClickListener(v -> Toast.makeText(this, "Vui lòng nâng cấp Premium để làm bài này", Toast.LENGTH_SHORT).show());
        } else {
            ivLock.setVisibility(View.GONE);
            itemView.setOnClickListener(v -> openTestDetail(test.getTenBKT(), test.getThoiGianLamBai(), test.getTongSoCau()));
        }

        container.addView(itemView);
    }

    // ===== Fallback defaults =====

    private void setupFulltestDefault() {
        for (int i = 1; i <= 5; i++) {
            BaiKiemTra test = new BaiKiemTra("ft_dummy_" + i, "cd_1", "Test " + i, "ETS 2023", 200, 120, i > 5);
            addTestToLayout(binding.llFulltest, test, R.drawable.ic_test, "#FFFDE7");
        }
    }

    private void setupMinitestDefault() {
        for (int i = 1; i <= 5; i++) {
            BaiKiemTra test = new BaiKiemTra("mt_dummy_" + i, "cd_2", "Test " + i, "Minitest", 100, 60, i > 5);
            addTestToLayout(binding.llMinitest, test, R.drawable.ic_listen, "#E1F5FE");
        }
    }

    private void setupSpeakingDefault() {
        for (int i = 1; i <= 5; i++) {
            BaiKiemTra test = new BaiKiemTra("sp_dummy_" + i, "cd_3", "Test " + i, "Speaking", 16, 70, i > 5);
            addTestToLayout(binding.llSpeaking, test, R.drawable.ic_speak, "#FCE4EC");
        }
    }
}
