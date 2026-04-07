package com.example.waviapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waviapp.databinding.ActivityPremiumBinding;

import java.util.Arrays;
import java.util.List;

public class PremiumActivity extends BaseActivity {

    private ActivityPremiumBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPremiumBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Nút quay lại (Back)
        if (binding.ivBack != null) {
            binding.ivBack.setOnClickListener(v -> finish());
        }

        // Setup
        setupPricePackages();
        setupUserReviews();

        binding.tvRestore.setOnClickListener(v ->
                Toast.makeText(this, "Đang kiểm tra lịch sử thanh toán...", Toast.LENGTH_SHORT).show()
        );

        // Xử lý Bottom Navigation
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent intent = new Intent(PremiumActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            } else if (id == R.id.nav_exam) {
                Intent intent = new Intent(PremiumActivity.this, ExamActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            } else if (id == R.id.nav_premium) {
                // Already on Premium, không làm gì
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(PremiumActivity.this, UserInfoActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_setting) {
                Intent intent = new Intent(PremiumActivity.this, SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
            return true;
        });
    }

    // ================= PACKAGE =================
    private void setupPricePackages() {
        binding.rvPricePackages.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        List<String> packages = Arrays.asList("1 Tháng", "1 Năm", "Trọn đời");
        binding.rvPricePackages.setAdapter(new PackageAdapter(packages));
    }

    // ================= REVIEW =================
    private void setupUserReviews() {
        binding.rvReviews.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        List<String> reviews = Arrays.asList(
                "App dùng rất mượt, đề thi sát thực tế!",
                "Nhờ gói Premium mà mình hiểu rõ các bẫy Part 5.",
                "Tiện lợi khi học trên xe bus không cần mạng."
        );

        binding.rvReviews.setAdapter(new ReviewAdapter(reviews));
    }

    // ===== Package Adapter =====
    class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.ViewHolder> {

        List<String> data;

        PackageAdapter(List<String> data) { this.data = data; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            CardView cardView = new CardView(parent.getContext());
            cardView.setRadius(24f);
            cardView.setCardElevation(6f);

            RecyclerView.LayoutParams params =
                    new RecyclerView.LayoutParams(400, 500);
            params.setMargins(20, 20, 20, 20);
            cardView.setLayoutParams(params);

            TextView tv = new TextView(parent.getContext());
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(18);
            cardView.addView(tv);

            return new ViewHolder(cardView, tv);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.textView.setText(data.get(position));
            holder.itemView.setOnClickListener(v ->
                    Toast.makeText(PremiumActivity.this,
                            "Bạn chọn gói: " + data.get(position),
                            Toast.LENGTH_SHORT).show()
            );
        }

        @Override
        public int getItemCount() { return data.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            ViewHolder(View itemView, TextView tv) {
                super(itemView);
                textView = tv;
            }
        }
    }

    // ===== Review Adapter =====
    class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

        List<String> data;

        ReviewAdapter(List<String> data) { this.data = data; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            CardView cardView = new CardView(parent.getContext());
            cardView.setRadius(20f);

            RecyclerView.LayoutParams params =
                    new RecyclerView.LayoutParams(600, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(20, 20, 20, 20);
            cardView.setLayoutParams(params);

            TextView tv = new TextView(parent.getContext());
            tv.setPadding(32, 32, 32, 32);
            tv.setTextSize(16);
            cardView.addView(tv);

            return new ViewHolder(cardView, tv);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.textView.setText(data.get(position));
        }

        @Override
        public int getItemCount() { return data.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            ViewHolder(View itemView, TextView tv) {
                super(itemView);
                textView = tv;
            }
        }
    }
}