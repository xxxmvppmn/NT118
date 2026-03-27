package com.example.waviapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.graphics.Typeface;
import androidx.appcompat.app.AppCompatActivity;

public class TestDetailActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_TIME = "extra_time";
    public static final String EXTRA_QUESTIONS = "extra_questions";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_detail);

        ImageView ivBack = findViewById(R.id.ivBackDetail);
        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        TextView tvTime = findViewById(R.id.tvDetailTime);
        TextView tvQuestions = findViewById(R.id.tvDetailQuestions);
        Button btnStart = findViewById(R.id.btnStartNow);

        ivBack.setOnClickListener(v -> finish());

        String title = getIntent().getStringExtra(EXTRA_TITLE);
        int time = getIntent().getIntExtra(EXTRA_TIME, 60);
        int questions = getIntent().getIntExtra(EXTRA_QUESTIONS, 100);

        if (title != null) {
            tvTitle.setText(title);
        }

        // Apply partial bold text like the mockup "Thời gian: 60 phút"
        String timeStr = "Thời gian: " + time + " phút";
        SpannableString timeSpannable = new SpannableString(timeStr);
        timeSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, 10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvTime.setText(timeSpannable);

        String questionStr = "Câu hỏi: " + questions;
        SpannableString qSpannable = new SpannableString(questionStr);
        qSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvQuestions.setText(qSpannable);

        btnStart.setOnClickListener(v -> {
            Toast.makeText(this, "Bài thi đang bắt đầu...", Toast.LENGTH_SHORT).show();
            // Implement test start logic here
        });
    }
}
