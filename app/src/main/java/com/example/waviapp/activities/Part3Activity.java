package com.example.waviapp.activities;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waviapp.R;
import com.example.waviapp.adapters.Part3Adapter;
import com.example.waviapp.models.Part3Passage;
import com.example.waviapp.utils.AppConfig;
import com.example.waviapp.utils.OfflineAssetManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Part3Activity extends BaseActivity {

    private TextView tvProgress, tvXP, tvCurrentTime, tvTotalTime, tvTranscript;
    private ImageButton btnPlayPause;
    private SeekBar seekBar;
    private RecyclerView rvQuestions;
    private Button btnShowTranscript, btnNext;
    private View scrollTranscript; // Changed from NestedScrollView to View to match CardView in XML

    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler(android.os.Looper.getMainLooper());
    private List<Part3Passage> passageList = new ArrayList<>();
    private int currentPassageIndex = 0;
    private int currentXP = 0;
    private boolean isPrepared = false;
    private Part3Adapter adapter;
    private int setIndex = 0;
    private OfflineAssetManager offlineManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_part3);

        setIndex = getIntent().getIntExtra("SET_INDEX", 0);
        offlineManager = new OfflineAssetManager(this);

        initViews();
        loadPart3JSON();
        displayPassage(currentPassageIndex);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void initViews() {
        tvProgress = findViewById(R.id.tvProgress);
        tvXP = findViewById(R.id.tvXP);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        tvTranscript = findViewById(R.id.tvTranscript);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        seekBar = findViewById(R.id.seekBar);
        rvQuestions = findViewById(R.id.rvQuestions);
        btnShowTranscript = findViewById(R.id.btnShowTranscript);
        btnNext = findViewById(R.id.btnNext);
        scrollTranscript = findViewById(R.id.scrollTranscript);

        rvQuestions.setLayoutManager(new LinearLayoutManager(this));
        rvQuestions.setNestedScrollingEnabled(false); // Enable smooth scrolling inside NestedScrollView

        btnPlayPause.setOnClickListener(v -> togglePlayback());
        btnShowTranscript.setOnClickListener(v -> {
            scrollTranscript.setVisibility(View.VISIBLE);
            tvTranscript.setText(passageList.get(currentPassageIndex).getTranscript());
            adapter.setFinished(true);
        });

        btnNext.setOnClickListener(v -> {
            if (currentPassageIndex < passageList.size() - 1) {
                currentPassageIndex++;
                displayPassage(currentPassageIndex);
            } else {
                Toast.makeText(this, "Chúc mừng! Bạn đã hoàn thành bộ câu hỏi này.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

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

    private void loadPart3JSON() {
        try {
            InputStream is = getAssets().open("listening_p3.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            Gson gson = new Gson();
            Type listType = new TypeToken<List<Part3Passage>>() {}.getType();
            List<Part3Passage> allPassages = gson.fromJson(json, listType);

            int start = (setIndex < 4) ? (setIndex * 4) : (16 + (setIndex - 4) * 6);
            int count = (setIndex < 4) ? 4 : 6;

            passageList = new ArrayList<>();
            for (int i = start; i < start + count && i < allPassages.size(); i++) {
                passageList.add(allPassages.get(i));
            }

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayPassage(int index) {
        if (passageList == null || index >= passageList.size()) return;

        Part3Passage passage = passageList.get(index);
        tvProgress.setText("Đoạn " + (index + 1) + "/" + passageList.size());
        
        btnNext.setVisibility(View.GONE);
        btnShowTranscript.setVisibility(View.GONE);
        scrollTranscript.setVisibility(View.GONE);
        resetMediaPlayer(passage.getAudioName());

        adapter = new Part3Adapter(this, passage.getQuestions(), new Part3Adapter.OnAllQuestionsAnsweredListener() {
            @Override
            public void onAnswered(int position, boolean isCorrect) {
                if (isCorrect) {
                    currentXP += 10;
                    tvXP.setText(currentXP + " XP");
                }
            }

            @Override
            public void onAllAnswered() {
                btnShowTranscript.setVisibility(View.VISIBLE);
                btnNext.setVisibility(View.VISIBLE);
            }
        });
        rvQuestions.setAdapter(adapter);
    }

    private void resetMediaPlayer(String audioName) {
        if (mediaPlayer != null) {
            handler.removeCallbacks(updater);
            try { mediaPlayer.stop(); } catch (Exception e) {}
            mediaPlayer.release();
            mediaPlayer = null;
        }
        isPrepared = false;

        mediaPlayer = new MediaPlayer();
        
        // Show loading state
        btnPlayPause.setEnabled(false);
        tvTotalTime.setText("--:--");
        tvCurrentTime.setText("00:00");
        seekBar.setProgress(0);
        
        try {
            String relativePath = AppConfig.AUDIO_P3_FOLDER + "/" + audioName;
            File localFile = AppConfig.getLocalAssetFile(this, relativePath);
            
            if (localFile.exists()) {
                mediaPlayer.setDataSource(localFile.getAbsolutePath());
            } else {
                try {
                    AssetFileDescriptor afd = getAssets().openFd("audio_p3/" + audioName);
                    mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                    afd.close();
                } catch (IOException assetError) {
                    String url = AppConfig.getFirebaseUrl(relativePath);
                    mediaPlayer.setDataSource(url);
                }
            }

            mediaPlayer.setOnPreparedListener(mp -> {
                isPrepared = true;
                btnPlayPause.setEnabled(true);
                tvTotalTime.setText(formatTime(mp.getDuration()));
                tvCurrentTime.setText("00:00");
                seekBar.setMax(mp.getDuration());
                seekBar.setProgress(0);
                btnPlayPause.setImageResource(R.drawable.ic_play);
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                btnPlayPause.setImageResource(R.drawable.ic_play);
                handler.removeCallbacks(updater);
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Toast.makeText(this, "Không thể phát audio. Vui lòng kiểm tra kết nối mạng.", Toast.LENGTH_SHORT).show();
                btnPlayPause.setEnabled(false);
                return true;
            });

            mediaPlayer.prepareAsync();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Audio file not found: " + audioName, Toast.LENGTH_SHORT).show();
        }
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

    private String formatTime(int milliseconds) {
        int minutes = (milliseconds / 1000) / 60;
        int seconds = (milliseconds / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            btnPlayPause.setImageResource(R.drawable.ic_play);
        }
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
