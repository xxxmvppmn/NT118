package com.example.waviapp.activities;

import android.os.Bundle;
import android.widget.Toast;

import com.example.waviapp.R;
import com.example.waviapp.databinding.ActivityLearningPathBinding;
import com.example.waviapp.firebase.DatabaseHelper;
import com.example.waviapp.managers.UserSessionManager;
import com.example.waviapp.models.TaiKhoan;

import java.util.HashMap;
import java.util.Map;

public class LearningPathActivity extends BaseActivity {

    private ActivityLearningPathBinding binding;
    private DatabaseHelper dbHelper;
    private TaiKhoan currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLearningPathBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new DatabaseHelper();
        binding.ivBack.setOnClickListener(v -> finish());

        UserSessionManager.getInstance().getUserLiveData().observe(this, user -> {
            if (user != null) {
                currentUser = user;
                updateUI();
            }
        });

        // Lắng nghe thay đổi của mục tiêu điểm số để cập nhật gợi ý học tập động
        binding.rgScoreGoal.setOnCheckedChangeListener((group, checkedId) -> {
            updateSuggestion(checkedId);
        });

        binding.btnSaveGoal.setOnClickListener(v -> saveGoals());
    }

    private void updateUI() {
        if (currentUser == null) return;

        // Hiển thị mục tiêu điểm
        int scoreGoal = currentUser.getMucTieuDiem();
        if (scoreGoal == 600) {
            binding.rb600.setChecked(true);
            updateSuggestion(R.id.rb600);
        } else if (scoreGoal == 700) {
            binding.rb700.setChecked(true);
            updateSuggestion(R.id.rb700);
        } else if (scoreGoal == 800) {
            binding.rb800.setChecked(true);
            updateSuggestion(R.id.rb800);
        }
        
        // Hiển thị mục tiêu thời gian
        int timeGoal = currentUser.getMucTieuHangNgay();
        if (timeGoal == 5) binding.rb5min.setChecked(true);
        else if (timeGoal == 10) binding.rb10min.setChecked(true);
        else if (timeGoal == 20) binding.rb20min.setChecked(true);
    }

    private void updateSuggestion(int checkedId) {
        if (checkedId == R.id.rb600) {
            binding.tvGoalSuggestion.setText(getString(R.string.suggestion_600));
        } else if (checkedId == R.id.rb700) {
            binding.tvGoalSuggestion.setText(getString(R.string.suggestion_700));
        } else if (checkedId == R.id.rb800) {
            binding.tvGoalSuggestion.setText(getString(R.string.suggestion_800));
        }
    }

    private void saveGoals() {
        if (currentUser == null) return;

        int score = 600;
        if (binding.rb700.isChecked()) score = 700;
        else if (binding.rb800.isChecked()) score = 800;

        int time = 5;
        if (binding.rb10min.isChecked()) time = 10;
        else if (binding.rb20min.isChecked()) time = 20;

        Map<String, Object> updates = new HashMap<>();
        updates.put("mucTieuHangNgay", time);
        updates.put("mucTieuDiem", score);

        final int finalTime = time;
        final int finalScore = score;

        dbHelper.updateUser(currentUser.getId(), updates, new DatabaseHelper.SimpleCallback() {
            @Override
            public void onSuccess() {
                currentUser.setMucTieuHangNgay(finalTime);
                currentUser.setMucTieuDiem(finalScore);
                UserSessionManager.getInstance().updateUserDataLocally(currentUser);
                updateUI();
                
                // Hiển thị thông báo lưu thành công
                Toast.makeText(LearningPathActivity.this, getString(R.string.goal_save_success), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(LearningPathActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
