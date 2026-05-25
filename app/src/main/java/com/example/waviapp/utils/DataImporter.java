package com.example.waviapp.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.example.waviapp.models.NguPhap;
import com.example.waviapp.models.TuVung;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataImporter {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static void importVocab(Context context) {
        executor.execute(() -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            try {
                // 1. Đọc file tu_vung.json từ assets
                InputStream is = context.getAssets().open("tu_vung.json");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                String json = new String(buffer, "UTF-8");

                // 2. Dùng GSON để chuyển JSON thành List<TuVung>
                Gson gson = new Gson();
                Type listType = new TypeToken<List<TuVung>>(){}.getType();
                List<TuVung> vocabList = gson.fromJson(json, listType);
                
                // 3. Đẩy lên Firestore
                for (TuVung w : vocabList) {
                    db.collection("TuVung").document(w.getMaTV().trim()).set(w)
                            .addOnSuccessListener(aVoid -> Log.d("IMPORT", "Thành công: " + w.getTuTiengAnh()))
                            .addOnFailureListener(e -> Log.e("IMPORT_ERROR", "Lỗi tại từ " + w.getTuTiengAnh() + ": " + e.getMessage()));
                }
                
                mainHandler.post(() -> {
                    Toast.makeText(context.getApplicationContext(), "Đang import Từ Vựng trong nền...", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                Log.e("IMPORT_ERROR", e.getMessage());
            }
        });
    }

    public static void importGrammar(Context context) {
        executor.execute(() -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            try {
                // 1. Đọc file ngu_phap.json từ assets
                InputStream is = context.getAssets().open("ngu_phap.json");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                String json = new String(buffer, "UTF-8");

                // 2. Dùng GSON để chuyển JSON thành List<NguPhap>
                Gson gson = new Gson();
                Type listType = new TypeToken<List<NguPhap>>(){}.getType();
                List<NguPhap> grammarList = gson.fromJson(json, listType);
                
                // 3. Đẩy lên Firestore
                for (NguPhap np : grammarList) {
                    db.collection("nguPhap").document(np.getMaNP().trim()).set(np)
                            .addOnSuccessListener(aVoid -> Log.d("IMPORT", "Thành công ngữ pháp: " + np.getTenBai()))
                            .addOnFailureListener(e -> Log.e("IMPORT_ERROR", "Lỗi ngữ pháp " + np.getTenBai() + ": " + e.getMessage()));
                }
                
                mainHandler.post(() -> {
                    Toast.makeText(context.getApplicationContext(), "Đang import Ngữ Pháp trong nền...", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                Log.e("IMPORT_ERROR", e.getMessage());
            }
        });
    }
}