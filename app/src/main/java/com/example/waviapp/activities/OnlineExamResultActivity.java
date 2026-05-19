package com.example.waviapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.waviapp.R;
import com.example.waviapp.adapters.LeaderboardAdapter;
import com.example.waviapp.databinding.ActivityOnlineExamResultBinding;
import com.example.waviapp.firebase.DatabaseHelper;
import com.example.waviapp.models.OnlineExamResult;

import java.util.List;

public class OnlineExamResultActivity extends BaseActivity {

    public static final String EXTRA_EXAM_ID      = "result_exam_id";
    public static final String EXTRA_EXAM_TITLE   = "result_exam_title";
    public static final String EXTRA_SCORE        = "result_score";
    public static final String EXTRA_TOTAL        = "result_total";
    public static final String EXTRA_DURATION_SEC = "result_duration_sec";
    public static final String EXTRA_USER_ID      = "result_user_id";

    private ActivityOnlineExamResultBinding binding;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnlineExamResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new DatabaseHelper();

        String examId      = getIntent().getStringExtra(EXTRA_EXAM_ID);
        String examTitle   = getIntent().getStringExtra(EXTRA_EXAM_TITLE);
        int score          = getIntent().getIntExtra(EXTRA_SCORE, 0);
        int total          = getIntent().getIntExtra(EXTRA_TOTAL, 0);
        int durationSec    = getIntent().getIntExtra(EXTRA_DURATION_SEC, 0);
        String currentUserId = getIntent().getStringExtra(EXTRA_USER_ID);

        binding.ivBackResult.setOnClickListener(v -> finish());
        binding.btnBackToList.setOnClickListener(v -> {
            Intent intent = new Intent(this, OnlineExamActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        // Hiển thị kết quả
        displayResult(examTitle, score, total, durationSec);

        // Load leaderboard
        if (examId != null) {
            loadLeaderboard(examId, currentUserId, score, total);
        }
    }

    private void displayResult(String examTitle, int score, int total, int durationSec) {
        // Tên kỳ thi
        binding.tvResultExamTitle.setText(examTitle != null ? examTitle : "Kỳ thi");

        // Điểm
        binding.tvResultScore.setText(score + "/" + total);

        // Phần trăm
        int percent = total > 0 ? (int)(score * 100.0 / total) : 0;
        binding.tvResultPercent.setText(percent + "%");

        // Message dựa theo điểm
        String message;
        if (percent >= 90)      message = "🎉 Xuất sắc! Bạn thật tuyệt vời!";
        else if (percent >= 70) message = "👍 Tốt lắm! Tiếp tục cố gắng nhé!";
        else if (percent >= 50) message = "😊 Ổn đấy! Luyện tập thêm nhé!";
        else                    message = "💪 Cần cố gắng hơn! Đừng bỏ cuộc!";
        binding.tvResultMessage.setText(message);

        // Đúng / Sai
        binding.tvResultCorrect.setText(String.valueOf(score));
        binding.tvResultWrong.setText(String.valueOf(total - score));

        // Thời gian
        int min = durationSec / 60;
        int sec = durationSec % 60;
        binding.tvResultTime.setText(String.format("%d:%02d", min, sec));
    }

    private void loadLeaderboard(String examId, String currentUserId, int myScore, int total) {
        dbHelper.getOnlineExamLeaderboard(examId, new DatabaseHelper.OnlineExamResultCallback() {
            @Override
            public void onSuccess(List<OnlineExamResult> results) {
                // Tính rank của user hiện tại
                int myRank = results.size() + 1;
                for (int i = 0; i < results.size(); i++) {
                    if (currentUserId != null &&
                            currentUserId.equals(results.get(i).getUserId())) {
                        myRank = i + 1;
                        break;
                    }
                }
                binding.tvResultRank.setText("#" + myRank + " / " + results.size() + " người thi");

                // Setup RecyclerView leaderboard
                LeaderboardAdapter adapter = new LeaderboardAdapter(results, currentUserId);
                binding.rvLeaderboard.setLayoutManager(
                        new LinearLayoutManager(OnlineExamResultActivity.this));
                binding.rvLeaderboard.setAdapter(adapter);
            }

            @Override
            public void onFailure(String error) {
                binding.tvResultRank.setText("Không thể tải bảng xếp hạng");
                Toast.makeText(OnlineExamResultActivity.this,
                        "Lỗi tải bảng xếp hạng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
