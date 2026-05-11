package com.example.waviapp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.waviapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.File;
import java.io.IOException;

public class MockMicCheckActivity extends BaseActivity {

    private static final int REQUEST_RECORD_AUDIO = 200;
    private FloatingActionButton fabRecordTest;
    private TextView tvMicStatus;
    private MaterialButton btnConfirm;

    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private String tempAudioPath;
    private boolean isRecording = false;
    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mock_mic_check);

        fabRecordTest = findViewById(R.id.fabRecordTest);
        tvMicStatus = findViewById(R.id.tvMicStatus);
        btnConfirm = findViewById(R.id.btnConfirm);

        tempAudioPath = getCacheDir().getAbsolutePath() + "/mic_test.m4a";

        fabRecordTest.setOnClickListener(v -> {
            if (checkPermissions()) {
                if (!isRecording && !isPlaying) {
                    startRecordingTest();
                }
            }
        });

        btnConfirm.setOnClickListener(v -> {
            // Start the actual mock test
            Intent intent = new Intent(MockMicCheckActivity.this, MockSpeakActivity.class);
            startActivity(intent);
            finish();
        });
        
        checkPermissions();
    }

    private boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                Toast.makeText(this, "Bạn phải cấp quyền Mic để thi thử!", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void startRecordingTest() {
        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setOutputFile(tempAudioPath);
            mediaRecorder.prepare();
            mediaRecorder.start();

            isRecording = true;
            tvMicStatus.setText("Đang ghi âm (5s)...");
            fabRecordTest.setImageResource(R.drawable.ic_stop);
            fabRecordTest.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F44336")));

            new CountDownTimer(5000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    tvMicStatus.setText("Đang ghi âm (" + (millisUntilFinished / 1000) + "s)...");
                }

                @Override
                public void onFinish() {
                    stopRecordingAndPlay();
                }
            }.start();

        } catch (IOException e) {
            Toast.makeText(this, "Lỗi khi ghi âm!", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecordingAndPlay() {
        if (mediaRecorder != null) {
            try { mediaRecorder.stop(); } catch (Exception ignored) {}
            mediaRecorder.release();
            mediaRecorder = null;
        }
        isRecording = false;

        tvMicStatus.setText("Đang phát lại...");
        fabRecordTest.setImageResource(R.drawable.ic_listen); // Make sure you have ic_listen or similar
        fabRecordTest.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50")));

        isPlaying = true;
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(tempAudioPath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                tvMicStatus.setText("Nhấn Mic để thử lại");
                fabRecordTest.setImageResource(R.drawable.ic_mic);
                fabRecordTest.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#9370DB")));
                btnConfirm.setEnabled(true);
            });
        } catch (IOException e) {
            isPlaying = false;
            Toast.makeText(this, "Lỗi khi phát lại!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (mediaRecorder != null) {
            try { mediaRecorder.stop(); } catch (Exception e) {}
            mediaRecorder.release();
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        super.onDestroy();
    }
}
