package com.example.waviapp.activities;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waviapp.R;
import com.example.waviapp.databinding.ActivityPremiumBinding;

import java.util.Arrays;
import java.util.List;

public class PremiumActivity extends BaseActivity {

    private ActivityPremiumBinding binding;

    // Model để quản lý dữ liệu
    static class PricePackage {
        String name;
        String oldPrice;
        String newPrice;
        String promo;

        PricePackage(String name, String oldPrice, String newPrice, String promo) {
            this.name = name;
            this.oldPrice = oldPrice;
            this.newPrice = newPrice;
            this.promo = promo;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPremiumBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (binding.ivBack != null) {
            binding.ivBack.setOnClickListener(v -> finish());
        }

        setupPricePackages();
        setupUserReviews();

        binding.tvRestore.setOnClickListener(v ->
                Toast.makeText(this, "Đang kiểm tra lịch sử thanh toán...", Toast.LENGTH_SHORT).show()
        );

        setupBottomNavigation();
    }

    private void setupPricePackages() {
        binding.rvPricePackages.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        List<PricePackage> packages = Arrays.asList(
                new PricePackage("1 Tháng", "150.000đ", "99.000đ", "TIẾT KIỆM"),
                new PricePackage("1 Năm", "600.000đ", "399.000đ", "50% OFF"),
                new PricePackage("Trọn đời", "1.500.000đ", "999.000đ", "HOT")
        );
        binding.rvPricePackages.setAdapter(new PackageAdapter(packages));
    }

    private void setupUserReviews() {
        binding.rvReviews.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        List<String> reviews = Arrays.asList(
                "App dùng rất mượt, đề thi sát thực tế!",
                "Ấn tượng đầu tiên về WaviApp là giao diện cực kỳ 'clean' và hiện đại.",
                "Lộ trình học tập cá nhân hóa rõ ràng. Rất đáng tiền!"
        );
        binding.rvReviews.setAdapter(new ReviewAdapter(reviews));
    }

    private void setupBottomNavigation() {
        binding.bottomNav.setSelectedItemId(R.id.nav_premium);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
            } else if (id == R.id.nav_exam) {
                startActivity(new Intent(this, ExamActivity.class));
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, UserInfoActivity.class));
            } else if (id == R.id.nav_setting) {
                startActivity(new Intent(this, SettingsActivity.class));
            }
            return true;
        });
    }

    // ===== Package Adapter (ĐÃ SỬA ĐỂ KHỚP XML) =====
    class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.ViewHolder> {
        List<PricePackage> data;

        PackageAdapter(List<PricePackage> data) { this.data = data; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // QUAN TRỌNG: Nạp file XML item_price_package vào đây
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_price_package, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            PricePackage item = data.get(position);
            holder.tvName.setText(item.name);
            holder.tvCurrent.setText(item.newPrice);
            holder.tvPromo.setText(item.promo);

            // Hiển thị giá gốc và gạch ngang
            holder.tvOld.setText(item.oldPrice);
            holder.tvOld.setPaintFlags(holder.tvOld.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            holder.itemView.setOnClickListener(v ->
                    Toast.makeText(PremiumActivity.this, "Bạn chọn gói: " + item.name, Toast.LENGTH_SHORT).show()
            );
        }

        @Override
        public int getItemCount() { return data.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvOld, tvCurrent, tvPromo;

            ViewHolder(View itemView) {
                super(itemView);
                // Ánh xạ đúng ID từ file XML của Ngân
                tvName = itemView.findViewById(R.id.tvPackageName);
                tvOld = itemView.findViewById(R.id.tvOriginalPrice);
                tvCurrent = itemView.findViewById(R.id.tvCurrentPrice);
                tvPromo = itemView.findViewById(R.id.tvPromoTag);
            }
        }
    }

    // ===== Review Adapter (ĐÃ SỬA ĐỂ GIAO DIỆN ĐẸP HƠN) =====
    class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {
        List<String> data;
        ReviewAdapter(List<String> data) { this.data = data; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Nếu Ngân có file xml cho review thì nạp vào, nếu chưa thì tạm dùng code này
            androidx.cardview.widget.CardView card = new androidx.cardview.widget.CardView(parent.getContext());
            card.setRadius(20f);
            card.setCardElevation(4f);
            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(600, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(16, 16, 16, 16);
            card.setLayoutParams(params);

            TextView tv = new TextView(parent.getContext());
            tv.setPadding(30, 30, 30, 30);
            card.addView(tv);
            return new ViewHolder(card, tv);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.textView.setText(data.get(position));
        }

        @Override
        public int getItemCount() { return data.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            ViewHolder(View v, TextView tv) { super(v); textView = tv; }
        }
    }
}

