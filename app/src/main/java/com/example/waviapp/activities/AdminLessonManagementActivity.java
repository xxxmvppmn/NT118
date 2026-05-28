package com.example.waviapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.waviapp.R;
import com.example.waviapp.adapters.AdminLessonAdapter;
import com.example.waviapp.firebase.DatabaseHelper;
import com.example.waviapp.models.ChuDe;

import java.util.ArrayList;
import java.util.List;

public class AdminLessonManagementActivity extends BaseAdminActivity implements AdminLessonAdapter.OnLessonActionListener {

    private RecyclerView rvLessons;
    private EditText edtSearchLesson;
    private TextView txtLessonCount, txtEmpty;
    private SwipeRefreshLayout swipeRefresh;
    private AdminLessonAdapter adapter;
    private DatabaseHelper dbHelper;
    private List<ChuDe> allLessons = new ArrayList<>();

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_lesson_management);

        dbHelper = new DatabaseHelper();
        initViews();
        setupRecyclerView();
        setupSearch();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLessons();
    }

    private void initViews() {
        rvLessons = findViewById(R.id.rvLessons);
        edtSearchLesson = findViewById(R.id.edtSearchLesson);
        txtLessonCount = findViewById(R.id.txtLessonCount);
        txtEmpty = findViewById(R.id.txtEmpty);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.fabAddLesson).setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminAddLessonActivity.class);
            startActivity(intent);
        });

        swipeRefresh.setColorSchemeResources(R.color.admin_primary);
        swipeRefresh.setOnRefreshListener(this::loadLessons);
    }

    private void setupRecyclerView() {
        adapter = new AdminLessonAdapter(this);
        rvLessons.setLayoutManager(new LinearLayoutManager(this));
        rvLessons.setAdapter(adapter);
    }

    private void setupSearch() {
        edtSearchLesson.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchHandler.removeCallbacks(searchRunnable);
                searchRunnable = () -> {
                    if (!(isFinishing() || isDestroyed())) {
                        filterLessons(s.toString());
                    }
                };
                searchHandler.postDelayed(searchRunnable, 300); // 300ms Debounce
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadLessons() {
        swipeRefresh.setRefreshing(true);
        dbHelper.getAllLessons(new DatabaseHelper.LessonsListCallback() {
            @Override
            public void onSuccess(List<ChuDe> lessons) {
                if (isFinishing() || isDestroyed()) return;
                swipeRefresh.setRefreshing(false);
                allLessons = lessons;
                adapter.setLessons(lessons);
                txtLessonCount.setText(lessons.size() + " bài học");
                txtEmpty.setVisibility(lessons.isEmpty() ? View.VISIBLE : View.GONE);
                rvLessons.setVisibility(lessons.isEmpty() ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onFailure(String error) {
                if (isFinishing() || isDestroyed()) return;
                swipeRefresh.setRefreshing(false);
                Toast.makeText(AdminLessonManagementActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterLessons(String query) {
        if (query.isEmpty()) {
            adapter.setLessons(allLessons);
            txtLessonCount.setText(allLessons.size() + " bài học");
            return;
        }
        String lower = query.toLowerCase();
        List<ChuDe> filtered = new ArrayList<>();
        for (ChuDe l : allLessons) {
            if ((l.getTenCD() != null && l.getTenCD().toLowerCase().contains(lower)) ||
                    (l.getMaCD() != null && l.getMaCD().toLowerCase().contains(lower))) {
                filtered.add(l);
            }
        }
        adapter.setLessons(filtered);
        txtLessonCount.setText(filtered.size() + " bài học");
    }

    @Override
    public void onEdit(ChuDe lesson) {
        Intent intent = new Intent(this, AdminEditLessonActivity.class);
        intent.putExtra("maCD", lesson.getMaCD());
        intent.putExtra("tenCD", lesson.getTenCD());
        intent.putExtra("loaiChuDe", lesson.getLoaiChuDe());
        intent.putExtra("moTa", lesson.getMoTa());
        intent.putExtra("hinhAnh", lesson.getHinhAnh());
        startActivity(intent);
    }

    @Override
    public void onDelete(ChuDe lesson, int position) {
        new android.app.AlertDialog.Builder(this)
                .setTitle(R.string.admin_delete)
                .setMessage(R.string.admin_confirm_delete_lesson)
                .setPositiveButton(R.string.admin_delete, (dialog, which) -> {
                    swipeRefresh.setRefreshing(true);
                    dbHelper.deleteLesson(lesson.getMaCD(), new DatabaseHelper.SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            if (isFinishing() || isDestroyed()) return;
                            swipeRefresh.setRefreshing(false);
                            allLessons.remove(lesson);
                            adapter.removeItem(position);
                            txtLessonCount.setText(allLessons.size() + " bài học");
                            Toast.makeText(AdminLessonManagementActivity.this, "Đã xóa", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(String error) {
                            if (isFinishing() || isDestroyed()) return;
                            swipeRefresh.setRefreshing(false);
                            Toast.makeText(AdminLessonManagementActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(R.string.admin_cancel, null)
                .show();
    }

    @Override
    protected void onDestroy() {
        searchHandler.removeCallbacks(searchRunnable);
        super.onDestroy();
    }
}
