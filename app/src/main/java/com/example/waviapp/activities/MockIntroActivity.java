package com.example.waviapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.waviapp.R;
import com.example.waviapp.utils.MockTestManager;
import com.example.waviapp.utils.MockTestSeeder;

public class MockIntroActivity extends AppCompatActivity {

    private String testId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mock_intro);

        testId = getIntent().getStringExtra("TEST_ID");
        if (testId == null) testId = "Test 1"; // Fallback

        Button btnContinue = findViewById(R.id.btnContinue);
        btnContinue.setEnabled(false); // Disabled cho tới khi load xong

        // Load dữ liệu từ Firestore
        MockTestManager.getInstance().loadTest(testId, new MockTestManager.LoadCallback() {
            @Override
            public void onSuccess() {
                btnContinue.setEnabled(true);
                btnContinue.setText("Continue to Mic Check");
            }

            @Override
            public void onFailure(String error) {
                btnContinue.setEnabled(true);
                btnContinue.setText("Continue (Offline Mode)");
                Toast.makeText(MockIntroActivity.this, "Lỗi tải đề: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(MockIntroActivity.this, MockMicCheckActivity.class);
            intent.putExtra("TEST_ID", testId);
            startActivity(intent);
            finish();
        });

        // ============================================================
        // NÚT ẨN: Bấm giữ tiêu đề 3 giây để seed dữ liệu mẫu lên Firestore.
        // Chỉ cần bấm 1 lần duy nhất. Sau đó có thể xóa đoạn code này.
        // ============================================================
        TextView tvTitle = findViewById(R.id.tvTestTitle);
        if (tvTitle != null) {
            tvTitle.setOnLongClickListener(v -> {
                Toast.makeText(this, "Đang seed dữ liệu lên Firestore...", Toast.LENGTH_SHORT).show();
                MockTestSeeder.seedAllTests(new MockTestSeeder.SeedCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(MockIntroActivity.this, "✅ Seed thành công!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(MockIntroActivity.this, "❌ Seed thất bại: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
                return true;
            });
        }
    }
}
