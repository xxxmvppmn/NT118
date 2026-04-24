package com.example.waviapp.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.waviapp.R;
import com.example.waviapp.firebase.DatabaseHelper;
import com.example.waviapp.models.SpeakQuestion;
import com.example.waviapp.utils.ProgressManager;
import com.example.waviapp.utils.SkillDataProvider;
import com.github.jinatonic.confetti.CommonConfetti;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Màn hình luyện nói Speaking.
 * Chức năng:
 *  - Hiển thị prompt từng câu với instruction
 *  - Ghi âm giọng người dùng bằng MediaRecorder → file .m4a tạm
 *  - Upload file lên Firebase Storage: speaking/{userId}/part{n}_q{n}.m4a
 *  - Lưu download URL + metadata vào Firestore (user-specific, an toàn khi đổi account)
 *  - Phát lại bản ghi bằng MediaPlayer (từ file tạm local)
 *  - TTS đọc sample answer
 *  - Timer đếm ngược cho prep + response
 */
public class SpeakPracticeActivity extends BaseActivity {

    private static final String TAG = "SpeakPractice";
    private static final int REQUEST_RECORD_AUDIO = 200;

    // Views
    private TextView tvToolbarTitle, tvProgressText, tvInstruction, tvPrompt;
    private TextView tvPrepTimer, tvResponseTimer, tvRecordingStatus;
    private ProgressBar progressBar;
    private FloatingActionButton fabRecord;
    private MaterialButton btnPlaySample, btnPlayback, btnReRecord, btnNext;
    private LinearLayout llWaveform, llPlayback;

    // Data
    private List<SpeakQuestion> questions;
    private int currentIndex = 0;
    private int partIndex = 0;

    // Recording
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private boolean isRecording = false;
    private String currentRecordingPath;  // local temp path
    private boolean isUploading = false;

    // TTS
    private TextToSpeech tts;
    private boolean ttsReady = false;

    // Timer
    private CountDownTimer prepTimer, responseTimer;
    private boolean prepDone = false;

    // Firebase
    private ProgressManager progressManager;
    private DatabaseHelper dbHelper;
    private FirebaseStorage storage;
    private int completedCount = 0;

