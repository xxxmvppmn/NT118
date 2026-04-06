package com.example.waviapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.waviapp.adapters.VocabularyAdapter;
import com.example.waviapp.adapters.GrammarAdapter;
import com.example.waviapp.databinding.ActivityTheoryBinding;
import com.example.waviapp.firebase.DatabaseHelper;
import com.example.waviapp.models.NguPhap;
import com.example.waviapp.models.TuVung;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class TheoryActivity extends AppCompatActivity {

    private ActivityTheoryBinding binding;
    private DatabaseHelper dbHelper;

    // Map spinner index → lesson key trên Firebase
    private final String[] lessonKeys = {"bai_1", "bai_2", "bai_3", "bai_4", "bai_5"};
    private int currentLessonIndex = 0;
    private boolean isVocabularyTab = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTheoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new DatabaseHelper();

        // 1. Xử lý nút quay lại
        if (binding.ivBack != null) {
            binding.ivBack.setOnClickListener(v -> finish());
        }

        // 2. Thiết lập Spinner (Dropdown)
        setupSpinners();

        // 3. Mặc định khi vào màn hình sẽ hiện Từ vựng từ Firebase
        loadVocabularyFromFirebase(lessonKeys[0]);

        // 4. Xử lý sự kiện khi chọn Tab (Từ vựng / Ngữ pháp)
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    isVocabularyTab = true;
                    loadVocabularyFromFirebase(lessonKeys[currentLessonIndex]);
                    binding.spLevel.setVisibility(View.VISIBLE);
                } else {
                    isVocabularyTab = false;
                    loadGrammarFromFirebase(lessonKeys[currentLessonIndex]);
                    binding.spLevel.setVisibility(View.GONE);
                }
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupSpinners() {
        // --- DROPDOWN CHỌN BÀI (spLesson) ---
        String[] listLessons = {"Bài 1", "Bài 2", "Bài 3", "Bài 4", "Bài 5"};
        ArrayAdapter<String> adapterLesson = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, listLessons);
        adapterLesson.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spLesson.setAdapter(adapterLesson);

        // Lắng nghe khi thay đổi bài
        binding.spLesson.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                currentLessonIndex = position;
                if (isVocabularyTab) {
                    loadVocabularyFromFirebase(lessonKeys[position]);
                } else {
                    loadGrammarFromFirebase(lessonKeys[position]);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // --- DROPDOWN CHỌN TRÌNH ĐỘ (spLevel) ---
        String[] listLevels = {"Cơ bản (0-250)", "Trung cấp (250-500)", "Nâng cao (500+)"};
        ArrayAdapter<String> adapterLevel = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, listLevels);
        adapterLevel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spLevel.setAdapter(adapterLevel);
    }

    /**
     * Load từ vựng từ Firebase
     */
    private void loadVocabularyFromFirebase(String lessonKey) {
        dbHelper.getVocabulary(lessonKey, new DatabaseHelper.VocabularyCallback() {
            @Override
            public void onSuccess(List<TuVung> words) {
                if (words.isEmpty()) {
                    // Nếu Firebase chưa có dữ liệu, dùng dữ liệu mặc định
                    setupDefaultVocabulary();
                    return;
                }
                VocabularyAdapter adapter = new VocabularyAdapter(words);
                binding.rvVocabulary.setLayoutManager(new LinearLayoutManager(TheoryActivity.this));
                binding.rvVocabulary.setAdapter(adapter);
            }

            @Override
            public void onFailure(String error) {
                // Fallback: hiện dữ liệu mặc định
                setupDefaultVocabulary();
            }
        });
    }

    /**
     * Load ngữ pháp từ Firebase
     */
    private void loadGrammarFromFirebase(String lessonKey) {
        dbHelper.getGrammar(lessonKey, new DatabaseHelper.GrammarCallback() {
            @Override
            public void onSuccess(List<NguPhap> grammarList) {
                if (grammarList.isEmpty()) {
                    setupDefaultGrammar();
                    return;
                }
                GrammarAdapter adapter = new GrammarAdapter(grammarList);
                binding.rvVocabulary.setLayoutManager(new LinearLayoutManager(TheoryActivity.this));
                binding.rvVocabulary.setAdapter(adapter);
            }

            @Override
            public void onFailure(String error) {
                setupDefaultGrammar();
            }
        });
    }

    // Fallback data khi Firebase chưa có dữ liệu
    private void setupDefaultVocabulary() {
        List<TuVung> demoList = new ArrayList<>();
        demoList.add(new TuVung("tv1", "bai1", "New", "Mới (Adj)", "Adj", "/njuː/"));
        demoList.add(new TuVung("tv1", "bai1", "Company", "Công ty (N)", "N", "/ˈkʌmpəni/"));
        demoList.add(new TuVung("tv1", "bai1", "Services", "Dịch vụ (N)", "N", "/ˈsɜːrvɪsɪz/"));
        demoList.add(new TuVung("tv1", "bai1", "Please", "Vui lòng (V)", "V", "/pliːz/"));

        VocabularyAdapter adapter = new VocabularyAdapter(demoList);
        binding.rvVocabulary.setLayoutManager(new LinearLayoutManager(this));
        binding.rvVocabulary.setAdapter(adapter);
    }

    private void setupDefaultGrammar() {
        List<NguPhap> grammarList = new ArrayList<>();
        grammarList.add(new NguPhap("np1", "bai1", "Cấu trúc chung của một câu trong tiếng anh", "", "", 1));
        grammarList.add(new NguPhap("np1", "bai1", "Subject (chủ ngữ)", "", "", 2));
        grammarList.add(new NguPhap("np1", "bai1", "Verb (động từ)", "", "", 3));
        grammarList.add(new NguPhap("np1", "bai1", "Complement (vị ngữ)", "", "", 4));
        grammarList.add(new NguPhap("np1", "bai1", "Modifier (trạng từ)", "", "", 5));
        grammarList.add(new NguPhap("np1", "bai1", "Danh từ đếm được và không đếm được", "", "", 6));

        GrammarAdapter adapter = new GrammarAdapter(grammarList);
        binding.rvVocabulary.setLayoutManager(new LinearLayoutManager(this));
        binding.rvVocabulary.setAdapter(adapter);
    }
}