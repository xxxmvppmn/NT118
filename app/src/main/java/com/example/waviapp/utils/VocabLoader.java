package com.example.waviapp.utils;

import android.content.Context;
import android.util.Log;
import com.example.waviapp.models.TuVung;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Singleton VocabLoader for Wavi App.
 * Efficiently loads 750 words from tu_vung.json assets.
 */
public class VocabLoader {
    private static final String TAG = "VocabLoader";
    private static VocabLoader instance;
    private List<TuVung> vocabList;

    private VocabLoader(Context context) {
        vocabList = loadVocabFromAssets(context);
    }

    public static synchronized VocabLoader getInstance(Context context) {
        if (instance == null) {
            instance = new VocabLoader(context.getApplicationContext());
        }
        return instance;
    }

    private List<TuVung> loadVocabFromAssets(Context context) {
        try {
            InputStream is = context.getAssets().open("tu_vung.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            Gson gson = new Gson();
            List<TuVung> list = gson.fromJson(json, new TypeToken<List<TuVung>>(){}.getType());
            if (list != null) {
                Log.d(TAG, "Successfully loaded " + list.size() + " words.");
                return list;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to load vocab from assets: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    public TuVung getRandomWord() {
        if (vocabList == null || vocabList.isEmpty()) {
            // Safety fallback
            return new TuVung("def_01", "bai_1", "Wavi", "Vocabulary App", "N", "/ˈwɑːvi/", "Basic");
        }
        return vocabList.get(new Random().nextInt(vocabList.size()));
    }
}
