package com.example.waviapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.waviapp.R;

public class MockResultActivity extends AppCompatActivity {

    private String testId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mock_result);

        testId = getIntent().getStringExtra("TEST_ID");

        Button btnReview = findViewById(R.id.btnReview);
        Button btnGoHome = findViewById(R.id.btnGoHome);

        btnReview.setOnClickListener(v -> {
            Intent intent = new Intent(MockResultActivity.this, MockReviewActivity.class);
            if (testId != null) {
                intent.putExtra("TEST_ID", testId);
            }
            startActivity(intent);
            finish();
        });

        btnGoHome.setOnClickListener(v -> {
            Intent intent = new Intent(MockResultActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
    
    @Override
    public void onBackPressed() {
        // disable back
    }
}
