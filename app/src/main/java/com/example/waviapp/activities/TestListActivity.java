package com.example.waviapp.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.waviapp.R;
import com.example.waviapp.firebase.DatabaseHelper;
import com.example.waviapp.models.BaiKiemTra;
import com.example.waviapp.models.TaiKhoan;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class TestListActivity extends BaseActivity {

    public static final String EXTRA_CATEGORY = "extra_category";
    public static final String EXTRA_FOLDER_PREFIX = "extra_folder_prefix";
    public static final String CAT_FULLTEST = "TOEIC Listening & Reading Fulltest";
    public static final String CAT_FULLTEST_2 = "TOEIC Listening & Reading Fulltest 2";
    public static final String CAT_MINITEST = "TOEIC Listening & Reading Minitest";
    public static final String CAT_SPEAKING = "TOEIC Speaking & Writing";

    private LinearLayout llTestListContainer;
    private TextView tvToolbarTitle;
    private ImageView ivBack;
    private DatabaseHelper dbHelper;
    private String folderPrefix = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_list);

        dbHelper = new DatabaseHelper();
        folderPrefix = getIntent().getStringExtra(EXTRA_FOLDER_PREFIX);
        if (folderPrefix == null) folderPrefix = "";

        llTestListContainer = findViewById(R.id.llTestListContainer);
        tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        ivBack = findViewById(R.id.ivBack);

        ivBack.setOnClickListener(v -> finish());

        String category = getIntent().getStringExtra(EXTRA_CATEGORY);
        if (category == null) category = CAT_MINITEST;
        tvToolbarTitle.setText(category);
        
        loadUserAndData(category);
    }

    private void loadUserAndData(String category) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            dbHelper.getUser(user.getUid(), new DatabaseHelper.UserCallback() {
                @Override
                public void onSuccess(TaiKhoan taiKhoan) {
                    isUserPremium = taiKhoan.isPremium();
                    loadTestsFromFirebase(category);
                }
                @Override
                public void onFailure(String error) {
                    isUserPremium = false;
                    loadTestsFromFirebase(category);
                }
            });
        } else {
            isUserPremium = false;
            loadTestsFromFirebase(category);
        }
    }

    private boolean isUserPremium = false;

    private void loadTestsFromFirebase(String category) {
        String firebaseKey;
        if (CAT_FULLTEST.equals(category)) firebaseKey = "fulltest";
        else if (CAT_FULLTEST_2.equals(category)) firebaseKey = "fulltest2";
        else if (CAT_SPEAKING.equals(category)) firebaseKey = "speaking";
        else firebaseKey = "minitest";

        dbHelper.getTests(firebaseKey, new DatabaseHelper.TestCallback() {
            @Override
            public void onSuccess(List<BaiKiemTra> tests) {
                if (tests.isEmpty()) {
                    setupTestDataDefault(category);
                    return;
                }
                
                java.util.Collections.sort(tests, (t1, t2) -> {
                    try {
                        String num1 = t1.getMaBKT().replaceAll("\\D+", "");
                        String num2 = t2.getMaBKT().replaceAll("\\D+", "");
                        return Integer.compare(Integer.parseInt(num1.isEmpty() ? "0" : num1), 
                                              Integer.parseInt(num2.isEmpty() ? "0" : num2));
                    } catch (Exception e) { return t1.getMaBKT().compareTo(t2.getMaBKT()); }
                });

                displayTests(tests);
            }
            @Override public void onFailure(String error) { setupTestDataDefault(category); }
        });
    }

    private void displayTests(List<BaiKiemTra> tests) {
        llTestListContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (BaiKiemTra test : tests) {
            View itemView = inflater.inflate(R.layout.item_test_list, llTestListContainer, false);
            TextView tvListTitle = itemView.findViewById(R.id.tvListTitle);
            TextView tvTimeValue = itemView.findViewById(R.id.tvTimeValue);
            TextView tvQuestionValue = itemView.findViewById(R.id.tvQuestionValue);
            Button btnStart = itemView.findViewById(R.id.btnStart);
            ImageView ivListLock = itemView.findViewById(R.id.ivListLock);

            String displayName = test.getTenBKT();
            if (test.getLoaiKiemTra() != null && !test.getLoaiKiemTra().isEmpty()) {
                displayName += " " + test.getLoaiKiemTra();
            }
            tvListTitle.setText(displayName);
            tvTimeValue.setText(test.getThoiGianLamBai() + getString(R.string.auto_minute) + "  |  ");
            tvQuestionValue.setText(String.valueOf(test.getTongSoCau()));

            if (test.isLocked() && !isUserPremium) {
                ivListLock.setVisibility(View.VISIBLE);
                btnStart.setBackgroundResource(R.drawable.bg_test_btn_disabled);
                tvListTitle.setTextColor(Color.GRAY);
                View.OnClickListener lockListener = v -> Toast.makeText(this, getString(R.string.upgrade_required), Toast.LENGTH_SHORT).show();
                itemView.setOnClickListener(lockListener);
                btnStart.setOnClickListener(lockListener);
            } else {
                ivListLock.setVisibility(View.GONE);
                itemView.setOnClickListener(v -> openTestDetail(test.getTenBKT(), test.getThoiGianLamBai(), test.getTongSoCau()));
                btnStart.setOnClickListener(v -> openTestDetail(test.getTenBKT(), test.getThoiGianLamBai(), test.getTongSoCau()));
            }
            llTestListContainer.addView(itemView);
        }
    }

    private void setupTestDataDefault(String category) {
        int time = 120, questions = 200;
        String nameSuffix = " ETS 2024";
        
        if (CAT_FULLTEST.equals(category)) nameSuffix = " ETS 2023";
        else if (CAT_SPEAKING.equals(category)) { time = 70; questions = 16; nameSuffix = ""; }

        llTestListContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 1; i <= 10; i++) {
            View itemView = inflater.inflate(R.layout.item_test_list, llTestListContainer, false);
            TextView tvListTitle = itemView.findViewById(R.id.tvListTitle);
            TextView tvTimeValue = itemView.findViewById(R.id.tvTimeValue);
            TextView tvQuestionValue = itemView.findViewById(R.id.tvQuestionValue);
            Button btnStart = itemView.findViewById(R.id.btnStart);
            ImageView ivListLock = itemView.findViewById(R.id.ivListLock);

            String title = "Test " + i + nameSuffix;
            tvListTitle.setText(title);
            tvTimeValue.setText(time + getString(R.string.auto_minute) + "  |  ");
            tvQuestionValue.setText(String.valueOf(questions));

            boolean isLocked = i > 2;
            if (isLocked && !isUserPremium) {
                ivListLock.setVisibility(View.VISIBLE);
                btnStart.setBackgroundResource(R.drawable.bg_test_btn_disabled);
                tvListTitle.setTextColor(Color.GRAY);
            }

            int finalTime = time, finalQuestions = questions;
            View.OnClickListener clickListener = v -> {
                if (isLocked && !isUserPremium) Toast.makeText(this, getString(R.string.upgrade_required), Toast.LENGTH_SHORT).show();
                else openTestDetail(title, finalTime, finalQuestions);
            };
            itemView.setOnClickListener(clickListener);
            btnStart.setOnClickListener(clickListener);

            llTestListContainer.addView(itemView);
        }
    }

    private void openTestDetail(String title, int time, int questions) {
        Intent intent = new Intent(this, TestDetailActivity.class);
        intent.putExtra(TestDetailActivity.EXTRA_TITLE, title);
        intent.putExtra(TestDetailActivity.EXTRA_TIME, time);
        intent.putExtra(TestDetailActivity.EXTRA_QUESTIONS, questions);
        intent.putExtra(EXTRA_FOLDER_PREFIX, folderPrefix);
        startActivity(intent);
    }
}
