package com.example.waviapp;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class GrammarDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grammar_detail);

        ImageView ivBack = findViewById(R.id.ivBack);
        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        TextView tvContent = findViewById(R.id.tvDetailContent);
        // Lấy dữ liệu từ Intent gửi qua
        String title = getIntent().getStringExtra("grammar_title");
        String content = getIntent().getStringExtra("grammar_content");
        if (title != null && !title.isEmpty()) {
            tvTitle.setText(title);
            tvTitle.setText("Nội dung học");
            // Đổ dữ liệu vào giao diện
            if (title != null) tvTitle.setText(title);
            if (content != null) tvContent.setText(content);

            // Nút quay lại
            ivBack.setOnClickListener(v -> finish());
        }
    }
}