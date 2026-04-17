package com.example.waviapp.utils;

import android.content.Context;
import com.example.waviapp.models.Part5Question;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class JsonHelper {
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
}
