package com.example.waviapp;

import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

public class TheoryActivity extends BaseActivity {

    private ActivityTheoryBinding binding;
    private DatabaseHelper dbHelper;

    // Danh sách mã bài (maCD) dùng chung cho các Level
    private final String[] lessonKeys = {
            "bai_1", "bai_2", "bai_3", "bai_4", "bai_5",
            "bai_6", "bai_7", "bai_8", "bai_9", "bai_10",
            "bai_11", "bai_12", "bai_13", "bai_14", "bai_15",
            "bai_16", "bai_17", "bai_18", "bai_19", "bai_20"
    };

    private int currentLessonIndex = 0;
    private boolean isVocabularyTab = true;
    private List<TuVung> wordList = new ArrayList<>();
    private VocabularyAdapter vocabularyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sử dụng ViewBinding khớp với activity_theory.xml
        binding = ActivityTheoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new DatabaseHelper();

        // 1. Nút quay lại
        if (binding.ivBack != null) {
            binding.ivBack.setOnClickListener(v -> finish());
        }

        // 2. Thiết lập RecyclerView hiển thị từ vựng
        vocabularyAdapter = new VocabularyAdapter(wordList);
        binding.rvVocabulary.setLayoutManager(new LinearLayoutManager(this));
        binding.rvVocabulary.setAdapter(vocabularyAdapter);

        // 3. Khởi tạo các Spinner (Level & Lesson)
        setupSpinners();

        // 4. Mặc định load Bài 1 - Basic khi vừa vào
        loadVocabularyFromFirebase("bai_1", "Basic");

        // 5. Xử lý Tab chuyển đổi Từ vựng / Ngữ pháp
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
        // --- SPINNER CHỌN LEVEL ---
        String[] levels = {"Basic", "Intermediate", "Advanced"};
        ArrayAdapter<String> adapterLevel = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, levels);
        adapterLevel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spLevel.setAdapter(adapterLevel);

        binding.spLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Khi đổi Level -> Cập nhật lại số lượng Bài trong Spinner còn lại
                updateLessonSpinner(levels[position]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateLessonSpinner(String level) {
        List<String> listLessons = new ArrayList<>();
        int maxLessons = 10; // Ngữ pháp chỉ có 10 bài

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
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Lọc theo cả mã bài (maCD) và trình độ (level)
        db.collection("TuVung")
                .whereEqualTo("maCD", lessonKey)
                .whereEqualTo("level", level)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    wordList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        TuVung word = document.toObject(TuVung.class);
                        wordList.add(word);
                    }

                    if (wordList.isEmpty()) {
                        setupDefaultVocabulary();
                    } else {
                        vocabularyAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("THEORY_ERROR", "Lỗi: " + e.getMessage());
                    setupDefaultVocabulary();
                });
    }

    private void loadGrammarFromFirebase(String lessonKey) {
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
        wordList.add(new TuVung("tv_default", "bai_1", "Example", "Ví dụ", "Noun", "/ɪɡˈzæmpl/"));
        vocabularyAdapter.notifyDataSetChanged();
    }

    private void setupDefaultGrammar() {
        List<NguPhap> grammarList = new ArrayList<>();
        GrammarAdapter gAdapter = new GrammarAdapter(grammarList);
        binding.rvVocabulary.setAdapter(gAdapter);
    }
}