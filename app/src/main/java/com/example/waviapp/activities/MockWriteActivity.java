package com.example.waviapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.waviapp.R;
import com.example.waviapp.models.WriteQuestion;
import com.example.waviapp.utils.SkillDataProvider;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MockWriteActivity extends BaseActivity {

    private TextView tvToolbarTitle, tvTimer, tvProgressText;
    private ProgressBar progressBar;
    private TextView tvInstruction, tvPrompt, tvKeyword1, tvKeyword2, tvWordCount;
    private ImageView ivQuestionImage;
    private LinearLayout llKeywords;
    private EditText etAnswer;
    private MaterialButton btnBack, btnNext;

    private List<WriteQuestion> questions;
    private ArrayList<String> audioPaths;
    private String[] answers;

    private int currentIndex = 0;
    private CountDownTimer currentTimer;
    private long timeLeftInMillis = 0;
    
    // Timers configuration
    private static final long PART1_TIME = 8 * 60 * 1000L;
    private static final long PART2_TIME = 10 * 60 * 1000L;
    private static final long PART3_TIME = 30 * 60 * 1000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mock_write);

        audioPaths = getIntent().getStringArrayListExtra("audioPaths");
        if (audioPaths == null) audioPaths = new ArrayList<>();

        questions = SkillDataProvider.getMockWritingQuestions();
        answers = new String[questions.size()];
        for (int i = 0; i < answers.length; i++) answers[i] = "";

        initViews();
        setupAntiCheat();
        
        displayQuestion(0);
    }

    private void initViews() {
        tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        tvTimer = findViewById(R.id.tvTimer);
        tvProgressText = findViewById(R.id.tvProgressText);
        progressBar = findViewById(R.id.progressBar);
        
        tvInstruction = findViewById(R.id.tvInstruction);
        tvPrompt = findViewById(R.id.tvPrompt);
        ivQuestionImage = findViewById(R.id.ivQuestionImage);
        llKeywords = findViewById(R.id.llKeywords);
        tvKeyword1 = findViewById(R.id.tvKeyword1);
        tvKeyword2 = findViewById(R.id.tvKeyword2);
        
        etAnswer = findViewById(R.id.etAnswer);
        tvWordCount = findViewById(R.id.tvWordCount);
        
        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);
        
        progressBar.setMax(questions.size());

        etAnswer.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                answers[currentIndex] = s.toString();
                if (questions.get(currentIndex).getPartNumber() == 3) {
                    updateWordCount(s.toString());
                }
            }
        });

        btnBack.setOnClickListener(v -> navigate(-1));
        btnNext.setOnClickListener(v -> navigate(1));
    }

    private void setupAntiCheat() {
        etAnswer.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            public boolean onCreateActionMode(ActionMode mode, Menu menu) { return false; }
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) { return false; }
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) { return false; }
            public void onDestroyActionMode(ActionMode mode) {}
        });
    }

    private void displayQuestion(int index) {
        if (index >= questions.size()) {
            submitExam();
            return;
        }

        // Save current time state if moving within Part 1
        boolean isSamePart = (index > 0 && questions.get(index).getPartNumber() == questions.get(currentIndex).getPartNumber());
        
        currentIndex = index;
        WriteQuestion q = questions.get(currentIndex);
        
        tvProgressText.setText((currentIndex + 1) + "/" + questions.size());
        progressBar.setProgress(currentIndex + 1);
        
        tvInstruction.setText(q.getInstruction());
        tvPrompt.setText(q.getPrompt());
        etAnswer.setText(answers[currentIndex]);

        // Setup Part specific UI
        if (q.getPartNumber() == 1) {
            llKeywords.setVisibility(View.VISIBLE);
            tvKeyword1.setText(q.getKeyword1());
            tvKeyword2.setText(q.getKeyword2());
            ivQuestionImage.setVisibility(View.VISIBLE);
            ivQuestionImage.setImageResource(R.drawable.wavi_background); // Mock image
            tvWordCount.setVisibility(View.GONE);
            
            btnBack.setVisibility(currentIndex > 0 && questions.get(currentIndex - 1).getPartNumber() == 1 ? View.VISIBLE : View.GONE);
            btnNext.setText(currentIndex == 4 ? "Tiếp tục (Part 2)" : "Next");
            
            if (!isSamePart) startTimer(PART1_TIME);
            
        } else if (q.getPartNumber() == 2) {
            llKeywords.setVisibility(View.GONE);
            ivQuestionImage.setVisibility(View.GONE);
            tvWordCount.setVisibility(View.GONE);
            
            btnBack.setVisibility(View.GONE); // No back in Part 2
            btnNext.setText(currentIndex == 6 ? "Tiếp tục (Part 3)" : "Nộp & Qua câu sau");
            
            startTimer(PART2_TIME);
            
        } else if (q.getPartNumber() == 3) {
            llKeywords.setVisibility(View.GONE);
            ivQuestionImage.setVisibility(View.GONE);
            tvWordCount.setVisibility(View.VISIBLE);
            updateWordCount(etAnswer.getText().toString());
            
            btnBack.setVisibility(View.GONE);
            btnNext.setText("Nộp bài thi");
            
            startTimer(PART3_TIME);
        }
    }

    private void navigate(int direction) {
        int nextIndex = currentIndex + direction;
        WriteQuestion currentQ = questions.get(currentIndex);
        
        if (direction > 0 && currentQ.getPartNumber() > 1) {
            // Confirm early submission for Part 2 and 3
            displayQuestion(nextIndex);
        } else {
            // Free navigation in Part 1
            displayQuestion(nextIndex);
        }
    }

    private void updateWordCount(String text) {
        String[] words = text.trim().split("\\s+");
        int count = text.trim().isEmpty() ? 0 : words.length;
        tvWordCount.setText("Word count: " + count);
    }

    private void startTimer(long timeInMillis) {
        if (currentTimer != null) currentTimer.cancel();
        
        timeLeftInMillis = timeInMillis;
        currentTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long ms) {
                timeLeftInMillis = ms;
                int min = (int) (ms / 1000) / 60;
                int sec = (int) (ms / 1000) % 60;
                tvTimer.setText(String.format(Locale.US, "%02d:%02d", min, sec));
            }

            @Override
            public void onFinish() {
                tvTimer.setText("00:00");
                handleTimeUp();
            }
        }.start();
    }

    private void handleTimeUp() {
        WriteQuestion q = questions.get(currentIndex);
        if (q.getPartNumber() == 1) {
            displayQuestion(5); // Jump to Part 2
        } else if (q.getPartNumber() == 2) {
            displayQuestion(currentIndex + 1); // Jump to next question
        } else {
            submitExam();
        }
    }

    private void submitExam() {
        if (currentTimer != null) currentTimer.cancel();
        
        Intent intent = new Intent(MockWriteActivity.this, MockResultActivity.class);
        intent.putStringArrayListExtra("audioPaths", audioPaths);
        
        ArrayList<String> answersList = new ArrayList<>();
        for (String ans : answers) answersList.add(ans);
        intent.putStringArrayListExtra("writingAnswers", answersList);
        
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Không thể thoát trong lúc thi.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        if (currentTimer != null) currentTimer.cancel();
        super.onDestroy();
    }
}
