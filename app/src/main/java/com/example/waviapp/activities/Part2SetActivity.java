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

public class Part2SetActivity extends BaseActivity {

    private RecyclerView rvSets;
    private ProgressManager progressManager;
    private TextView tvTotalXP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_part2_sets);

        progressManager = new ProgressManager(this);
        tvTotalXP = findViewById(R.id.tvTotalXP);
        rvSets = findViewById(R.id.rvSets);

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
        for (int i = 1; i <= 6; i++) {
            setNames.add("Listening Part 2 - Set " + i);
        }

        rvSets.setLayoutManager(new LinearLayoutManager(this));
        rvSets.setAdapter(new Part2SetAdapter(setNames));
    }

    private class Part2SetAdapter extends RecyclerView.Adapter<Part2SetAdapter.ViewHolder> {
        private List<String> sets;

        public Part2SetAdapter(List<String> sets) {
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
            holder.tvStat.setText("6 câu hỏi");
            holder.ivLock.setVisibility(View.GONE);
            
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(Part2SetActivity.this, Part2PracticeActivity.class);
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
