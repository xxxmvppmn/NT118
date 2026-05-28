package com.example.waviapp.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.example.waviapp.R;
import com.example.waviapp.firebase.DatabaseHelper;
import com.example.waviapp.models.ChuDe;

public class AdminAddLessonActivity extends BaseAdminActivity {

    private EditText edtLessonCode, edtLessonName, edtLessonType, edtLessonDesc, edtLessonImage;
    private CardView cardPreview;
    private ImageView imgPreview;
    private Button btnSave;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_lesson);

        dbHelper = new DatabaseHelper();
        initViews();
        setupImagePreview();
    }

    private void initViews() {
        edtLessonCode = findViewById(R.id.edtLessonCode);
        edtLessonName = findViewById(R.id.edtLessonName);
        edtLessonType = findViewById(R.id.edtLessonType);
        edtLessonDesc = findViewById(R.id.edtLessonDesc);
        edtLessonImage = findViewById(R.id.edtLessonImage);
        cardPreview = findViewById(R.id.cardPreview);
        imgPreview = findViewById(R.id.imgPreview);
        btnSave = findViewById(R.id.btnSave);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveLesson());
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
                    Glide.with(AdminAddLessonActivity.this)
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

    private void saveLesson() {
        String code = edtLessonCode.getText().toString().trim();
        String name = edtLessonName.getText().toString().trim();
        String type = edtLessonType.getText().toString().trim();
        String desc = edtLessonDesc.getText().toString().trim();
        String image = edtLessonImage.getText().toString().trim();

        if (code.isEmpty() || name.isEmpty() || type.isEmpty() || desc.isEmpty() || image.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        ChuDe lesson = new ChuDe();
        lesson.setMaCD(code);
        lesson.setTenCD(name);
        lesson.setLoaiChuDe(type);
        lesson.setMoTa(desc);
        lesson.setHinhAnh(image);
        // Note: ngayTao is annotated with @ServerTimestamp in ChuDe.java, 
        // so leaving it null allows Firestore to write Google's server timestamp on save!

        btnSave.setEnabled(false);
        dbHelper.addLesson(lesson, new DatabaseHelper.SimpleCallback() {
            @Override
            public void onSuccess() {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(AdminAddLessonActivity.this, "Đã thêm bài học thành công", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(String error) {
                if (isFinishing() || isDestroyed()) return;
                btnSave.setEnabled(true);
                Toast.makeText(AdminAddLessonActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
