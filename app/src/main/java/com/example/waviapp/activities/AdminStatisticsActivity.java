package com.example.waviapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.waviapp.R;
import com.example.waviapp.firebase.DatabaseHelper;
import com.example.waviapp.widgets.BarChartView;

import java.util.LinkedHashMap;
import java.util.Map;

public class AdminStatisticsActivity extends BaseAdminActivity {

    private TextView txtStatUsers, txtStatLessons, txtStatVocab, txtStatGrammar;
    private BarChartView barChart;
    private ImageView btnRefresh;
    private ProgressBar progressLoading;
    private DatabaseHelper dbHelper;

    private int completedQueriesCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_statistics);

        dbHelper = new DatabaseHelper();
        initViews();
        loadStatistics();
    }

    private void initViews() {
        txtStatUsers = findViewById(R.id.txtStatUsers);
        txtStatLessons = findViewById(R.id.txtStatLessons);
        txtStatVocab = findViewById(R.id.txtStatVocab);
        txtStatGrammar = findViewById(R.id.txtStatGrammar);
        barChart = findViewById(R.id.barChart);
        btnRefresh = findViewById(R.id.btnRefresh);
        progressLoading = findViewById(R.id.progressLoading);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnRefresh.setOnClickListener(v -> loadStatistics());
    }

    private void loadStatistics() {
        completedQueriesCount = 0;
        progressLoading.setVisibility(View.VISIBLE);

        // Load count stats
        dbHelper.getUserCount(new DatabaseHelper.CountCallback() {
            @Override
            public void onSuccess(int count) {
                if (isFinishing() || isDestroyed()) return;
                txtStatUsers.setText(String.valueOf(count));
                checkAllQueriesCompleted();
            }

            @Override
            public void onFailure(String error) {
                if (isFinishing() || isDestroyed()) return;
                txtStatUsers.setText("–");
                checkAllQueriesCompleted();
            }
        });

        dbHelper.getLessonCount(new DatabaseHelper.CountCallback() {
            @Override
            public void onSuccess(int count) {
                if (isFinishing() || isDestroyed()) return;
                txtStatLessons.setText(String.valueOf(count));
                checkAllQueriesCompleted();
            }

            @Override
            public void onFailure(String error) {
                if (isFinishing() || isDestroyed()) return;
                txtStatLessons.setText("–");
                checkAllQueriesCompleted();
            }
        });

        dbHelper.getVocabularyCount(new DatabaseHelper.CountCallback() {
            @Override
            public void onSuccess(int count) {
                if (isFinishing() || isDestroyed()) return;
                txtStatVocab.setText(String.valueOf(count));
                checkAllQueriesCompleted();
            }

            @Override
            public void onFailure(String error) {
                if (isFinishing() || isDestroyed()) return;
                txtStatVocab.setText("–");
                checkAllQueriesCompleted();
            }
        });

        dbHelper.getGrammarCount(new DatabaseHelper.CountCallback() {
            @Override
            public void onSuccess(int count) {
                if (isFinishing() || isDestroyed()) return;
                txtStatGrammar.setText(String.valueOf(count));
                checkAllQueriesCompleted();
            }

            @Override
            public void onFailure(String error) {
                if (isFinishing() || isDestroyed()) return;
                txtStatGrammar.setText("–");
                checkAllQueriesCompleted();
            }
        });

        // Load registration stats
        dbHelper.getUserRegistrationStats(new DatabaseHelper.StatsCallback() {
            @Override
            public void onSuccess(Map<String, Integer> stats) {
                if (isFinishing() || isDestroyed()) return;
                LinkedHashMap<String, Integer> linkedStats = new LinkedHashMap<>(stats);
                barChart.setData(linkedStats);
                checkAllQueriesCompleted();
            }

            @Override
            public void onFailure(String error) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(AdminStatisticsActivity.this, "Lỗi tải biểu đồ: " + error, Toast.LENGTH_SHORT).show();
                barChart.setData(new LinkedHashMap<>()); // Will trigger "Chưa có dữ liệu" empty state in custom chart view
                checkAllQueriesCompleted();
            }
        });
    }

    private void checkAllQueriesCompleted() {
        completedQueriesCount++;
        if (completedQueriesCount >= 5) {
            progressLoading.setVisibility(View.GONE);
        }
    }
}
