package com.example.waviapp.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.example.waviapp.R;
import com.example.waviapp.adapters.Part5Adapter;
import com.example.waviapp.models.Part5Question;
import com.example.waviapp.utils.JsonHelper;
import com.example.waviapp.utils.ProgressManager;
import com.github.jinatonic.confetti.CommonConfetti;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class Part5PracticeActivity extends BaseActivity {

    private RecyclerView rvQuestions;
    private Part5Adapter adapter;
    private List<Part5Question> fullQuestionList;
    private List<Part5Question> setQuestions = new ArrayList<>();
    private ProgressBar progressBar;
    private TextView tvProgressText;
    private MaterialButton btnNext;
    private ImageView ivBack;
    private int setIndex;
    private ProgressManager progressManager;
    private int correctCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_part5);

        progressManager = new ProgressManager(this);

        // 1. Lấy đúng SET_INDEX từ màn hình chọn bài
        setIndex = getIntent().getIntExtra("SET_INDEX", 0);
        Log.d("WaviDebug", "Activity nhận SetIndex: " + setIndex);

        initViews();

        // 2. Phải cắt dữ liệu trước khi làm bất cứ việc gì khác
        loadSetData();

        // 3. Sau đó mới kiểm tra và hiển thị giao diện
        checkContinueProgress();
    }

    private void initViews() {
        rvQuestions = findViewById(R.id.rvQuestions);
        progressBar = findViewById(R.id.progressBar);
        tvProgressText = findViewById(R.id.tvProgressText);
        btnNext = findViewById(R.id.btnNext);
        ivBack = findViewById(R.id.ivBack);

        TextView tvTitle = findViewById(R.id.tvToolbarTitle);
        if (tvTitle != null) {
            tvTitle.setText("Set " + (setIndex + 1));
        }
        ivBack.setOnClickListener(v -> finish());
    }

    private void loadSetData() {
        fullQuestionList = JsonHelper.loadPart5Questions(this);
        setQuestions.clear();

        // Logic chia 20 câu: 0-19, 20-39, 40-59...
        int start = setIndex * 20;
        int end = Math.min(start + 20, fullQuestionList.size());

        Log.d("WaviDebug", "Cắt từ câu: " + start + " đến " + end);

        for (int i = start; i < end; i++) {
            setQuestions.add(fullQuestionList.get(i));
        }

        if (progressBar != null) {
            progressBar.setMax(setQuestions.size());
        }
    }

    private void checkContinueProgress() {
        int savedIndex = progressManager.getSetProgress(setIndex);
        if (savedIndex > 0 && savedIndex < setQuestions.size() - 1) {
            new AlertDialog.Builder(this)
                    .setTitle("Continue?")
                    .setMessage("Do you want to continue from your last question?")
                    .setPositiveButton("Continue", (dialog, which) -> setupRecyclerView(savedIndex))
                    .setNegativeButton("Restart", (dialog, which) -> {
                        progressManager.resetSetProgress(setIndex);
                        setupRecyclerView(0);
                    })
                    .setCancelable(false)
                    .show();
        } else {
            setupRecyclerView(0);
        }
    }

    private void setupRecyclerView(int startPos) {
        if (adapter == null) {
            adapter = new Part5Adapter(this, setQuestions, (position, isCorrect) -> {
                if (isCorrect) {
                    correctCount++;
                    progressManager.addXP(10);
                }
                btnNext.setEnabled(true);
                updateProgress(position + 1);
                progressManager.saveSetProgress(setIndex, position);

                if (position == setQuestions.size() - 1) {
                    showCelebration();
                }
            });
            rvQuestions.setAdapter(adapter);

            LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            rvQuestions.setLayoutManager(layoutManager);

            PagerSnapHelper snapHelper = new PagerSnapHelper();
            snapHelper.attachToRecyclerView(rvQuestions);
        } else {
            adapter.setQuestions(setQuestions);
        }

        rvQuestions.scrollToPosition(startPos);
        updateProgress(startPos + 1);

        btnNext.setOnClickListener(v -> {
            LinearLayoutManager lm = (LinearLayoutManager) rvQuestions.getLayoutManager();
            int current = lm.findFirstVisibleItemPosition();
            if (current < setQuestions.size() - 1) {
                rvQuestions.smoothScrollToPosition(current + 1);
                btnNext.setEnabled(false);
            }
        });
    }

    private void updateProgress(int progress) {
        if (progressBar != null) progressBar.setProgress(progress);
        if (tvProgressText != null) tvProgressText.setText(progress + "/" + setQuestions.size());
    }

    private void showCelebration() {
        CommonConfetti.rainingConfetti(findViewById(android.R.id.content), new int[] {
                getColor(R.color.purple_main),
                android.graphics.Color.WHITE,
                android.graphics.Color.YELLOW
        }).oneShot();

        progressManager.markSetCompleted(setIndex);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_summary_celebration, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        TextView tvCorrect = dialogView.findViewById(R.id.tvCorrectCount);
        TextView tvXP = dialogView.findViewById(R.id.tvXPCount);
        MaterialButton btnFinish = dialogView.findViewById(R.id.btnFinish);

        tvCorrect.setText(correctCount + "/" + setQuestions.size());
        tvXP.setText("+" + (correctCount * 10) + " XP");

        btnFinish.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        dialog.show();
    }
}