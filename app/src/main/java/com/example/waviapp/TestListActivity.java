package com.example.waviapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class TestListActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY = "extra_category";
    public static final String CAT_FULLTEST = "TOEIC Listening & Reading Fulltest";
    public static final String CAT_MINITEST = "TOEIC Listening & Reading Minitest";
    public static final String CAT_SPEAKING = "TOEIC Speaking & Writing";

    private LinearLayout llTestListContainer;
    private TextView tvToolbarTitle;
    private ImageView ivBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_list);

        llTestListContainer = findViewById(R.id.llTestListContainer);
        tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        ivBack = findViewById(R.id.ivBack);

        ivBack.setOnClickListener(v -> finish());

        String category = getIntent().getStringExtra(EXTRA_CATEGORY);
        if (category == null) {
            category = CAT_MINITEST;
        }

        tvToolbarTitle.setText(category);
        setupTestData(category);
    }

    private void setupTestData(String category) {
        LayoutInflater inflater = LayoutInflater.from(this);
        
        int time = 60;
        int questions = 100;
        int startId = 1;
        boolean reverseOffset = false;
        String nameSuffix = "";
        
        if (CAT_FULLTEST.equals(category)) {
            time = 120;
            questions = 200;
            startId = 10;
            reverseOffset = true;
            nameSuffix = " ETS 2023";
        } else if (CAT_SPEAKING.equals(category)) {
            time = 70;
            questions = 16;
        }

        int limit = 10;
        
        for (int i = 0; i < limit; i++) {
            int currentId = reverseOffset ? (startId - i) : (startId + i);
            
            View itemView = inflater.inflate(R.layout.item_test_list, llTestListContainer, false);
            
            TextView tvListTitle = itemView.findViewById(R.id.tvListTitle);
            TextView tvTimeValue = itemView.findViewById(R.id.tvTimeValue);
            TextView tvQuestionValue = itemView.findViewById(R.id.tvQuestionValue);
            TextView tvTimeLabel = itemView.findViewById(R.id.tvTimeLabel);
            TextView tvQuestionLabel = itemView.findViewById(R.id.tvQuestionLabel);
            Button btnStart = itemView.findViewById(R.id.btnStart);
            ImageView ivListLock = itemView.findViewById(R.id.ivListLock);
            
            tvListTitle.setText("Test " + currentId + nameSuffix);
            tvTimeValue.setText(time + " phút  |  ");
            tvQuestionValue.setText(String.valueOf(questions));

            // Mở khóa tất cả bài test
            boolean isLocked = false;
            
            if (isLocked) {
                ivListLock.setVisibility(View.VISIBLE);
                btnStart.setBackgroundResource(R.drawable.bg_test_btn_disabled);
                
                int grayColor = Color.parseColor("#9E9E9E");
                tvListTitle.setTextColor(grayColor);
                tvTimeLabel.setTextColor(grayColor);
                tvTimeValue.setTextColor(grayColor);
                tvQuestionLabel.setTextColor(grayColor);
                tvQuestionValue.setTextColor(grayColor);
            } else {
                final String title = "Test " + currentId + nameSuffix;
                final int fTime = time;
                final int fQuestions = questions;
                itemView.setOnClickListener(v -> openTestDetail(title, fTime, fQuestions));
                btnStart.setOnClickListener(v -> openTestDetail(title, fTime, fQuestions));
            }
            
            llTestListContainer.addView(itemView);
        }
    }

    private void openTestDetail(String title, int time, int questions) {
        Intent intent = new Intent(TestListActivity.this, TestDetailActivity.class);
        intent.putExtra(TestDetailActivity.EXTRA_TITLE, title);
        intent.putExtra(TestDetailActivity.EXTRA_TIME, time);
        intent.putExtra(TestDetailActivity.EXTRA_QUESTIONS, questions);
        startActivity(intent);
    }
}
