package com.example.waviapp.firebase;

import androidx.annotation.NonNull;

import com.example.waviapp.models.BaiKiemTra;
import com.example.waviapp.models.NguPhap;
import com.example.waviapp.models.TaiKhoan;
import com.example.waviapp.models.TuVung;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DatabaseHelper {
    private static final String TAG = "DatabaseHelper";
    private final DatabaseReference mDatabase;

    // Callback interfaces
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

    public interface SimpleCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public DatabaseHelper() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    // =================== TAI KHOAN ===================

    public void saveUser(String userId, TaiKhoan user, SimpleCallback callback) {
        mDatabase.child("taiKhoan").child(userId).setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (callback != null) callback.onSuccess();
                    } else {
                        if (callback != null) callback.onFailure("Lưu thông tin thất bại");
                    }
                });
    }

    public void getUser(String userId, UserCallback callback) {
        mDatabase.child("taiKhoan").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                TaiKhoan user = snapshot.getValue(TaiKhoan.class);
                if (user != null) {
                    callback.onSuccess(user);
                } else {
                    callback.onFailure("Không tìm thấy thông tin người dùng");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        });
    }

    public void updateUser(String userId, Map<String, Object> updates, SimpleCallback callback) {
        mDatabase.child("taiKhoan").child(userId).updateChildren(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (callback != null) callback.onSuccess();
                    } else {
                        if (callback != null) callback.onFailure("Cập nhật thất bại");
                    }
                });
    }

    public void deleteUser(String userId, SimpleCallback callback) {
        mDatabase.child("taiKhoan").child(userId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (callback != null) callback.onSuccess();
                    } else {
                        if (callback != null) callback.onFailure("Xóa dữ liệu thất bại");
                    }
                });
        mDatabase.child("user_data").child(userId).removeValue();
    }

    // =================== TU VUNG ===================

    public void getVocabulary(String maCD, VocabularyCallback callback) {
        mDatabase.child("tuVung").child(maCD)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<TuVung> words = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            TuVung word = child.getValue(TuVung.class);
                            if (word != null) {
                                words.add(word);
                            }
                        }
                        callback.onSuccess(words);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailure(error.getMessage());
                    }
                });
    }

    // =================== NGU PHAP ===================

    public void getGrammar(String maCD, GrammarCallback callback) {
        mDatabase.child("nguPhap").child(maCD)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<NguPhap> grammarList = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            NguPhap grammar = child.getValue(NguPhap.class);
                            if (grammar != null) {
                                grammarList.add(grammar);
                            }
                        }
                        callback.onSuccess(grammarList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailure(error.getMessage());
                    }
                });
    }

    // =================== BAI KIEM TRA ===================

    public void getTests(String loaiKiemTra, TestCallback callback) {
        mDatabase.child("baiKiemTra").child(loaiKiemTra)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<BaiKiemTra> tests = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            BaiKiemTra test = child.getValue(BaiKiemTra.class);
                            if (test != null) {
                                tests.add(test);
                            }
                        }
                        callback.onSuccess(tests);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailure(error.getMessage());
                    }
                });
    }

    public DatabaseReference getReference() {
        return mDatabase;
    }
}
