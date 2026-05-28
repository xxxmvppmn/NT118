package com.example.waviapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.waviapp.R;
import com.example.waviapp.firebase.DatabaseHelper;
import com.example.waviapp.firebase.FirebaseAuthHelper;
import com.example.waviapp.managers.UserSessionManager;
import com.example.waviapp.models.TaiKhoan;

public class AdminDashboardActivity extends BaseAdminActivity {

    private TextView txtAdminWelcome, txtAdminEmail;
    private TextView txtQuickUsers, txtQuickLessons, txtQuickVocab, txtQuickGrammar;
    private ProgressBar progressLoading;
    private DatabaseHelper dbHelper;
    private FirebaseAuthHelper authHelper;

    private int completedQueriesCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        dbHelper = new DatabaseHelper();
        authHelper = new FirebaseAuthHelper();

        initViews();
        setupClickListeners();
        loadAdminInfo();
    }

    private void initViews() {
        txtAdminWelcome = findViewById(R.id.txtAdminWelcome);
        txtAdminEmail = findViewById(R.id.txtAdminEmail);
        txtQuickUsers = findViewById(R.id.txtQuickUsers);
        txtQuickLessons = findViewById(R.id.txtQuickLessons);
        txtQuickVocab = findViewById(R.id.txtQuickVocab);
        txtQuickGrammar = findViewById(R.id.txtQuickGrammar);
        progressLoading = findViewById(R.id.progressLoading);
    }

    private void loadAdminInfo() {
        TaiKhoan admin = UserSessionManager.getInstance().getUserData();
        if (admin != null) {
            txtAdminWelcome.setText("Xin chào, " + (admin.getHoTen() != null ? admin.getHoTen() : "Admin"));
            txtAdminEmail.setText(admin.getEmail() != null ? admin.getEmail() : "");
        }
    }

    private void setupClickListeners() {
        // Logout
        ImageView btnLogout = findViewById(R.id.btnAdminLogout);
        btnLogout.setOnClickListener(v -> {
            authHelper.logout();
            UserSessionManager.getInstance().clearSession();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Navigation cards
        findViewById(R.id.cardUserManagement).setOnClickListener(v ->
                startActivity(new Intent(this, AdminUserManagementActivity.class)));

        findViewById(R.id.cardLessonManagement).setOnClickListener(v ->
                startActivity(new Intent(this, AdminLessonManagementActivity.class)));

        findViewById(R.id.cardContentManagement).setOnClickListener(v ->
                startActivity(new Intent(this, AdminContentManagementActivity.class)));

        findViewById(R.id.cardStatistics).setOnClickListener(v ->
                startActivity(new Intent(this, AdminStatisticsActivity.class)));
    }

    private void loadQuickStats() {
        completedQueriesCount = 0;
        progressLoading.setVisibility(View.VISIBLE);

        dbHelper.getUserCount(new DatabaseHelper.CountCallback() {
            @Override
            public void onSuccess(int count) {
                if (isFinishing() || isDestroyed()) return;
                txtQuickUsers.setText(String.valueOf(count));
                checkAllQueriesCompleted();
            }
            @Override
            public void onFailure(String error) {
                if (isFinishing() || isDestroyed()) return;
                txtQuickUsers.setText("–");
                checkAllQueriesCompleted();
            }
        });

        dbHelper.getLessonCount(new DatabaseHelper.CountCallback() {
            @Override
            public void onSuccess(int count) {
                if (isFinishing() || isDestroyed()) return;
                txtQuickLessons.setText(String.valueOf(count));
                checkAllQueriesCompleted();
            }
            @Override
            public void onFailure(String error) {
                if (isFinishing() || isDestroyed()) return;
                txtQuickLessons.setText("–");
                checkAllQueriesCompleted();
            }
        });

        dbHelper.getVocabularyCount(new DatabaseHelper.CountCallback() {
            @Override
            public void onSuccess(int count) {
                if (isFinishing() || isDestroyed()) return;
                txtQuickVocab.setText(String.valueOf(count));
                checkAllQueriesCompleted();
            }
            @Override
            public void onFailure(String error) {
                if (isFinishing() || isDestroyed()) return;
                txtQuickVocab.setText("–");
                checkAllQueriesCompleted();
            }
        });

        dbHelper.getGrammarCount(new DatabaseHelper.CountCallback() {
            @Override
            public void onSuccess(int count) {
                if (isFinishing() || isDestroyed()) return;
                txtQuickGrammar.setText(String.valueOf(count));
                checkAllQueriesCompleted();
            }
            @Override
            public void onFailure(String error) {
                if (isFinishing() || isDestroyed()) return;
                txtQuickGrammar.setText("–");
                checkAllQueriesCompleted();
            }
        });
    }

    private void checkAllQueriesCompleted() {
        completedQueriesCount++;
        if (completedQueriesCount >= 4) {
            progressLoading.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadQuickStats();
    }
}
