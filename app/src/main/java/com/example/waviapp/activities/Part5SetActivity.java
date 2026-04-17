package com.example.waviapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.waviapp.R;
import com.example.waviapp.adapters.Part5SetAdapter;
import com.example.waviapp.utils.ProgressManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình chọn Set bài tập Part 5.
 * Chia làm 5 Set, mỗi Set 20 câu.
 */
public class Part5SetActivity extends BaseActivity {

    private RecyclerView rvSets;
    private Part5SetAdapter adapter;
    private ProgressManager progressManager;
    private TextView tvTotalXP;
    private ImageView ivBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_part5_sets);

        progressManager = new ProgressManager(this);
        initViews();
        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cập nhật lại XP và trạng thái Hoàn thành khi quay lại từ màn hình luyện tập
        if (tvTotalXP != null) {
            tvTotalXP.setText(progressManager.getTotalXP() + " XP");
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void initViews() {
        rvSets = findViewById(R.id.rvSets);
        tvTotalXP = findViewById(R.id.tvTotalXP);
        ivBack = findViewById(R.id.ivBack);

        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }
        
        if (tvTotalXP != null) {
            tvTotalXP.setText(progressManager.getTotalXP() + " XP");
        }
    }

    private void setupRecyclerView() {
        List<String> setNames = new ArrayList<>();
        // Định nghĩa 5 Set bài tập
        setNames.add("TOEIC Part 5 - Set 1");
        setNames.add("TOEIC Part 5 - Set 2");
        setNames.add("TOEIC Part 5 - Set 3");
        setNames.add("TOEIC Part 5 - Set 4");
        setNames.add("TOEIC Part 5 - Set 5");

        adapter = new Part5SetAdapter(setNames, progressManager, setIndex -> {
            Log.d("WaviDebug", "Chọn Set: " + setIndex);
            Intent intent = new Intent(Part5SetActivity.this, Part5PracticeActivity.class);
            // Truyền index để lọc câu hỏi (0, 1, 2, 3, 4)
            intent.putExtra("SET_INDEX", setIndex);
            startActivity(intent);
        });

        rvSets.setLayoutManager(new LinearLayoutManager(this));
        rvSets.setAdapter(adapter);
    }
}
