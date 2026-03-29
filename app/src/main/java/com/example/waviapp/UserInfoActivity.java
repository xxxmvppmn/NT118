package com.example.waviapp;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.waviapp.databinding.ActivityUserInfoBinding;
import com.google.android.material.navigation.NavigationBarView;

import java.util.Calendar;

public class UserInfoActivity extends AppCompatActivity {

    private ActivityUserInfoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // CHỐNG HIỆN BÀN PHÍM: Ép 2 ô này không nhận focus bàn phím
        binding.etDob.setInputType(InputType.TYPE_NULL);
        binding.etGender.setInputType(InputType.TYPE_NULL);

        // 1. Nút quay lại (Back)
        binding.ivBack.setOnClickListener(v -> finish());

        // 2. Click vào Ngày sinh
        binding.etDob.setOnClickListener(v -> showDateSelectionDialog());

        // 3. Click vào Giới tính
        binding.etGender.setOnClickListener(v -> showGenderSelectionDialog());

        // Khởi tạo trạng thái View mode
        setEditMode(false);

        // 4. Nút Chỉnh sửa
        binding.btnEdit.setOnClickListener(v -> setEditMode(true));

        binding.btnChangePassword.setOnClickListener(v -> {
            startActivity(new Intent(UserInfoActivity.this, ChangePasswordActivity.class));
        });

        binding.tvDeleteAccount.setPaintFlags(binding.tvDeleteAccount.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
        binding.tvDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());

        // Các nút trong Edit mode
        binding.btnSave.setOnClickListener(v -> {
            String name = binding.etName.getText().toString();
            Toast.makeText(this, "Đã lưu thông tin cho: " + name, Toast.LENGTH_SHORT).show();
            setEditMode(false);
        });

        binding.tvCancel.setPaintFlags(binding.tvCancel.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
        binding.tvCancel.setOnClickListener(v -> setEditMode(false));

        // 5. Bottom Navigation
        setupBottomNavigation();
    }

    private void setEditMode(boolean isEdit) {
        int viewVisibility = isEdit ? android.view.View.GONE : android.view.View.VISIBLE;
        int editVisibility = isEdit ? android.view.View.VISIBLE : android.view.View.GONE;

        binding.btnEdit.setVisibility(viewVisibility);
        binding.btnChangePassword.setVisibility(viewVisibility);
        binding.tvDeleteAccount.setVisibility(viewVisibility);

        binding.btnSave.setVisibility(editVisibility);
        binding.tvCancel.setVisibility(editVisibility);

        binding.etName.setFocusable(isEdit);
        binding.etName.setFocusableInTouchMode(isEdit);
        binding.etName.setCursorVisible(isEdit);

        binding.etPhone.setFocusable(isEdit);
        binding.etPhone.setFocusableInTouchMode(isEdit);
        binding.etPhone.setCursorVisible(isEdit);

        binding.etDob.setClickable(isEdit);
        binding.etGender.setClickable(isEdit);

        binding.etEmail.setFocusable(false);
        binding.etEmail.setFocusableInTouchMode(false);
        binding.etEmail.setCursorVisible(false);

        if (isEdit) {
            binding.etEmail.setBackgroundResource(R.drawable.bg_edittext_disabled);
            binding.etName.requestFocus();
        } else {
            binding.etEmail.setBackgroundResource(R.drawable.bg_edittext_outline_purple);
        }
    }

