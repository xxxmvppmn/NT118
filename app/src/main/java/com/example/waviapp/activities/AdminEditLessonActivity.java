package com.example.waviapp.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.example.waviapp.R;
import com.example.waviapp.firebase.DatabaseHelper;

import java.util.HashMap;
import java.util.Map;

public class AdminEditLessonActivity extends BaseAdminActivity {

    private EditText edtLessonCode, edtLessonName, edtLessonType, edtLessonDesc, edtLessonImage;
    private TextView txtTitle;
    private CardView cardPreview;
    private ImageView imgPreview;
    private Button btnSave;
    private DatabaseHelper dbHelper;
    private String maCD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_lesson); // Reuse the add lesson layout

        dbHelper = new DatabaseHelper();
        initViews();
        setupImagePreview();
        loadLessonData();
    }

    private void initViews() {
        txtTitle = findViewById(R.id.txtTitle);
        edtLessonCode = findViewById(R.id.edtLessonCode);
        edtLessonName = findViewById(R.id.edtLessonName);
        edtLessonType = findViewById(R.id.edtLessonType);
        edtLessonDesc = findViewById(R.id.edtLessonDesc);
        edtLessonImage = findViewById(R.id.edtLessonImage);
        cardPreview = findViewById(R.id.cardPreview);
        imgPreview = findViewById(R.id.imgPreview);
        btnSave = findViewById(R.id.btnSave);

        txtTitle.setText("Chỉnh sửa bài học");
        edtLessonCode.setEnabled(false); // Code cannot be edited (database document key)
        btnSave.setText("Cập nhật");

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> updateLesson());
    }

    private void setupImagePreview() {
        edtLessonImage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String url = s.toString().trim();
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    cardPreview.setVisibility(View.VISIBLE);
                    Glide.with(AdminEditLessonActivity.this)
                            .load(url)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_menu_report_image)
                            .into(imgPreview);
                } else {
                    cardPreview.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadLessonData() {
        // Removed redundant getIntent() != null check as Intent is never null in an Activity
        maCD = getIntent().getStringExtra("maCD");
        String tenCD = getIntent().getStringExtra("tenCD");
        String loaiChuDe = getIntent().getStringExtra("loaiChuDe");
        String moTa = getIntent().getStringExtra("moTa");
        String hinhAnh = getIntent().getStringExtra("hinhAnh");

        edtLessonCode.setText(maCD);
        edtLessonName.setText(tenCD);
        edtLessonType.setText(loaiChuDe);
        edtLessonDesc.setText(moTa);
        edtLessonImage.setText(hinhAnh);

        if (hinhAnh != null && (hinhAnh.startsWith("http://") || hinhAnh.startsWith("https://"))) {
            cardPreview.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(hinhAnh)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(imgPreview);
        }
    }

    private void updateLesson() {
        String name = edtLessonName.getText().toString().trim();
        String type = edtLessonType.getText().toString().trim();
        String desc = edtLessonDesc.getText().toString().trim();
        String image = edtLessonImage.getText().toString().trim();

        if (name.isEmpty() || type.isEmpty() || desc.isEmpty() || image.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("tenCD", name);
        updates.put("loaiChuDe", type);
        updates.put("moTa", desc);
        updates.put("hinhAnh", image);

        btnSave.setEnabled(false);
        dbHelper.updateLesson(maCD, updates, new DatabaseHelper.SimpleCallback() {
            @Override
            public void onSuccess() {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(AdminEditLessonActivity.this, "Cập nhật bài học thành công", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(String error) {
                if (isFinishing() || isDestroyed()) return;
                btnSave.setEnabled(true);
                Toast.makeText(AdminEditLessonActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
