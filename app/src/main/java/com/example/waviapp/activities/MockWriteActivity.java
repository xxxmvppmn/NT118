package com.example.waviapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.waviapp.R;
import com.example.waviapp.models.WriteQuestion;
import com.example.waviapp.utils.MockTestManager;

import java.util.ArrayList;
import java.util.List;

public class MockWriteActivity extends AppCompatActivity {

    private TextView tvPartTitle, tvQuestionCount, tvInstruction, tvPrompt, tvTimer;
    private TextView tvWordCount, tvKeyword1, tvKeyword2;
    private ImageView ivQuestionImage;
    private EditText etAnswer;
    private Button btnNext;
    private LinearLayout llKeywords;

    private List<WriteQuestion> questionList;
    private int currentQuestionIndex = 0;
    private CountDownTimer countDownTimer;
    private String testId;

    // Static list to pass data to Submit Activity
    public static List<String> mockWriteAnswers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mock_write);

        mockWriteAnswers.clear();
        testId = getIntent().getStringExtra("TEST_ID");

        initViews();
        // Lấy câu hỏi từ MockTestManager
        questionList = MockTestManager.getInstance().getWritingQuestions();

        btnNext.setOnClickListener(v -> moveToNextQuestion());

        etAnswer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int words = s.toString().trim().isEmpty() ? 0 : s.toString().trim().split("\\s+").length;
                tvWordCount.setText(String.valueOf(words));
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        startQuestionFlow();
    }

    private void initViews() {
        tvPartTitle = findViewById(R.id.tvPartTitle);
        tvQuestionCount = findViewById(R.id.tvQuestionCount);
        tvInstruction = findViewById(R.id.tvInstruction);
        tvPrompt = findViewById(R.id.tvPrompt);
        tvTimer = findViewById(R.id.tvTimer);
        tvWordCount = findViewById(R.id.tvWordCount);
        tvKeyword1 = findViewById(R.id.tvKeyword1);
        tvKeyword2 = findViewById(R.id.tvKeyword2);
        ivQuestionImage = findViewById(R.id.ivQuestionImage);
        etAnswer = findViewById(R.id.etAnswer);
        btnNext = findViewById(R.id.btnNext);
        llKeywords = findViewById(R.id.llKeywords);
    }

    private void startQuestionFlow() {
        if (currentQuestionIndex >= questionList.size()) {
            finishWritingTest();
            return;
        }

        WriteQuestion currentQuestion = questionList.get(currentQuestionIndex);

        tvPartTitle.setText("Part " + currentQuestion.getPartNumber());
        tvQuestionCount.setText((currentQuestionIndex + 1) + "/" + questionList.size());
        tvInstruction.setText(currentQuestion.getInstruction());
        etAnswer.setText("");

        // Hiển thị prompt (nếu có)
        String prompt = currentQuestion.getPrompt();
        if (prompt != null && !prompt.isEmpty()) {
            tvPrompt.setText(prompt);
            tvPrompt.setVisibility(View.VISIBLE);
        } else {
            tvPrompt.setVisibility(View.GONE);
        }

        // Hiển thị hình ảnh bằng Glide (ưu tiên imageUrl)
        loadQuestionImage(currentQuestion.getImageUrl(), currentQuestion.getImageResId());

        // Keywords
        if (currentQuestion.getKeyword1() != null && !currentQuestion.getKeyword1().isEmpty()) {
            llKeywords.setVisibility(View.VISIBLE);
            tvKeyword1.setText(currentQuestion.getKeyword1());
            tvKeyword2.setText(currentQuestion.getKeyword2());
        } else {
            llKeywords.setVisibility(View.GONE);
        }

        startTimer(currentQuestion.getPartNumber());
    }

    private void loadQuestionImage(String imageUrl, Integer imageResId) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            ivQuestionImage.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.bg_edittext)
                    .error(R.drawable.bg_edittext)
                    .centerCrop()
                    .into(ivQuestionImage);
        } else if (imageResId != null) {
            ivQuestionImage.setImageResource(imageResId);
            ivQuestionImage.setVisibility(View.VISIBLE);
        } else {
            ivQuestionImage.setVisibility(View.GONE);
        }
    }

    private void startTimer(int partNumber) {
        if (countDownTimer != null) countDownTimer.cancel();

        int timeMs;
        if (partNumber == 1) timeMs = 96000;      // 1.6 mins per question
        else if (partNumber == 2) timeMs = 600000; // 10 mins
        else timeMs = 1800000;                     // 30 mins

        countDownTimer = new CountDownTimer(timeMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000) % 60;
                int minutes = (int) ((millisUntilFinished / (1000 * 60)) % 60);
                tvTimer.setText(String.format("%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                moveToNextQuestion();
            }
        }.start();
    }

    private void moveToNextQuestion() {
        if (countDownTimer != null) countDownTimer.cancel();

        mockWriteAnswers.add(etAnswer.getText().toString().trim());
        currentQuestionIndex++;

        startQuestionFlow();
    }

    private void finishWritingTest() {
        Intent intent = new Intent(MockWriteActivity.this, MockSubmitActivity.class);
        intent.putExtra("TEST_ID", testId);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (countDownTimer != null) countDownTimer.cancel();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "You cannot go back during the test.", Toast.LENGTH_SHORT).show();
    }
}
