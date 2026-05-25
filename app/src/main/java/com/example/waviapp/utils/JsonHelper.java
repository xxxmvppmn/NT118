package com.example.waviapp.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.example.waviapp.models.Part2Model;
import com.example.waviapp.models.Part5Question;
import com.example.waviapp.models.Part6Paragraph;
import com.example.waviapp.models.Part7Paragraph;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JsonHelper {

    private static final ExecutorService executor = Executors.newFixedThreadPool(2);
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface OnJsonLoadedListener<T> {
        void onLoaded(List<T> data);
    }

    public static List<Part5Question> loadPart5Questions(Context context) {
        try {
            InputStream is = context.getAssets().open("reading_part5.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            return new Gson().fromJson(json, new TypeToken<List<Part5Question>>(){}.getType());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void loadPart5QuestionsAsync(Context context, OnJsonLoadedListener<Part5Question> listener) {
        executor.execute(() -> {
            List<Part5Question> result = loadPart5Questions(context);
            mainHandler.post(() -> {
                if (listener != null) {
                    listener.onLoaded(result);
                }
            });
        });
    }

    public static List<Part6Paragraph> loadPart6Paragraphs(Context context) {
        try {
            InputStream is = context.getAssets().open("reading_part6.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            return new Gson().fromJson(json, new TypeToken<List<Part6Paragraph>>(){}.getType());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void loadPart6ParagraphsAsync(Context context, OnJsonLoadedListener<Part6Paragraph> listener) {
        executor.execute(() -> {
            List<Part6Paragraph> result = loadPart6Paragraphs(context);
            mainHandler.post(() -> {
                if (listener != null) {
                    listener.onLoaded(result);
                }
            });
        });
    }

    public static List<Part7Paragraph> loadPart7Paragraphs(Context context) {
        try {
            InputStream is = context.getAssets().open("reading_part7.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            return new Gson().fromJson(json, new TypeToken<List<Part7Paragraph>>(){}.getType());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void loadPart7ParagraphsAsync(Context context, OnJsonLoadedListener<Part7Paragraph> listener) {
        executor.execute(() -> {
            List<Part7Paragraph> result = loadPart7Paragraphs(context);
            mainHandler.post(() -> {
                if (listener != null) {
                    listener.onLoaded(result);
                }
            });
        });
    }

    public static List<Part2Model> loadPart2Data(Context context) {
        try {
            InputStream is = context.getAssets().open("listening_p2.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            return new Gson().fromJson(json, new TypeToken<List<Part2Model>>(){}.getType());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void loadPart2DataAsync(Context context, OnJsonLoadedListener<Part2Model> listener) {
        executor.execute(() -> {
            List<Part2Model> result = loadPart2Data(context);
            mainHandler.post(() -> {
                if (listener != null) {
                    listener.onLoaded(result);
                }
            });
        });
    }
}
