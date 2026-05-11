package com.example.waviapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.waviapp.R;
import com.example.waviapp.firebase.DatabaseHelper;
import com.example.waviapp.utils.ProgressManager;
import com.github.jinatonic.confetti.CommonConfetti;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MockResultActivity extends BaseActivity {

    private ProgressBar progressBar;
    private ImageView ivSuccess;
    private TextView tvStatus, tvProgress;
    private MaterialButton btnHome;

    private ArrayList<String> audioPaths;
    private ArrayList<String> writingAnswers;
    private List<String> uploadedUrls = new ArrayList<>();
    
    private FirebaseStorage storage;
    private FirebaseFirestore db;
    private FirebaseUser user;
    
    private int uploadedCount = 0;
    private String examSessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mock_result);

        audioPaths = getIntent().getStringArrayListExtra("audioPaths");
        writingAnswers = getIntent().getStringArrayListExtra("writingAnswers");

        if (audioPaths == null) audioPaths = new ArrayList<>();
        if (writingAnswers == null) writingAnswers = new ArrayList<>();

        progressBar = findViewById(R.id.progressBar);
        ivSuccess = findViewById(R.id.ivSuccess);
        tvStatus = findViewById(R.id.tvStatus);
        tvProgress = findViewById(R.id.tvProgress);
        btnHome = findViewById(R.id.btnHome);

        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        examSessionId = UUID.randomUUID().toString();

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(MockResultActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        startUploadProcess();
    }

    private void startUploadProcess() {
        if (user == null || audioPaths.isEmpty()) {
            saveToFirestore();
            return;
        }

        uploadNextAudio(0);
    }

    private void uploadNextAudio(int index) {
        if (index >= audioPaths.size()) {
            saveToFirestore();
            return;
        }

        String localPath = audioPaths.get(index);
        File audioFile = new File(localPath);
        if (!audioFile.exists()) {
            uploadedUrls.add("");
            uploadNextAudio(index + 1);
            return;
        }

        String storagePath = "mock_exams/" + user.getUid() + "/" + examSessionId + "/speaking_q" + (index + 1) + ".m4a";
        StorageReference ref = storage.getReference().child(storagePath);

        UploadTask uploadTask = ref.putFile(Uri.fromFile(audioFile));
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            ref.getDownloadUrl().addOnSuccessListener(uri -> {
                uploadedUrls.add(uri.toString());
                uploadedCount++;
                updateProgressUI();
                audioFile.delete(); // Xóa file rác
                uploadNextAudio(index + 1);
            }).addOnFailureListener(e -> {
                uploadedUrls.add("gs://" + ref.getBucket() + "/" + storagePath);
                uploadedCount++;
                updateProgressUI();
                audioFile.delete();
                uploadNextAudio(index + 1);
            });
        }).addOnFailureListener(e -> {
            uploadedUrls.add("");
            uploadedCount++;
            updateProgressUI();
            uploadNextAudio(index + 1);
        });
    }

    private void updateProgressUI() {
        tvProgress.setText("Đã tải lên " + uploadedCount + "/" + audioPaths.size() + " file ghi âm");
    }

    private void saveToFirestore() {
        tvStatus.setText("Đang lưu kết quả...");
        tvProgress.setVisibility(View.GONE);

        if (user == null) {
            showSuccess();
            return;
        }

        Map<String, Object> examData = new HashMap<>();
        examData.put("timestamp", System.currentTimeMillis());
        examData.put("type", "MockSpeakWrite");
        examData.put("speakingUrls", uploadedUrls);
        examData.put("writingAnswers", writingAnswers);

        db.collection("users").document(user.getUid())
          .collection("mock_history").document(examSessionId)
          .set(examData)
          .addOnSuccessListener(aVoid -> {
              ProgressManager pm = new ProgressManager(this);
              pm.addXP(100); // 100 XP cho full mock test
              showSuccess();
          })
          .addOnFailureListener(e -> {
              Toast.makeText(this, "Lỗi khi lưu kết quả lên server", Toast.LENGTH_SHORT).show();
              showSuccess(); // Vẫn cho thành công vì đã upload audio
          });
    }

    private void showSuccess() {
        progressBar.setVisibility(View.GONE);
        ivSuccess.setVisibility(View.VISIBLE);
        tvStatus.setText("Nộp bài thi thành công!\nBạn nhận được 100 XP.");
        tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
        btnHome.setVisibility(View.VISIBLE);

        CommonConfetti.rainingConfetti(
            findViewById(android.R.id.content),
            new int[]{getResources().getColor(R.color.purple_main), android.graphics.Color.WHITE, android.graphics.Color.YELLOW}
        ).oneShot();
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Đang nộp bài, vui lòng không thoát...", Toast.LENGTH_SHORT).show();
    }
}
