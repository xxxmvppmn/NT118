package com.example.waviapp.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.waviapp.R;
import com.example.waviapp.models.TuVung;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Source;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizActivity extends AppCompatActivity {

    private TextView tvQuestion, tvProgress;
    private ProgressBar progressBar;
    private MaterialButton btnOption1, btnOption2, btnOption3, btnOption4;
    private List<TuVung> wordList = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private String correctAnswer;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isAnswered = false;

    // Định nghĩa mã màu chuẩn
    private final String COLOR_DEFAULT_TEXT = "#9370DB"; 
    private final String COLOR_CORRECT = "#4CAF50"; 
    private final String COLOR_WRONG = "#F44336";   
    private final String COLOR_WHITE = "#FFFFFF";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // Ánh xạ View
        tvQuestion = findViewById(R.id.tvQuestion);
        tvProgress = findViewById(R.id.tvProgress);
        progressBar = findViewById(R.id.progressBar);
        btnOption1 = findViewById(R.id.btnOption1);
        btnOption2 = findViewById(R.id.btnOption2);
        btnOption3 = findViewById(R.id.btnOption3);
        btnOption4 = findViewById(R.id.btnOption4);

        // Nhận dữ liệu từ Intent - Sử dụng key in hoa theo yêu cầu audit
        String maCD = getIntent().getStringExtra("MA_CD");
        String level = getIntent().getStringExtra("LEVEL");

        if (maCD == null || level == null) {
            Toast.makeText(this, "Dữ liệu không hợp lệ!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadDataFromCache(maCD, level);

        // Sự kiện click cho các nút đáp án
        btnOption1.setOnClickListener(v -> checkAnswer(btnOption1.getText().toString(), btnOption1));
        btnOption2.setOnClickListener(v -> checkAnswer(btnOption2.getText().toString(), btnOption2));
        btnOption3.setOnClickListener(v -> checkAnswer(btnOption3.getText().toString(), btnOption3));
        btnOption4.setOnClickListener(v -> checkAnswer(btnOption4.getText().toString(), btnOption4));
    }

    private void loadDataFromCache(String maCD, String level) {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Ép buộc sử dụng Source.CACHE để tránh tốn Quota Firestore
        db.collection("TuVung")
                .whereEqualTo("maCD", maCD)
                .whereEqualTo("level", level)
                .get(Source.CACHE)
                .addOnSuccessListener(query -> {
                    wordList.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        TuVung w = doc.toObject(TuVung.class);
                        wordList.add(w);
                    }
                    progressBar.setVisibility(View.GONE);

                    if (wordList.size() < 4) {
                        Toast.makeText(this, "Không đủ dữ liệu trong Cache (cần tối thiểu 4 từ). Vui lòng vào mục học tập trước!", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }

                    Collections.shuffle(wordList);
                    progressBar.setMax(wordList.size());
                    loadNextQuestion();
                })
                .addOnFailureListener(e -> {
                    Log.e("QUIZ_ERROR", "Cache load failed: " + e.getMessage());
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Không tìm thấy dữ liệu trong Cache!", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void loadNextQuestion() {
        if (currentQuestionIndex >= wordList.size()) {
            Toast.makeText(this, "Chúc mừng! Bạn đã hoàn thành bài Quiz!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        TuVung currentWord = wordList.get(currentQuestionIndex);
        tvQuestion.setText("Từ này có nghĩa là gì: '" + currentWord.getTuTiengAnh() + "'?");
        correctAnswer = currentWord.getNghiaTiengViet();

        List<String> options = new ArrayList<>();
        options.add(correctAnswer);

        // Lấy 3 đáp án nhiễu ngẫu nhiên từ wordList
        List<TuVung> pool = new ArrayList<>(wordList);
        pool.remove(currentWord);
        Collections.shuffle(pool);
        
        for (int i = 0; i < 3 && i < pool.size(); i++) {
            options.add(pool.get(i).getNghiaTiengViet());
        }

        // Trộn các đáp án
        Collections.shuffle(options);

        btnOption1.setText(options.size() > 0 ? options.get(0) : "");
        btnOption2.setText(options.size() > 1 ? options.get(1) : "");
        btnOption3.setText(options.size() > 2 ? options.get(2) : "");
        btnOption4.setText(options.size() > 3 ? options.get(3) : "");

        resetButtons();
        updateProgress();
    }

    private void checkAnswer(String selected, MaterialButton button) {
        if (isAnswered) return;
        isAnswered = true;

        disableButtons();

        if (selected.equals(correctAnswer)) {
            button.setBackgroundColor(Color.parseColor(COLOR_CORRECT));
            button.setTextColor(Color.WHITE);
        } else {
            button.setBackgroundColor(Color.parseColor(COLOR_WRONG));
            button.setTextColor(Color.WHITE);
            highlightCorrectAnswer();
        }

        // Đợi 1.5 giây rồi qua câu tiếp theo theo yêu cầu
        handler.postDelayed(() -> {
            currentQuestionIndex++;
            isAnswered = false;
            loadNextQuestion();
        }, 1500);
    }

    private void highlightCorrectAnswer() {
        if (btnOption1.getText().toString().equals(correctAnswer)) {
            btnOption1.setBackgroundColor(Color.parseColor(COLOR_CORRECT));
            btnOption1.setTextColor(Color.WHITE);
        } else if (btnOption2.getText().toString().equals(correctAnswer)) {
            btnOption2.setBackgroundColor(Color.parseColor(COLOR_CORRECT));
            btnOption2.setTextColor(Color.WHITE);
        } else if (btnOption3.getText().toString().equals(correctAnswer)) {
            btnOption3.setBackgroundColor(Color.parseColor(COLOR_CORRECT));
            btnOption3.setTextColor(Color.WHITE);
        } else if (btnOption4.getText().toString().equals(correctAnswer)) {
            btnOption4.setBackgroundColor(Color.parseColor(COLOR_CORRECT));
            btnOption4.setTextColor(Color.WHITE);
        }
    }

    private void resetButtons() {
        // Reset về style mặc định: Nền trắng, chữ tím
        int whiteColor = Color.parseColor(COLOR_WHITE);
        int purpleColor = Color.parseColor(COLOR_DEFAULT_TEXT);
        
        btnOption1.setBackgroundColor(whiteColor);
        btnOption1.setTextColor(purpleColor);
        btnOption2.setBackgroundColor(whiteColor);
        btnOption2.setTextColor(purpleColor);
        btnOption3.setBackgroundColor(whiteColor);
        btnOption3.setTextColor(purpleColor);
        btnOption4.setBackgroundColor(whiteColor);
        btnOption4.setTextColor(purpleColor);
        
        enableButtons();
    }

    private void disableButtons() {
        btnOption1.setEnabled(false);
        btnOption2.setEnabled(false);
        btnOption3.setEnabled(false);
        btnOption4.setEnabled(false);
    }

    private void enableButtons() {
        btnOption1.setEnabled(true);
        btnOption2.setEnabled(true);
        btnOption3.setEnabled(true);
        btnOption4.setEnabled(true);
    }

    private void updateProgress() {
        int current = currentQuestionIndex + 1;
        int total = wordList.size();
        tvProgress.setText("Câu hỏi " + current + "/" + total);
        progressBar.setProgress(current);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
