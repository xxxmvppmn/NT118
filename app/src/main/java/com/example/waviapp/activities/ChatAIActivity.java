package com.example.waviapp.activities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.waviapp.R;
import com.example.waviapp.adapters.ChatAdapter;
import com.example.waviapp.databinding.ActivityChatAiBinding;
import com.example.waviapp.models.ChatMessage;
import com.example.waviapp.models.TuVung;
import com.example.waviapp.utils.DictionaryService;
import com.example.waviapp.utils.VocabLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatAIActivity extends BaseActivity {

    private ActivityChatAiBinding binding;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private CollectionReference chatRef;
    
    private DictionaryService dictionaryService;
    private boolean isFirstLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatAiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initFirebase();
        setupRecyclerView();
        listenMessages();
        
        dictionaryService = new DictionaryService();

        binding.btnSend.setOnClickListener(v -> sendMessage());
        binding.ivBack.setOnClickListener(v -> finish());
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
            chatRef = db.collection("taiKhoan")
                    .document(currentUserId)
                    .collection("tinNhanChat");
        } else {
            Toast.makeText(this, getString(R.string.chat_login_warning), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.rcvChat.setLayoutManager(layoutManager);
        binding.rcvChat.setAdapter(chatAdapter);
    }

    private void listenMessages() {
        chatRef.orderBy("thoiGian", Query.Direction.ASCENDING)
                .limitToLast(30) // Giới hạn 30 tin nhắn để tối ưu hiệu năng load
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("ChatAI", "Lỗi: " + error.getMessage());
                        return;
                    }
                    if (value != null) {
                        messageList.clear();
                        List<ChatMessage> list = value.toObjects(ChatMessage.class);
                        
                        // Sắp xếp lại để đảm bảo tin nhắn chưa có timestamp (đang gửi) luôn ở cuối
                        Collections.sort(list, (m1, m2) -> {
                            if (m1.getThoiGian() == null) return 1;
                            if (m2.getThoiGian() == null) return -1;
                            return m1.getThoiGian().compareTo(m2.getThoiGian());
                        });

                        if (list.isEmpty()) {
                            messageList.add(new ChatMessage(getString(R.string.bot_hello), "bot", null));
                        } else {
                            messageList.addAll(list);
                        }
                        chatAdapter.notifyDataSetChanged();
                        
                        if (isFirstLoad) {
                            binding.rcvChat.scrollToPosition(messageList.size() - 1);
                            isFirstLoad = false;
                        } else {
                            binding.rcvChat.smoothScrollToPosition(messageList.size() - 1);
                        }
                    }
                });
    }

    private void sendMessage() {
        String content = binding.edtMessage.getText().toString().trim();
        if (TextUtils.isEmpty(content)) return; // Ngăn chặn gửi tin nhắn trống hoặc toàn khoảng trắng

        Map<String, Object> message = new HashMap<>();
        message.put("noiDung", content);
        message.put("nguoiGui", "user");
        message.put("thoiGian", FieldValue.serverTimestamp());

        binding.edtMessage.setText("");

        chatRef.add(message).addOnSuccessListener(doc -> generateBotResponse(content));
    }

    private void generateBotResponse(String userQuestion) {
        String wordToSearch = extractWord(userQuestion);
        
        // 1. Thử tìm trong database nội bộ trước (Ưu tiên TOEIC - Hoạt động offline hoàn hảo)
        TuVung result = VocabLoader.getInstance(this).findWord(wordToSearch);
        if (result != null) {
            String formatStr = getString(R.string.dictionary_local_format);
            String response = getString(R.string.dictionary_local_prefix) +
                    String.format(formatStr, 
                            result.getTuTiengAnh(), 
                            result.getNghiaTiengViet(), 
                            result.getLoaiTu(), 
                            result.getPhienAm(), 
                            result.getCauViDu());
            sendBotMessage(response);
            return;
        }

        // 2. Kiểm tra kết nối mạng trước khi gọi Dictionary API
        if (!isNetworkAvailable()) {
            sendBotMessage(getString(R.string.bot_offline_warning));
            return;
        }

        // Hiển thị tin nhắn chờ Bot tra cứu
        ChatMessage loadingMsg = new ChatMessage(getString(R.string.bot_typing), "bot_loading", null);
        messageList.add(loadingMsg);
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        binding.rcvChat.smoothScrollToPosition(messageList.size() - 1);

        // 3. Gọi Free Dictionary API
        dictionaryService.lookupWord(wordToSearch, new DictionaryService.DictionaryCallback() {
            @Override
            public void onSuccess(String result) {
                removeLoadingMessage();
                sendBotMessage(result);
            }

            @Override
            public void onError(String error) {
                removeLoadingMessage();
                
                // Xử lý các từ chào hỏi thông thường
                if (wordToSearch.equalsIgnoreCase("hello") || wordToSearch.equalsIgnoreCase("hi") ||
                    wordToSearch.equalsIgnoreCase("xin chào") || wordToSearch.equalsIgnoreCase("chào")) {
                    sendBotMessage(getString(R.string.bot_hello));
                } else {
                    String notFoundFormat = getString(R.string.dictionary_not_found);
                    sendBotMessage(String.format(notFoundFormat, wordToSearch, error));
                }
            }
        });
    }

    private void removeLoadingMessage() {
        runOnUiThread(() -> {
            for (int i = messageList.size() - 1; i >= 0; i--) {
                if ("bot_loading".equals(messageList.get(i).getNguoiGui())) {
                    messageList.remove(i);
                    chatAdapter.notifyItemRemoved(i);
                    break;
                }
            }
        });
    }

    private void sendBotMessage(String content) {
        Map<String, Object> botMessage = new HashMap<>();
        botMessage.put("noiDung", content);
        botMessage.put("nguoiGui", "bot");
        botMessage.put("thoiGian", FieldValue.serverTimestamp());
        chatRef.add(botMessage);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = 
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    private String extractWord(String question) {
        if (TextUtils.isEmpty(question)) return "";
        String q = question.toLowerCase().trim();
        
        // Loại bỏ dấu nháy bọc từ nếu có
        if ((q.startsWith("\"") && q.endsWith("\"")) || (q.startsWith("'") && q.endsWith("'"))) {
            return q.substring(1, q.length() - 1).trim();
        }
        
        // Loại bỏ các ký tự dấu câu thông dụng
        q = q.replaceAll("[\\?\\.\\!\\,\\:\\;\\\"\\']", " ");
        
        // Loại bỏ tiền tố hội thoại tiếng Việt/Anh thông dụng
        String[] prefixes = {
            "cho tôi hỏi", "cho hoi", "cho tôi biết", "cho biet",
            "nghĩa của từ", "nghia cua tu", "nghĩa của", "nghia cua",
            "giải thích từ", "giai thich tu", "giải thích", "giai thich",
            "dịch từ", "dich tu", "dịch câu", "dich cau", "dịch", "dich",
            "tra từ", "tra tu", "tìm từ", "tim tu", "what is", "meaning of", "define"
        };
        for (String prefix : prefixes) {
            if (q.startsWith(prefix)) {
                q = q.substring(prefix.length()).trim();
            }
        }
        
        // Loại bỏ hậu tố hội thoại tiếng Việt thông dụng
        String[] suffixes = {
            "nghĩa là gì thế", "nghĩa là gì vậy", "nghĩa là gì",
            "nghia la gi the", "nghia la gi vay", "nghia la gi",
            "là gì thế", "là gì vậy", "là gì", "la gi the", "la gi vay", "la gi",
            "nhé", "nha", "đi", "với", "giùm", "hộ", "nào", "nhe", "di", "voi", "gium", "ho", "nao"
        };
        for (String suffix : suffixes) {
            if (q.endsWith(suffix)) {
                q = q.substring(0, q.length() - suffix.length()).trim();
            }
        }
        
        // Loại bỏ các từ đệm đứng một mình ở giữa
        String[] words = q.split("\\s+");
        List<String> cleanWords = new ArrayList<>();
        for (String word : words) {
            if (word.equals("từ") || word.equals("tu") || word.equals("chữ") || word.equals("chu") ||
                word.equals("của") || word.equals("cua") || word.equals("cho") || word.equals("tôi") ||
                word.equals("toi") || word.equals("là") || word.equals("la") || word.equals("gì") || 
                word.equals("gi")) {
                continue;
            }
            cleanWords.add(word);
        }
        
        if (!cleanWords.isEmpty()) {
            return String.join(" ", cleanWords).trim();
        }
        
        return q.trim();
    }
}