    private void showDeleteAccountDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_delete_account);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        dialog.findViewById(R.id.btnCancelDelete).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btnConfirmDelete).setOnClickListener(v -> {
            dialog.dismiss();
            Toast.makeText(this, "Tài khoản của bạn đã được xóa", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(UserInfoActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        dialog.show();
    }

    /**
     * Hộp thoại chọn Ngày/Tháng/Năm định dạng dd/mm/yyyy và ràng buộc ngày nhuận
     */
    private void showDateSelectionDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_date_picker);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9); // Rộng 90% màn hình
            dialog.getWindow().setAttributes(lp);
        }

        NumberPicker npDay = dialog.findViewById(R.id.npDay);
        NumberPicker npMonth = dialog.findViewById(R.id.npMonth);
        NumberPicker npYear = dialog.findViewById(R.id.npYear);

        // Cấu hình Năm
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        npYear.setMinValue(1950);
        npYear.setMaxValue(currentYear);
        npYear.setValue(2005);

        // Cấu hình Tháng (Hiển thị 01, 02... 12)
        String[] months = new String[12];
        for (int i = 0; i < 12; i++) {
            months[i] = String.format("%02d", i + 1);
        }
        npMonth.setMinValue(0);
        npMonth.setMaxValue(11);
        npMonth.setDisplayedValues(months);
        npMonth.setValue(1); // Mặc định tháng 02

        // Cấu hình Ngày ban đầu
        npDay.setMinValue(1);
        updateMaxDays(npDay, npMonth.getValue(), npYear.getValue());
        npDay.setValue(28);

        // LẮNG NGHE THAY ĐỔI ĐỂ RÀNG BUỘC NGÀY NHUẬN
        NumberPicker.OnValueChangeListener dateChangeListener = (picker, oldVal, newVal) -> {
            updateMaxDays(npDay, npMonth.getValue(), npYear.getValue());
        };
        npMonth.setOnValueChangedListener(dateChangeListener);
        npYear.setOnValueChangedListener(dateChangeListener);

        // Nút Close
        dialog.findViewById(R.id.ivClose).setOnClickListener(v -> dialog.dismiss());

        // Nút OK - Trả về định dạng dd/mm/yyyy
        dialog.findViewById(R.id.btnOk).setOnClickListener(v -> {
            String dayStr = String.format("%02d", npDay.getValue());
            String monthStr = months[npMonth.getValue()];
            String yearStr = String.valueOf(npYear.getValue());

            binding.etDob.setText(dayStr + "/" + monthStr + "/" + yearStr);
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * Hàm tính toán số ngày tối đa trong tháng (Xử lý tháng 30, 31 và năm nhuận)
     */
    private void updateMaxDays(NumberPicker npDay, int monthIndex, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthIndex);

        int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        npDay.setMaxValue(maxDays);

        // Nếu đang ở ngày 31 mà xoay sang tháng có 30 ngày thì tự nhảy về 30
        if (npDay.getValue() > maxDays) {
            npDay.setValue(maxDays);
        }
    }

    /**
     * Hộp thoại chọn Giới tính
     */
    private void showGenderSelectionDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_gender_selection);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        RadioGroup rgGender = dialog.findViewById(R.id.rgGender);
        RadioButton rbMale = dialog.findViewById(R.id.rbMale);
        RadioButton rbFemale = dialog.findViewById(R.id.rbFemale);

        String currentGender = binding.etGender.getText().toString();
        if (currentGender.equals("Nam")) rbMale.setChecked(true);
        else if (currentGender.equals("Nữ")) rbFemale.setChecked(true);

        rgGender.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbMale) {
                binding.etGender.setText("Nam");
            } else if (checkedId == R.id.rbFemale) {
                binding.etGender.setText("Nữ");
            }
            // Delay một chút để người dùng thấy animation trước khi đóng
            new android.os.Handler().postDelayed(dialog::dismiss, 300);
        });

        dialog.show();
    }

    /**
     * Xử lý Bottom Navigation
     */
    private void setupBottomNavigation() {
        binding.bottomNav.setSelectedItemId(R.id.nav_profile);

        binding.bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    startActivity(new Intent(UserInfoActivity.this, HomeActivity.class));
                } else if (id == R.id.nav_exam) {
                    startActivity(new Intent(UserInfoActivity.this, ExamActivity.class));
                } else if (id == R.id.nav_premium) {
                    startActivity(new Intent(UserInfoActivity.this, PremiumActivity.class));
                } else if (id == R.id.nav_setting) {
                    startActivity(new Intent(UserInfoActivity.this, SettingsActivity.class));
                } else if (id == R.id.nav_profile) {
                    return true;
                }

                overridePendingTransition(0, 0);
                finish();
                return true;
            }
        });
    }
}