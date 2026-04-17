package com.example.waviapp.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.example.waviapp.R;
import com.example.waviapp.adapters.Part5Adapter;
import com.example.waviapp.models.Part5Question;
import com.example.waviapp.utils.JsonHelper;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class Part5Activity extends BaseActivity {

    private RecyclerView rvQuestions;
    private Part5Adapter adapter;
    private List<Part5Question> questionList;
    private ProgressBar progressBar;
    private TextView tvProgressText;
    private MaterialButton btnNext;
    private ImageView ivBack;
    private int currentPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_part5);

        initViews();
        loadData();
        setupRecyclerView();
        setupListeners();
    }

    private void initViews() {
        rvQuestions = findViewById(R.id.rvQuestions);
        progressBar = findViewById(R.id.progressBar);
        tvProgressText = findViewById(R.id.tvProgressText);
        btnNext = findViewById(R.id.btnNext);
        ivBack = findViewById(R.id.ivBack);
    }

    private void loadData() {
        questionList = JsonHelper.loadPart5Questions(this);
        if (questionList != null && !questionList.isEmpty()) {
            progressBar.setMax(questionList.size());
            updateProgress(0);
        }
    }

    private void setupRecyclerView() {
        adapter = new Part5Adapter(this, questionList, (position, isCorrect) -> {
            btnNext.setEnabled(true);
            updateProgress(position + 1);
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvQuestions.setLayoutManager(layoutManager);
        rvQuestions.setAdapter(adapter);

        // Snap to one item at a time
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(rvQuestions);

        rvQuestions.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    currentPosition = layoutManager.findFirstVisibleItemPosition();
                    // We don't necessarily update progress here if we only want progress to count answered questions
                }
            }
        });
        
        adapter.notifyDataSetChanged();
    }

    private void setupListeners() {
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }

        btnNext.setOnClickListener(v -> {
            if (currentPosition < questionList.size() - 1) {
                rvQuestions.smoothScrollToPosition(currentPosition + 1);
                btnNext.setEnabled(false);
            } else {
                // Finish practice
                finish();
            }
        });
    }

    private void updateProgress(int progress) {
        progressBar.setProgress(progress);
        tvProgressText.setText(progress + "/" + questionList.size());
    }
}
