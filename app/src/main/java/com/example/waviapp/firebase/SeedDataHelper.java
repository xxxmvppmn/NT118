package com.example.waviapp.firebase;

import android.util.Log;

import com.example.waviapp.models.BaiKiemTra;
import com.example.waviapp.models.NguPhap;
import com.example.waviapp.models.TuVung;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Hàm tiện ích để đổ dữ liệu mẫu lên Firebase lần đầu.
 * Gọi SeedDataHelper.seedAllIfNeeded() một lần duy nhất, dữ liệu sẽ được lưu trên cloud.
 */
public class SeedDataHelper {
    private static final String TAG = "SeedDataHelper";

    public static void seedAllIfNeeded() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        // Kiểm tra nếu đã có dữ liệu thì bỏ qua
        ref.child("tuVung").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.d(TAG, "Bắt đầu đổ dữ liệu mẫu...");
                    seedVocabulary(ref);
                    seedGrammar(ref);
                    seedTests(ref);
                    Log.d(TAG, "Đổ dữ liệu mẫu hoàn tất!");
                } else {
                    Log.d(TAG, "Dữ liệu đã tồn tại, bỏ qua seed.");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Lỗi kiểm tra seed: " + error.getMessage());
            }
        });
    }

    private static void seedVocabulary(DatabaseReference ref) {
        // Bài 1
        ref.child("tuVung").child("bai_1").child("tv_01").setValue(new TuVung("tv_01", "bai_1", "New", "Mới", "Adj", "/njuː/"));
        ref.child("tuVung").child("bai_1").child("tv_02").setValue(new TuVung("tv_02", "bai_1", "Company", "Công ty", "N", "/ˈkʌmpəni/"));
        ref.child("tuVung").child("bai_1").child("tv_03").setValue(new TuVung("tv_03", "bai_1", "Services", "Dịch vụ", "N", "/ˈsɜːrvɪsɪz/"));
        ref.child("tuVung").child("bai_1").child("tv_04").setValue(new TuVung("tv_04", "bai_1", "Please", "Vui lòng", "V", "/pliːz/"));
        ref.child("tuVung").child("bai_1").child("tv_05").setValue(new TuVung("tv_05", "bai_1", "Information", "Thông tin", "N", "/ˌɪnfərˈmeɪʃn/"));

        // Bài 2
        ref.child("tuVung").child("bai_2").child("tv_06").setValue(new TuVung("tv_06", "bai_2", "Meeting", "Cuộc họp", "N", "/ˈmiːtɪŋ/"));
        ref.child("tuVung").child("bai_2").child("tv_07").setValue(new TuVung("tv_07", "bai_2", "Schedule", "Lịch trình", "N", "/ˈskedʒuːl/"));
        ref.child("tuVung").child("bai_2").child("tv_08").setValue(new TuVung("tv_08", "bai_2", "Department", "Phòng ban", "N", "/dɪˈpɑːrtmənt/"));
        ref.child("tuVung").child("bai_2").child("tv_09").setValue(new TuVung("tv_09", "bai_2", "Employee", "Nhân viên", "N", "/ɪmˈplɔɪiː/"));
        ref.child("tuVung").child("bai_2").child("tv_10").setValue(new TuVung("tv_10", "bai_2", "Manager", "Quản lý", "N", "/ˈmænɪdʒər/"));
    }

    private static void seedGrammar(DatabaseReference ref) {
        // Bài 1 - Ngữ pháp cơ bản
        ref.child("nguPhap").child("bai_1").child("np_01").setValue(new NguPhap("np_01", "bai_1", "Cấu trúc chung của một câu trong tiếng anh", "Nội dung...", "Vi dụ...", 1));
        ref.child("nguPhap").child("bai_1").child("np_02").setValue(new NguPhap("np_02", "bai_1", "Subject (chủ ngữ)", "Nội dung...", "Vi dụ...", 2));
        ref.child("nguPhap").child("bai_1").child("np_03").setValue(new NguPhap("np_03", "bai_1", "Verb (động từ)", "Nội dung...", "Vi dụ...", 3));

        // Bài 2 - Thì
        ref.child("nguPhap").child("bai_2").child("np_04").setValue(new NguPhap("np_04", "bai_2", "Thì hiện tại đơn", "Nội dung...", "Vi dụ...", 1));
        ref.child("nguPhap").child("bai_2").child("np_05").setValue(new NguPhap("np_05", "bai_2", "Thì tương lai đơn", "Nội dung...", "Vi dụ...", 2));
    }

    private static void seedTests(DatabaseReference ref) {
        // Fulltest
        for (int i = 1; i <= 10; i++) {
            // Test từ số 6 trở đi sẽ bị khóa (yêu cầu Premium)
            boolean isLocked = i > 5;
            String id = String.format("%02d", i);
            BaiKiemTra test = new BaiKiemTra("fc_" + id, "cd_full", "Test " + i, "ETS 2023", 200, 120, isLocked);
            ref.child("baiKiemTra").child("fulltest").child(test.getMaBKT()).setValue(test);
        }

        // Minitest
        for (int i = 1; i <= 10; i++) {
            boolean isLocked = i > 5;
            String id = String.format("%02d", i);
            BaiKiemTra test = new BaiKiemTra("mc_" + id, "cd_mini", "Mini Test " + i, "minitest", 100, 60, isLocked);
            ref.child("baiKiemTra").child("minitest").child(test.getMaBKT()).setValue(test);
        }

        // Speaking
        for (int i = 1; i <= 10; i++) {
            boolean isLocked = i > 5;
            String id = String.format("%02d", i);
            BaiKiemTra test = new BaiKiemTra("sc_" + id, "cd_speak", "Speaking Test " + i, "speaking", 16, 70, isLocked);
            ref.child("baiKiemTra").child("speaking").child(test.getMaBKT()).setValue(test);
        }
    }
}
