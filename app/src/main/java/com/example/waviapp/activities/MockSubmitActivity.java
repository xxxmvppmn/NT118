package com.example.waviapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.waviapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MockSubmitActivity extends AppCompatActivity {

    private TextView tvSubmitStatus;
    private int uploadedCount = 0;
    private List<String> audioUrls = new ArrayList<>();
    private String testId;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mock_submit);

        tvSubmitStatus = findViewById(R.id.tvSubmitStatus);
        
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            userId = "guest_" + System.currentTimeMillis();
        }
        
        testId = UUID.randomUUID().toString();

        uploadAudioFiles();
    }

    private void uploadAudioFiles() {
        // We have 11 speaking questions.
        // Files are saved as mock_speak_q1.3gp to mock_speak_q11.3gp in external cache
        
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("mock_tests_audio").child(userId).child(testId);
        
        for (int i = 1; i <= 11; i++) {
            String fileName = "mock_speak_q" + i + ".3gp";
            File file = new File(getExternalCacheDir(), fileName);
            
            if (file.exists()) {
                Uri fileUri = Uri.fromFile(file);
                StorageReference fileRef = storageRef.child(fileName);
                
                final int finalI = i; // For sorting/tracking if needed
                fileRef.putFile(fileUri).addOnSuccessListener(taskSnapshot -> {
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Store the URL. To keep order, we might need a map or array of exact size.
                        // Let's just synchronize and count. Since they complete asynchronously, order is not guaranteed.
                        // We will store them in a map to sort later.
                        handleAudioUploadSuccess(finalI, uri.toString());
                    });
                }).addOnFailureListener(e -> {
                    handleAudioUploadSuccess(finalI, ""); // empty if failed to keep count
                });
            } else {
                handleAudioUploadSuccess(i, ""); // File not found
            }
        }
    }

    private Map<Integer, String> urlMap = new HashMap<>();

    private synchronized void handleAudioUploadSuccess(int index, String url) {
        urlMap.put(index, url);
        uploadedCount++;
        tvSubmitStatus.setText("Uploading audio files (" + uploadedCount + "/11)");
        
        if (uploadedCount == 11) {
            // Sort URLs into list
            for (int i = 1; i <= 11; i++) {
                audioUrls.add(urlMap.get(i));
            }
            saveToFirestore();
        }
    }

    private void saveToFirestore() {
        tvSubmitStatus.setText("Saving results...");
        
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        Map<String, Object> testData = new HashMap<>();
        testData.put("timestamp", System.currentTimeMillis());
        testData.put("audioUrls", audioUrls);
        testData.put("writingAnswers", MockWriteActivity.mockWriteAnswers);
        
        db.collection("mock_history").document(userId).collection("tests").document(testId)
            .set(testData)
            .addOnSuccessListener(aVoid -> {
                // Pass testId to Review
                Intent intent = new Intent(MockSubmitActivity.this, MockResultActivity.class);
                intent.putExtra("TEST_ID", testId);
                startActivity(intent);
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to save results.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MockSubmitActivity.this, MockResultActivity.class);
                startActivity(intent);
                finish();
            });
    }
    
    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Please wait for submission to finish.", Toast.LENGTH_SHORT).show();
    }
}
