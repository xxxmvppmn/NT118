package com.example.waviapp.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.example.waviapp.R;
import com.example.waviapp.adapters.Part7ParagraphAdapter;
import com.example.waviapp.models.Part7Paragraph;
import com.example.waviapp.utils.JsonHelper;
import com.example.waviapp.utils.ProgressManager;
import com.github.jinatonic.confetti.CommonConfetti;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class Part7PracticeActivity extends BaseActivity {

    private RecyclerView rvParagraphs;
    private Part7ParagraphAdapter adapter;
    private List<Part7Paragraph> fullParagraphList;
    private List<Part7Paragraph> setParagraphs = new ArrayList<>();
    private ProgressBar progressBar;
    private TextView tvProgressText;
    private MaterialButton btnNext;
    private ImageView ivBack;
    private int setIndex;
    private ProgressManager progressManager;
    private int totalCorrect = 0;
    private int internalSetIndex; // Part 7 base 200

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_part7_practice);

        progressManager = new ProgressManager(this);
        setIndex = getIntent().getIntExtra("SET_INDEX", 0);
        internalSetIndex = setIndex + 200;

        initViews();
        loadSetData();
        checkContinueProgress();
    }

    private void initViews() {
        rvParagraphs = findViewById(R.id.rvParagraphs);
        progressBar = findViewById(R.id.progressBar);
        tvProgressText = findViewById(R.id.tvProgressText);
        btnNext = findViewById(R.id.btnNext);
        ivBack = findViewById(R.id.ivBack);

        TextView tvTitle = findViewById(R.id.tvToolbarTitle);
        if (tvTitle != null) tvTitle.setText("Part 7 - Set " + (setIndex + 1));
        
        ivBack.setOnClickListener(v -> finish());
    }

    private void loadSetData() {
        fullParagraphList = JsonHelper.loadPart7Paragraphs(this);
        setParagraphs.clear();

        int start = setIndex * 5;
        int end = Math.min(start + 5, fullParagraphList.size());

        for (int i = start; i < end; i++) {
            setParagraphs.add(fullParagraphList.get(i));
        }

        if (progressBar != null) progressBar.setMax(setParagraphs.size());
    }

    private void checkContinueProgress() {
        int savedParaIndex = progressManager.getSetProgress(internalSetIndex);
        if (savedParaIndex > 0 && savedParaIndex < setParagraphs.size()) {
            new AlertDialog.Builder(this)
                    .setTitle("Continue?")
                    .setMessage("Continue from paragraph " + (savedParaIndex + 1) + "?")
                    .setPositiveButton("Continue", (dialog, which) -> setupRecyclerView(savedParaIndex))
                    .setNegativeButton("Restart", (dialog, which) -> {
                        progressManager.resetSetProgress(internalSetIndex);
                        setupRecyclerView(0);
                    })
                    .setCancelable(false)
                    .show();
        } else {
            setupRecyclerView(0);
        }
    }

    private void setupRecyclerView(int startPos) {
        adapter = new Part7ParagraphAdapter(this, setParagraphs, new Part7ParagraphAdapter.OnPart7InteractionListener() {
            @Override
            public void onQuestionAnswered(boolean isCorrect) {
                if (isCorrect) {
                    totalCorrect++;
                    progressManager.addXP(10);
                }
                btnNext.setEnabled(true);
            }

            @Override
            public void onParagraphCompleted(int paragraphPos, int correctInParagraph) {
                // This is called when no more questions in paragraph
                updateProgress(paragraphPos + 1);
                progressManager.saveSetProgress(internalSetIndex, paragraphPos + 1);

                if (paragraphPos == setParagraphs.size() - 1) {
                    showCelebration();
                } else {
                    rvParagraphs.smoothScrollToPosition(paragraphPos + 1);
                    btnNext.setEnabled(false);
                }
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvParagraphs.setLayoutManager(layoutManager);
        rvParagraphs.setAdapter(adapter);

        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(rvParagraphs);

        rvParagraphs.scrollToPosition(startPos);
        updateProgress(startPos);

        btnNext.setOnClickListener(v -> {
            int current = layoutManager.findFirstVisibleItemPosition();
            Part7ParagraphAdapter.ViewHolder holder = (Part7ParagraphAdapter.ViewHolder) rvParagraphs.findViewHolderForAdapterPosition(current);
            if (holder != null) {
                // If it returns true, it moved to next question. 
                // If false, it called onParagraphCompleted logic already or will call it.
                boolean hasNextQ = adapter.goToNextQuestion(current, holder);
                if (hasNextQ) {
                    btnNext.setEnabled(false);
                }
            }
        });
    }

    private void updateProgress(int progress) {
        if (progressBar != null) progressBar.setProgress(progress);
        if (tvProgressText != null) tvProgressText.setText(progress + "/" + setParagraphs.size());
    }

    private void showCelebration() {
        CommonConfetti.rainingConfetti(findViewById(android.R.id.content), new int[] {
                getColor(R.color.purple_main),
                android.graphics.Color.WHITE,
                android.graphics.Color.YELLOW
        }).oneShot();

        progressManager.markSetCompleted(internalSetIndex);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_summary_celebration, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        TextView tvCorrect = dialogView.findViewById(R.id.tvCorrectCount);
        TextView tvXP = dialogView.findViewById(R.id.tvXPCount);
        MaterialButton btnFinish = dialogView.findViewById(R.id.btnFinish);

        int totalQuestions = 0;
        for (Part7Paragraph p : setParagraphs) totalQuestions += p.getQuestions().size();

        tvCorrect.setText(totalCorrect + "/" + totalQuestions);
        tvXP.setText("+" + (totalCorrect * 10) + " XP");

        btnFinish.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        dialog.show();
    }
}
