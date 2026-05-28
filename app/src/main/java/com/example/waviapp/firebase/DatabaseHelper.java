package com.example.waviapp.firebase;

import com.example.waviapp.models.BaiKiemTra;
import com.example.waviapp.models.NguPhap;
import com.example.waviapp.models.OnlineExam;
import com.example.waviapp.models.OnlineExamQuestion;
import com.example.waviapp.models.OnlineExamResult;
import com.example.waviapp.models.TaiKhoan;
import com.example.waviapp.models.TuVung;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
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

    public interface OnlineExamListCallback {
        void onSuccess(List<OnlineExam> exams);
        void onFailure(String error);
    }

    public interface OnlineExamQuestionCallback {
        void onSuccess(List<OnlineExamQuestion> questions);
        void onFailure(String error);
    }

    public interface OnlineExamResultCallback {
        void onSuccess(List<OnlineExamResult> results);
        void onFailure(String error);
    }

    /** Callback trả về số câu/bài đã hoàn thành trong một phần */
    public interface SkillProgressCallback {
        void onSuccess(int completedCount);
        void onFailure(String error);
    }

    // =================== ADMIN CALLBACKS ===================

    public interface UsersListCallback {
        void onSuccess(List<TaiKhoan> users);
        void onFailure(String error);
    }

    public interface LessonsListCallback {
        void onSuccess(List<com.example.waviapp.models.ChuDe> lessons);
        void onFailure(String error);
    }

    public interface StatsCallback {
        void onSuccess(Map<String, Integer> stats);
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

    // =================== THI ONLINE ===================

    /** Lấy danh sách tất cả kỳ thi online đang active */
    public void getOnlineExams(OnlineExamListCallback callback) {
        db.collection("onlineExams")
                .orderBy("startTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<OnlineExam> exams = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        OnlineExam exam = doc.toObject(OnlineExam.class);
                        exam.setExamId(doc.getId());
                        exams.add(exam);
                    }
                    callback.onSuccess(exams);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /** Lấy câu hỏi của một kỳ thi, sắp xếp theo thứ tự */
    public void getOnlineExamQuestions(String examId, OnlineExamQuestionCallback callback) {
        db.collection("onlineExams").document(examId)
                .collection("questions")
                .orderBy("order", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<OnlineExamQuestion> questions = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        OnlineExamQuestion q = doc.toObject(OnlineExamQuestion.class);
                        q.setQuestionId(doc.getId());
                        questions.add(q);
                    }
                    callback.onSuccess(questions);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /** Nộp kết quả thi của user */
    public void submitOnlineExamResult(String examId, OnlineExamResult result, SimpleCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId",          result.getUserId());
        data.put("displayName",     result.getDisplayName());
        data.put("examId",          examId);
        data.put("score",           result.getScore());
        data.put("totalQuestions",  result.getTotalQuestions());
        data.put("durationSeconds", result.getDurationSeconds());
        data.put("submittedAt",     result.getSubmittedAt());

        db.collection("onlineExams").document(examId)
                .collection("results").document(result.getUserId())
                .set(data)
                .addOnSuccessListener(aVoid -> { if (callback != null) callback.onSuccess(); })
                .addOnFailureListener(e -> { if (callback != null) callback.onFailure(e.getMessage()); });
    }

    /** Lấy bảng xếp hạng top 50 của một kỳ thi (sắp xếp: điểm cao → thời gian thấp) */
    public void getOnlineExamLeaderboard(String examId, OnlineExamResultCallback callback) {
        db.collection("onlineExams").document(examId)
                .collection("results")
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<OnlineExamResult> results = new ArrayList<>();
                    int rank = 1;
                    for (QueryDocumentSnapshot doc : snapshots) {
                        OnlineExamResult r = doc.toObject(OnlineExamResult.class);
                        r.setRank(rank++);
                        results.add(r);
                    }
                    callback.onSuccess(results);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /** Lấy lịch sử thi online của user hiện tại */
    public void getUserOnlineExamHistory(String userId, OnlineExamResultCallback callback) {
        db.collectionGroup("results")
                .whereEqualTo("userId", userId)
                .orderBy("submittedAt", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<OnlineExamResult> results = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        OnlineExamResult r = doc.toObject(OnlineExamResult.class);
                        results.add(r);
                    }
                    callback.onSuccess(results);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // =================== ADMIN: QUẢN LÝ TÀI KHOẢN ===================

    /** Lấy tất cả users (trừ Admin) */
    public void getAllUsers(UsersListCallback callback) {
        db.collection("taiKhoan")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<TaiKhoan> users = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        TaiKhoan user = doc.toObject(TaiKhoan.class);
                        user.setId(doc.getId());
                        if (!"Admin".equals(user.getVaiTro())) {
                            users.add(user);
                        }
                    }
                    callback.onSuccess(users);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /** Khóa/Mở khóa tài khoản */
    public void lockUser(String userId, boolean lock, SimpleCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("isLocked", lock);
        db.collection("taiKhoan").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> { if (callback != null) callback.onSuccess(); })
                .addOnFailureListener(e -> { if (callback != null) callback.onFailure(e.getMessage()); });
    }

    // =================== ADMIN: QUẢN LÝ BÀI HỌC (CHỦ ĐỀ) ===================

    /** Lấy tất cả chủ đề */
    public void getAllLessons(LessonsListCallback callback) {
        db.collection("chuDe")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<com.example.waviapp.models.ChuDe> lessons = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        com.example.waviapp.models.ChuDe lesson = doc.toObject(com.example.waviapp.models.ChuDe.class);
                        if (lesson.getMaCD() == null || lesson.getMaCD().isEmpty()) {
                            lesson.setMaCD(doc.getId());
                        }
                        lessons.add(lesson);
                    }
                    callback.onSuccess(lessons);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /** Thêm chủ đề mới */
    public void addLesson(com.example.waviapp.models.ChuDe lesson, SimpleCallback callback) {
        db.collection("chuDe").document(lesson.getMaCD())
                .set(lesson)
                .addOnSuccessListener(aVoid -> { if (callback != null) callback.onSuccess(); })
                .addOnFailureListener(e -> { if (callback != null) callback.onFailure(e.getMessage()); });
    }

    /** Cập nhật chủ đề */
    public void updateLesson(String maCD, Map<String, Object> updates, SimpleCallback callback) {
        db.collection("chuDe").document(maCD)
                .update(updates)
                .addOnSuccessListener(aVoid -> { if (callback != null) callback.onSuccess(); })
                .addOnFailureListener(e -> { if (callback != null) callback.onFailure(e.getMessage()); });
    }

    /** Xóa chủ đề */
    public void deleteLesson(String maCD, SimpleCallback callback) {
        db.collection("chuDe").document(maCD)
                .delete()
                .addOnSuccessListener(aVoid -> { if (callback != null) callback.onSuccess(); })
                .addOnFailureListener(e -> { if (callback != null) callback.onFailure(e.getMessage()); });
    }

    // =================== ADMIN: QUẢN LÝ NỘI DUNG ===================

    /** Lấy tất cả từ vựng */
    public void getAllVocabulary(VocabularyCallback callback) {
        db.collection("tuVung")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<TuVung> words = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        TuVung w = doc.toObject(TuVung.class);
                        if (w.getMaTV() == null || w.getMaTV().isEmpty()) {
                            w.setMaTV(doc.getId());
                        }
                        words.add(w);
                    }
                    callback.onSuccess(words);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /** Thêm từ vựng */
    public void addVocabulary(TuVung word, SimpleCallback callback) {
        String docId = word.getMaTV() != null && !word.getMaTV().isEmpty()
                ? word.getMaTV()
                : db.collection("tuVung").document().getId();
        word.setMaTV(docId);
        db.collection("tuVung").document(docId)
                .set(word)
                .addOnSuccessListener(aVoid -> { if (callback != null) callback.onSuccess(); })
                .addOnFailureListener(e -> { if (callback != null) callback.onFailure(e.getMessage()); });
    }

    /** Cập nhật từ vựng */
    public void updateVocabulary(String maTV, Map<String, Object> updates, SimpleCallback callback) {
        db.collection("tuVung").document(maTV)
                .update(updates)
                .addOnSuccessListener(aVoid -> { if (callback != null) callback.onSuccess(); })
                .addOnFailureListener(e -> { if (callback != null) callback.onFailure(e.getMessage()); });
    }

    /** Xóa từ vựng */
    public void deleteVocabulary(String maTV, SimpleCallback callback) {
        db.collection("tuVung").document(maTV)
                .delete()
                .addOnSuccessListener(aVoid -> { if (callback != null) callback.onSuccess(); })
                .addOnFailureListener(e -> { if (callback != null) callback.onFailure(e.getMessage()); });
    }

    /** Lấy tất cả ngữ pháp */
    public void getAllGrammar(GrammarCallback callback) {
        db.collection("nguPhap")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<com.example.waviapp.models.NguPhap> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        com.example.waviapp.models.NguPhap np = doc.toObject(com.example.waviapp.models.NguPhap.class);
                        if (np.getMaNP() == null || np.getMaNP().isEmpty()) {
                            np.setMaNP(doc.getId());
                        }
                        list.add(np);
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /** Thêm ngữ pháp */
    public void addGrammar(com.example.waviapp.models.NguPhap grammar, SimpleCallback callback) {
        String docId = grammar.getMaNP() != null && !grammar.getMaNP().isEmpty()
                ? grammar.getMaNP()
                : db.collection("nguPhap").document().getId();
        grammar.setMaNP(docId);
        db.collection("nguPhap").document(docId)
                .set(grammar)
                .addOnSuccessListener(aVoid -> { if (callback != null) callback.onSuccess(); })
                .addOnFailureListener(e -> { if (callback != null) callback.onFailure(e.getMessage()); });
    }

    /** Cập nhật ngữ pháp */
    public void updateGrammar(String maNP, Map<String, Object> updates, SimpleCallback callback) {
        db.collection("nguPhap").document(maNP)
                .update(updates)
                .addOnSuccessListener(aVoid -> { if (callback != null) callback.onSuccess(); })
                .addOnFailureListener(e -> { if (callback != null) callback.onFailure(e.getMessage()); });
    }

    /** Xóa ngữ pháp */
    public void deleteGrammar(String maNP, SimpleCallback callback) {
        db.collection("nguPhap").document(maNP)
                .delete()
                .addOnSuccessListener(aVoid -> { if (callback != null) callback.onSuccess(); })
                .addOnFailureListener(e -> { if (callback != null) callback.onFailure(e.getMessage()); });
    }

    // =================== ADMIN: THỐNG KÊ ===================

    /** Đếm tổng users */
    public void getUserCount(CountCallback callback) {
        db.collection("taiKhoan").get()
                .addOnSuccessListener(snapshots -> callback.onSuccess(snapshots.size()))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /** Đếm tổng bài học */
    public void getLessonCount(CountCallback callback) {
        db.collection("chuDe").get()
                .addOnSuccessListener(snapshots -> callback.onSuccess(snapshots.size()))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /** Đếm tổng từ vựng */
    public void getVocabularyCount(CountCallback callback) {
        db.collection("tuVung").get()
                .addOnSuccessListener(snapshots -> callback.onSuccess(snapshots.size()))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /** Đếm tổng ngữ pháp */
    public void getGrammarCount(CountCallback callback) {
        db.collection("nguPhap").get()
                .addOnSuccessListener(snapshots -> callback.onSuccess(snapshots.size()))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /** Lấy thống kê user đăng ký theo tháng (6 tháng gần nhất) */
    public void getUserRegistrationStats(StatsCallback callback) {
        db.collection("taiKhoan").get()
                .addOnSuccessListener(snapshots -> {
                    Map<String, Integer> monthCounts = new java.util.LinkedHashMap<>();

                    // Khởi tạo 6 tháng gần nhất
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    String[] monthNames = {"T1", "T2", "T3", "T4", "T5", "T6",
                            "T7", "T8", "T9", "T10", "T11", "T12"};

                    for (int i = 5; i >= 0; i--) {
                        java.util.Calendar c = java.util.Calendar.getInstance();
                        c.add(java.util.Calendar.MONTH, -i);
                        String key = monthNames[c.get(java.util.Calendar.MONTH)] + "/" +
                                String.valueOf(c.get(java.util.Calendar.YEAR)).substring(2);
                        monthCounts.put(key, 0);
                    }

                    // Đếm user theo ngày tạo
                    for (QueryDocumentSnapshot doc : snapshots) {
                        com.google.firebase.Timestamp ngayTao = doc.getTimestamp("ngayTao");
                        if (ngayTao != null) {
                            java.util.Calendar userCal = java.util.Calendar.getInstance();
                            userCal.setTime(ngayTao.toDate());
                            String key = monthNames[userCal.get(java.util.Calendar.MONTH)] + "/" +
                                    String.valueOf(userCal.get(java.util.Calendar.YEAR)).substring(2);
                            if (monthCounts.containsKey(key)) {
                                monthCounts.put(key, monthCounts.get(key) + 1);
                            }
                        }
                    }

                    callback.onSuccess(monthCounts);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}