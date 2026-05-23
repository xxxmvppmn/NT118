package com.example.waviapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waviapp.R;
import com.example.waviapp.adapters.VocabularyAdapter;
import com.example.waviapp.models.TuVung;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FavoriteWordsActivity extends BaseActivity {

    private RecyclerView rvFavoriteWords;
    private LinearLayout layoutStudyModes, layoutEmpty;
    private ImageView ivBack;
    private TextView tvTitle;

    private VocabularyAdapter adapter;
    private List<TuVung> favoriteList = new ArrayList<>();
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_words);

        initViews();
        initTTS();
        setupRecyclerView();
        loadFavoriteWords();
        setupClickListeners();
    }

    private void initViews() {
        rvFavoriteWords = findViewById(R.id.rvFavoriteWords);
        layoutStudyModes = findViewById(R.id.layoutStudyModes);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        ivBack = findViewById(R.id.ivBack);
        tvTitle = findViewById(R.id.tvTitle);
    }

    private void initTTS() {
        tts = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                int result = tts.setLanguage(Locale.ENGLISH);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "Ngôn ngữ không hỗ trợ!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new VocabularyAdapter(favoriteList, new VocabularyAdapter.OnVocabularyClickListener() {
            @Override
            public void onSpeakClick(String text) {
                if (tts != null) {
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                }
            }
        });

        rvFavoriteWords.setLayoutManager(new LinearLayoutManager(this));
        rvFavoriteWords.setAdapter(adapter);
    }

    private void loadFavoriteWords() {
        FirebaseFirestore.getInstance().collection("TuVung")
                .whereEqualTo("favorite", true)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Lỗi tải dữ liệu!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    favoriteList.clear();
                    if (value != null) {
                        for (DocumentSnapshot doc : value) {
                            TuVung word = doc.toObject(TuVung.class);
                            if (word != null) {
                                word.setMaTV(doc.getId());
                                favoriteList.add(word);
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();
                    updateUIState();
                });
    }

    private void updateUIState() {
        if (favoriteList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvFavoriteWords.setVisibility(View.GONE);
            layoutStudyModes.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvFavoriteWords.setVisibility(View.VISIBLE);
            layoutStudyModes.setVisibility(View.VISIBLE);
        }
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> finish());

        // Chức năng Flashcard cho sổ tay
        findViewById(R.id.cardFlashcard).setOnClickListener(v -> {
            if (favoriteList.isEmpty()) {
                Toast.makeText(this, "Sổ tay của bạn đang trống!", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, FlashcardActivity.class);
            intent.putExtra("MODE", "FAVORITE");
            startActivity(intent);
        });

        // Chức năng Kiểm tra cho sổ tay
        findViewById(R.id.cardTest).setOnClickListener(v -> {
            if (favoriteList.size() < 4) {
                Toast.makeText(this, "Cần tối thiểu 4 từ trong sổ tay để bắt đầu kiểm tra!", Toast.LENGTH_LONG).show();
                return;
            }
            Intent intent = new Intent(this, QuizActivity.class);
            intent.putExtra("MODE", "FAVORITE");
            startActivity(intent);
        });
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
