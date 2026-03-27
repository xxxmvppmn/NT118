package com.example.waviapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SkillPracticeActivity extends AppCompatActivity {

    public static final String EXTRA_SKILL_CATEGORY = "extra_skill_category";
    public static final String CAT_LISTEN = "Nghe";
    public static final String CAT_READ = "Đọc";
    public static final String CAT_SPEAK = "Nói";
    public static final String CAT_WRITE = "Viết";

    private LinearLayout llPartsContainer;
    private TextView tvToolbarTitle;
    private ImageView ivBackSkill;
    private ImageView ivHeaderIcon;
    private TextView tvHeaderStat1;
    private TextView tvHeaderStat2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skill_practice);

        tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        llPartsContainer = findViewById(R.id.llPartsContainer);
        ivBackSkill = findViewById(R.id.ivBackSkill);
        ivHeaderIcon = findViewById(R.id.ivHeaderIcon);
        tvHeaderStat1 = findViewById(R.id.tvHeaderStat1);
        tvHeaderStat2 = findViewById(R.id.tvHeaderStat2);

        ivBackSkill.setOnClickListener(v -> finish());

        String category = getIntent().getStringExtra(EXTRA_SKILL_CATEGORY);
        if (category == null) {
            category = CAT_SPEAK;
        }

        setupUI(category);
    }

    private void setupUI(String category) {
        String[] parts;
        int activeParts = 1;
        boolean hasDoubleStats = false;

        if (CAT_SPEAK.equals(category)) {
            tvToolbarTitle.setText("Luyện nói");
            ivHeaderIcon.setImageResource(R.drawable.ic_speak);
            parts = new String[]{"Phần 1 - Đọc văn bản", "Phần 2 - Mô tả tranh", "Phần 3 - Trả lời câu hỏi (1)", "Phần 4 - Trả lời câu hỏi (2)", "Phần 5 - Đề xuất giải pháp", "Phần 6 - Thể hiện quan điểm"};
            activeParts = 1;
        } else if (CAT_WRITE.equals(category)) {
            tvToolbarTitle.setText("Viết");
            ivHeaderIcon.setImageResource(R.drawable.ic_write);
            parts = new String[]{"Phần 1 - Mô tả tranh", "Phần 2 - Phản hồi yêu cầu", "Phần 3 - Viết luận"};
            activeParts = 1;
        } else if (CAT_LISTEN.equals(category)) {
            tvToolbarTitle.setText("Nghe Hiểu");
            ivHeaderIcon.setImageResource(R.drawable.ic_listen);
            parts = new String[]{"Phần 1 - Mô Tả Hình Ảnh", "Phần 2 - Hỏi & Đáp", "Phần 3 - Đoạn Hội Thoại", "Phần 4 - Bài Nói Chuyện Ngắn"};
            activeParts = 2;
            hasDoubleStats = true;
        } else { // CAT_READ
            tvToolbarTitle.setText("Đọc Hiểu");
            ivHeaderIcon.setImageResource(R.drawable.ic_read);
            parts = new String[]{"Phần 5 - Điền Vào Câu", "Phần 6 - Điền Vào Đoạn Văn", "Phần 7 - Đọc Hiểu Đoạn Văn"};
            activeParts = 1;
            hasDoubleStats = true;
        }

        if (hasDoubleStats) {
            tvHeaderStat2.setVisibility(View.VISIBLE);
        } else {
            tvHeaderStat2.setVisibility(View.GONE);
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < parts.length; i++) {
            View itemView = inflater.inflate(R.layout.item_skill_part, llPartsContainer, false);
            
            TextView tvPartTitle = itemView.findViewById(R.id.tvPartTitle);
            TextView tvPartStat = itemView.findViewById(R.id.tvPartStat);
            ImageView ivPartLock = itemView.findViewById(R.id.ivPartLock);

            tvPartTitle.setText(parts[i]);

            if (hasDoubleStats) {
                tvPartStat.setText("Trả lời đúng   0/0");
            } else {
                tvPartStat.setText("Số câu đã làm   0");
            }

            // Đã mở khóa tất cả các phần
            ivPartLock.setVisibility(View.GONE);
            int finalI = i;
            itemView.setOnClickListener(v -> Toast.makeText(this, "Bắt đầu làm " + parts[finalI], Toast.LENGTH_SHORT).show());

            llPartsContainer.addView(itemView);
        }
    }
}
