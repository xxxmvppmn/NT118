package com.example.waviapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.waviapp.R;
import com.example.waviapp.adapters.Part7SetAdapter;
import com.example.waviapp.utils.ProgressManager;
import java.util.ArrayList;
import java.util.List;

public class Part7SetActivity extends BaseActivity {

    private RecyclerView rvSets;
    private Part7SetAdapter adapter;
    private ProgressManager progressManager;
    private TextView tvTotalXP;
    private ImageView ivBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_part7_sets);

        progressManager = new ProgressManager(this);
        initViews();
        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (tvTotalXP != null) {
            tvTotalXP.setText(progressManager.getTotalXP() + " XP");
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void initViews() {
        rvSets = findViewById(R.id.rvSets);
        tvTotalXP = findViewById(R.id.tvTotalXP);
        ivBack = findViewById(R.id.ivBack);

        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }
        
        if (tvTotalXP != null) {
            tvTotalXP.setText(progressManager.getTotalXP() + " XP");
        }
    }

    private void setupRecyclerView() {
        List<String> setNames = new ArrayList<>();
        setNames.add("Reading Part 7 - Set 1");
        setNames.add("Reading Part 7 - Set 2");
        setNames.add("Reading Part 7 - Set 3");
        setNames.add("Reading Part 7 - Set 4");
        setNames.add("Reading Part 7 - Set 5");

        adapter = new Part7SetAdapter(setNames, progressManager, setIndex -> {
            Intent intent = new Intent(Part7SetActivity.this, Part7PracticeActivity.class);
            intent.putExtra("SET_INDEX", setIndex);
            startActivity(intent);
        });

        rvSets.setLayoutManager(new LinearLayoutManager(this));
        rvSets.setAdapter(adapter);
    }
}
