package com.example.waviapp.activities;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.waviapp.R;
import com.example.waviapp.models.SpeakQuestion;
import com.example.waviapp.models.WriteQuestion;
import com.example.waviapp.utils.MockTestManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MockReviewActivity extends AppCompatActivity {

    private TextView tvReviewTitle, tvQuestionCount, tvInstruction, tvPrompt;
    private ImageView ivQuestionImage;
    private TextView tvMyTextAnswer, tvSampleAnswer, tvAudioStatus;
    private LinearLayout llMyAudioAnswer;
    private Button btnPlayMyAudio, btnPrev, btnNext;

    private int currentIndex = 0;
    private List<SpeakQuestion> speakQuestions;
    private List<WriteQuestion> writeQuestions;
    private int totalQuestions;

    private String testId;
    private List<String> fetchedAudioUrls;
    private List<String> fetchedWriteAnswers;

    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mock_review);

        initViews();

        // Lấy câu hỏi từ MockTestManager (đã load sẵn)
        speakQuestions = MockTestManager.getInstance().getSpeakingQuestions();
        writeQuestions = MockTestManager.getInstance().getWritingQuestions();
        totalQuestions = speakQuestions.size() + writeQuestions.size();

        testId = getIntent().getStringExtra("TEST_ID");

        if (testId != null) {
            fetchDataFromFirebase();
        } else {
            updateUI();
        }

        btnNext.setOnClickListener(v -> {
            if (currentIndex < totalQuestions - 1) {
                stopAudio();
                currentIndex++;
                updateUI();
            } else {
                finish();
            }
        });

        btnPrev.setOnClickListener(v -> {
            if (currentIndex > 0) {
                stopAudio();
                currentIndex--;
                updateUI();
            }
        });

        btnPlayMyAudio.setOnClickListener(v -> handleAudioPlayback());
    }

    private void initViews() {
        tvReviewTitle = findViewById(R.id.tvReviewTitle);
        tvQuestionCount = findViewById(R.id.tvQuestionCount);
        tvInstruction = findViewById(R.id.tvInstruction);
        tvPrompt = findViewById(R.id.tvPrompt);
        ivQuestionImage = findViewById(R.id.ivQuestionImage);
        tvMyTextAnswer = findViewById(R.id.tvMyTextAnswer);
        tvSampleAnswer = findViewById(R.id.tvSampleAnswer);
        llMyAudioAnswer = findViewById(R.id.llMyAudioAnswer);
        btnPlayMyAudio = findViewById(R.id.btnPlayMyAudio);
        tvAudioStatus = findViewById(R.id.tvAudioStatus);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
    }

    @SuppressWarnings("unchecked")
    private void fetchDataFromFirebase() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (userId == null) {
            updateUI();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("mock_history")
                .document(userId)
                .collection("tests")
                .document(testId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        fetchedAudioUrls = (List<String>) documentSnapshot.get("audioUrls");
                        fetchedWriteAnswers = (List<String>) documentSnapshot.get("writingAnswers");
                    }
                    updateUI();
                })
                .addOnFailureListener(e -> updateUI());
    }

    private void updateUI() {
        tvQuestionCount.setText((currentIndex + 1) + "/" + totalQuestions);
        btnPrev.setEnabled(currentIndex > 0);
        btnNext.setText(currentIndex == totalQuestions - 1 ? "Finish Review" : "Next");

        if (currentIndex < speakQuestions.size()) {
            // ========== SPEAKING QUESTION ==========
            SpeakQuestion sq = speakQuestions.get(currentIndex);
            tvReviewTitle.setText("Speaking - Part " + sq.getPartNumber() + " (Q" + (currentIndex + 1) + ")");
            tvInstruction.setText(sq.getInstruction());

            // Hiển thị prompt
            String prompt = sq.getPrompt();
            if (prompt != null && !prompt.isEmpty()) {
                tvPrompt.setText(prompt);
                tvPrompt.setVisibility(View.VISIBLE);
            } else {
                tvPrompt.setVisibility(View.GONE);
            }

            // Hiển thị đáp án mẫu
            tvSampleAnswer.setText(sq.getSampleAnswer());

            // Load ảnh bằng Glide
            loadImage(sq.getImageUrl(), sq.getImageResId());

            // Hiển thị phần phát audio
            tvMyTextAnswer.setVisibility(View.GONE);
            llMyAudioAnswer.setVisibility(View.VISIBLE);
            tvAudioStatus.setText("Ready");

        } else {
            // ========== WRITING QUESTION ==========
            int writeIndex = currentIndex - speakQuestions.size();
            WriteQuestion wq = writeQuestions.get(writeIndex);
            tvReviewTitle.setText("Writing - Part " + wq.getPartNumber() + " (Q" + (currentIndex + 1) + ")");
            tvInstruction.setText(wq.getInstruction());

            // Hiển thị prompt
            String prompt = wq.getPrompt();
            if (prompt != null && !prompt.isEmpty()) {
                tvPrompt.setText(prompt);
                tvPrompt.setVisibility(View.VISIBLE);
            } else {
                tvPrompt.setVisibility(View.GONE);
            }

            // Hiển thị đáp án mẫu
            tvSampleAnswer.setText(wq.getSampleAnswer());

            // Load ảnh bằng Glide
            loadImage(wq.getImageUrl(), wq.getImageResId());

            // Hiển thị bài viết của người dùng
            llMyAudioAnswer.setVisibility(View.GONE);
            tvMyTextAnswer.setVisibility(View.VISIBLE);

            if (fetchedWriteAnswers != null && writeIndex < fetchedWriteAnswers.size()) {
                String answer = fetchedWriteAnswers.get(writeIndex);
                tvMyTextAnswer.setText(answer != null && !answer.isEmpty() ? answer : "(No answer provided)");
            } else if (MockWriteActivity.mockWriteAnswers != null && writeIndex < MockWriteActivity.mockWriteAnswers.size()) {
                String answer = MockWriteActivity.mockWriteAnswers.get(writeIndex);
                tvMyTextAnswer.setText(answer != null && !answer.isEmpty() ? answer : "(No answer provided)");
            } else {
                tvMyTextAnswer.setText("(No answer provided)");
            }
        }
    }

    private void loadImage(String imageUrl, Integer imageResId) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            ivQuestionImage.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.bg_edittext)
                    .error(R.drawable.bg_edittext)
                    .centerCrop()
                    .into(ivQuestionImage);
        } else if (imageResId != null) {
            ivQuestionImage.setImageResource(imageResId);
            ivQuestionImage.setVisibility(View.VISIBLE);
        } else {
            ivQuestionImage.setVisibility(View.GONE);
        }
    }

    private void handleAudioPlayback() {
        if (isPlaying) {
            stopAudio();
            return;
        }

        String audioSource = null;

        // Try Firebase URL first
        if (fetchedAudioUrls != null && currentIndex < fetchedAudioUrls.size()) {
            audioSource = fetchedAudioUrls.get(currentIndex);
        }

        // Fallback: local cache
        if (audioSource == null || audioSource.isEmpty()) {
            File file = new File(getExternalCacheDir(), "mock_speak_q" + (currentIndex + 1) + ".3gp");
            if (file.exists()) {
                audioSource = file.getAbsolutePath();
            }
        }

        if (audioSource == null || audioSource.isEmpty()) {
            Toast.makeText(this, "Audio file not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        tvAudioStatus.setText("Loading...");
        btnPlayMyAudio.setEnabled(false);

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioSource);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                isPlaying = true;
                btnPlayMyAudio.setText("Stop");
                btnPlayMyAudio.setEnabled(true);
                tvAudioStatus.setText("Playing...");
            });
            mediaPlayer.setOnCompletionListener(mp -> stopAudio());
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                stopAudio();
                Toast.makeText(this, "Error playing audio", Toast.LENGTH_SHORT).show();
                return true;
            });
        } catch (IOException e) {
            e.printStackTrace();
            stopAudio();
        }
    }

    private void stopAudio() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        isPlaying = false;
        btnPlayMyAudio.setText("Play Audio");
        btnPlayMyAudio.setEnabled(true);
        tvAudioStatus.setText("Ready");
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopAudio();
    }
}
