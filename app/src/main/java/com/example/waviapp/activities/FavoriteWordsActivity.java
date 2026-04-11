package com.example.waviapp.activities;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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

public class FavoriteWordsActivity extends AppCompatActivity {

    // Khai báo các View từ XML
    private RecyclerView rvFavoriteWords;
    private LinearLayout layoutStudyModes, layoutEmpty;
    private ImageView ivBack;
    private TextView tvTitle;

    // Khai báo Adapter và List
    private VocabularyAdapter adapter;
    private List<TuVung> favoriteList = new ArrayList<>();

    // Khai báo TextToSpeech (TTS)
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_words);

        // 1. Ánh xạ các View
        initViews();

        // 2. Cấu hình TextToSpeech (Cái loa)
        initTTS();

        // 3. Cấu hình RecyclerView và Adapter
        setupRecyclerView();

        // 4. Lấy dữ liệu từ Firestore (Thời gian thực)
        loadFavoriteWords();

        // 5. Xử lý các sự kiện nút bấm
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
        // Khởi tạo Adapter với Interface đã viết trong Prompt trước
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
        // Lắng nghe bảng TuVung, chỉ lấy những từ có favorite = true
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
                                word.setMaTV(doc.getId()); // Đảm bảo lấy đúng ID để update tim
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
        // Nút quay lại
        ivBack.setOnClickListener(v -> finish());

        // Thêm sự kiện cho 2 CardView (Flashcard và Test)
        findViewById(R.id.cardFlashcard).setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng Flashcard sắp ra mắt!", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.cardTest).setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng Kiểm tra sắp ra mắt!", Toast.LENGTH_SHORT).show();
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