package com.example.waviapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;

import com.example.waviapp.R;

public class GrammarDetailActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grammar_detail);

        ImageView ivBack = findViewById(R.id.ivBack);
        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        TextView tvContent = findViewById(R.id.tvDetailContent);
        TextView tvExample = findViewById(R.id.tvExample);
        CardView cvExample = findViewById(R.id.cvExample);

        // Lấy dữ liệu từ Intent gửi qua
        String title = getIntent().getStringExtra("grammar_title");
        String content = getIntent().getStringExtra("grammar_content");
        String example = getIntent().getStringExtra("grammar_example");

        // Nút quay lại
        ivBack.setOnClickListener(v -> finish());

        // Đổ dữ liệu vào giao diện
        if (title != null && !title.isEmpty()) {
            tvTitle.setText(title);
        } else {
            tvTitle.setText("Chi tiết ngữ pháp");
        }

        if (content != null && !content.isEmpty()) {
            // Thay thế \n bằng ký tự xuống dòng thực sự
            content = content.replace("\\n", "\n");
            tvContent.setText(content);
        } else {
            tvContent.setText("Nội dung đang được cập nhật...");
        }

        if (example != null && !example.isEmpty()) {
            example = example.replace("\\n", "\n");
            tvExample.setText(example);
            cvExample.setVisibility(View.VISIBLE);
        } else {
            cvExample.setVisibility(View.GONE);
        }
    }
}

