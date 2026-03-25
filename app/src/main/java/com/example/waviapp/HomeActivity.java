package com.example.waviapp;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.waviapp.databinding.ActivityHomeBinding;
import com.google.android.material.navigation.NavigationBarView;
import android.view.MenuItem;


public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Xử lý icon thông báo
        binding.icNotification.setOnClickListener(v ->
                Toast.makeText(this, "Bạn chưa có thông báo mới", Toast.LENGTH_SHORT).show()
        );

        // Xử lý Bottom Navigation
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // Đã ở trang chủ rồi
            } else if (id == R.id.nav_exam) {
                Toast.makeText(this, "Chuyển sang màn hình Thi", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_pencil) {
                Toast.makeText(this, "Luyện tập hàng ngày", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_profile) {
                Toast.makeText(this, "Hồ sơ cá nhân", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_setting) {
                Toast.makeText(this, "Cài đặt ứng dụng", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }
}