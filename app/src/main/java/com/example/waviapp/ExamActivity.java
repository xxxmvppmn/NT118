package com.example.waviapp;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Toast;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.waviapp.databinding.ActivityExamBinding;
import android.view.LayoutInflater;

public class ExamActivity extends AppCompatActivity {

    private ActivityExamBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExamBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Cập nhật trạng thái BottomNavigation hiển thị item Exam được chọn
        binding.bottomNav.setSelectedItemId(R.id.nav_exam);

        // Xử lý sự kiện Bottom Navigation
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent intent = new Intent(ExamActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                finish();
            } else if (id == R.id.nav_exam) {
                // Đang ở màn hình Thi
            } else if (id == R.id.nav_premium) {
                Intent intent = new Intent(ExamActivity.this, PremiumActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(ExamActivity.this, UserInfoActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            } else if (id == R.id.nav_setting) {
                Intent intent = new Intent(ExamActivity.this, SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
            return true;
        });

        // Setup các danh sách thẻ
        setupFulltestList();
        setupMinitestList();
        setupSpeakingList();

        // Xử lý sự kiện click "Xem thêm"
        binding.tvSeeMore1.setOnClickListener(v -> openTestList(TestListActivity.CAT_FULLTEST));
        binding.tvSeeMore2.setOnClickListener(v -> openTestList(TestListActivity.CAT_MINITEST));
        binding.tvSeeMore3.setOnClickListener(v -> openTestList(TestListActivity.CAT_SPEAKING));
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

    private void setupFulltestList() {
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 10; i >= 3; i--) {
            View itemView = inflater.inflate(R.layout.item_test_card, binding.llFulltest, false);
            TextView tvTestName = itemView.findViewById(R.id.tvTestName);
            TextView tvTestDesc = itemView.findViewById(R.id.tvTestDesc);
            ImageView ivLock = itemView.findViewById(R.id.ivLock);

            tvTestName.setText("Test " + i);
            tvTestDesc.setText("ETS 2023");

            ivLock.setVisibility(View.GONE);

            final int testId = i;
            itemView.setOnClickListener(v -> openTestDetail("Test " + testId + " ETS 2023", 120, 200));

            binding.llFulltest.addView(itemView);
        }
    }

    private void setupMinitestList() {
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 1; i <= 4; i++) {
            View itemView = inflater.inflate(R.layout.item_test_card, binding.llMinitest, false);
            TextView tvTestName = itemView.findViewById(R.id.tvTestName);
            TextView tvTestDesc = itemView.findViewById(R.id.tvTestDesc);

            tvTestName.setText("Test " + i);
            tvTestDesc.setVisibility(View.GONE); // Không hiển thị text dưới cùng
            ImageView ivLock = itemView.findViewById(R.id.ivLock);
            ivLock.setVisibility(View.GONE);

            final int testId = i;
            itemView.setOnClickListener(v -> openTestDetail("Test " + testId, 60, 100));

            binding.llMinitest.addView(itemView);
        }
    }

    private void setupSpeakingList() {
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 1; i <= 4; i++) {
            View itemView = inflater.inflate(R.layout.item_test_card, binding.llSpeaking, false);
            TextView tvTestName = itemView.findViewById(R.id.tvTestName);
            TextView tvTestDesc = itemView.findViewById(R.id.tvTestDesc);

            tvTestName.setText("Test " + i);
            tvTestDesc.setVisibility(View.GONE);
            ImageView ivLock = itemView.findViewById(R.id.ivLock);
            ivLock.setVisibility(View.GONE);

            final int testId = i;
            itemView.setOnClickListener(v -> openTestDetail("Test " + testId, 70, 16));

            binding.llSpeaking.addView(itemView);
        }
    }
}
