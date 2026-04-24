package com.example.waviapp.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.waviapp.R;
import com.example.waviapp.firebase.DatabaseHelper;
import com.example.waviapp.models.WriteQuestion;
import com.example.waviapp.utils.ProgressManager;
import com.example.waviapp.utils.SkillDataProvider;
import com.github.jinatonic.confetti.CommonConfetti;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

/**
 * Màn hình luyện viết Writing.
 * Chức năng:
 *  - Hiển thị đề bài + keyword chips (Part 1) hoặc email (Part 2/3)
 *  - EditText cho người dùng gõ câu trả lời
 *  - Word counter cập nhật real-time
 *  - Toggle xem sample answer (collapsible)
 *  - Nút Next chuyển bài tiếp
 *  - Dialog kết quả khi hoàn thành
 */
public class WritePracticeActivity extends BaseActivity {

    // Views
    private TextView tvToolbarTitle, tvProgressText, tvInstruction, tvPrompt;
    private TextView tvWordCount, tvWordLimit, tvSampleAnswer;
    private EditText etAnswer;
    private ProgressBar progressBar;
    private LinearLayout llKeywords, llToggleSample;
    private Chip chipKeyword1, chipKeyword2;
    private ImageView ivToggleArrow;
    private MaterialButton btnNext;
    private View cardSampleAnswer;

    // Data
    private List<WriteQuestion> questions;
    private int currentIndex = 0;
    private int partIndex = 0;

    // State
    private boolean sampleVisible = false;
    private int submittedCount = 0;

