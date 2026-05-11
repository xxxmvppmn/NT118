package com.example.waviapp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.waviapp.R;

import java.io.IOException;

public class MockMicCheckActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 200;
    private Button btnRecordTest, btnPlayTest, btnStartTest;
    private TextView tvStatus;
    
    private MediaRecorder recorder;
    private MediaPlayer player;
    private String fileName;
    
    private boolean isRecording = false;
    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mock_mic_check);

        btnRecordTest = findViewById(R.id.btnRecordTest);
        btnPlayTest = findViewById(R.id.btnPlayTest);
        btnStartTest = findViewById(R.id.btnStartTest);
        tvStatus = findViewById(R.id.tvStatus);

        fileName = getExternalCacheDir().getAbsolutePath() + "/mic_test.3gp";

        btnRecordTest.setOnClickListener(v -> {
            if (checkPermissions()) {
                if (isRecording) {
                    stopRecording();
                } else {
                    startRecording();
                }
            } else {
                requestPermissions();
            }
        });

        btnPlayTest.setOnClickListener(v -> {
            if (isPlaying) {
                stopPlaying();
            } else {
                startPlaying();
            }
        });

        btnStartTest.setOnClickListener(v -> {
            Intent intent = new Intent(MockMicCheckActivity.this, MockSpeakActivity.class);
            intent.putExtra("TEST_ID", getIntent().getStringExtra("TEST_ID"));
            startActivity(intent);
            finish();
        });
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording();
            } else {
                Toast.makeText(this, "Permission denied. Cannot test microphone.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
            recorder.start();
            isRecording = true;
            btnRecordTest.setText("STOP");
            btnRecordTest.setBackgroundColor(0xFFEF4444); // Red
            tvStatus.setText("Recording... Please speak.");
            btnPlayTest.setEnabled(false);
            btnStartTest.setEnabled(false);
            
            // Auto stop after 5 seconds
            new Handler().postDelayed(() -> {
                if (isRecording) stopRecording();
            }, 5000);
            
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Recording failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        try {
            recorder.stop();
            recorder.release();
            recorder = null;
            isRecording = false;
            btnRecordTest.setText("RECORD");
            btnRecordTest.setBackgroundColor(0xFF4F46E5); // Indigo
            tvStatus.setText("Recording saved. Press Play to listen.");
            btnPlayTest.setEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startPlaying() {
        player = new MediaPlayer();
        try {
            player.setDataSource(fileName);
            player.prepare();
            player.start();
            isPlaying = true;
            btnPlayTest.setText("Stop Playing");
            
            player.setOnCompletionListener(mp -> {
                stopPlaying();
                btnStartTest.setEnabled(true); // Enable start test after playing successfully
                tvStatus.setText("Mic test completed! You can start the test.");
            });
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopPlaying() {
        if (player != null) {
            player.release();
            player = null;
            isPlaying = false;
            btnPlayTest.setText("Play Recording");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
