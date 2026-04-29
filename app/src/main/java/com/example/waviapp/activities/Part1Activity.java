package com.example.waviapp.activities;

import android.app.AlertDialog;
import android.content.res.AssetFileDescriptor;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.waviapp.R;
import com.example.waviapp.models.Part1Model;
import com.example.waviapp.utils.ProgressManager;
import com.github.jinatonic.confetti.CommonConfetti;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
    private Handler handler = new Handler();
    private List<Part1Model> questionList = new ArrayList<>();
    private int currentIndex = 0;
    private int earnedXP = 0;
    private int correctCount = 0;
    private int setIndex;
    private ProgressManager progressManager;
    private String selectedAnswer = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_part1);

        progressManager = new ProgressManager(this);
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
                if (fromUser && mediaPlayer != null) {
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
        
        try {
            InputStream ims = getAssets().open("img_p1/" + question.getImageName());
            Drawable d = Drawable.createFromStream(ims, null);
            ivQuestion.setImageDrawable(d);
            ims.close();
        } catch (IOException e) {
            ivQuestion.setImageResource(android.R.color.darker_gray);
        }

        resetUI();
        prepareAudio(question.getAudioName());
        
        if (currentIndex == questionList.size() - 1) {
            btnNext.setText("Hoàn thành");
        } else {
            btnNext.setText("Next");
        }
    }

    private void prepareAudio(String audioName) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();
        try {
            AssetFileDescriptor afd = getAssets().openFd("audio_p1/" + audioName);
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            mediaPlayer.prepare();
            
            tvTotalTime.setText(formatTime(mediaPlayer.getDuration()));
            seekBar.setMax(mediaPlayer.getDuration());
            seekBar.setProgress(0);
            tvCurrentTime.setText("00:00");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleAnswer(String selected, MaterialButton btn) {
        if (!selectedAnswer.isEmpty()) return;

        selectedAnswer = selected;
        Part1Model current = questionList.get(currentIndex);
        boolean isCorrect = selected.equalsIgnoreCase(current.getCorrectAnswer());

        if (isCorrect) {
            correctCount++;
            btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
            btn.setTextColor(Color.WHITE);
            earnedXP += 10;
            tvXP.setText(earnedXP + " XP");
        } else {
            btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F44336")));
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
            target.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
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
        
        CommonConfetti.rainingConfetti(findViewById(android.R.id.content), new int[] {
                Color.parseColor("#9370DB"), Color.YELLOW, Color.GREEN, Color.WHITE
        }).oneShot();

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
        
        MaterialButton[] buttons = {btnA, btnB, btnC, btnD};
        for (MaterialButton btn : buttons) {
            btn.setEnabled(true);
            btn.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            btn.setTextColor(Color.parseColor("#333333"));
            btn.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#E0E0E0")));
        }
    }

    private void disableAnswers() {
        btnA.setEnabled(false);
        btnB.setEnabled(false);
        btnC.setEnabled(false);
        btnD.setEnabled(false);
    }

    private void togglePlayback() {
        if (mediaPlayer == null) return;

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
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacks(updater);
    }
}
