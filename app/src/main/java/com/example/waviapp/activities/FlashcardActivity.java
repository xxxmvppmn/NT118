package com.example.waviapp.activities;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
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

public class FlashcardActivity extends AppCompatActivity {

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

        // Get data from Intent
        String maCD = getIntent().getStringExtra("MA_CD");
        String level = getIntent().getStringExtra("LEVEL");

        if (maCD == null || level == null) {
            finish(); // No data, close
            return;
        }

        // Initialize TTS
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

        loadDataFromCache(maCD, level);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateProgress();
            }
        });

        btnShuffle.setOnClickListener(v -> shuffleCards());
    }

    private void loadDataFromCache(String maCD, String level) {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("TuVung")
                .whereEqualTo("maCD", maCD)
                .whereEqualTo("level", level)
                .limit(30)
                .get(Source.CACHE)
                .addOnSuccessListener(query -> {
                    wordList.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        TuVung w = doc.toObject(TuVung.class);
                        w.setMaTV(doc.getId());
                        wordList.add(w);
                    }
                    adapter.notifyDataSetChanged();
                    updateProgress();
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Log.e("FLASHCARD_ERROR", "Cache load failed: " + e.getMessage());
                    progressBar.setVisibility(View.GONE);
                    // Optionally, load from server or show error message
                });
    }

    private void updateProgress() {
        int current = viewPager.getCurrentItem() + 1;
        int total = wordList.size();
        tvProgress.setText(current + " / " + total);
    }

    private void shuffleCards() {
        Collections.shuffle(wordList);
        adapter.notifyDataSetChanged();
        viewPager.setCurrentItem(0, false);
        updateProgress();
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
