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
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waviapp.R;
import com.example.waviapp.adapters.AnswerSheetAdapter;
import com.example.waviapp.adapters.PdfAdapter;
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

public class EtsTestActivity extends AppCompatActivity {

    public static final String EXTRA_ETS_ID = "extra_ets_id"; 
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ets_test);

        if (getIntent().hasExtra(EXTRA_ETS_ID)) {
            etsId = getIntent().getStringExtra(EXTRA_ETS_ID);
        }

        initViews();
        setupAudio();
        setupTabs();
        loadAllAnswers();
        setupAnswerSheet();
        
        // Load mặc định trang LC
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

    private void loadPdfWithFallback(String suffix) {
        String[] possibleNames = {
            etsId + suffix,
            etsId.replace("ets", "est") + suffix
        };
        
        boolean success = false;
        for (String name : possibleNames) {
            if (tryLoadPdf(etsId + "/" + name)) {
                success = true;
                break;
            }
        }
        
        if (!success) {
            Toast.makeText(this, "Không tìm thấy file PDF: " + suffix, Toast.LENGTH_SHORT).show();
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
        String estId = etsId.replace("ets", "est");
        
        String[] possiblePaths = {
            etsId + "/" + etsId + "_full_audio.mp3",
            etsId + "/" + estId + "_full_audio.mp3",
            etsId + "/" + estId + "_full_audio.mp3.mp3", // Sửa lỗi double extension
            etsId + "/" + etsId + "_audio.mp3"
        };

        boolean loaded = false;
        for (String path : possiblePaths) {
            try {
                AssetFileDescriptor afd = getAssets().openFd(path);
                mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                mediaPlayer.prepare();
                afd.close();
                loaded = true;
                Log.d(TAG, "Loaded audio: " + path);
                break;
            } catch (IOException ignored) {}
        }

        if (!loaded) {
            Toast.makeText(this, "Không thể tải file Audio", Toast.LENGTH_SHORT).show();
        }

        btnPlay.setOnClickListener(v -> {
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
            seekBar.setMax(mediaPlayer.getDuration());
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(SeekBar sb, int p, boolean b) { if (b) mediaPlayer.seekTo(p); }
                @Override public void onStartTrackingTouch(SeekBar sb) {}
                @Override public void onStopTrackingTouch(SeekBar sb) {}
            });
        }
    }

    private void updateSeekBar() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
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
        
        // Thử tìm LC
        String[] lcExtensions = {"_lc_answers.json", "_lc_aswers.json"};
        boolean lcFound = false;
        for (String id : new String[]{etsId, estId, "ets01", "ets02"}) { // Thử cả ID gốc nếu copy nhầm
            for (String ext : lcExtensions) {
                if (loadJsonArrayToMap(etsId + "/" + id + ext)) {
                    lcFound = true; break;
                }
            }
            if (lcFound) break;
        }

        // Thử tìm RC
        String[] rcExtensions = {"_rc_answers.json", "_rc_aswers.json"};
        boolean rcFound = false;
        for (String id : new String[]{etsId, estId, "ets01", "ets02"}) {
            for (String ext : rcExtensions) {
                if (loadJsonArrayToMap(etsId + "/" + id + ext)) {
                    rcFound = true; break;
                }
            }
            if (rcFound) break;
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
                int qNum = obj.getInt("q");
                String ans = obj.getString("ans");
                correctAnswers.put(qNum, ans);
            }
            Log.d(TAG, "Loaded " + jsonArray.length() + " answers from " + path);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void setupAnswerSheet() {
        answerAdapter = new AnswerSheetAdapter(200, userAnswers);
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
