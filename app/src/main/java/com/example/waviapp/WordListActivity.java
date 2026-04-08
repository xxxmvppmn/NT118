package com.example.waviapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.waviapp.adapters.VocabularyAdapter;
import com.example.waviapp.models.TuVung;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class WordListActivity extends AppCompatActivity {

    private RecyclerView rvWordList;
    private VocabularyAdapter adapter;
    private List<TuVung> wordList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_list);

        // 1. Ánh xạ cái RecyclerView từ file XML siêu sạch lúc nãy
        rvWordList = findViewById(R.id.rvWordList);

        // 2. Cấu hình danh sách (Hiển thị dạng danh sách dọc)
        wordList = new ArrayList<>();
        adapter = new VocabularyAdapter(wordList);
        rvWordList.setLayoutManager(new LinearLayoutManager(this));
        rvWordList.setAdapter(adapter);

        // 3. Lấy mã bài từ Intent (Mặc định là bài 1 nếu chưa truyền)
        String maCD = getIntent().getStringExtra("maCD");
        if (maCD == null) maCD = "bai_1";

        // 4. Bắt đầu lấy 15 từ về
        loadDataFromFirestore(maCD);
    }

    private void loadDataFromFirestore(String maCD) {
        db = FirebaseFirestore.getInstance();

        // Truy vấn: Tìm trong Collection 'TuVung' những từ có 'maCD' khớp
        db.collection("TuVung")
                .whereEqualTo("maCD", maCD)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    wordList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Chuyển dữ liệu từ Firebase thành object TuVung
                        TuVung word = document.toObject(TuVung.class);
                        wordList.add(word);
                    }

                    // Báo cho Adapter biết dữ liệu đã về, hãy vẽ lên màn hình đi!
                    adapter.notifyDataSetChanged();

                    if (wordList.isEmpty()) {
                        Toast.makeText(this, "Không tìm thấy từ vựng cho " + maCD, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FIREBASE_ERROR", "Lỗi: " + e.getMessage());
                    Toast.makeText(this, "Lỗi kết nối dữ liệu!", Toast.LENGTH_SHORT).show();
                });
    }
}