package com.example.waviapp.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DictionaryService {
    private static final String BASE_URL = "https://api.dictionaryapi.dev/api/v2/entries/en/";
    private final OkHttpClient client;

    public interface DictionaryCallback {
        void onSuccess(String result);
        void onError(String error);
    }

    public DictionaryService() {
        this.client = new OkHttpClient();
    }

    public void lookupWord(String word, DictionaryCallback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + word)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Lỗi kết nối: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    if (response.code() == 404) {
                        callback.onError("Không tìm thấy từ này trong từ điển.");
                    } else {
                        callback.onError("Lỗi hệ thống: " + response.code());
                    }
                    return;
                }

                try {
                    String jsonData = response.body().string();
                    JSONArray jsonArray = new JSONArray(jsonData);
                    JSONObject firstEntry = jsonArray.getJSONObject(0);
                    
                    String wordName = firstEntry.getString("word");
                    String phonetic = firstEntry.optString("phonetic", "");
                    
                    StringBuilder result = new StringBuilder();
                    result.append("Kết quả từ điển cho **").append(wordName).append("**");
                    if (!phonetic.isEmpty()) {
                        result.append(" (").append(phonetic).append(")");
                    }
                    result.append(":\n");

                    JSONArray meanings = firstEntry.getJSONArray("meanings");
                    for (int i = 0; i < Math.min(meanings.length(), 2); i++) {
                        JSONObject meaning = meanings.getJSONObject(i);
                        String partOfSpeech = meaning.getString("partOfSpeech");
                        result.append("\n*").append(partOfSpeech).append("*:\n");

                        JSONArray definitions = meaning.getJSONArray("definitions");
                        for (int j = 0; j < Math.min(definitions.length(), 2); j++) {
                            JSONObject defObj = definitions.getJSONObject(j);
                            String definition = defObj.getString("definition");
                            result.append("- ").append(definition).append("\n");
                            
                            if (defObj.has("example")) {
                                result.append("  Ex: _").append(defObj.getString("example")).append("_\n");
                            }
                        }
                    }
                    
                    callback.onSuccess(result.toString());
                } catch (Exception e) {
                    Log.e("DictionaryService", "Parse error", e);
                    callback.onError("Lỗi xử lý dữ liệu.");
                }
            }
        });
    }
}
