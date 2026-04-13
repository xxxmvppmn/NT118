package com.example.waviapp.firebase;

import android.util.Log;

import com.example.waviapp.models.BaiKiemTra;
import com.example.waviapp.models.NguPhap;
import com.example.waviapp.models.TuVung;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

/**
 * Hàm tiện ích để đổ dữ liệu mẫu lên Firebase Firestore.
 * Sử dụng WriteBatch để đảm bảo dữ liệu được nạp đồng bộ và ghi đè nếu đã tồn tại.
 */
public class SeedDataHelper {
    private static final String TAG = "SeedDataHelper";

    public static void seedAllIfNeeded() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();

        Log.d(TAG, "Đang chuẩn bị gói dữ liệu mẫu (WriteBatch)...");

        // 1. Nạp Từ vựng
        seedVocabulary(db, batch);
        
        // 2. Nạp Ngữ pháp
        seedGrammar(db, batch);
        
        // 3. Nạp Bài kiểm tra
        seedTests(db, batch);

        // 4. Kích hoạt gửi toàn bộ gói dữ liệu lên Cloud
        batch.commit().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "==> THÀNH CÔNG: Đã nạp toàn bộ dữ liệu mẫu vào Firestore!");
            } else {
                Log.e(TAG, "==> THẤT BẠI: Không thể nạp dữ liệu. Lỗi: ", task.getException());
            }
        });
    }

    private static void seedVocabulary(FirebaseFirestore db, WriteBatch batch) {
        // Bài 1
        TuVung[] words1 = {
                new TuVung("tv_01", "bai_1", "New", "Mới", "Adj", "/njuː/", "Basic"),
                new TuVung("tv_02", "bai_1", "Old", "Cũ", "Adj", "/oʊld/", "Basic"),
                new TuVung("tv_03", "bai_1", "Big", "To", "Adj", "/bɪɡ/", "Basic"),
                new TuVung("tv_04", "bai_1", "Small", "Nhỏ", "Adj", "/smɔːl/", "Basic"),
                new TuVung("tv_05", "bai_1", "Good", "Tốt", "Adj", "/ɡʊd/", "Basic")
        };
        for (TuVung t : words1) {
            batch.set(db.collection("TuVung").document(t.getMaTV()), t);
        }

        // Bài 2
        TuVung[] words2 = {
                new TuVung("tv_06", "bai_2", "Meeting", "Cuộc họp", "N", "/ˈmiːtɪŋ/", "Basic"),
                new TuVung("tv_07", "bai_2", "Office", "Văn phòng", "N", "/ˈɒfɪs/", "Basic"),
                new TuVung("tv_08", "bai_2", "Staff", "Nhân viên", "N", "/stɑːf/", "Basic"),
                new TuVung("tv_09", "bai_2", "Manager", "Quản lý", "N", "/ˈmænɪdʒə/", "Basic"),
                new TuVung("tv_10", "bai_2", "Project", "Dự án", "N", "/ˈprɒdʒekt/", "Basic")
        };
        for (TuVung t : words2) {
            batch.set(db.collection("TuVung").document(t.getMaTV()), t);
        }
    }

    private static void seedGrammar(FirebaseFirestore db, WriteBatch batch) {
        // Bài 1
        NguPhap[] grammar1 = {
                new NguPhap("np_01", "bai_1", "Cấu trúc chung của một câu trong tiếng Anh", "Nội dung...", "Vi dụ...", 1),
                new NguPhap("np_02", "bai_1", "Subject (chủ ngữ)", "Nội dung...", "Vi dụ...", 2),
                new NguPhap("np_03", "bai_1", "Verb (động từ)", "Nội dung...", "Vi dụ...", 3)
        };
        for (NguPhap np : grammar1) {
            batch.set(db.collection("nguPhap").document(np.getMaNP()), np);
        }

        // Bài 2
        NguPhap[] grammar2 = {
                new NguPhap("np_04", "bai_2", "Thì hiện tại đơn", "Nội dung...", "Vi dụ...", 1),
                new NguPhap("np_05", "bai_2", "Thì tương lai đơn", "Nội dung...", "Vi dụ...", 2)
        };
        for (NguPhap np : grammar2) {
            batch.set(db.collection("nguPhap").document(np.getMaNP()), np);
        }
    }

    private static void seedTests(FirebaseFirestore db, WriteBatch batch) {
        // Fulltest
        for (int i = 1; i <= 10; i++) {
            boolean isLocked = i > 5;
            String id = String.format("%02d", i);
            BaiKiemTra test = new BaiKiemTra("fc_" + id, "cd_full", "Test " + i, "ETS 2023", 200, 120, isLocked);
            batch.set(db.collection("baiKiemTra").document(test.getMaBKT()), test);
        }

        // Minitest
        for (int i = 1; i <= 10; i++) {
            boolean isLocked = i > 5;
            String id = String.format("%02d", i);
            BaiKiemTra test = new BaiKiemTra("mc_" + id, "cd_mini", "Mini Test " + i, "minitest", 100, 60, isLocked);
            batch.set(db.collection("baiKiemTra").document(test.getMaBKT()), test);
        }

        // Speaking
        for (int i = 1; i <= 10; i++) {
            boolean isLocked = i > 5;
            String id = String.format("%02d", i);
            BaiKiemTra test = new BaiKiemTra("sc_" + id, "cd_speak", "Speaking Test " + i, "speaking", 16, 70, isLocked);
            batch.set(db.collection("baiKiemTra").document(test.getMaBKT()), test);
        }
    }
}
