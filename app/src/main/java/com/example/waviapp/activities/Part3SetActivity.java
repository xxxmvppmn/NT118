package com.example.waviapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waviapp.R;
import com.example.waviapp.utils.ProgressManager;

import java.util.ArrayList;
import java.util.List;

public class Part3SetActivity extends BaseActivity {

    private RecyclerView rvSets;
    private ProgressManager progressManager;
    private TextView tvTotalXP;
    private int partType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_part3_sets);

        partType = getIntent().getIntExtra("PART_TYPE", 3);

        progressManager = new ProgressManager(this);
        tvTotalXP = findViewById(R.id.tvTotalXP);
        rvSets = findViewById(R.id.rvSets);

        TextView tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        if (tvToolbarTitle != null) {
            tvToolbarTitle.setText("Listening Part " + partType + " Sets");
        }

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        tvTotalXP.setText(progressManager.getTotalXP() + " XP");
    }

    private void setupRecyclerView() {
        List<String> setNames = new ArrayList<>();
        String prefix = "Listening Part " + partType + " - Set ";
        
        // Cấu trúc 4 đoạn/set, riêng set cuối có thể nhiều hơn để đủ 20-22 đoạn
        for (int i = 1; i <= 4; i++) {
            setNames.add(prefix + i + " (5 đoạn)");
        }

        rvSets.setLayoutManager(new LinearLayoutManager(this));
        rvSets.setAdapter(new Part3SetAdapter(setNames));
    }

    private class Part3SetAdapter extends RecyclerView.Adapter<Part3SetAdapter.ViewHolder> {
        private List<String> sets;

        public Part3SetAdapter(List<String> sets) {
            this.sets = sets;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_skill_part, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.tvTitle.setText(sets.get(position));
            holder.tvStat.setText("5 đoạn hội thoại");
            holder.ivLock.setVisibility(View.GONE);
            
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(Part3SetActivity.this, Part34Activity.class);
                intent.putExtra("PART_TYPE", partType);
                intent.putExtra("SET_INDEX", position);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() { return sets.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvStat;
            ImageView ivLock;
            ViewHolder(View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tvPartTitle);
                tvStat = v.findViewById(R.id.tvPartStat);
                ivLock = v.findViewById(R.id.ivPartLock);
            }
        }
    }
}
