package com.example.waviapp;

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
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Arrays;
import java.util.List;

public class PremiumActivity extends AppCompatActivity {

    private ActivityPremiumBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPremiumBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        setupFeaturesSlider();
        setupPricePackages();
        setupUserReviews();

        binding.tvRestore.setOnClickListener(v ->
                Toast.makeText(this, "Đang kiểm tra lịch sử thanh toán...", Toast.LENGTH_SHORT).show()
        );
    }

    // ================= FEATURE SLIDER =================
    private void setupFeaturesSlider() {
        List<String> features = Arrays.asList(
                "Học TOEIC Offline mọi lúc mọi nơi",
                "Mở khóa lộ trình học chuyên sâu 7 Parts",
                "Giải thích đáp án chi tiết & Mẹo tránh bẫy"
        );

        FeatureAdapter adapter = new FeatureAdapter(features);
        binding.viewPagerFeatures.setAdapter(adapter);

        new TabLayoutMediator(binding.tabIndicator, binding.viewPagerFeatures,
                (tab, position) -> {}
        ).attach();
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

    // =========================================================
    // ====================== ADAPTERS =========================
    // =========================================================

    // ===== Feature Adapter =====
    class FeatureAdapter extends RecyclerView.Adapter<FeatureAdapter.ViewHolder> {

        List<String> data;

        FeatureAdapter(List<String> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            TextView tv = new TextView(parent.getContext());
            tv.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));

            tv.setBackgroundColor(Color.parseColor("#F3E5F5"));
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(18);
            tv.setPadding(40, 40, 40, 40);

            return new ViewHolder(tv);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.textView.setText(data.get(position));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            ViewHolder(View itemView) {
                super(itemView);
                textView = (TextView) itemView;
            }
        }
    }

    // ===== Package Adapter =====
    class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.ViewHolder> {

        List<String> data;

        PackageAdapter(List<String> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            CardView cardView = new CardView(parent.getContext());
            cardView.setRadius(24f);
            cardView.setCardElevation(6f);

            // ✅ FIX CHUẨN Ở ĐÂY
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
        public int getItemCount() {
            return data.size();
        }

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

        ReviewAdapter(List<String> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            CardView cardView = new CardView(parent.getContext());
            cardView.setRadius(20f);

            // ✅ FIX CHUẨN Ở ĐÂY
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
        public int getItemCount() {
            return data.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            ViewHolder(View itemView, TextView tv) {
                super(itemView);
                textView = tv;
            }
        }
    }
}