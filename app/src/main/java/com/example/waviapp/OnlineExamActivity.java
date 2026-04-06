package com.example.waviapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.waviapp.adapters.OnlineExamHistoryAdapter;
import com.example.waviapp.databinding.ActivityOnlineExamBinding;
import java.util.ArrayList;
import java.util.List;

public class OnlineExamActivity extends AppCompatActivity {
    private ActivityOnlineExamBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnlineExamBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.ivBack.setOnClickListener(v -> finish());
        binding.ivInfo.setOnClickListener(v -> 
            startActivity(new android.content.Intent(OnlineExamActivity.this, EventInfoActivity.class))
        );

        setupRecyclerView();
    }

    private void setupRecyclerView() {
        List<OnlineExamHistory> list = new ArrayList<>();
        list.add(new OnlineExamHistory("Dragon Quiz", 10, 10, R.color.bg_listen));
        list.add(new OnlineExamHistory("Your Passport to TOEIC Success", 200, 120, R.color.bg_read));
        list.add(new OnlineExamHistory("MOT37: TOEIC Readiness Test", 200, 120, R.color.bg_speak));
        list.add(new OnlineExamHistory("MOT33: TOEIC Summit Contest", 200, 120, R.color.bg_write));

        OnlineExamHistoryAdapter adapter = new OnlineExamHistoryAdapter(list);
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.rvHistory.setAdapter(adapter);
    }
}
