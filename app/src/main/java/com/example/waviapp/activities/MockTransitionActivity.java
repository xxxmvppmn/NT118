package com.example.waviapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.waviapp.R;

public class MockTransitionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mock_transition);

        String testId = getIntent().getStringExtra("TEST_ID");

        Button btnStartWriting = findViewById(R.id.btnStartWriting);
        btnStartWriting.setOnClickListener(v -> {
            Intent intent = new Intent(MockTransitionActivity.this, MockWriteActivity.class);
            intent.putExtra("TEST_ID", testId);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        // Disable back
    }
}
