package com.example.waviapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.waviapp.R;
import com.example.waviapp.adapters.SkillSetAdapter;
import com.example.waviapp.utils.ProgressManager;
import java.util.Arrays;
import java.util.List;

/**
 * Màn hình chọn Part cho Speaking (6 parts).
 */
public class SpeakSetActivity extends BaseActivity {

    public static final String EXTRA_PART_INDEX = "extra_part_index";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speak_sets);

        ImageView ivBack = findViewById(R.id.ivBack);
        if (ivBack != null) ivBack.setOnClickListener(v -> finish());

        TextView tvTotalXP = findViewById(R.id.tvTotalXP);
        ProgressManager pm = new ProgressManager(this);
        if (tvTotalXP != null) tvTotalXP.setText(pm.getTotalXP() + " XP");

        RecyclerView rvSets = findViewById(R.id.rvSets);
        rvSets.setLayoutManager(new LinearLayoutManager(this));

        List<String> names = Arrays.asList(
            "Phần 1 - Đọc văn bản",
            "Phần 2 - Mô tả tranh",
            "Phần 3 - Trả lời câu hỏi (1)",
            "Phần 4 - Trả lời câu hỏi (2)",
            "Phần 5 - Đề xuất giải pháp",
            "Phần 6 - Thể hiện quan điểm"
        );
        List<String> descs = Arrays.asList(
            "Read a Text Aloud  •  45 giây",
            "Describe a Picture  •  30s chuẩn bị, 45s nói",
            "Respond to Questions  •  15s chuẩn bị, 30s trả lời",
            "Respond with Info Given  •  30s chuẩn bị, 30s trả lời",
            "Propose a Solution  •  20s chuẩn bị, 60s nói",
            "Express an Opinion  •  15s chuẩn bị, 60s nói"
        );

        SkillSetAdapter adapter = new SkillSetAdapter(names, descs, partIndex -> {
            Intent intent = new Intent(SpeakSetActivity.this, SpeakPracticeActivity.class);
            intent.putExtra(EXTRA_PART_INDEX, partIndex);
            startActivity(intent);
        });
        rvSets.setAdapter(adapter);
    }
}
