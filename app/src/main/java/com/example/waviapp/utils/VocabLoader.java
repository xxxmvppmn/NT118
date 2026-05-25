package com.example.waviapp.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.example.waviapp.models.TuVung;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Singleton VocabLoader for Wavi App.
 * Efficiently loads 750 words from tu_vung.json assets ASYNCHRONOUSLY
 * to avoid blocking the UI thread.
 */
public class VocabLoader {
    private static final String TAG = "VocabLoader";
    private static VocabLoader instance;
    private List<TuVung> vocabList;
    private boolean isLoaded = false;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface OnVocabLoadedListener {
        void onLoaded(List<TuVung> vocabList);
    }

    private VocabLoader() {
        vocabList = new ArrayList<>();
    }

    public static synchronized VocabLoader getInstance(Context context) {
        if (instance == null) {
            instance = new VocabLoader();
            instance.loadVocabAsync(context.getApplicationContext(), null);
        }
        return instance;
    }

    /**
     * Loads vocab data asynchronously on a background thread.
     * Calls the listener on the main thread when done.
     */
    public void loadVocabAsync(Context context, OnVocabLoadedListener listener) {
        if (isLoaded && vocabList != null && !vocabList.isEmpty()) {
            if (listener != null) {
                mainHandler.post(() -> listener.onLoaded(vocabList));
            }
            return;
        }

        executor.execute(() -> {
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
                    vocabList = list;
                    isLoaded = true;
                    Log.d(TAG, "Successfully loaded " + list.size() + " words (async).");
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to load vocab from assets: " + e.getMessage());
            }

            if (listener != null) {
                mainHandler.post(() -> listener.onLoaded(vocabList));
            }
        });
    }

    /**
     * Returns a random word from the loaded vocabulary.
     * If data hasn't been loaded yet, returns a default placeholder.
     */
    public TuVung getRandomWord() {
        if (vocabList == null || vocabList.isEmpty()) {
            // Safety fallback
            return new TuVung("def_01", "bai_1", "Wavi", "Vocabulary App", "N", "/ˈwɑːvi/", "Basic");
        }
        return vocabList.get(new Random().nextInt(vocabList.size()));
    }

    /**
     * Returns the full vocabulary list. May be empty if not yet loaded.
     */
    public List<TuVung> getVocabList() {
        return vocabList != null ? vocabList : new ArrayList<>();
    }

    /**
     * Returns whether the vocabulary has finished loading.
     */
    public boolean isLoaded() {
        return isLoaded;
    }
}
