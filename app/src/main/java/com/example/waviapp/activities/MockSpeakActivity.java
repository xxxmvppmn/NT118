package com.example.waviapp.activities;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.waviapp.R;
import com.example.waviapp.models.SpeakQuestion;
import com.example.waviapp.utils.MockTestManager;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MockSpeakActivity extends AppCompatActivity {

    private TextView tvPartTitle, tvQuestionCount, tvInstruction, tvPrompt, tvTimer, tvStatus;
    private ImageView ivQuestionImage;
    private ProgressBar progressBar;

    private List<SpeakQuestion> questionList;
    private int currentQuestionIndex = 0;

    private TextToSpeech tts;
    private ToneGenerator toneGenerator;
    private CountDownTimer countDownTimer;
    private MediaRecorder recorder;

    private boolean isRecording = false;
    private String testId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mock_speak);

        initViews();
        testId = getIntent().getStringExtra("TEST_ID");

        // Lấy câu hỏi từ MockTestManager (đã load sẵn ở MockIntroActivity)
        questionList = MockTestManager.getInstance().getSpeakingQuestions();

        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {}
                    @Override
                    public void onDone(String utteranceId) {
                        triggerNextStep(utteranceId);
                    }
                    @Override
                    public void onError(String utteranceId) {
                        triggerNextStep(utteranceId);
                    }
                });
                startQuestionFlow();
            } else {
                Toast.makeText(this, "TTS Init Failed", Toast.LENGTH_SHORT).show();
                startQuestionFlow();
            }
        });
    }

    private void initViews() {
        tvPartTitle = findViewById(R.id.tvPartTitle);
        tvQuestionCount = findViewById(R.id.tvQuestionCount);
        tvInstruction = findViewById(R.id.tvInstruction);
        tvPrompt = findViewById(R.id.tvPrompt);
        tvTimer = findViewById(R.id.tvTimer);
        tvStatus = findViewById(R.id.tvStatus);
        ivQuestionImage = findViewById(R.id.ivQuestionImage);
        progressBar = findViewById(R.id.progressBar);
    }

    private void startQuestionFlow() {
        if (currentQuestionIndex >= questionList.size()) {
            finishSpeakingTest();
            return;
        }

        SpeakQuestion currentQuestion = questionList.get(currentQuestionIndex);

        tvPartTitle.setText("Part " + currentQuestion.getPartNumber());
        tvQuestionCount.setText((currentQuestionIndex + 1) + "/" + questionList.size());
        tvInstruction.setText(currentQuestion.getInstruction());

        // Hiển thị prompt (nếu có)
        String prompt = currentQuestion.getPrompt();
        if (prompt != null && !prompt.isEmpty()) {
            tvPrompt.setText(prompt);
            tvPrompt.setVisibility(View.VISIBLE);
        } else {
            tvPrompt.setVisibility(View.GONE);
        }

        // Hiển thị hình ảnh bằng Glide (nếu có imageUrl)
        loadQuestionImage(currentQuestion.getImageUrl(), currentQuestion.getImageResId());

        // Delay slightly so user can read instruction
        new Handler().postDelayed(() -> {
            boolean shouldReadPrompt = !currentQuestion.isReadAloud() && prompt != null && !prompt.isEmpty();
            if (shouldReadPrompt) {
                speakTextWithId(prompt, "READ_PROMPT");
            } else {
                speakTextWithId("Begin preparing now.", "BEGIN_PREP");
            }
        }, 1000);
    }

    /**
     * Load ảnh từ URL (ưu tiên) hoặc từ Resource ID local.
     */
    private void loadQuestionImage(String imageUrl, Integer imageResId) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            ivQuestionImage.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.bg_edittext) // placeholder khi đang tải
                    .error(R.drawable.bg_edittext)       // fallback nếu lỗi
                    .centerCrop()
                    .into(ivQuestionImage);
        } else if (imageResId != null) {
            ivQuestionImage.setImageResource(imageResId);
            ivQuestionImage.setVisibility(View.VISIBLE);
        } else {
            ivQuestionImage.setVisibility(View.GONE);
        }
    }

    private void startPreparationTimer(SpeakQuestion question) {
        tvStatus.setText("Preparation Time");
        tvStatus.setTextColor(0xFF3B82F6);
        tvTimer.setTextColor(0xFF111827);
        progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFF3B82F6));

        int prepTimeMs = question.getPrepTimeSec() * 1000;
        progressBar.setMax(prepTimeMs);
        progressBar.setProgress(prepTimeMs);

        countDownTimer = new CountDownTimer(prepTimeMs, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateTimerUI(millisUntilFinished);
                progressBar.setProgress((int) millisUntilFinished);
            }

            @Override
            public void onFinish() {
                updateTimerUI(0);
                progressBar.setProgress(0);
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 500);

                new Handler().postDelayed(() -> {
                    speakTextWithId("Begin speaking now.", "BEGIN_SPEAK");
                }, 500);
            }
        }.start();
    }

    private void startSpeakingTimer(SpeakQuestion question) {
        tvStatus.setText("Recording...");
        tvStatus.setTextColor(0xFFEF4444);
        tvTimer.setTextColor(0xFFEF4444);
        progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFFEF4444));

        startRecordingAudio();

        int speakTimeMs = question.getResponseTimeSec() * 1000;
        progressBar.setMax(speakTimeMs);
        progressBar.setProgress(speakTimeMs);

        countDownTimer = new CountDownTimer(speakTimeMs, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateTimerUI(millisUntilFinished);
                progressBar.setProgress((int) millisUntilFinished);
            }

            @Override
            public void onFinish() {
                updateTimerUI(0);
                progressBar.setProgress(0);
                stopRecordingAudio();
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 500);

                currentQuestionIndex++;
                new Handler().postDelayed(() -> startQuestionFlow(), 1500);
            }
        }.start();
    }

    private void startRecordingAudio() {
        String fileName = getExternalCacheDir().getAbsolutePath() + "/mock_speak_q" + (currentQuestionIndex + 1) + ".3gp";
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
            recorder.start();
            isRecording = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecordingAudio() {
        if (isRecording && recorder != null) {
            try {
                recorder.stop();
            } catch (RuntimeException stopException) {
                // handle stop error
            }
            recorder.release();
            recorder = null;
            isRecording = false;
        }
    }

    private void updateTimerUI(long millis) {
        int seconds = (int) (millis / 1000);
        String timeStr = String.format("00:%02d", seconds);
        tvTimer.setText(timeStr);
    }

    private void speakTextWithId(String text, String utteranceId) {
        if (tts != null) {
            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId);
        } else {
            triggerNextStep(utteranceId);
        }
    }

    private void triggerNextStep(String utteranceId) {
        runOnUiThread(() -> {
            if ("READ_PROMPT".equals(utteranceId)) {
                new Handler().postDelayed(() -> {
                    speakTextWithId("Begin preparing now.", "BEGIN_PREP");
                }, 500);
            } else if ("BEGIN_PREP".equals(utteranceId)) {
                startPreparationTimer(questionList.get(currentQuestionIndex));
            } else if ("BEGIN_SPEAK".equals(utteranceId)) {
                startSpeakingTimer(questionList.get(currentQuestionIndex));
            }
        });
    }

    private void finishSpeakingTest() {
        Intent intent = new Intent(MockSpeakActivity.this, MockTransitionActivity.class);
        intent.putExtra("TEST_ID", testId);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (countDownTimer != null) countDownTimer.cancel();
        if (tts != null) { tts.stop(); tts.shutdown(); }
        if (toneGenerator != null) toneGenerator.release();
        stopRecordingAudio();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "You cannot go back during the test.", Toast.LENGTH_SHORT).show();
    }
}
