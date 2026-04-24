package com.example.waviapp.firebase;

import com.example.waviapp.models.BaiKiemTra;
import com.example.waviapp.models.NguPhap;
import com.example.waviapp.models.TaiKhoan;
import com.example.waviapp.models.TuVung;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper {
    private final FirebaseFirestore db;

    public interface UserCallback {
        void onSuccess(TaiKhoan user);
        void onFailure(String error);
    }

    public interface VocabularyCallback {
        void onSuccess(List<TuVung> words);
        void onFailure(String error);
    }

    public interface GrammarCallback {
        void onSuccess(List<NguPhap> grammarList);
        void onFailure(String error);
    }

    public interface TestCallback {
        void onSuccess(List<BaiKiemTra> tests);
        void onFailure(String error);
    }

    public interface CountCallback {
        void onSuccess(int count);
        void onFailure(String error);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onFailure(String error);
    }

    /** Callback trả về số câu/bài đã hoàn thành trong một phần */
    public interface SkillProgressCallback {
        void onSuccess(int completedCount);
        void onFailure(String error);
    }

    public DatabaseHelper() {
        db = FirebaseFirestore.getInstance();
    }

    // =================== TÀI KHOẢN (FIRESTORE) ===================

    public void saveUser(String userId, TaiKhoan user, SimpleCallback callback) {
        db.collection("taiKhoan").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    public void getUser(String userId, UserCallback callback) {
        db.collection("taiKhoan").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    try {
                        if (documentSnapshot.exists()) {
                            TaiKhoan user = documentSnapshot.toObject(TaiKhoan.class);
                            if (user != null) {
                                callback.onSuccess(user);
                            } else {
                                callback.onFailure("Không tìm thấy thông tin người dùng");
                            }
                        } else {
                            callback.onFailure("Không tìm thấy thông tin người dùng (Không tồn tại)");
                        }
                    } catch (Exception ex) {
                        callback.onFailure("Lỗi map dữ liệu: " + ex.getMessage());
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void updateUser(String userId, Map<String, Object> updates, SimpleCallback callback) {
        db.collection("taiKhoan").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    public void deleteUser(String userId, SimpleCallback callback) {
        db.collection("taiKhoan").document(userId).delete()
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }

    // =================== TỪ VỰNG ===================

    public void getVocabulary(String maCD, VocabularyCallback callback) {
        db.collection("tuVung")
                .whereEqualTo("maCD", maCD)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<TuVung> words = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        words.add(doc.toObject(TuVung.class));
                    }
                    callback.onSuccess(words);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // =================== NGỮ PHÁP ===================

    public void getGrammar(String maCD, GrammarCallback callback) {
        db.collection("nguPhap")
                .whereEqualTo("maCD", maCD)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<NguPhap> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        list.add(doc.toObject(NguPhap.class));
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // =================== BÀI KIỂM TRA ===================

    public void getTests(String loaiKiemTra, TestCallback callback) {
        db.collection("baiKiemTra")
                .whereEqualTo("loaiKiemTra", loaiKiemTra)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<BaiKiemTra> tests = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        tests.add(doc.toObject(BaiKiemTra.class));
                    }
                    callback.onSuccess(tests);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // =================== THÔNG BÁO ===================

    public void getUnreadNotificationCount(String userId, CountCallback callback) {
        db.collection("thongBao")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        int count = task.getResult().size();
                        callback.onSuccess(count);
                    } else {
                        callback.onFailure(task.getException() != null ?
                                task.getException().getMessage() : "Lỗi đếm thông báo");
                    }
                });
    }

    // =================== SKILL PROGRESS (SPEAKING & WRITING) ===================

    /**
     * Lưu kết quả một câu Speaking lên Firestore.
     *
     * Path: skillProgress/{userId}/speaking/{partIndex}_{questionIndex}
     * Fields:
     *   - partIndex     : int    - phần (0–5)
     *   - questionIndex : int    - thứ tự câu trong phần
     *   - recorded      : bool   - đã ghi âm chưa
     *   - downloadUrl   : String - Firebase Storage download URL (rỗng nếu upload thất bại)
     *   - timestamp     : long   - System.currentTimeMillis()
     */
    public void saveSpeakProgress(String userId, int partIndex, int questionIndex,
                                  boolean recorded, String downloadUrl,
                                  SimpleCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("partIndex",     partIndex);
        data.put("questionIndex", questionIndex);
        data.put("recorded",      recorded);
        data.put("downloadUrl",   downloadUrl != null ? downloadUrl : "");
        data.put("timestamp",     System.currentTimeMillis());


        db.collection("skillProgress")
                .document(userId)
                .collection("speaking")
                .document(partIndex + "_" + questionIndex)
                .set(data)
                .addOnSuccessListener(aVoid -> { if (callback != null) callback.onSuccess(); })
                .addOnFailureListener(e -> { if (callback != null) callback.onFailure(e.getMessage()); });
    }

    /**
     * Lấy số câu Speaking đã ghi âm của user trong một phần.
     */
    public void getSpeakProgress(String userId, int partIndex,
                                 SkillProgressCallback callback) {
        db.collection("skillProgress")
                .document(userId)
                .collection("speaking")
                .whereEqualTo("partIndex", partIndex)
                .get()
                .addOnSuccessListener(snapshots -> {
                    int count = 0;
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Boolean recorded = doc.getBoolean("recorded");
                        if (Boolean.TRUE.equals(recorded)) count++;
                    }
                    callback.onSuccess(count);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Lưu kết quả một bài Writing lên Firestore.
     *
     * Path: skillProgress/{userId}/writing/{partIndex}_{questionIndex}
     * Fields:
     *   - partIndex      : int    - phần (0–2)
     *   - questionIndex  : int    - thứ tự bài trong phần
     *   - answer         : String - nội dung người dùng viết
     *   - wordCount      : int    - số từ
     *   - submitted      : bool   - đã nộp
     *   - timestamp      : long
     */
    public void saveWriteProgress(String userId, int partIndex, int questionIndex,
                                  String answer, int wordCount,
                                  SimpleCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("partIndex",     partIndex);
        data.put("questionIndex", questionIndex);
        data.put("answer",        answer != null ? answer : "");
        data.put("wordCount",     wordCount);
        data.put("submitted",     true);
        data.put("timestamp",     System.currentTimeMillis());

        db.collection("skillProgress")
                .document(userId)
                .collection("writing")
                .document(partIndex + "_" + questionIndex)
                .set(data)
                .addOnSuccessListener(aVoid -> { if (callback != null) callback.onSuccess(); })
                .addOnFailureListener(e -> { if (callback != null) callback.onFailure(e.getMessage()); });
    }

    /**
     * Lấy số bài Writing đã nộp của user trong một phần.
     */
    public void getWriteProgress(String userId, int partIndex,
                                 SkillProgressCallback callback) {
        db.collection("skillProgress")
                .document(userId)
                .collection("writing")
                .whereEqualTo("partIndex", partIndex)
                .get()
                .addOnSuccessListener(snapshots -> {
                    int count = 0;
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Boolean submitted = doc.getBoolean("submitted");
                        if (Boolean.TRUE.equals(submitted)) count++;
                    }
                    callback.onSuccess(count);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}