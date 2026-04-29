package com.example.waviapp.activities;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waviapp.R;
import com.example.waviapp.adapters.Part34Adapter;
import com.example.waviapp.models.Part3Passage;
import com.example.waviapp.utils.ProgressManager;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Part34Activity extends BaseActivity {

    private int partType;
    private int setIndex;
    private List<Part3Passage> passageList = new ArrayList<>();
    private int currentPassageIndex = 0;
    private int totalXP = 0;

    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Part34Adapter adapter;
    private ProgressManager progressManager;

    private TextView tvToolbarTitle, tvXP, tvCurrentTime, tvTotalTime, tvTranscript, tvProgress;
    private ImageButton btnPlayPause;
    private SeekBar seekBar;
    private RecyclerView rvQuestions;
    private MaterialButton btnCheck, btnNext;
    private View cardScript;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_part34);

        progressManager = new ProgressManager(this);
        partType = getIntent().getIntExtra("PART_TYPE", 3);
        setIndex = getIntent().getIntExtra("SET_INDEX", 0);

        initViews();
        loadData();
        displayPassage();
    }

    private void initViews() {
        tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        tvToolbarTitle.setText("Listening Part " + partType);
        
        tvXP = findViewById(R.id.tvXP);
        tvProgress = findViewById(R.id.tvProgress);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        tvTranscript = findViewById(R.id.tvTranscript);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        seekBar = findViewById(R.id.seekBar);
        rvQuestions = findViewById(R.id.rvQuestions);
        btnCheck = findViewById(R.id.btnCheck);
        btnNext = findViewById(R.id.btnNext);
        cardScript = findViewById(R.id.cardScript);

        rvQuestions.setLayoutManager(new LinearLayoutManager(this));

        btnPlayPause.setOnClickListener(v -> togglePlayback());
        btnCheck.setOnClickListener(v -> checkAnswers());
        btnNext.setOnClickListener(v -> nextPassage());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                    tvCurrentTime.setText(formatTime(progress));
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void loadData() {
        String fileName = (partType == 3) ? "listening_p3.json" : "listening_p4.json";
        try {
            InputStream is = getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            Gson gson = new Gson();
            List<Part3Passage> all = gson.fromJson(json, new TypeToken<List<Part3Passage>>(){}.getType());
            
            if (all != null) {
                int start = setIndex * 5;
                for (int i = start; i < start + 5 && i < all.size(); i++) {
                    passageList.add(all.get(i));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayPassage() {
        if (passageList == null || passageList.isEmpty() || currentPassageIndex >= passageList.size()) {
            Toast.makeText(this, "No data found for this set", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Part3Passage passage = passageList.get(currentPassageIndex);
        tvProgress.setText("Passage " + (currentPassageIndex + 1) + "/" + passageList.size());

        resetUI();
        prepareAudio(passage.getAudioName());

        adapter = new Part34Adapter(this, passage.getQuestions(), () -> {
            btnCheck.setEnabled(true);
            btnCheck.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#9370DB")));
        });
        rvQuestions.setAdapter(adapter);
    }

    private void prepareAudio(String audioName) {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.release();
            } catch (Exception e) {}
        }
        mediaPlayer = new MediaPlayer();

        try {
            String folder = (partType == 3) ? "audio_p3/" : "audio_p4/";
            AssetFileDescriptor afd = getAssets().openFd(folder + audioName);
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            mediaPlayer.prepare();
            
            tvTotalTime.setText(formatTime(mediaPlayer.getDuration()));
            seekBar.setMax(mediaPlayer.getDuration());
            seekBar.setProgress(0);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Audio file not found: " + audioName, Toast.LENGTH_SHORT).show();
        }
    }

    private void checkAnswers() {
        if (adapter != null) {
            adapter.setRevealResults(true);
            int correctCount = adapter.getCorrectAnswersCount();
            int earnedXP = correctCount * 10;
            totalXP += earnedXP;
            
            tvXP.setText(totalXP + " XP");
            cardScript.setVisibility(View.VISIBLE);
            tvTranscript.setText(passageList.get(currentPassageIndex).getTranscript());
            
            btnCheck.setVisibility(View.GONE);
            btnNext.setVisibility(View.VISIBLE);
        }
    }

    private void nextPassage() {
        if (currentPassageIndex < passageList.size() - 1) {
            currentPassageIndex++;
            displayPassage();
        } else {
            if (progressManager != null) {
                progressManager.addXP(totalXP);
            }
            Toast.makeText(this, "Set completed! +" + totalXP + " XP", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void togglePlayback() {
        if (mediaPlayer == null) return;
        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                btnPlayPause.setImageResource(R.drawable.ic_play);
                handler.removeCallbacks(updater);
            } else {
                mediaPlayer.start();
                btnPlayPause.setImageResource(R.drawable.ic_pause);
                handler.post(updater);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Runnable updater = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null) {
                try {
                    if (mediaPlayer.isPlaying()) {
                        seekBar.setProgress(mediaPlayer.getCurrentPosition());
                        tvCurrentTime.setText(formatTime(mediaPlayer.getCurrentPosition()));
                        handler.postDelayed(this, 100);
                    }
                } catch (Exception e) {}
            }
        }
    };

    private void resetUI() {
        btnCheck.setVisibility(View.VISIBLE);
        btnCheck.setEnabled(false);
        btnCheck.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#BDBDBD")));
        btnNext.setVisibility(View.GONE);
        cardScript.setVisibility(View.GONE);
        btnPlayPause.setImageResource(R.drawable.ic_play);
        tvCurrentTime.setText("00:00");
    }

    private String formatTime(int ms) {
        int m = (ms / 1000) / 60;
        int s = (ms / 1000) % 60;
        return String.format("%02d:%02d", m, s);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    btnPlayPause.setImageResource(R.drawable.ic_play);
                }
            } catch (Exception e) {}
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
            } catch (Exception e) {}
            mediaPlayer = null;
        }
        handler.removeCallbacks(updater);
    }
}
