package com.example.waviapp.activities;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.example.waviapp.R;
import com.example.waviapp.adapters.FlashcardAdapter;
import com.example.waviapp.models.TuVung;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Source;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class FlashcardActivity extends BaseActivity {

    private ViewPager2 viewPager;
    private TextView tvProgress;
    private Button btnShuffle;
    private ProgressBar progressBar;
    private List<TuVung> wordList;
    private FlashcardAdapter adapter;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard);

        // Khởi tạo TTS
        tts = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.ENGLISH);
            }
        });

        viewPager = findViewById(R.id.viewPager);
        tvProgress = findViewById(R.id.tvProgress);
        btnShuffle = findViewById(R.id.btnShuffle);
        progressBar = findViewById(R.id.progressBar);

        wordList = new ArrayList<>();
        adapter = new FlashcardAdapter(wordList, tts);
        viewPager.setAdapter(adapter);

        // Kiểm tra chế độ chạy: Theo chủ đề hay Theo từ yêu thích
        String mode = getIntent().getStringExtra("MODE");
        if ("FAVORITE".equals(mode)) {
            loadFavoriteData();
        } else {
            String maCD = getIntent().getStringExtra("MA_CD");
            String level = getIntent().getStringExtra("LEVEL");
            if (maCD != null && level != null) {
                loadDataFromCache(maCD, level);
            } else {
                Toast.makeText(this, "Không có dữ liệu bài học!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateProgress();
            }
        });

        btnShuffle.setOnClickListener(v -> shuffleCards());
        
        findViewById(R.id.ivBack).setOnClickListener(v -> finish());
    }

    private void loadDataFromCache(String maCD, String level) {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("TuVung")
                .whereEqualTo("maCD", maCD)
                .whereEqualTo("level", level)
                .limit(50)
                .get(Source.DEFAULT) // Đổi sang DEFAULT để đảm bảo có dữ liệu mới nhất
                .addOnSuccessListener(query -> {
                    wordList.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        TuVung w = doc.toObject(TuVung.class);
                        w.setMaTV(doc.getId());
                        wordList.add(w);
                    }
                    if (wordList.isEmpty()) {
                        Toast.makeText(this, "Chưa có từ vựng nào!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    adapter.notifyDataSetChanged();
                    updateProgress();
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Log.e("FLASHCARD_ERROR", "Load failed: " + e.getMessage());
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi tải dữ liệu!", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadFavoriteData() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("TuVung")
                .whereEqualTo("favorite", true)
                .get()
                .addOnSuccessListener(query -> {
                    wordList.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        TuVung w = doc.toObject(TuVung.class);
                        w.setMaTV(doc.getId());
                        wordList.add(w);
                    }
                    if (wordList.isEmpty()) {
                        Toast.makeText(this, "Danh sách yêu thích trống!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    adapter.notifyDataSetChanged();
                    updateProgress();
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi tải dữ liệu yêu thích!", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateProgress() {
        if (wordList.isEmpty()) return;
        int current = viewPager.getCurrentItem() + 1;
        int total = wordList.size();
        tvProgress.setText(current + " / " + total);
    }

    private void shuffleCards() {
        if (wordList.size() > 1) {
            Collections.shuffle(wordList);
            adapter.notifyDataSetChanged();
            viewPager.setCurrentItem(0, false);
            updateProgress();
            Toast.makeText(this, "Đã trộn thẻ!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
