package com.example.waviapp.firebase;

import com.example.waviapp.models.BaiKiemTra;
import com.example.waviapp.models.NguPhap;
import com.example.waviapp.models.TaiKhoan;
import com.example.waviapp.models.TuVung;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
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
        // Khi thành công, trả về con số (int)
        void onSuccess(int count);

        // Khi thất bại, trả về tin nhắn lỗi (String)
        void onFailure(String error);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public DatabaseHelper() {
        // Khởi tạo Firestore
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
                    TaiKhoan user = documentSnapshot.toObject(TaiKhoan.class);
                    if (user != null) {
                        callback.onSuccess(user);
                    } else {
                        callback.onFailure("Không tìm thấy thông tin người dùng");
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

    // =================== TỪ VỰNG (Lọc theo mã chủ đề) ===================

    public void getVocabulary(String maCD, VocabularyCallback callback) {
        // Firestore hỗ trợ lọc cực mạnh bằng whereEqualTo
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
    /**
     * Đếm số lượng thông báo chưa đọc của một User
     */
    public void getUnreadNotificationCount(String userId, CountCallback callback) {
        db.collection("thongBao")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false) // Chỉ lấy những cái chưa đọc
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        // Trả về số lượng tài liệu tìm thấy
                        int count = task.getResult().size();
                        callback.onSuccess(count);
                    } else {
                        callback.onFailure(task.getException() != null ?
                                task.getException().getMessage() : "Lỗi đếm thông báo");
                    }
                });
    }
}