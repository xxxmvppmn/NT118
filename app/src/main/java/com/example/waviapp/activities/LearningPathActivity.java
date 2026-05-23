package com.example.waviapp.activities;

import android.os.Bundle;
import android.widget.Toast;

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

        binding.btnSaveGoal.setOnClickListener(v -> saveGoals());
    }

    private void updateUI() {
        if (currentUser == null) return;

        // Hiển thị mục tiêu điểm
        int scoreGoal = currentUser.getMucTieuDiem();
        if (scoreGoal == 600) binding.rb600.setChecked(true);
        else if (scoreGoal == 700) binding.rb700.setChecked(true);
        else if (scoreGoal == 800) binding.rb800.setChecked(true);
        
        // Hiển thị mục tiêu thời gian
        int timeGoal = currentUser.getMucTieuHangNgay();
        if (timeGoal == 5) binding.rb5min.setChecked(true);
        else if (timeGoal == 10) binding.rb10min.setChecked(true);
        else if (timeGoal == 20) binding.rb20min.setChecked(true);
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
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(LearningPathActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
