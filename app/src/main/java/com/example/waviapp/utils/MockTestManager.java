package com.example.waviapp.utils;

import android.util.Log;

import com.example.waviapp.models.SpeakQuestion;
import com.example.waviapp.models.WriteQuestion;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Singleton quản lý dữ liệu Mock Test.
 * Tải câu hỏi Speaking & Writing từ Firestore theo testId,
 * lưu tạm trong RAM để các Activity dùng chung.
 */
public class MockTestManager {

    private static final String TAG = "MockTestManager";
    private static MockTestManager instance;

    private List<SpeakQuestion> speakingQuestions = new ArrayList<>();
    private List<WriteQuestion> writingQuestions = new ArrayList<>();
    private String currentTestId;
    private boolean isLoaded = false;

    public interface LoadCallback {
        void onSuccess();
        void onFailure(String error);
    }

    private MockTestManager() {}

    public static synchronized MockTestManager getInstance() {
        if (instance == null) {
            instance = new MockTestManager();
        }
        return instance;
    }

    /**
     * Tải dữ liệu đề thi từ Firestore.
     * Collection: mock_test_details / Document: testId
     * Chứa 2 array: speaking_questions và writing_questions
     */
    public void loadTest(String testId, LoadCallback callback) {
        // Nếu đã load đúng testId này rồi thì trả về luôn
        if (isLoaded && testId.equals(currentTestId)) {
            callback.onSuccess();
            return;
        }

        currentTestId = testId;
        isLoaded = false;
        speakingQuestions.clear();
        writingQuestions.clear();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("mock_test_details").document(testId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        parseSpeakingQuestions(documentSnapshot);
                        parseWritingQuestions(documentSnapshot);
                        isLoaded = true;
                        Log.d(TAG, "Loaded test: " + testId +
                                " | Speaking: " + speakingQuestions.size() +
                                " | Writing: " + writingQuestions.size());
                        callback.onSuccess();
                    } else {
                        // Fallback: dùng dữ liệu local nếu Firestore chưa có
                        Log.w(TAG, "Test not found on Firestore, using local data.");
                        speakingQuestions = SkillDataProvider.getMockSpeakingQuestions();
                        writingQuestions = SkillDataProvider.getMockWritingQuestions();
                        isLoaded = true;
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load test: " + e.getMessage());
                    // Fallback: dùng dữ liệu local
                    speakingQuestions = SkillDataProvider.getMockSpeakingQuestions();
                    writingQuestions = SkillDataProvider.getMockWritingQuestions();
                    isLoaded = true;
                    callback.onSuccess();
                });
    }

    @SuppressWarnings("unchecked")
    private void parseSpeakingQuestions(DocumentSnapshot doc) {
        List<Map<String, Object>> rawList =
                (List<Map<String, Object>>) doc.get("speaking_questions");
        if (rawList == null) return;

        for (Map<String, Object> map : rawList) {
            SpeakQuestion q = new SpeakQuestion();
            q.setPartNumber(getInt(map, "partNumber", 1));
            q.setInstruction(getString(map, "instruction"));
            q.setPrompt(getString(map, "prompt"));
            q.setSampleAnswer(getString(map, "sampleAnswer"));
            q.setPrepTimeSec(getInt(map, "prepTimeSec", 45));
            q.setResponseTimeSec(getInt(map, "responseTimeSec", 45));
            q.setReadAloud(getBool(map, "isReadAloud"));
            q.setImageUrl(getString(map, "imageUrl"));
            speakingQuestions.add(q);
        }
    }

    @SuppressWarnings("unchecked")
    private void parseWritingQuestions(DocumentSnapshot doc) {
        List<Map<String, Object>> rawList =
                (List<Map<String, Object>>) doc.get("writing_questions");
        if (rawList == null) return;

        for (Map<String, Object> map : rawList) {
            WriteQuestion q = new WriteQuestion();
            q.setPartNumber(getInt(map, "partNumber", 1));
            q.setTaskType(getString(map, "taskType"));
            q.setInstruction(getString(map, "instruction"));
            q.setPrompt(getString(map, "prompt"));
            q.setKeyword1(getString(map, "keyword1"));
            q.setKeyword2(getString(map, "keyword2"));
            q.setSampleAnswer(getString(map, "sampleAnswer"));
            q.setMinWords(getInt(map, "minWords", 5));
            q.setMaxWords(getInt(map, "maxWords", 300));
            q.setImageUrl(getString(map, "imageUrl"));
            writingQuestions.add(q);
        }
    }

    // --- Helper methods ---
    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }

    private int getInt(Map<String, Object> map, String key, int defaultVal) {
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).intValue();
        return defaultVal;
    }

    private boolean getBool(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Boolean) return (Boolean) val;
        return false;
    }

    // --- Getters ---
    public List<SpeakQuestion> getSpeakingQuestions() { return speakingQuestions; }
    public List<WriteQuestion> getWritingQuestions() { return writingQuestions; }
    public String getCurrentTestId() { return currentTestId; }
    public boolean isLoaded() { return isLoaded; }

    /** Xoá dữ liệu tạm khi kết thúc bài thi */
    public void clear() {
        speakingQuestions.clear();
        writingQuestions.clear();
        currentTestId = null;
        isLoaded = false;
    }
}