    // Waveform animation
    private Handler waveHandler = new Handler();
    private Runnable waveRunnable;
    private java.util.Random random = new java.util.Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speak_practice);

        partIndex = getIntent().getIntExtra(SpeakSetActivity.EXTRA_PART_INDEX, 0);
        progressManager = new ProgressManager(this);
        dbHelper = new DatabaseHelper();
        storage = FirebaseStorage.getInstance();

        initViews();
        initTTS();
        loadData();
        checkAudioPermission();
        displayQuestion(currentIndex);
    }

    private void initViews() {
        tvToolbarTitle    = findViewById(R.id.tvToolbarTitle);
        tvProgressText    = findViewById(R.id.tvProgressText);
        tvInstruction     = findViewById(R.id.tvInstruction);
        tvPrompt          = findViewById(R.id.tvPrompt);
        tvPrepTimer       = findViewById(R.id.tvPrepTimer);
        tvResponseTimer   = findViewById(R.id.tvResponseTimer);
        tvRecordingStatus = findViewById(R.id.tvRecordingStatus);
        progressBar       = findViewById(R.id.progressBar);
        fabRecord         = findViewById(R.id.fabRecord);
        btnPlaySample     = findViewById(R.id.btnPlaySample);
        btnPlayback       = findViewById(R.id.btnPlayback);
        btnReRecord       = findViewById(R.id.btnReRecord);
        btnNext           = findViewById(R.id.btnNext);
        llWaveform        = findViewById(R.id.llWaveform);
        llPlayback        = findViewById(R.id.llPlayback);

        ImageView ivBack = findViewById(R.id.ivBack);
        if (ivBack != null) ivBack.setOnClickListener(v -> onBackPressed());

        String[] partTitles = {
            "Part 1 - Đọc văn bản",
            "Part 2 - Mô tả tranh",
            "Part 3 - Trả lời câu hỏi",
            "Part 4 - Trả lời câu hỏi",
            "Part 5 - Đề xuất giải pháp",
            "Part 6 - Thể hiện quan điểm"
        };
        if (tvToolbarTitle != null) {
            tvToolbarTitle.setText(partIndex < partTitles.length ? partTitles[partIndex] : "Speaking");
        }

        fabRecord.setOnClickListener(v -> handleRecordClick());
        btnPlaySample.setOnClickListener(v -> playSample());
        btnPlayback.setOnClickListener(v -> playbackRecording());
        btnReRecord.setOnClickListener(v -> resetToRecord());
        btnNext.setOnClickListener(v -> goNext());
    }

    private void initTTS() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.ENGLISH);
                ttsReady = (result != TextToSpeech.LANG_MISSING_DATA
                        && result != TextToSpeech.LANG_NOT_SUPPORTED);
                if (!ttsReady) {
                    Log.w(TAG, "TTS: language not supported");
                    runOnUiThread(() -> btnPlaySample.setEnabled(false));
                }
            } else {
                Log.e(TAG, "TTS init failed");
            }
        });
    }

    private void loadData() {
        questions = SkillDataProvider.getSpeakQuestions(partIndex);
        progressBar.setMax(questions.size());
    }

    private void checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Cần quyền microphone để ghi âm!", Toast.LENGTH_LONG).show();
                fabRecord.setEnabled(false);
            }
        }
    }

    // ─── Display ───

    private void displayQuestion(int idx) {
        if (idx >= questions.size()) return;
        SpeakQuestion q = questions.get(idx);

        tvInstruction.setText(q.getInstruction());
        tvPrompt.setText(q.getPrompt());
        tvProgressText.setText((idx + 1) + "/" + questions.size());
        progressBar.setProgress(idx + 1);

        resetToRecord();
        cancelTimers();
        prepDone = false;

        tvPrepTimer.setText(formatTime(q.getPrepTimeSec()));
        tvResponseTimer.setText(formatTime(q.getResponseTimeSec()));

        startPrepTimer(q.getPrepTimeSec(), q.getResponseTimeSec());

        llPlayback.setVisibility(View.GONE);
        btnNext.setText(idx == questions.size() - 1 ? "Hoàn thành ✓" : "Câu tiếp theo →");
    }

    // ─── Timers ───

    private void startPrepTimer(int prepSec, int responseSec) {
        prepTimer = new CountDownTimer(prepSec * 1000L, 1000) {
            @Override public void onTick(long ms) {
                tvPrepTimer.setText(formatTime((int)(ms / 1000)));
            }
            @Override public void onFinish() {
                tvPrepTimer.setText("0:00");
                prepDone = true;
                if (ContextCompat.checkSelfPermission(SpeakPracticeActivity.this,
                        Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    startRecording();
                    startResponseTimer(responseSec);
                }
            }
        }.start();
    }

    private void startResponseTimer(int responseSec) {
        responseTimer = new CountDownTimer(responseSec * 1000L, 1000) {
            @Override public void onTick(long ms) {
                tvResponseTimer.setText(formatTime((int)(ms / 1000)));
            }
            @Override public void onFinish() {
                tvResponseTimer.setText("0:00");
                if (isRecording) stopRecordingAndUpload();
            }
        }.start();
    }

    private void cancelTimers() {
        if (prepTimer != null) { prepTimer.cancel(); prepTimer = null; }
        if (responseTimer != null) { responseTimer.cancel(); responseTimer = null; }
    }

    private String formatTime(int totalSeconds) {
        int min = totalSeconds / 60;
        int sec = totalSeconds % 60;
        return String.format(Locale.US, "%d:%02d", min, sec);
    }

    // ─── Recording ───

    private void handleRecordClick() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            checkAudioPermission();
            return;
        }
        if (isUploading) {
            Toast.makeText(this, "Đang tải lên, vui lòng đợi...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isRecording) {
            stopRecordingAndUpload();
            cancelTimers();
        } else {
            startRecording();
            if (!prepDone) {
                cancelTimers();
                prepDone = true;
                SpeakQuestion q = questions.get(currentIndex);
                startResponseTimer(q.getResponseTimeSec());
            }
        }
    }

    private void startRecording() {
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String uid = (user != null) ? user.getUid() : "anonymous";

            // File tạm: tên chứa userId để tránh xung đột giữa các tài khoản trên cùng thiết bị
            File dir = new File(getCacheDir(), "speaking_tmp");
            if (!dir.exists()) dir.mkdirs();
            currentRecordingPath = dir.getAbsolutePath()
                    + "/" + uid + "_p" + partIndex + "_q" + currentIndex + ".m4a";

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setAudioSamplingRate(44100);
            mediaRecorder.setAudioEncodingBitRate(128000);
            mediaRecorder.setOutputFile(currentRecordingPath);
            mediaRecorder.prepare();
            mediaRecorder.start();

            isRecording = true;
            fabRecord.setImageResource(R.drawable.ic_stop);
            fabRecord.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            android.graphics.Color.parseColor("#F44336")));
            tvRecordingStatus.setText("🔴 Đang ghi âm...");
            llWaveform.setVisibility(View.VISIBLE);
            startWaveAnimation();
            Log.d(TAG, "Recording started: " + currentRecordingPath);
        } catch (IOException e) {
            Log.e(TAG, "startRecording failed", e);
            Toast.makeText(this, "Không thể ghi âm!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Dừng ghi âm, phát lại cho user nghe, sau đó upload lên Firebase Storage.
     * File local (trong getCacheDir) sẽ được xóa SAU KHI upload thành công.
     * Nếu upload thất bại, file vẫn còn để user nghe lại và sẽ thử lại.
     */
    private void stopRecordingAndUpload() {
        // 1. Dừng MediaRecorder
        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
            } catch (RuntimeException e) {
                Log.e(TAG, "stopRecording error", e);
                currentRecordingPath = null;
            }
            mediaRecorder.release();
            mediaRecorder = null;
        }
        isRecording = false;
        stopWaveAnimation();
        llWaveform.setVisibility(View.INVISIBLE);
        fabRecord.setImageResource(R.drawable.ic_mic);
        fabRecord.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#9370DB")));

        if (currentRecordingPath == null || !new File(currentRecordingPath).exists()) {
            tvRecordingStatus.setText("Nhấn nút để ghi âm");
            return;
        }

        // 2. Cho phép nghe lại ngay (từ file tạm cục bộ)
        tvRecordingStatus.setText("☁️ Đang tải lên...");
        llPlayback.setVisibility(View.VISIBLE);
        fabRecord.setEnabled(false);
        isUploading = true;

        // 3. Upload lên Firebase Storage
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // Không có user → chỉ lưu local, không upload
            onUploadSuccess(null, false);
            return;
        }

        String storagePath = "speaking/" + user.getUid()
                + "/part" + partIndex + "_q" + currentIndex + ".m4a";
        StorageReference ref = storage.getReference().child(storagePath);
        Uri fileUri = Uri.fromFile(new File(currentRecordingPath));

        UploadTask uploadTask = ref.putFile(fileUri);
        uploadTask
            .addOnProgressListener(snapshot -> {
                long transferred = snapshot.getBytesTransferred();
                long total = snapshot.getTotalByteCount();
                if (total > 0) {
                    int pct = (int)((100.0 * transferred) / total);
                    tvRecordingStatus.setText("☁️ Đang tải lên " + pct + "%...");
                }
            })
            .addOnSuccessListener(taskSnapshot -> {
                // Lấy download URL
                ref.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    onUploadSuccess(downloadUri.toString(), true);
                }).addOnFailureListener(e -> {
                    // URL lỗi nhưng file đã upload, dùng storage path thay thế
                    onUploadSuccess("gs://" + ref.getBucket() + "/" + storagePath, true);
                });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Upload failed", e);
                // Upload thất bại → vẫn cho dùng file local, ghi Firestore không có URL
                runOnUiThread(() -> {
                    tvRecordingStatus.setText("✅ Ghi âm xong (chưa lưu cloud)");
                    Toast.makeText(this, "Tải lên thất bại, sẽ thử lại sau", Toast.LENGTH_SHORT).show();
                    onUploadSuccess(null, false);
                });
            });
    }

    private void onUploadSuccess(String downloadUrl, boolean uploaded) {
        isUploading = false;
        fabRecord.setEnabled(true);
        completedCount++;
        progressManager.addXP(5);

        if (uploaded) {
            tvRecordingStatus.setText("✅ Đã lưu cloud thành công");
            // Xóa file tạm sau khi upload thành công để giải phóng bộ nhớ
            if (currentRecordingPath != null) {
                new File(currentRecordingPath).delete();
                Log.d(TAG, "Temp file deleted after upload");
            }
        } else {
            tvRecordingStatus.setText("✅ Đã ghi âm xong");
        }

        // Lưu metadata vào Firestore
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String urlToSave = (downloadUrl != null) ? downloadUrl : "";
            dbHelper.saveSpeakProgress(
                    user.getUid(), partIndex, currentIndex,
                    true, urlToSave,
                    null  // fire-and-forget
            );
        }
    }

    private void resetToRecord() {
        if (isRecording) {
            if (mediaRecorder != null) {
                try { mediaRecorder.stop(); } catch (RuntimeException ignored) {}
                mediaRecorder.release();
                mediaRecorder = null;
            }
            isRecording = false;
            stopWaveAnimation();
            llWaveform.setVisibility(View.INVISIBLE);
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (tts != null && tts.isSpeaking()) tts.stop();
        isUploading = false;
        fabRecord.setEnabled(true);
        fabRecord.setImageResource(R.drawable.ic_mic);
        fabRecord.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#9370DB")));
        tvRecordingStatus.setText("Nhấn nút bên dưới để ghi âm");
        llPlayback.setVisibility(View.GONE);
        currentRecordingPath = null;
    }

    // ─── Playback (từ file tạm local) ───

    private void playbackRecording() {
        if (isUploading) {
            Toast.makeText(this, "Đang tải lên...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentRecordingPath == null || !new File(currentRecordingPath).exists()) {
            Toast.makeText(this, "File tạm đã được xóa sau khi lưu cloud", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(currentRecordingPath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            btnPlayback.setText("⏸  Đang phát...");
            mediaPlayer.setOnCompletionListener(mp -> btnPlayback.setText("▶  Nghe lại"));
        } catch (IOException e) {
            Log.e(TAG, "playback failed", e);
            Toast.makeText(this, "Lỗi phát lại!", Toast.LENGTH_SHORT).show();
        }
    }

    // ─── TTS Sample ───

    private void playSample() {
        if (!ttsReady) {
            Toast.makeText(this, "Text-to-Speech chưa sẵn sàng", Toast.LENGTH_SHORT).show();
            return;
        }
        if (tts.isSpeaking()) {
            tts.stop();
            btnPlaySample.setText("🔊  Nghe câu trả lời mẫu");
            return;
        }
        SpeakQuestion q = questions.get(currentIndex);
        tts.setSpeechRate(0.85f);
        tts.setPitch(1.0f);
        tts.speak(q.getSampleAnswer(), TextToSpeech.QUEUE_FLUSH, null, "sample_" + currentIndex);
        btnPlaySample.setText("⏹  Dừng");
        tts.setOnUtteranceProgressListener(new android.speech.tts.UtteranceProgressListener() {
            @Override public void onStart(String utteranceId) {}
            @Override public void onDone(String utteranceId) {
                runOnUiThread(() -> btnPlaySample.setText("🔊  Nghe câu trả lời mẫu"));
            }
            @Override public void onError(String utteranceId) {
                runOnUiThread(() -> btnPlaySample.setText("🔊  Nghe câu trả lời mẫu"));
            }
        });
    }

    // ─── Navigation ───

    private void goNext() {
        if (isUploading) {
            Toast.makeText(this, "Đang tải lên, vui lòng đợi...", Toast.LENGTH_SHORT).show();
            return;
        }
        cancelTimers();
        if (isRecording) stopRecordingAndUpload();
        if (tts != null && tts.isSpeaking()) tts.stop();
        if (mediaPlayer != null) { mediaPlayer.release(); mediaPlayer = null; }

        if (currentIndex < questions.size() - 1) {
            currentIndex++;
            displayQuestion(currentIndex);
        } else {
            showCompletion();
        }
    }

    private void showCompletion() {
        CommonConfetti.rainingConfetti(
                findViewById(android.R.id.content),
                new int[]{getColor(R.color.purple_main),
                          android.graphics.Color.WHITE,
                          android.graphics.Color.YELLOW}
        ).oneShot();

        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_summary_celebration, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        TextView tvCorrect = dialogView.findViewById(R.id.tvCorrectCount);
        TextView tvXP      = dialogView.findViewById(R.id.tvXPCount);
        MaterialButton btnFinish = dialogView.findViewById(R.id.btnFinish);

        tvCorrect.setText(completedCount + "/" + questions.size() + " câu đã ghi âm");
        tvXP.setText("+" + (completedCount * 5) + " XP");
        btnFinish.setOnClickListener(v -> { dialog.dismiss(); finish(); });
        dialog.show();
    }

    // ─── Wave Animation ───

    private void startWaveAnimation() {
        int[] waveIds = {R.id.wave1, R.id.wave2, R.id.wave3,
                         R.id.wave4, R.id.wave5, R.id.wave6, R.id.wave7};
        waveRunnable = new Runnable() {
            @Override public void run() {
                if (!isRecording) return;
                for (int id : waveIds) {
                    View w = llWaveform.findViewById(id);
                    if (w == null) continue;
                    float scale = 0.3f + random.nextFloat() * 0.7f;
                    ScaleAnimation anim = new ScaleAnimation(
                            1f, 1f, 1f, scale,
                            Animation.RELATIVE_TO_SELF, 0.5f,
                            Animation.RELATIVE_TO_SELF, 1f);
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

    // ─── Lifecycle ───

    @Override
    protected void onDestroy() {
        cancelTimers();
        stopWaveAnimation();
        if (isRecording && mediaRecorder != null) {
            try { mediaRecorder.stop(); } catch (RuntimeException ignored) {}
            mediaRecorder.release();
        }
        if (mediaPlayer != null) mediaPlayer.release();
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (isUploading) {
            Toast.makeText(this, "Đang tải lên, vui lòng đợi...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isRecording) {
            new AlertDialog.Builder(this)
                    .setTitle("Thoát?")
                    .setMessage("Bạn đang ghi âm. Thoát sẽ dừng ghi âm.")
                    .setPositiveButton("Thoát", (d, w) -> { stopRecordingAndUpload(); finish(); })
                    .setNegativeButton("Tiếp tục", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }
}
