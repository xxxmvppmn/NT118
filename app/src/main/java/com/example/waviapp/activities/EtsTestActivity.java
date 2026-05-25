package com.example.waviapp.activities;

import android.content.res.AssetFileDescriptor;
import android.graphics.pdf.PdfRenderer;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waviapp.R;
import com.example.waviapp.adapters.AnswerSheetAdapter;
import com.example.waviapp.adapters.PdfAdapter;
import com.example.waviapp.utils.AppConfig;
import com.example.waviapp.utils.OfflineAssetManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class EtsTestActivity extends BaseActivity {

    public static final String EXTRA_ETS_ID = "extra_ets_id"; 
    public static final String EXTRA_FOLDER_PREFIX = "extra_folder_prefix";
    private static final String TAG = "EtsTestActivity";
    private RecyclerView rvPdf;
    private RecyclerView rvAnswers;
    private TabLayout tabLayout;
    private ImageButton btnPlay;
    private SeekBar seekBar;
    private TextView tvTime;
    private MaterialButton btnSubmit;

    private MediaPlayer mediaPlayer;
    private PdfAdapter pdfAdapter;
    private AnswerSheetAdapter answerAdapter;
    private final Map<Integer, String> userAnswers = new HashMap<>();
    private final Map<Integer, String> correctAnswers = new HashMap<>();
    private final Handler handler = new Handler();
    
    private String etsId = "ets01"; 
    private String folderPrefix = "";
    private int totalQuestions = 200;
    private boolean isPrepared = false;
    private OfflineAssetManager offlineManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ets_test);

        if (getIntent().hasExtra(EXTRA_ETS_ID)) {
            etsId = getIntent().getStringExtra(EXTRA_ETS_ID);
        }
        if (getIntent().hasExtra(EXTRA_FOLDER_PREFIX)) {
            folderPrefix = getIntent().getStringExtra(EXTRA_FOLDER_PREFIX);
        }
        totalQuestions = getIntent().getIntExtra("extra_questions", 200);
        offlineManager = new OfflineAssetManager(this);

        initViews();
        setupAudio();
        setupTabs();
        loadAllAnswers();
        setupAnswerSheet();
        
        loadPdfWithFallback("_lc.pdf");
    }

    private void initViews() {
        rvPdf = findViewById(R.id.rvPdf);
        rvPdf.setLayoutManager(new LinearLayoutManager(this));
        rvAnswers = findViewById(R.id.rvAnswers);
        rvAnswers.setLayoutManager(new LinearLayoutManager(this));
        
        tabLayout = findViewById(R.id.tabLayout);
        btnPlay = findViewById(R.id.btnPlayPause);
        seekBar = findViewById(R.id.audioSeekBar);
        tvTime = findViewById(R.id.tvAudioTime);
        btnSubmit = findViewById(R.id.btnSubmit);

        tabLayout.addTab(tabLayout.newTab().setText("Listening"));
        tabLayout.addTab(tabLayout.newTab().setText("Reading"));

        btnSubmit.setOnClickListener(v -> submitExam());
    }

    private String getAssetPath(String fileName) {
        if (folderPrefix == null || folderPrefix.isEmpty()) {
            return etsId + "/" + fileName;
        } else {
            return folderPrefix + "/" + etsId + "/" + fileName;
        }
    }

    private void loadPdfWithFallback(String suffix) {
        String estId = etsId.replace("ets", "est");
        String[] possibleNames = {
            "2024_" + etsId + suffix,
            "2024_" + estId + suffix,
            etsId + suffix,
            estId + suffix
        };
        
        boolean success = false;
        for (String name : possibleNames) {
            if (tryLoadPdf(getAssetPath(name))) {
                success = true;
                break;
            }
        }
        
        if (!success) {
            Toast.makeText(this, "Không tìm thấy file PDF: " + etsId, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean tryLoadPdf(String assetPath) {
        try {
            InputStream is = getAssets().open(assetPath);
            if (pdfAdapter != null) pdfAdapter.close();
            
            File tempFile = new File(getCacheDir(), "temp_display.pdf");
            FileOutputStream fos = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = is.read(buffer)) != -1) fos.write(buffer, 0, read);
            fos.close();
            is.close();

            ParcelFileDescriptor pfd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY);
            pdfAdapter = new PdfAdapter(pfd);
            rvPdf.setAdapter(pdfAdapter);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void setupAudio() {
        mediaPlayer = new MediaPlayer();
        isPrepared = false;
        String estId = etsId.replace("ets", "est");
        
        String[] possibleFiles = {
            "2024_" + etsId + "_full_audio.mp3",
            "2024_" + estId + "_full_audio.mp3",
            etsId + "_full_audio.mp3",
            estId + "_full_audio.mp3",
            etsId + "_audio.mp3"
        };

        boolean loaded = false;
        for (String fileName : possibleFiles) {
            try {
                AssetFileDescriptor afd = getAssets().openFd(getAssetPath(fileName));
                mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();
                loaded = true;
                break;
            } catch (IOException ignored) {}
        }

        if (!loaded) {
            Toast.makeText(this, "Không thể tải file Audio cho " + etsId, Toast.LENGTH_SHORT).show();
        }

        btnPlay.setOnClickListener(v -> {
            if (!isPrepared) return;
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                btnPlay.setImageResource(android.R.drawable.ic_media_play);
            } else {
                mediaPlayer.start();
                btnPlay.setImageResource(android.R.drawable.ic_media_pause);
                updateSeekBar();
            }
        });

        if (loaded) {
            mediaPlayer.setOnPreparedListener(mp -> {
                isPrepared = true;
                seekBar.setMax(mp.getDuration());
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Toast.makeText(this, "Lỗi phát audio", Toast.LENGTH_SHORT).show();
                return true;
            });

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(SeekBar sb, int p, boolean b) { if (b && isPrepared) mediaPlayer.seekTo(p); }
                @Override public void onStartTrackingTouch(SeekBar sb) {}
                @Override public void onStopTrackingTouch(SeekBar sb) {}
            });

            mediaPlayer.prepareAsync();
        }
    }

    private void updateSeekBar() {
        if (mediaPlayer != null && isPrepared && mediaPlayer.isPlaying()) {
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            int sec = (mediaPlayer.getCurrentPosition() / 1000) % 60;
            int min = (mediaPlayer.getCurrentPosition() / 60000);
            tvTime.setText(String.format("%02d:%02d", min, sec));
            handler.postDelayed(this::updateSeekBar, 1000);
        }
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                String suffix = tab.getPosition() == 0 ? "_lc.pdf" : "_rc.pdf";
                loadPdfWithFallback(suffix);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadAllAnswers() {
        correctAnswers.clear();
        String estId = etsId.replace("ets", "est");

        String[] ids = {"2024_" + etsId, "2024_" + estId, etsId, estId};
        String[] suffixes = {"_lc_answers.json", "_lc_aswers.json", "_rc_answers.json", "_rc_aswers.json"};

        for (String id : ids) {
            for (String suffix : suffixes) {
                loadJsonArrayToMap(getAssetPath(id + suffix));
            }
        }
    }

    private boolean loadJsonArrayToMap(String path) {
        try {
            InputStream is = getAssets().open(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            is.close();
            
            JSONArray jsonArray = new JSONArray(sb.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                correctAnswers.put(obj.getInt("q"), obj.getString("ans"));
            }
            Log.d(TAG, "Loaded answers from: " + path);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void setupAnswerSheet() {
        answerAdapter = new AnswerSheetAdapter(totalQuestions, userAnswers);
        rvAnswers.setAdapter(answerAdapter);
    }

    private void submitExam() {
        int correct = 0;
        for (Map.Entry<Integer, String> entry : correctAnswers.entrySet()) {
            String userAns = userAnswers.get(entry.getKey());
            if (userAns != null && userAns.equalsIgnoreCase(entry.getValue())) correct++;
        }

        new AlertDialog.Builder(this)
                .setTitle("Kết quả bài thi")
                .setMessage("Số câu đúng: " + correct + "/" + correctAnswers.size() + "\nĐiểm dự kiến: " + (correct * 5))
                .setPositiveButton("Xem lại bài", (d, w) -> {
                    answerAdapter.setSubmitted(true, correctAnswers);
                    rvAnswers.scrollToPosition(0);
                })
                .setNegativeButton("Thoát", (d, w) -> finish())
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) mediaPlayer.release();
        if (pdfAdapter != null) pdfAdapter.close();
        handler.removeCallbacksAndMessages(null);
    }
}
