package com.example.waviapp.activities;

import android.content.res.AssetFileDescriptor;
import android.content.res.ColorStateList;
import android.graphics.Color;
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

import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.NestedScrollView;

import com.example.waviapp.R;
import com.example.waviapp.models.Part2Model;
import com.example.waviapp.utils.JsonHelper;
import com.example.waviapp.utils.ProgressManager;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Part2PracticeActivity extends BaseActivity {

    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;
    private Handler handler = new Handler();
    private List<Part2Model> setList;
    private int currentIndex = 0;
    private int correctCount = 0;
    private int setIndex;
    private boolean isAnswered = false;
    private ProgressManager progressManager;

    private TextView tvProgress, tvCurrentTime, tvTotalTime, tvScript;
    private ImageButton btnPlayPause;
    private ImageView ivSpeakerIcon;
    private MaterialButton btnA, btnB, btnC, btnNext, btnShowScript;
    private View cardScript;
    private NestedScrollView nestedScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_part2_practice);

        progressManager = new ProgressManager(this);
        setIndex = getIntent().getIntExtra("SET_INDEX", 0);

        initViews();
        loadData();
        setupQuestion();
    }

    private void initViews() {
        tvProgress = findViewById(R.id.tvProgress);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        tvScript = findViewById(R.id.tvScript);
        seekBar = findViewById(R.id.seekBar);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        ivSpeakerIcon = findViewById(R.id.ivSpeakerIcon);
        btnA = findViewById(R.id.btnAnsA);
        btnB = findViewById(R.id.btnAnsB);
        btnC = findViewById(R.id.btnAnsC);
        btnNext = findViewById(R.id.btnNext);
        btnShowScript = findViewById(R.id.btnShowScript);
        cardScript = findViewById(R.id.cardScript);
        nestedScrollView = findViewById(R.id.nestedScrollView);

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());
        
        // Cả nút Play và Icon Loa đều có thể điều khiển nhạc
        btnPlayPause.setOnClickListener(v -> togglePlay());
        ivSpeakerIcon.setOnClickListener(v -> togglePlay());
        
        btnA.setOnClickListener(v -> checkAnswer("A", btnA));
        btnB.setOnClickListener(v -> checkAnswer("B", btnB));
        btnC.setOnClickListener(v -> checkAnswer("C", btnC));
        
        btnNext.setOnClickListener(v -> nextQuestion());
        
        btnShowScript.setOnClickListener(v -> {
            cardScript.setVisibility(View.VISIBLE);
            // Cuộn xuống để người dùng thấy Script ngay lập tức
            handler.postDelayed(() -> {
                nestedScrollView.smoothScrollTo(0, cardScript.getBottom());
            }, 100);
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                    tvCurrentTime.setText(formatTime(progress));
                }
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });
    }

    private void loadData() {
        List<Part2Model> fullList = JsonHelper.loadPart2Data(this);
        setList = new ArrayList<>();
        int start = setIndex * 6;
        for (int i = start; i < start + 6 && i < fullList.size(); i++) {
            setList.add(fullList.get(i));
        }
        
        if (setList.isEmpty()) {
            Toast.makeText(this, "Không có dữ liệu cho Set này", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupQuestion() {
        isAnswered = false;
        Part2Model current = setList.get(currentIndex);
        
        tvProgress.setText("Câu " + (currentIndex + 1) + "/6");
        tvScript.setText(current.getScript() + "\n\n💡 " + current.getExplanation());
        
        resetButtons();
        releaseMediaPlayer();
        initMediaPlayer(current.getAudioName());
        
        cardScript.setVisibility(View.GONE);
        btnNext.setVisibility(View.INVISIBLE);
        btnShowScript.setVisibility(View.INVISIBLE);
        
        // Cuộn lên đầu trang khi sang câu mới
        nestedScrollView.scrollTo(0, 0);
    }

    private void initMediaPlayer(String fileName) {
        mediaPlayer = new MediaPlayer();
        try {
            AssetFileDescriptor afd = getAssets().openFd("audio_p2/" + fileName);
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            mediaPlayer.prepare();
            
            seekBar.setMax(mediaPlayer.getDuration());
            seekBar.setProgress(0);
            tvTotalTime.setText(formatTime(mediaPlayer.getDuration()));
            tvCurrentTime.setText("00:00");
            btnPlayPause.setImageResource(R.drawable.ic_play);

            mediaPlayer.setOnCompletionListener(mp -> {
                btnPlayPause.setImageResource(R.drawable.ic_play);
                handler.removeCallbacks(updater);
                seekBar.setProgress(0);
                tvCurrentTime.setText("00:00");
            });

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi tải file âm thanh", Toast.LENGTH_SHORT).show();
        }
    }

    private void togglePlay() {
        if (mediaPlayer == null) return;
        
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            // Hiện hình tam giác (Play) khi dừng
            btnPlayPause.setImageResource(R.drawable.ic_play);
            handler.removeCallbacks(updater);
        } else {
            mediaPlayer.start();
            // Hiện hai gạch (Pause) khi đang phát
            btnPlayPause.setImageResource(R.drawable.ic_pause);
            handler.post(updater);
        }
    }

    private Runnable updater = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                int current = mediaPlayer.getCurrentPosition();
                seekBar.setProgress(current);
                tvCurrentTime.setText(formatTime(current));
                handler.postDelayed(this, 100);
            }
        }
    };

    private void checkAnswer(String selected, MaterialButton btn) {
        if (isAnswered) return;
        isAnswered = true;
        
        String correct = setList.get(currentIndex).getCorrectAnswer();
        if (selected.equals(correct)) {
            // Đúng: Đổi màu nền sang xanh solid
            btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
            btn.setTextColor(Color.WHITE);
            btn.setStrokeWidth(0);
            correctCount++;
            progressManager.addXP(10);
        } else {
            // Sai: Nút chọn thành đỏ
            btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F44336")));
            btn.setTextColor(Color.WHITE);
            btn.setStrokeWidth(0);
            // Hiện đáp án đúng bằng màu nền xanh solid
            highlightCorrect(correct);
        }
        
        btnNext.setVisibility(View.VISIBLE);
        btnShowScript.setVisibility(View.VISIBLE);
    }

    private void highlightCorrect(String correct) {
        MaterialButton correctBtn = null;
        if (correct.equals("A")) correctBtn = btnA;
        else if (correct.equals("B")) correctBtn = btnB;
        else if (correct.equals("C")) correctBtn = btnC;

        if (correctBtn != null) {
            // Ép màu nền xanh solid cho đáp án đúng
            correctBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
            correctBtn.setTextColor(Color.WHITE);
            correctBtn.setStrokeWidth(0);
        }
    }

    private void nextQuestion() {
        if (currentIndex < setList.size() - 1) {
            currentIndex++;
            setupQuestion();
        } else {
            showSummary();
        }
    }

    private void showSummary() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_summary_celebration, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create();
        
        TextView tvCorrect = view.findViewById(R.id.tvCorrectCount);
        TextView tvXP = view.findViewById(R.id.tvXPCount);
        MaterialButton btnFinish = view.findViewById(R.id.btnFinish);

        tvCorrect.setText(correctCount + "/6");
        tvXP.setText("+" + (correctCount * 10) + " XP");
        
        btnFinish.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });
        dialog.show();
    }

    private String formatTime(int ms) {
        int sec = ms / 1000;
        int min = sec / 60;
        sec = sec % 60;
        return String.format("%02d:%02d", min, sec);
    }

    private void resetButtons() {
        MaterialButton[] btns = {btnA, btnB, btnC};
        for (MaterialButton b : btns) {
            b.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            b.setTextColor(Color.BLACK);
            b.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#E0E0E0")));
            b.setStrokeWidth(2);
        }
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            handler.removeCallbacks(updater);
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            togglePlay();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
    }
}
