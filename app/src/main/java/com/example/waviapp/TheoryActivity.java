package com.example.waviapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.waviapp.adapters.VocabularyAdapter;
import com.example.waviapp.adapters.GrammarAdapter; // Nhớ import Adapter mới
import com.example.waviapp.databinding.ActivityTheoryBinding;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class TheoryActivity extends AppCompatActivity {

    private ActivityTheoryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo Binding
        binding = ActivityTheoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Xử lý nút quay lại
        if (binding.ivBack != null) {
            binding.ivBack.setOnClickListener(v -> finish());
        }

        // 2. Thiết lập Spinner (Dropdown) - Gọi hàm để code gọn hơn
        setupSpinners();

        // 3. Mặc định khi vào màn hình sẽ hiện Từ vựng
        setupVocabularyRecyclerView();

        // 4. Xử lý sự kiện khi chọn Tab (Từ vựng / Ngữ pháp)
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    // Hiện từ vựng
                    setupVocabularyRecyclerView();
                    binding.spLevel.setVisibility(View.VISIBLE); // Hiện trình độ
                    // Hiện các nút Flashcard, Chọn từ... nếu cần
                } else {
                    // Hiện ngữ pháp
                    setupGrammarRecyclerView();
                    binding.spLevel.setVisibility(View.GONE); // Ẩn trình độ
                }
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupSpinners() {
        // --- XỬ LÝ DROPDOWN CHỌN BÀI (spLesson) ---
        String[] listLessons = {"Bài 1", "Bài 2", "Bài 3", "Bài 4", "Bài 5"};
        ArrayAdapter<String> adapterLesson = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, listLessons);
        adapterLesson.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spLesson.setAdapter(adapterLesson);

        // --- XỬ LÝ DROPDOWN CHỌN TRÌNH ĐỘ (spLevel) ---
        String[] listLevels = {"Cơ bản (0-250)", "Trung cấp (250-500)", "Nâng cao (500+)"};
        ArrayAdapter<String> adapterLevel = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, listLevels);
        adapterLevel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spLevel.setAdapter(adapterLevel);
    }

    // Hàm hiển thị danh sách Từ vựng
    private void setupVocabularyRecyclerView() {
        List<Word> demoList = new ArrayList<>();
        demoList.add(new Word("New", "/njuː/", "Mới (Adj)"));
        demoList.add(new Word("Company", "/ˈkʌmpəni/", "Công ty (N)"));
        demoList.add(new Word("Services", "/ˈsɜːrvɪsɪz/", "Dịch vụ (N)"));
        demoList.add(new Word("Please", "/pliːz/", "Vui lòng (V)"));

        VocabularyAdapter adapter = new VocabularyAdapter(demoList);
        binding.rvVocabulary.setLayoutManager(new LinearLayoutManager(this));
        binding.rvVocabulary.setAdapter(adapter);
    }

    // Hàm hiển thị danh sách Ngữ pháp (Dựa theo ảnh mẫu bạn gửi)
    private void setupGrammarRecyclerView() {
        List<Grammar> grammarList = new ArrayList<>();
        grammarList.add(new Grammar(1, "Cấu trúc chung của một câu trong tiếng anh"));
        grammarList.add(new Grammar(2, "Subject (chủ ngữ)"));
        grammarList.add(new Grammar(3, "Verb (động từ)"));
        grammarList.add(new Grammar(4, "Complement (vị ngữ)"));
        grammarList.add(new Grammar(5, "Modifier (trạng từ)"));
        grammarList.add(new Grammar(6, "Danh từ đếm được và không đếm được"));

        GrammarAdapter adapter = new GrammarAdapter(grammarList);
        binding.rvVocabulary.setLayoutManager(new LinearLayoutManager(this));
        binding.rvVocabulary.setAdapter(adapter);
    }
}