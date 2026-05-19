package com.example.waviapp.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.waviapp.R;
import com.example.waviapp.databinding.ActivityOnlineExamRoomBinding;
import com.example.waviapp.firebase.DatabaseHelper;
import com.example.waviapp.managers.UserSessionManager;
import com.example.waviapp.models.OnlineExamQuestion;
import com.example.waviapp.models.OnlineExamResult;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnlineExamRoomActivity extends BaseActivity {

    private ActivityOnlineExamRoomBinding binding;
    private DatabaseHelper dbHelper;

    private String examId, examTitle, examColor;
    private int totalQuestions, durationMinutes;

    private List<OnlineExamQuestion> questions;
    private Map<Integer, String> userAnswers = new HashMap<>(); // index → "A"/"B"/"C"/"D"
    private int currentIndex = 0;

    private CountDownTimer countDownTimer;
    private long remainingMs;
    private long startTimeMs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnlineExamRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new DatabaseHelper();

        // Nhận intent extras
        examId          = getIntent().getStringExtra(OnlineExamActivity.EXTRA_EXAM_ID);
        examTitle       = getIntent().getStringExtra(OnlineExamActivity.EXTRA_EXAM_TITLE);
        totalQuestions  = getIntent().getIntExtra(OnlineExamActivity.EXTRA_EXAM_TOTAL, 20);
        durationMinutes = getIntent().getIntExtra(OnlineExamActivity.EXTRA_EXAM_MINS, 20);
        examColor       = getIntent().getStringExtra(OnlineExamActivity.EXTRA_EXAM_COLOR);

        binding.tvExamTitleBar.setText(examTitle != null ? examTitle : "Thi Online");

        binding.ivBackRoom.setOnClickListener(v -> confirmExit());

        // Load câu hỏi từ Firestore
        loadQuestions();
    }

    private void loadQuestions() {
        showLoading(true);
        dbHelper.getOnlineExamQuestions(examId, new DatabaseHelper.OnlineExamQuestionCallback() {
            @Override
            public void onSuccess(List<OnlineExamQuestion> qs) {
                showLoading(false);
                questions = qs;
                if (questions.isEmpty()) {
                    Toast.makeText(OnlineExamRoomActivity.this,
                            "Không có câu hỏi!", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                startTimeMs = System.currentTimeMillis();
                remainingMs = (long) durationMinutes * 60 * 1000;
                startTimer();
                showQuestion(0);
            }

            @Override
            public void onFailure(String error) {
                showLoading(false);
                Toast.makeText(OnlineExamRoomActivity.this,
                        "Lỗi tải câu hỏi: " + error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(remainingMs, 1000) {
            @Override
            public void onTick(long ms) {
                long mins = ms / 60000;
                long secs = (ms % 60000) / 1000;
                binding.tvTimer.setText(String.format("%02d:%02d", mins, secs));
                // Đổi màu đỏ khi còn 60 giây
                if (ms <= 60000) {
                    binding.tvTimer.setTextColor(Color.parseColor("#E53935"));
                }
            }

            @Override
            public void onFinish() {
                binding.tvTimer.setText("00:00");
                Toast.makeText(OnlineExamRoomActivity.this,
                        "Hết giờ! Bài thi tự động nộp.", Toast.LENGTH_SHORT).show();
                submitExam();
            }
        }.start();
    }

    private void showQuestion(int index) {
        if (questions == null || index >= questions.size()) return;
        currentIndex = index;
        OnlineExamQuestion q = questions.get(index);

        // Progress
        int total = questions.size();
        binding.tvQuestionProgress.setText("Câu " + (index + 1) + "/" + total);
        binding.tvAnsweredCount.setText("Đã trả lời: " + userAnswers.size());
        binding.progressBar.setProgress((int)((index + 1) * 100.0 / total));

        // Nội dung
        binding.tvQuestionNumber.setText("Câu " + (index + 1));
        binding.tvQuestionText.setText(q.getQuestionText());
        binding.tvOptionA.setText(q.getOptionA());
        binding.tvOptionB.setText(q.getOptionB());
        binding.tvOptionC.setText(q.getOptionC());
        binding.tvOptionD.setText(q.getOptionD());

        // Reset tất cả option về trạng thái mặc định
        resetOptionStyles();

        // Highlight đáp án đã chọn (nếu có)
        String selected = userAnswers.get(index);
        if (selected != null) highlightSelected(selected);

        // Navigation buttons
        binding.btnPrev.setVisibility(index > 0 ? View.VISIBLE : View.INVISIBLE);
        boolean isLast = (index == total - 1);
        binding.btnNext.setVisibility(isLast ? View.GONE : View.VISIBLE);
        binding.btnSubmit.setVisibility(isLast ? View.VISIBLE : View.GONE);

        // Option click listeners
        binding.cardOptionA.setOnClickListener(v -> selectAnswer("A"));
        binding.cardOptionB.setOnClickListener(v -> selectAnswer("B"));
        binding.cardOptionC.setOnClickListener(v -> selectAnswer("C"));
        binding.cardOptionD.setOnClickListener(v -> selectAnswer("D"));

        // Navigation
        binding.btnPrev.setOnClickListener(v -> showQuestion(currentIndex - 1));
        binding.btnNext.setOnClickListener(v -> showQuestion(currentIndex + 1));
        binding.btnSubmit.setOnClickListener(v -> confirmSubmit());
    }

    private void selectAnswer(String answer) {
        userAnswers.put(currentIndex, answer);
        resetOptionStyles();
        highlightSelected(answer);
        binding.tvAnsweredCount.setText("Đã trả lời: " + userAnswers.size());
    }

    private void resetOptionStyles() {
        int defaultStroke = Color.parseColor("#E0E0E0");
        int defaultBg = Color.WHITE;
        applyOptionStyle(binding.cardOptionA, binding.tvLabelA, defaultBg, defaultStroke, "#9370DB");
        applyOptionStyle(binding.cardOptionB, binding.tvLabelB, defaultBg, defaultStroke, "#9370DB");
        applyOptionStyle(binding.cardOptionC, binding.tvLabelC, defaultBg, defaultStroke, "#9370DB");
        applyOptionStyle(binding.cardOptionD, binding.tvLabelD, defaultBg, defaultStroke, "#9370DB");
    }

    private void highlightSelected(String answer) {
        int selectedBg  = Color.parseColor("#F3E5F5");
        int selectedStroke = Color.parseColor("#9370DB");
        switch (answer) {
            case "A": applyOptionStyle(binding.cardOptionA, binding.tvLabelA, selectedBg, selectedStroke, "#7B1FA2"); break;
            case "B": applyOptionStyle(binding.cardOptionB, binding.tvLabelB, selectedBg, selectedStroke, "#7B1FA2"); break;
            case "C": applyOptionStyle(binding.cardOptionC, binding.tvLabelC, selectedBg, selectedStroke, "#7B1FA2"); break;
            case "D": applyOptionStyle(binding.cardOptionD, binding.tvLabelD, selectedBg, selectedStroke, "#7B1FA2"); break;
        }
    }

    private void applyOptionStyle(com.google.android.material.card.MaterialCardView card,
                                   android.widget.TextView labelView,
                                   int bgColor, int strokeColor, String labelColorHex) {
        card.setCardBackgroundColor(bgColor);
        card.setStrokeColor(strokeColor);
        labelView.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(Color.parseColor(labelColorHex)));
    }

    private void confirmSubmit() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận nộp bài")
                .setMessage("Bạn đã trả lời " + userAnswers.size() + "/" + questions.size()
                        + " câu.\nBạn có chắc muốn nộp bài không?")
                .setPositiveButton("Nộp bài", (d, w) -> submitExam())
                .setNegativeButton("Tiếp tục làm", null)
                .show();
    }

    private void confirmExit() {
        new AlertDialog.Builder(this)
                .setTitle("Thoát khỏi bài thi?")
                .setMessage("Bài thi sẽ không được lưu nếu bạn thoát.")
                .setPositiveButton("Thoát", (d, w) -> {
                    if (countDownTimer != null) countDownTimer.cancel();
                    finish();
                })
                .setNegativeButton("Tiếp tục", null)
                .show();
    }

    private void submitExam() {
        if (countDownTimer != null) countDownTimer.cancel();

        // Tính điểm
        int score = 0;
        for (int i = 0; i < questions.size(); i++) {
            String userAns = userAnswers.get(i);
            String correct = questions.get(i).getCorrectAnswer();
            if (correct != null && correct.equals(userAns)) score++;
        }

        int durationSeconds = (int)((System.currentTimeMillis() - startTimeMs) / 1000);

        // Lấy thông tin user
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = firebaseUser != null ? firebaseUser.getUid() : "anonymous";
        String displayName = "Bạn";
        com.example.waviapp.models.TaiKhoan tk = UserSessionManager.getInstance().getUserData();
        if (tk != null && tk.getHoTen() != null) displayName = tk.getHoTen();
        else if (firebaseUser != null && firebaseUser.getDisplayName() != null)
            displayName = firebaseUser.getDisplayName();

        final int finalScore = score;
        final String finalName = displayName;
        OnlineExamResult result = new OnlineExamResult(
                userId, finalName, examId,
                finalScore, questions.size(),
                durationSeconds, Timestamp.now()
        );

        // Lưu lên Firestore
        dbHelper.submitOnlineExamResult(examId, result, new DatabaseHelper.SimpleCallback() {
            @Override
            public void onSuccess() {
                openResult(finalScore, durationSeconds, finalName, userId);
            }
            @Override
            public void onFailure(String error) {
                // Vẫn mở màn hình kết quả dù lưu lỗi
                openResult(finalScore, durationSeconds, finalName, userId);
            }
        });
    }

    private void openResult(int score, int durationSeconds, String displayName, String userId) {
        Intent intent = new Intent(this, OnlineExamResultActivity.class);
        intent.putExtra(OnlineExamResultActivity.EXTRA_EXAM_ID,      examId);
        intent.putExtra(OnlineExamResultActivity.EXTRA_EXAM_TITLE,   examTitle);
        intent.putExtra(OnlineExamResultActivity.EXTRA_SCORE,        score);
        intent.putExtra(OnlineExamResultActivity.EXTRA_TOTAL,        questions.size());
        intent.putExtra(OnlineExamResultActivity.EXTRA_DURATION_SEC, durationSeconds);
        intent.putExtra(OnlineExamResultActivity.EXTRA_USER_ID,      userId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        // Dùng scrollContent để ẩn/hiện khi loading
        binding.scrollContent.setVisibility(show ? View.GONE : View.VISIBLE);
        binding.bottomActions.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }

    @Override
    public void onBackPressed() {
        confirmExit();
    }
}
