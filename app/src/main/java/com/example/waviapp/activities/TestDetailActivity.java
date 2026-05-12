package com.example.waviapp.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.graphics.Typeface;
import android.content.Intent;

import com.example.waviapp.R;

public class TestDetailActivity extends BaseActivity {

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

        String timeLabel = getString(R.string.test_detail_time);
        String timeStr = timeLabel + time + getString(R.string.auto_minute);
        SpannableString timeSpannable = new SpannableString(timeStr);
        timeSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, timeLabel.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvTime.setText(timeSpannable);

        String qLabel = getString(R.string.test_detail_questions);
        String questionStr = qLabel + questions;
        SpannableString qSpannable = new SpannableString(questionStr);
        qSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, qLabel.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvQuestions.setText(qSpannable);

        btnStart.setOnClickListener(v -> {
            String lowerTitle = (title != null) ? title.toLowerCase() : "";
            
            // Logic nhận diện bài thi
            if (questions == 19 || lowerTitle.contains("speaking") || lowerTitle.contains("writing")) {
                Intent intent = new Intent(TestDetailActivity.this, MockIntroActivity.class);
                intent.putExtra("TEST_ID", title);
                startActivity(intent);
                finish();
            } 
            // Nếu là Test 1 của phần Fulltest
            else if (lowerTitle.contains("test 1") && (questions == 200 || questions == 100)) {
                Intent intent = new Intent(TestDetailActivity.this, EtsTestActivity.class);
                intent.putExtra(EtsTestActivity.EXTRA_ETS_ID, "ets01");
                startActivity(intent);
                finish();
            } 
            // Nếu là Test 2 của phần Fulltest
            else if (lowerTitle.contains("test 2") && (questions == 200 || questions == 100)) {
                Intent intent = new Intent(TestDetailActivity.this, EtsTestActivity.class);
                intent.putExtra(EtsTestActivity.EXTRA_ETS_ID, "ets02");
                startActivity(intent);
                finish();
            }
            else {
                Toast.makeText(this, "Bài thi " + title + " đang được cập nhật dữ liệu...", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