    // Progress
    private ProgressManager progressManager;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_practice);

        partIndex = getIntent().getIntExtra(WriteSetActivity.EXTRA_PART_INDEX, 0);
        progressManager = new ProgressManager(this);
        dbHelper = new DatabaseHelper();

        initViews();
        loadData();
        displayQuestion(currentIndex);
    }

    private void initViews() {
        tvToolbarTitle  = findViewById(R.id.tvToolbarTitle);
        tvProgressText  = findViewById(R.id.tvProgressText);
        tvInstruction   = findViewById(R.id.tvInstruction);
        tvPrompt        = findViewById(R.id.tvPrompt);
        tvWordCount     = findViewById(R.id.tvWordCount);
        tvWordLimit     = findViewById(R.id.tvWordLimit);
        tvSampleAnswer  = findViewById(R.id.tvSampleAnswer);
        etAnswer        = findViewById(R.id.etAnswer);
        progressBar     = findViewById(R.id.progressBar);
        llKeywords      = findViewById(R.id.llKeywords);
        llToggleSample  = findViewById(R.id.llToggleSample);
        chipKeyword1    = findViewById(R.id.chipKeyword1);
        chipKeyword2    = findViewById(R.id.chipKeyword2);
        ivToggleArrow   = findViewById(R.id.ivToggleArrow);
        btnNext         = findViewById(R.id.btnNext);
        cardSampleAnswer = findViewById(R.id.cardSampleAnswer);

        ImageView ivBack = findViewById(R.id.ivBack);
        if (ivBack != null) ivBack.setOnClickListener(v -> finish());

        // Part title
        String[] partTitles = {
            "Part 1 - Mô tả tranh",
            "Part 2 - Phản hồi yêu cầu",
            "Part 3 - Viết luận"
        };
        if (tvToolbarTitle != null) {
            tvToolbarTitle.setText(partIndex < partTitles.length ? partTitles[partIndex] : "Writing");
        }

        // Word counter
        etAnswer.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int af) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                int words = countWords(s.toString());
                tvWordCount.setText(words + " từ");
                // Color feedback
                WriteQuestion q = questions.get(currentIndex);
                if (words >= q.getMinWords()) {
                    tvWordCount.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
                } else {
                    tvWordCount.setTextColor(android.graphics.Color.parseColor("#9370DB"));
                }
            }
        });

        // Toggle sample answer
        llToggleSample.setOnClickListener(v -> toggleSampleAnswer());

        // Next button
        btnNext.setOnClickListener(v -> goNext());
    }

    private void loadData() {
        questions = SkillDataProvider.getWriteQuestions(partIndex);
        progressBar.setMax(questions.size());
    }

    private void displayQuestion(int idx) {
        if (idx >= questions.size()) return;
        WriteQuestion q = questions.get(idx);

        tvInstruction.setText(q.getInstruction());
        tvPrompt.setText(q.getPrompt());
        tvProgressText.setText((idx + 1) + "/" + questions.size());
        progressBar.setProgress(idx + 1);

        // Word limit
        tvWordLimit.setText(" / tối thiểu " + q.getMinWords() + " từ");

        // Keywords (Part 1 only)
        if (q.getKeyword1() != null && !q.getKeyword1().isEmpty()) {
            llKeywords.setVisibility(View.VISIBLE);
            chipKeyword1.setText(q.getKeyword1());
            chipKeyword2.setText(q.getKeyword2());
        } else {
            llKeywords.setVisibility(View.GONE);
        }

        // Sample answer
        tvSampleAnswer.setText(q.getSampleAnswer());

        // Reset
        etAnswer.setText("");
        tvWordCount.setText("0 từ");
        tvWordCount.setTextColor(android.graphics.Color.parseColor("#9370DB"));
        sampleVisible = false;
        tvSampleAnswer.setVisibility(View.GONE);
        ivToggleArrow.setRotation(-90);

        // Next button text
        btnNext.setText(idx == questions.size() - 1 ? "Hoàn thành ✓" : "Bài tiếp theo →");
    }

    private void toggleSampleAnswer() {
        if (sampleVisible) {
            // Hide
            tvSampleAnswer.setVisibility(View.GONE);
            rotateArrow(0, -90);
            sampleVisible = false;
        } else {
            // Show
            tvSampleAnswer.setVisibility(View.VISIBLE);
            rotateArrow(-90, 0);
            sampleVisible = true;
        }
    }

    private void rotateArrow(float from, float to) {
        RotateAnimation anim = new RotateAnimation(
                from, to,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(200);
        anim.setFillAfter(true);
        ivToggleArrow.startAnimation(anim);
    }

    private void goNext() {
        String answer = etAnswer.getText().toString().trim();
        int wordCount = countWords(answer);
        WriteQuestion q = questions.get(currentIndex);

        if (answer.isEmpty()) {
            Toast.makeText(this, "Hãy viết câu trả lời của bạn trước!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (wordCount < q.getMinWords()) {
            Toast.makeText(this,
                    "Cần ít nhất " + q.getMinWords() + " từ (hiện tại: " + wordCount + ")",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Mark as submitted
        submittedCount++;
        progressManager.addXP(10);

        // Lưu lên Firestore (fire-and-forget)
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            dbHelper.saveWriteProgress(
                    user.getUid(), partIndex, currentIndex,
                    answer, wordCount, null);
        }

        if (currentIndex < questions.size() - 1) {
            currentIndex++;
            displayQuestion(currentIndex);
        } else {
            showCompletion();
        }
    }

    private void showCompletion() {
        CommonConfetti.rainingConfetti(
                findViewById(android.R.id.content),
                new int[]{getColor(R.color.purple_main),
                          android.graphics.Color.WHITE,
                          android.graphics.Color.YELLOW}
        ).oneShot();

        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_summary_celebration, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        TextView tvCorrect = dialogView.findViewById(R.id.tvCorrectCount);
        TextView tvXP      = dialogView.findViewById(R.id.tvXPCount);
        MaterialButton btnFinish = dialogView.findViewById(R.id.btnFinish);

        tvCorrect.setText(submittedCount + "/" + questions.size() + " bài đã nộp");
        tvXP.setText("+" + (submittedCount * 10) + " XP");
        btnFinish.setOnClickListener(v -> { dialog.dismiss(); finish(); });
        dialog.show();
    }

    // ─── Helpers ───

    private int countWords(String text) {
        if (text == null || text.trim().isEmpty()) return 0;
        String[] words = text.trim().split("\\s+");
        return words.length;
    }
}
