package com.example.waviapp.activities;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.waviapp.R;
import com.example.waviapp.models.SpeakQuestion;
import com.example.waviapp.utils.SkillDataProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MockSpeakActivity extends BaseActivity {

    private TextView tvToolbarTitle, tvProgressText, tvInstruction, tvPrompt;
    private TextView tvStatus, tvTimer;
    private ImageView ivQuestionImage;
    private ProgressBar progressBar;
    private LinearLayout llWaveform;

    private List<SpeakQuestion> questions;
    private int currentIndex = 0;

    private MediaPlayer mediaPlayer;
    private MediaRecorder mediaRecorder;
    private CountDownTimer currentTimer;
    private ToneGenerator toneGenerator;

    private ArrayList<String> recordedPaths = new ArrayList<>();
    private String currentRecordingPath;
    private boolean isRecording = false;

    private Handler waveHandler = new Handler();
    private Runnable waveRunnable;
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mock_speak);

        initViews();
        toneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        questions = SkillDataProvider.getMockSpeakingQuestions();
        progressBar.setMax(questions.size());

        displayQuestion();
    }

    private void initViews() {
        tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        tvProgressText = findViewById(R.id.tvProgressText);
        tvInstruction = findViewById(R.id.tvInstruction);
        tvPrompt = findViewById(R.id.tvPrompt);
        tvStatus = findViewById(R.id.tvStatus);
        tvTimer = findViewById(R.id.tvTimer);
        ivQuestionImage = findViewById(R.id.ivQuestionImage);
        progressBar = findViewById(R.id.progressBar);
        llWaveform = findViewById(R.id.llWaveform);
    }

    private void displayQuestion() {
        if (currentIndex >= questions.size()) {
            finishSpeakingTest();
            return;
        }

        SpeakQuestion q = questions.get(currentIndex);
        tvProgressText.setText((currentIndex + 1) + "/" + questions.size());
        progressBar.setProgress(currentIndex + 1);

        tvInstruction.setText(q.getInstruction());
        
        if (q.isShowQuestionText()) {
            tvPrompt.setVisibility(View.VISIBLE);
            tvPrompt.setText(q.getPrompt());
        } else {
            tvPrompt.setVisibility(View.GONE);
        }

        // Simulate Image display if prompt has "[Picture" (Temporary logic for mock data)
        if (q.getPrompt().contains("[Picture")) {
            ivQuestionImage.setVisibility(View.VISIBLE);
            ivQuestionImage.setImageResource(R.drawable.wavi_background); // placeholder
        } else {
            ivQuestionImage.setVisibility(View.GONE);
        }

        tvStatus.setText("Vui lòng chờ...");
        tvTimer.setText("--:--");

        // Xử lý Audio MP3 (hoặc nhảy qua nếu null)
        if (q.getAudioResId() != null) {
            playAudioPrompt(q.getAudioResId(), q);
        } else {
            // Không có file mp3, chờ 2s rồi vào Prep Time để User đọc đề
            new Handler().postDelayed(() -> startPreparationTime(q), 2000);
        }
    }

    private void playAudioPrompt(int audioResId, SpeakQuestion q) {
        tvStatus.setText("Đang phát câu hỏi...");
        tvStatus.setTextColor(getResources().getColor(R.color.black));
        try {
            mediaPlayer = MediaPlayer.create(this, audioResId);
            mediaPlayer.setOnCompletionListener(mp -> {
                mediaPlayer.release();
                mediaPlayer = null;
                startPreparationTime(q);
            });
            mediaPlayer.start();
        } catch (Exception e) {
            startPreparationTime(q);
        }
    }

    private void startPreparationTime(SpeakQuestion q) {
        tvStatus.setText("Preparation Time");
        tvStatus.setTextColor(getResources().getColor(R.color.black));
        
        currentTimer = new CountDownTimer(q.getPrepTimeSec() * 1000L, 1000) {
            @Override
            public void onTick(long ms) {
                tvTimer.setText(formatTime((int) (ms / 1000)));
            }

            @Override
            public void onFinish() {
                playBeep();
                // Đợi tiếng beep kêu xong (khoảng 300ms) rồi mới thu âm
                new Handler().postDelayed(() -> startResponseTime(q), 300);
            }
        }.start();
    }

    private void startResponseTime(SpeakQuestion q) {
        tvStatus.setText("Recording...");
        tvStatus.setTextColor(android.graphics.Color.parseColor("#F44336"));
        llWaveform.setVisibility(View.VISIBLE);
        startRecording();

        currentTimer = new CountDownTimer(q.getResponseTimeSec() * 1000L, 1000) {
            @Override
            public void onTick(long ms) {
                tvTimer.setText(formatTime((int) (ms / 1000)));
            }

            @Override
            public void onFinish() {
                stopRecording();
                playBeep();
                llWaveform.setVisibility(View.INVISIBLE);
                recordedPaths.add(currentRecordingPath);
                
                currentIndex++;
                new Handler().postDelayed(() -> displayQuestion(), 1000);
            }
        }.start();
    }

    private void startRecording() {
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String uid = (user != null) ? user.getUid() : "anonymous";

            File dir = new File(getCacheDir(), "mock_speaking");
            if (!dir.exists()) dir.mkdirs();
            currentRecordingPath = dir.getAbsolutePath() + "/" + uid + "_q" + currentIndex + ".m4a";

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setOutputFile(currentRecordingPath);
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            startWaveAnimation();
        } catch (IOException e) {
            Toast.makeText(this, "Lỗi khi thu âm!", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        if (mediaRecorder != null && isRecording) {
            try { mediaRecorder.stop(); } catch (Exception ignored) {}
            mediaRecorder.release();
            mediaRecorder = null;
        }
        isRecording = false;
        stopWaveAnimation();
    }

    private void playBeep() {
        if (toneGenerator != null) {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 200);
        }
    }

    private String formatTime(int totalSeconds) {
        int min = totalSeconds / 60;
        int sec = totalSeconds % 60;
        return String.format(Locale.US, "%02d:%02d", min, sec);
    }

    private void finishSpeakingTest() {
        Intent intent = new Intent(MockSpeakActivity.this, MockWriteActivity.class);
        intent.putStringArrayListExtra("audioPaths", recordedPaths);
        startActivity(intent);
        finish();
    }

    private void startWaveAnimation() {
        int[] waveIds = {R.id.wave1, R.id.wave2, R.id.wave3, R.id.wave4, R.id.wave5, R.id.wave6, R.id.wave7};
        waveRunnable = new Runnable() {
            @Override public void run() {
                if (!isRecording) return;
                for (int id : waveIds) {
                    View w = llWaveform.findViewById(id);
                    if (w == null) continue;
                    float scale = 0.3f + random.nextFloat() * 0.7f;
                    ScaleAnimation anim = new ScaleAnimation(1f, 1f, 1f, scale,
                            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1f);
                    anim.setDuration(150);
                    anim.setFillAfter(true);
                    w.startAnimation(anim);
                }
                waveHandler.postDelayed(this, 160);
            }
        };
        waveHandler.post(waveRunnable);
    }

    private void stopWaveAnimation() {
        if (waveRunnable != null) waveHandler.removeCallbacks(waveRunnable);
    }

    @Override
    public void onBackPressed() {
        // Prevent back press during mock test to enforce strict timing
        Toast.makeText(this, "Không thể thoát trong lúc thi. Vui lòng hoàn thành bài.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        if (currentTimer != null) currentTimer.cancel();
        if (mediaPlayer != null) { mediaPlayer.release(); mediaPlayer = null; }
        if (toneGenerator != null) { toneGenerator.release(); toneGenerator = null; }
        stopRecording();
        super.onDestroy();
    }
}
