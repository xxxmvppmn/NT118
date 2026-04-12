package com.example.waviapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech; // Thêm import này
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.waviapp.adapters.VocabularyAdapter;
import com.example.waviapp.adapters.GrammarAdapter;
import com.example.waviapp.databinding.ActivityTheoryBinding;
import com.example.waviapp.firebase.DatabaseHelper;
import com.example.waviapp.models.NguPhap;
import com.example.waviapp.models.TuVung;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale; // Thêm import này

public class TheoryActivity extends BaseActivity {

    private ActivityTheoryBinding binding;
    private DatabaseHelper dbHelper;
    private TextToSpeech tts; // 1. Khai báo biến tts ở đây

    private final String[] lessonKeys = {
            "bai_1", "bai_2", "bai_3", "bai_4", "bai_5",
            "bai_6", "bai_7", "bai_8", "bai_9", "bai_10",
            "bai_11", "bai_12", "bai_13", "bai_14", "bai_15",
            "bai_16", "bai_17", "bai_18", "bai_19", "bai_20",
            "bai_21", "bai_22", "bai_23", "bai_24", "bai_25",
            "bai_26", "bai_27", "bai_28", "bai_29", "bai_30"
    };

    private int currentLessonIndex = 0;
    private boolean isVocabularyTab = true;
    private List<TuVung> wordList = new ArrayList<>();
    private VocabularyAdapter vocabularyAdapter;

    private String lastLoadedKey = ""; // Field to track the last loaded key
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTheoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new DatabaseHelper();

        // 2. Khởi tạo TextToSpeech
        tts = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.ENGLISH);
            }
        });

        if (binding.ivBack != null) {
            binding.ivBack.setOnClickListener(v -> finish());
        }

        // 3. Thiết lập Adapter với xử lý OnVocabularyClickListener
        vocabularyAdapter = new VocabularyAdapter(wordList, new VocabularyAdapter.OnVocabularyClickListener() {
            @Override
            public void onSpeakClick(String text) {
                // Sử dụng biến tts đã khởi tạo
                if (text != null && !text.isEmpty() && tts != null) {
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                }
            }
        });

        binding.rvVocabulary.setLayoutManager(new LinearLayoutManager(this));
        binding.rvVocabulary.setAdapter(vocabularyAdapter);

        setupSpinners();
        loadVocabularyFromFirebase("bai_1", "Basic");

        // Assume there's a button with id btnFlashcard in activity_theory.xml
        if (binding.btnFlashcard != null) {
            binding.btnFlashcard.setOnClickListener(v -> startFlashcardActivity());
        }

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String selectedLevel = binding.spLevel.getSelectedItem().toString();
                if (tab.getPosition() == 0) {
                    isVocabularyTab = true;
                    binding.spLevel.setVisibility(View.VISIBLE);
                    binding.rvVocabulary.setLayoutManager(new LinearLayoutManager(TheoryActivity.this));
                    binding.rvVocabulary.setAdapter(vocabularyAdapter);
                    updateLessonSpinner(selectedLevel);
                } else {
                    isVocabularyTab = false;
                    binding.spLevel.setVisibility(View.GONE);
                    updateLessonSpinner(selectedLevel);
                }
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupSpinners() {
        String[] levels = {"Basic", "Intermediate", "Advanced"};
        ArrayAdapter<String> adapterLevel = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, levels);
        adapterLevel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spLevel.setAdapter(adapterLevel);

        binding.spLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateLessonSpinner(levels[position]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateLessonSpinner(String level) {
        List<String> listLessons = new ArrayList<>();
        int maxLessons = 30;

        if (isVocabularyTab) {
            maxLessons = level.equals("Basic") ? 20 : 15;
        }

        for (int i = 1; i <= maxLessons; i++) {
            listLessons.add("Bài " + i);
        }

        ArrayAdapter<String> adapterLesson = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, listLessons);
        adapterLesson.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spLesson.setAdapter(adapterLesson);

        binding.spLesson.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentLessonIndex = position;
                String selectedLevel = binding.spLevel.getSelectedItem().toString();
                String maCD = lessonKeys[position];

                if (isVocabularyTab) {
                    loadVocabularyFromFirebase(maCD, selectedLevel);
                } else {
                    loadGrammarFromFirebase(maCD);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadVocabularyFromFirebase(String lessonKey, String level) {
        String key = lessonKey + "_" + level + "_vocab";
        if (key.equals(lastLoadedKey) || isLoading) return;
        lastLoadedKey = key;
        isLoading = true;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (binding.progressBar != null) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }

        db.collection("TuVung")
                .whereEqualTo("maCD", lessonKey)
                .whereEqualTo("level", level)
                .limit(30)
                .get() // Source.DEFAULT
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isDestroyed()) {
                        wordList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            TuVung word = document.toObject(TuVung.class);
                            word.setMaTV(document.getId());
                            wordList.add(word);
                        }

                        if (wordList.isEmpty()) {
                            setupDefaultVocabulary();
                        } else {
                            vocabularyAdapter.notifyDataSetChanged();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isDestroyed()) {
                        Log.e("THEORY_ERROR", "Lỗi: " + e.getMessage());
                        setupDefaultVocabulary();
                    }
                })
                .addOnCompleteListener(task -> {
                    isLoading = false;
                    if (binding.progressBar != null) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void loadGrammarFromFirebase(String lessonKey) {
        String key = lessonKey + "_grammar";
        if (key.equals(lastLoadedKey)) return;
        lastLoadedKey = key;

        dbHelper.getGrammar(lessonKey, new DatabaseHelper.GrammarCallback() {
            @Override
            public void onSuccess(List<NguPhap> grammarList) {
                if (grammarList.isEmpty()) {
                    setupDefaultGrammar();
                    return;
                }
                GrammarAdapter gAdapter = new GrammarAdapter(grammarList);
                binding.rvVocabulary.setLayoutManager(new LinearLayoutManager(TheoryActivity.this));
                binding.rvVocabulary.setAdapter(gAdapter);
            }

            @Override
            public void onFailure(String error) {
                setupDefaultGrammar();
            }
        });
    }

    private void setupDefaultVocabulary() {
        wordList.clear();
        wordList.add(new TuVung("tv_default", "bai_1", "Example", "Ví dụ", "Noun", "/ɪɡˈzæmpl/","Basic"));
        vocabularyAdapter.notifyDataSetChanged();
    }

    private void setupDefaultGrammar() {
        List<NguPhap> grammarList = new ArrayList<>();
        GrammarAdapter gAdapter = new GrammarAdapter(grammarList);
        binding.rvVocabulary.setAdapter(gAdapter);
    }

    private void startFlashcardActivity() {
        String selectedLevel = binding.spLevel.getSelectedItem().toString();
        String maCD = lessonKeys[currentLessonIndex];
        Intent intent = new Intent(this, FlashcardActivity.class);
        intent.putExtra("maCD", maCD);
        intent.putExtra("level", selectedLevel);
        startActivity(intent);
    }

    // 4. Giải phóng bộ nhớ TTS khi thoát Activity
    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
