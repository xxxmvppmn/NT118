package com.example.waviapp.activities;

import android.app.AlertDialog;
import android.content.res.AssetFileDescriptor;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.waviapp.R;
import com.example.waviapp.models.Part1Model;
import com.example.waviapp.utils.AppConfig;
import com.example.waviapp.utils.OfflineAssetManager;
import com.example.waviapp.utils.ProgressManager;
import com.github.jinatonic.confetti.CommonConfetti;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Part1Activity extends BaseActivity {

    private ImageView ivQuestion;
    private TextView tvProgress, tvXP, tvCurrentTime, tvTotalTime, tvScript, tvExplanation;
    private ImageButton btnPlayPause, btnBack;
    private SeekBar seekBar;
    private MaterialButton btnA, btnB, btnC, btnD, btnNext;
    private View cardInfo;

    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler(Looper.getMainLooper());
    private List<Part1Model> questionList = new ArrayList<>();
    private int currentIndex = 0;
    private int earnedXP = 0;
    private int correctCount = 0;
    private int setIndex;
    private ProgressManager progressManager;
    private OfflineAssetManager offlineManager;
    private String selectedAnswer = "";
    private boolean isPrepared = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_part1);

        progressManager = new ProgressManager(this);
        offlineManager = new OfflineAssetManager(this);
        setIndex = getIntent().getIntExtra("SET_INDEX", 0);
        
        initViews();
        loadData();
        checkContinueProgress();
    }

    private void initViews() {
        ivQuestion = findViewById(R.id.ivQuestion);
        tvProgress = findViewById(R.id.tvProgress);
        tvXP = findViewById(R.id.tvXP);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        tvScript = findViewById(R.id.tvScript);
        tvExplanation = findViewById(R.id.tvExplanation);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        seekBar = findViewById(R.id.seekBar);
        btnA = findViewById(R.id.btnA);
        btnB = findViewById(R.id.btnB);
        btnC = findViewById(R.id.btnC);
        btnD = findViewById(R.id.btnD);
        btnNext = findViewById(R.id.btnNext);
        cardInfo = findViewById(R.id.cardInfo);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        btnPlayPause.setOnClickListener(v -> togglePlayback());
        btnNext.setOnClickListener(v -> nextQuestion());

        View.OnClickListener answerListener = v -> {
            MaterialButton btn = (MaterialButton) v;
            handleAnswer(btn.getText().toString(), btn);
        };

        btnA.setOnClickListener(answerListener);
        btnB.setOnClickListener(answerListener);
        btnC.setOnClickListener(answerListener);
        btnD.setOnClickListener(answerListener);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null && isPrepared) {
                    mediaPlayer.seekTo(progress);
                    tvCurrentTime.setText(formatTime(progress));
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void checkContinueProgress() {
        // Sử dụng offset 1000 cho Part 1 để không trùng với Part 5 (0-100)
        int savedIndex = progressManager.getSetProgress(1000 + setIndex);
        if (savedIndex > 0 && savedIndex < questionList.size()) {
            new AlertDialog.Builder(this)
                    .setTitle("Tiếp tục?")
                    .setMessage("Bạn có muốn tiếp tục từ câu " + (savedIndex + 1) + "?")
                    .setPositiveButton("Tiếp tục", (dialog, which) -> {
                        currentIndex = savedIndex;
                        displayQuestion();
                    })
                    .setNegativeButton("Làm lại", (dialog, which) -> {
                        currentIndex = 0;
                        progressManager.saveSetProgress(1000 + setIndex, 0);
                        displayQuestion();
                    })
                    .setCancelable(false)
                    .show();
        } else {
            displayQuestion();
        }
    }

    private void loadData() {
        try {
            InputStream is = getAssets().open("listening_p1.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            Gson gson = new Gson();
            Type listType = new TypeToken<List<Part1Model>>() {}.getType();
            List<Part1Model> allQuestions = gson.fromJson(json, listType);

            // Chia set: mỗi set 5 câu
            questionList = new ArrayList<>();
            int start = setIndex * 5;
            int end = Math.min(start + 5, allQuestions.size());
            for (int i = start; i < end; i++) {
                questionList.add(allQuestions.get(i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayQuestion() {
        if (questionList == null || questionList.isEmpty()) return;

        Part1Model question = questionList.get(currentIndex);
        tvProgress.setText("Question " + (currentIndex + 1) + "/" + questionList.size());
        
        // Load image using Glide with smart path resolution (offline-first)
        loadQuestionImage(question.getImageName());

        resetUI();
        prepareAudio(question.getAudioName());
        
        if (currentIndex == questionList.size() - 1) {
            btnNext.setText("Hoàn thành");
        } else {
            btnNext.setText("Next");
        }
    }

    /**
     * Load question image using Glide.
     * Priority: Local offline file → Assets → Firebase URL
     */
    private void loadQuestionImage(String imageName) {
        String relativePath = AppConfig.IMG_P1_FOLDER + "/" + imageName;
        File localFile = AppConfig.getLocalAssetFile(this, relativePath);
        
        if (localFile.exists()) {
            // Load from offline downloaded file
            Glide.with(this)
                    .load(localFile)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .into(ivQuestion);
        } else {
            // Try loading from assets first (backwards compatible)
            try {
                InputStream ims = getAssets().open("img_p1/" + imageName);
                ims.close(); // File exists in assets
                Glide.with(this)
                        .load(Uri.parse("file:///android_asset/img_p1/" + imageName))
                        .placeholder(R.drawable.ic_image_placeholder)
                        .into(ivQuestion);
            } catch (IOException e) {
                // Fallback to Firebase URL
                String url = AppConfig.getFirebaseUrl(relativePath);
                Glide.with(this)
                        .load(url)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .into(ivQuestion);
            }
        }
    }

    /**
     * Prepare audio using async method to avoid UI blocking.
     * Priority: Local offline file → Assets → Firebase URL
     */
    private void prepareAudio(String audioName) {
        releaseMediaPlayer();
        isPrepared = false;
        
        mediaPlayer = new MediaPlayer();
        
        // Show loading state
        btnPlayPause.setEnabled(false);
        tvCurrentTime.setText("00:00");
        tvTotalTime.setText("--:--");
        seekBar.setProgress(0);

        try {
            String relativePath = AppConfig.AUDIO_P1_FOLDER + "/" + audioName;
            File localFile = AppConfig.getLocalAssetFile(this, relativePath);
            
            if (localFile.exists()) {
                // Play from offline downloaded file
                mediaPlayer.setDataSource(localFile.getAbsolutePath());
            } else {
                // Try assets first (backwards compatible)
                try {
                    AssetFileDescriptor afd = getAssets().openFd("audio_p1/" + audioName);
                    mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                    afd.close();
                } catch (IOException assetError) {
                    // Fallback to Firebase URL
                    String url = AppConfig.getFirebaseUrl(relativePath);
                    mediaPlayer.setDataSource(url);
                }
            }

            // Use prepareAsync to avoid blocking the UI thread
            mediaPlayer.setOnPreparedListener(mp -> {
                isPrepared = true;
                btnPlayPause.setEnabled(true);
                tvTotalTime.setText(formatTime(mp.getDuration()));
                seekBar.setMax(mp.getDuration());
                seekBar.setProgress(0);
                tvCurrentTime.setText("00:00");
                btnPlayPause.setImageResource(R.drawable.ic_play);
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Toast.makeText(this, "Không thể phát audio. Vui lòng kiểm tra kết nối mạng.", Toast.LENGTH_SHORT).show();
                btnPlayPause.setEnabled(false);
                return true;
            });

            mediaPlayer.prepareAsync();
            
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi tải file âm thanh", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleAnswer(String selected, MaterialButton btn) {
        if (!selectedAnswer.isEmpty()) return;

        selectedAnswer = selected;
        Part1Model current = questionList.get(currentIndex);
        boolean isCorrect = selected.equalsIgnoreCase(current.getCorrectAnswer());

        if (isCorrect) {
            correctCount++;
            btn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.color_success));
            btn.setTextColor(Color.WHITE);
            earnedXP += 10;
            tvXP.setText(earnedXP + " XP");
        } else {
            btn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.color_error));
            btn.setTextColor(Color.WHITE);
            highlightCorrectAnswer(current.getCorrectAnswer());
        }

        // Lưu tiến trình
        progressManager.saveSetProgress(1000 + setIndex, currentIndex);
        
        disableAnswers();
        showInfo();
    }

    private void highlightCorrectAnswer(String correct) {
        MaterialButton target = null;
        if (correct.equalsIgnoreCase("A")) target = btnA;
        else if (correct.equalsIgnoreCase("B")) target = btnB;
        else if (correct.equalsIgnoreCase("C")) target = btnC;
        else if (correct.equalsIgnoreCase("D")) target = btnD;

        if (target != null) {
            target.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.color_success));
            target.setTextColor(Color.WHITE);
        }
    }

    private void showInfo() {
        Part1Model current = questionList.get(currentIndex);
        tvScript.setText(current.getScript());
        tvExplanation.setText(current.getExplanation());
        cardInfo.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.VISIBLE);
    }

    private void nextQuestion() {
        if (currentIndex < questionList.size() - 1) {
            currentIndex++;
            selectedAnswer = "";
            displayQuestion();
        } else {
            showCelebration();
        }
    }

    private void showCelebration() {
        progressManager.addXP(earnedXP);
        progressManager.markSetCompleted(1000 + setIndex);
        
        CommonConfetti.rainingConfetti(
                (ViewGroup) findViewById(android.R.id.content),
                new int[]{
                        ContextCompat.getColor(this, R.color.premium_purple), Color.YELLOW, Color.GREEN, Color.WHITE
                }
        ).oneShot();

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_summary_celebration, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        TextView tvCorrect = dialogView.findViewById(R.id.tvCorrectCount);
        TextView tvXPResult = dialogView.findViewById(R.id.tvXPCount);
        MaterialButton btnFinish = dialogView.findViewById(R.id.btnFinish);

        tvCorrect.setText(correctCount + "/" + questionList.size());
        tvXPResult.setText("+" + earnedXP + " XP");

        btnFinish.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        dialog.show();
    }

    private void resetUI() {
        cardInfo.setVisibility(View.GONE);
        btnNext.setVisibility(View.GONE);
        btnPlayPause.setImageResource(R.drawable.ic_play);
        resetButtons();
    }

    private void resetButtons() {
        MaterialButton[] btns = {btnA, btnB, btnC, btnD};
        for (MaterialButton btn : btns) {
            btn.setEnabled(true);
            btn.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            btn.setTextColor(ContextCompat.getColor(this, R.color.text_dark_gray));
            btn.setStrokeColor(ContextCompat.getColorStateList(this, R.color.divider));
            btn.setStrokeWidth(2);
        }
    }

    private void disableAnswers() {
        btnA.setEnabled(false);
        btnB.setEnabled(false);
        btnC.setEnabled(false);
        btnD.setEnabled(false);
    }

    private void togglePlayback() {
        if (mediaPlayer == null || !isPrepared) return;

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            btnPlayPause.setImageResource(R.drawable.ic_play);
            handler.removeCallbacks(updater);
        } else {
            mediaPlayer.start();
            btnPlayPause.setImageResource(R.drawable.ic_pause);
            handler.post(updater);
        }
    }

    private Runnable updater = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && isPrepared && mediaPlayer.isPlaying()) {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                tvCurrentTime.setText(formatTime(mediaPlayer.getCurrentPosition()));
                handler.postDelayed(this, 100);
            }
        }
    };

    private String formatTime(int ms) {
        int m = (ms / 1000) / 60;
        int s = (ms / 1000) % 60;
        return String.format("%02d:%02d", m, s);
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            handler.removeCallbacks(updater);
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
            } catch (IllegalStateException e) {
                // Ignore if already released
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && isPrepared && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            btnPlayPause.setImageResource(R.drawable.ic_play);
            handler.removeCallbacks(updater);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
    }
}
