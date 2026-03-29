package com.example.waviapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import com.example.waviapp.databinding.ActivityEventInfoBinding;

public class EventInfoActivity extends AppCompatActivity {
    private ActivityEventInfoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEventInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.ivBack.setOnClickListener(v -> finish());

        String p1 = "1. Bạn cần <b>đăng nhập</b> và <b>hoàn thành bài thi</b> trong thời gian diễn ra sự kiện. (hãy đăng ký nếu bạn chưa có tài khoản nhé)";
        binding.tvPara1.setText(HtmlCompat.fromHtml(p1, HtmlCompat.FROM_HTML_MODE_LEGACY));

        String p3 = "3. Mỗi người có <b>tối đa 2 lượt làm bài</b>, mỗi lượt làm bài được tạm dừng tối đa 1 lần.<br><br><b>Người dùng Premium sẽ có 3 lượt làm bài và được tạm dừng tối đa 2 lần.</b>";
        binding.tvPara3.setText(HtmlCompat.fromHtml(p3, HtmlCompat.FROM_HTML_MODE_LEGACY));
    }
}
