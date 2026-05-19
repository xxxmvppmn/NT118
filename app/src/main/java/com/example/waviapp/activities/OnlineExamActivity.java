package com.example.waviapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waviapp.R;
import com.example.waviapp.adapters.OnlineExamAdapter;
import com.example.waviapp.databinding.ActivityOnlineExamBinding;
import com.example.waviapp.firebase.DatabaseHelper;
import com.example.waviapp.models.OnlineExam;
import com.example.waviapp.models.OnlineExamResult;
import com.example.waviapp.utils.OnlineExamSeeder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class OnlineExamActivity extends BaseActivity {

    private ActivityOnlineExamBinding binding;
    private DatabaseHelper dbHelper;
    private OnlineExamAdapter examAdapter;
    private final List<OnlineExam> examList = new ArrayList<>();

    public static final String EXTRA_EXAM_ID    = "exam_id";
    public static final String EXTRA_EXAM_TITLE = "exam_title";
    public static final String EXTRA_EXAM_TOTAL = "exam_total";
    public static final String EXTRA_EXAM_MINS  = "exam_minutes";
    public static final String EXTRA_EXAM_COLOR = "exam_color";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnlineExamBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new DatabaseHelper();

        binding.ivBack.setOnClickListener(v -> finish());
        binding.ivInfo.setOnClickListener(v ->
            startActivity(new Intent(this, EventInfoActivity.class))
        );

        setupExamRecyclerView();

        // Seed dữ liệu nếu chưa có, sau đó load
        OnlineExamSeeder.seedIfNeeded(new OnlineExamSeeder.SeedCallback() {
            @Override
            public void onSuccess() {
                loadExams();
            }
            @Override
            public void onFailure(String error) {
                loadExams(); // Vẫn thử load dù seed lỗi
            }
        });

        // Load lịch sử thi của user hiện tại vào rvHistory (cũ)
        loadUserHistory();
    }

    private void setupExamRecyclerView() {
        examAdapter = new OnlineExamAdapter(examList, exam -> {
            // Mở phòng thi
            Intent intent = new Intent(this, OnlineExamRoomActivity.class);
            intent.putExtra(EXTRA_EXAM_ID,    exam.getExamId());
            intent.putExtra(EXTRA_EXAM_TITLE, exam.getTitle());
            intent.putExtra(EXTRA_EXAM_TOTAL, exam.getTotalQuestions());
            intent.putExtra(EXTRA_EXAM_MINS,  exam.getDurationMinutes());
            intent.putExtra(EXTRA_EXAM_COLOR, exam.getColorTag());
            startActivity(intent);
        });

        // Dùng RecyclerView hiện có (rvHistory) để hiện danh sách kỳ thi ở trên
        // và giữ rvHistory cũ cho lịch sử phía dưới
        // --> Thêm RecyclerView mới vào layout qua ID rvExams
        if (binding.getRoot().findViewById(R.id.rvExams) != null) {
            RecyclerView rvExams = binding.getRoot().findViewById(R.id.rvExams);
            rvExams.setLayoutManager(new LinearLayoutManager(this));
            rvExams.setAdapter(examAdapter);
        }
    }

    private void loadExams() {
        dbHelper.getOnlineExams(new DatabaseHelper.OnlineExamListCallback() {
            @Override
            public void onSuccess(List<OnlineExam> exams) {
                examList.clear();
                examList.addAll(exams);
                examAdapter.notifyDataSetChanged();

                // Ẩn empty state nếu có dữ liệu
                View emptyCard = binding.getRoot().findViewById(R.id.cardEmptyExam);
                if (emptyCard != null) {
                    emptyCard.setVisibility(exams.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }
            @Override
            public void onFailure(String error) {
                Toast.makeText(OnlineExamActivity.this,
                        "Không tải được danh sách kỳ thi", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserHistory() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        dbHelper.getUserOnlineExamHistory(user.getUid(), new DatabaseHelper.OnlineExamResultCallback() {
            @Override
            public void onSuccess(List<OnlineExamResult> results) {
                if (results.isEmpty()) return;
                // Hiển thị lịch sử thi trong rvHistory (giữ lại layout cũ)
                // Sử dụng OnlineExamHistoryAdapter cũ
                List<com.example.waviapp.models.OnlineExamHistory> historyList = new ArrayList<>();
                for (OnlineExamResult r : results) {
                    int colorRes = R.color.bg_listen;
                    historyList.add(new com.example.waviapp.models.OnlineExamHistory(
                            r.getExamId() + " — " + r.getScore() + "/" + r.getTotalQuestions() + " câu",
                            r.getTotalQuestions(),
                            r.getDurationSeconds() / 60,
                            colorRes
                    ));
                }
                com.example.waviapp.adapters.OnlineExamHistoryAdapter adapter =
                        new com.example.waviapp.adapters.OnlineExamHistoryAdapter(historyList);
                binding.rvHistory.setLayoutManager(new LinearLayoutManager(OnlineExamActivity.this));
                binding.rvHistory.setAdapter(adapter);
            }
            @Override
            public void onFailure(String error) { /* ignore */ }
        });
    }
}
