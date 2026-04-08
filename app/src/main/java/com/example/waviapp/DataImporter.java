package com.example.waviapp;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;

public class DataImporter {

    public static void importVocab(Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        try {
            // 1. Đọc file tu_vung.json từ assets
            InputStream is = context.getAssets().open("tu_vung.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");

            // 2. Dùng GSON để chuyển JSON thành List<Word>
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Word>>(){}.getType();
            List<Word> vocabList = gson.fromJson(json, listType);
            Log.d("NGAN_CHECK", "Số lượng từ đọc được từ JSON: " + (vocabList != null ? vocabList.size() : "0"));
            // 3. Đẩy lên Firestore
            for (Word w : vocabList) {
                db.collection("TuVung").document(w.getMaTV().trim()).set(w)
                        .addOnSuccessListener(aVoid -> Log.d("IMPORT", "Xong: " + w.getTuTiengAnh()));
            }
            Toast.makeText(context, "Đã đẩy xong " + vocabList.size() + " từ!", Toast.LENGTH_SHORT).show();
            for (Word w : vocabList) {
                db.collection("TuVung").document(w.getMaTV().trim()).set(w)
                        .addOnSuccessListener(aVoid -> Log.d("IMPORT", "Thành công: " + w.getTuTiengAnh()))
                        .addOnFailureListener(e -> Log.e("IMPORT_ERROR", "Lỗi tại từ " + w.getTuTiengAnh() + ": " + e.getMessage()));
            }
        } catch (Exception e) {
            Log.e("IMPORT_ERROR", e.getMessage());
        }

    }
}