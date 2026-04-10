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
    public static final String CAT_FULLTEST = "TOEIC Listening & Reading Fulltest";
    public static final String CAT_MINITEST = "TOEIC Listening & Reading Minitest";
    public static final String CAT_SPEAKING = "TOEIC Speaking & Writing";

    private LinearLayout llTestListContainer;
    private TextView tvToolbarTitle;
    private ImageView ivBack;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_list);

        dbHelper = new DatabaseHelper();

        llTestListContainer = findViewById(R.id.llTestListContainer);
        tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        ivBack = findViewById(R.id.ivBack);

        ivBack.setOnClickListener(v -> finish());

        String category = getIntent().getStringExtra(EXTRA_CATEGORY);
        if (category == null) {
            category = CAT_MINITEST;
        }

        tvToolbarTitle.setText(category);
        
        String finalCategory = category;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            dbHelper.getUser(user.getUid(), new DatabaseHelper.UserCallback() {
                @Override
                public void onSuccess(TaiKhoan taiKhoan) {
                    isUserPremium = taiKhoan.isPremium();
                    loadTestsFromFirebase(finalCategory);
                }

                @Override
                public void onFailure(String error) {
                    isUserPremium = false;
                    loadTestsFromFirebase(finalCategory);
                }
            });
        } else {
            isUserPremium = false;
            loadTestsFromFirebase(finalCategory);
        }
    }

    private boolean isUserPremium = false;

    private void loadTestsFromFirebase(String category) {
        // Map category title → Firebase key
        String firebaseKey;
        if (CAT_FULLTEST.equals(category)) {
            firebaseKey = "fulltest";
        } else if (CAT_SPEAKING.equals(category)) {
            firebaseKey = "speaking";
        } else {
            firebaseKey = "minitest";
        }

        dbHelper.getTests(firebaseKey, new DatabaseHelper.TestCallback() {
            @Override
            public void onSuccess(List<BaiKiemTra> tests) {
                if (tests.isEmpty()) {
                    setupTestDataDefault(category);
                    return;
                }
                
                // Sắp xếp các bài test theo số thứ tự (ví dụ từ fc_1 đến fc_10)
                java.util.Collections.sort(tests, new java.util.Comparator<BaiKiemTra>() {
                    @Override
                    public int compare(BaiKiemTra t1, BaiKiemTra t2) {
                        try {
                            String num1 = t1.getMaBKT().replaceAll("\\D+", "");
                            String num2 = t2.getMaBKT().replaceAll("\\D+", "");
                            int n1 = num1.isEmpty() ? 0 : Integer.parseInt(num1);
                            int n2 = num2.isEmpty() ? 0 : Integer.parseInt(num2);
                            return Integer.compare(n1, n2);
                        } catch (Exception e) {
                            return t1.getMaBKT().compareTo(t2.getMaBKT());
                        }
                    }
                });

                llTestListContainer.removeAllViews();
                LayoutInflater inflater = LayoutInflater.from(TestListActivity.this);

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
                        int grayColor = Color.parseColor("#9E9E9E");
                        tvListTitle.setTextColor(grayColor);
                        itemView.setOnClickListener(v -> Toast.makeText(TestListActivity.this, getString(R.string.upgrade_required), Toast.LENGTH_SHORT).show());
                        btnStart.setOnClickListener(v -> Toast.makeText(TestListActivity.this, getString(R.string.upgrade_required), Toast.LENGTH_SHORT).show());
                    } else {
                        ivListLock.setVisibility(View.GONE);
                        String finalTitle = test.getTenBKT();
                        itemView.setOnClickListener(v -> openTestDetail(finalTitle, test.getThoiGianLamBai(), test.getTongSoCau()));
                        btnStart.setOnClickListener(v -> openTestDetail(finalTitle, test.getThoiGianLamBai(), test.getTongSoCau()));
                    }

                    llTestListContainer.addView(itemView);
                }
            }

            @Override
            public void onFailure(String error) {
                setupTestDataDefault(category);
            }
        });
    }

    private void setupTestDataDefault(String category) {
        LayoutInflater inflater = LayoutInflater.from(this);

        int time = 60;
        int questions = 100;
        int startId = 1;
        boolean reverseOffset = false;
        String nameSuffix = "";

        if (CAT_FULLTEST.equals(category)) {
            time = 120;
            questions = 200;
            nameSuffix = " ETS 2023";
        } else if (CAT_SPEAKING.equals(category)) {
            time = 70;
            questions = 16;
        }

        int limit = 10;

        for (int i = 1; i <= limit; i++) {
            int currentId = i; // Sort normally 1 to 10
            boolean isLocked = i > 5; // Default locking pattern

            View itemView = inflater.inflate(R.layout.item_test_list, llTestListContainer, false);

            TextView tvListTitle = itemView.findViewById(R.id.tvListTitle);
            TextView tvTimeValue = itemView.findViewById(R.id.tvTimeValue);
            TextView tvQuestionValue = itemView.findViewById(R.id.tvQuestionValue);
            Button btnStart = itemView.findViewById(R.id.btnStart);
            ImageView ivListLock = itemView.findViewById(R.id.ivListLock);

            tvListTitle.setText("Test " + currentId + nameSuffix);
            tvTimeValue.setText(time + getString(R.string.auto_minute) + "  |  ");
            tvQuestionValue.setText(String.valueOf(questions));

            if (isLocked && !isUserPremium) {
                ivListLock.setVisibility(View.VISIBLE);
                btnStart.setBackgroundResource(R.drawable.bg_test_btn_disabled);
                int grayColor = Color.parseColor("#9E9E9E");
                tvListTitle.setTextColor(grayColor);
            } else {
                ivListLock.setVisibility(View.GONE);
            }

            final String title = "Test " + currentId + nameSuffix;
            final int fTime = time;
            final int fQuestions = questions;
            
            if (isLocked && !isUserPremium) {
                itemView.setOnClickListener(v -> Toast.makeText(TestListActivity.this, getString(R.string.upgrade_required), Toast.LENGTH_SHORT).show());
                btnStart.setOnClickListener(v -> Toast.makeText(TestListActivity.this, getString(R.string.upgrade_required), Toast.LENGTH_SHORT).show());
            } else {
                itemView.setOnClickListener(v -> openTestDetail(title, fTime, fQuestions));
                btnStart.setOnClickListener(v -> openTestDetail(title, fTime, fQuestions));
            }

            llTestListContainer.addView(itemView);
        }
    }

    private void openTestDetail(String title, int time, int questions) {
        Intent intent = new Intent(TestListActivity.this, TestDetailActivity.class);
        intent.putExtra(TestDetailActivity.EXTRA_TITLE, title);
        intent.putExtra(TestDetailActivity.EXTRA_TIME, time);
        intent.putExtra(TestDetailActivity.EXTRA_QUESTIONS, questions);
        startActivity(intent);
    }
}

