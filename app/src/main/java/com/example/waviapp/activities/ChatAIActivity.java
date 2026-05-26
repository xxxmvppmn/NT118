package com.example.waviapp.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waviapp.R;
import com.example.waviapp.adapters.ChatAdapter;
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

public class ChatAIActivity extends AppCompatActivity {

    private RecyclerView rcvChat;
    private EditText edtMessage;
    private ImageButton btnSend;
    private ImageView ivBack;

    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private CollectionReference chatRef;
    
    private DictionaryService dictionaryService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_ai);

        initViews();
        initFirebase();
        setupRecyclerView();
        listenMessages();
        
        dictionaryService = new DictionaryService();

        btnSend.setOnClickListener(v -> sendMessage());
        ivBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        rcvChat = findViewById(R.id.rcvChat);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
        ivBack = findViewById(R.id.ivBack);
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
            Toast.makeText(this, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rcvChat.setLayoutManager(layoutManager);
        rcvChat.setAdapter(chatAdapter);
    }

    private void listenMessages() {
        chatRef.orderBy("thoiGian", Query.Direction.ASCENDING)
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
                            messageList.add(new ChatMessage("Chào bạn! Tôi là Wavi AI. Bạn cần tra từ hay dịch câu gì không?", "bot", null));
                        } else {
                            messageList.addAll(list);
                        }
                        chatAdapter.notifyDataSetChanged();
                        rcvChat.scrollToPosition(messageList.size() - 1);
                    }
                });
    }

    private void sendMessage() {
        String content = edtMessage.getText().toString().trim();
        if (TextUtils.isEmpty(content)) return;

        Map<String, Object> message = new HashMap<>();
        message.put("noiDung", content);
        message.put("nguoiGui", "user");
        message.put("thoiGian", FieldValue.serverTimestamp());

        edtMessage.setText("");

        chatRef.add(message).addOnSuccessListener(doc -> generateBotResponse(content));
    }

    private void generateBotResponse(String userQuestion) {
        String wordToSearch = extractWord(userQuestion);
        
        // 1. Thử tìm trong database nội bộ trước (Ưu tiên TOEIC)
        TuVung result = VocabLoader.getInstance(this).findWord(wordToSearch);
        if (result != null) {
            String response = "Tôi tìm thấy từ này trong kho từ vựng TOEIC:\n\n" +
                    "**" + result.getTuTiengAnh() + "**\n" +
                    "• Nghĩa: " + result.getNghiaTiengViet() + "\n" +
                    "• Loại từ: " + result.getLoaiTu() + "\n" +
                    "• Phiên âm: " + result.getPhienAm() + "\n" +
                    "• Ví dụ: " + result.getCauViDu();
            sendBotMessage(response);
            return;
        }

        // 2. Nếu không có, gọi Free Dictionary API
        dictionaryService.lookupWord(wordToSearch, new DictionaryService.DictionaryCallback() {
            @Override
            public void onSuccess(String result) {
                sendBotMessage(result);
            }

            @Override
            public void onError(String error) {
                // Xử lý các từ chào hỏi thông thường
                if (wordToSearch.equalsIgnoreCase("hello") || wordToSearch.equalsIgnoreCase("hi")) {
                    sendBotMessage("Xin chào! Wavi AI có thể giúp gì cho bạn? Bạn có thể yêu cầu tôi dịch từ hoặc giải thích nghĩa từ vựng nhé.");
                } else {
                    sendBotMessage("Rất tiếc, tôi không tìm thấy thông tin cho từ \"" + wordToSearch + "\". " + error);
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

    private String extractWord(String question) {
        String q = question.toLowerCase().trim();
        String[] filters = {"nghĩa là gì", "là gì", "nghĩa của từ", "dịch từ", "từ", "what is", "meaning of"};
        for (String filter : filters) {
            if (q.contains(filter)) {
                q = q.replace(filter, "").trim();
            }
        }
        if (q.endsWith("?")) {
            q = q.substring(0, q.length() - 1).trim();
        }
        return q;
    }
}
