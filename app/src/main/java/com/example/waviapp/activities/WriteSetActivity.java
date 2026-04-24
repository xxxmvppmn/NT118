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
 * Màn hình chọn Part cho Writing (3 parts).
 */
public class WriteSetActivity extends BaseActivity {

    public static final String EXTRA_PART_INDEX = "extra_part_index";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_sets);

        ImageView ivBack = findViewById(R.id.ivBack);
        if (ivBack != null) ivBack.setOnClickListener(v -> finish());

        TextView tvTotalXP = findViewById(R.id.tvTotalXP);
        ProgressManager pm = new ProgressManager(this);
        if (tvTotalXP != null) tvTotalXP.setText(pm.getTotalXP() + " XP");

        RecyclerView rvSets = findViewById(R.id.rvSets);
        rvSets.setLayoutManager(new LinearLayoutManager(this));

        List<String> names = Arrays.asList(
            "Phần 1 - Mô tả tranh",
            "Phần 2 - Phản hồi yêu cầu",
            "Phần 3 - Viết luận"
        );
        List<String> descs = Arrays.asList(
            "Write a Sentence  •  1 câu dùng 2 từ gợi ý",
            "Respond to a Written Request  •  50–150 từ",
            "Write an Essay  •  150–300 từ"
        );

        SkillSetAdapter adapter = new SkillSetAdapter(names, descs, partIndex -> {
            Intent intent = new Intent(WriteSetActivity.this, WritePracticeActivity.class);
            intent.putExtra(EXTRA_PART_INDEX, partIndex);
            startActivity(intent);
        });
        rvSets.setAdapter(adapter);
    }
}
