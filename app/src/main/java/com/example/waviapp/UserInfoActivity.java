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
import com.example.waviapp.firebase.DatabaseHelper;
import com.example.waviapp.firebase.FirebaseAuthHelper;
import com.example.waviapp.models.TaiKhoan;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class UserInfoActivity extends AppCompatActivity {

    private ActivityUserInfoBinding binding;
    private FirebaseAuthHelper authHelper;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authHelper = new FirebaseAuthHelper();
        dbHelper = new DatabaseHelper();

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

        // 4. Load thông tin user từ Firebase
        loadUserInfo();

        // 5. Nút Chỉnh sửa
        binding.btnEdit.setOnClickListener(v -> setEditMode(true));

        binding.btnChangePassword.setOnClickListener(v -> {
            startActivity(new Intent(UserInfoActivity.this, ChangePasswordActivity.class));
        });

        binding.tvDeleteAccount.setPaintFlags(binding.tvDeleteAccount.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
        binding.tvDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());

        // Các nút trong Edit mode
        binding.btnSave.setOnClickListener(v -> saveUserInfo());

        binding.tvCancel.setPaintFlags(binding.tvCancel.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
        binding.tvCancel.setOnClickListener(v -> {
            setEditMode(false);
            loadUserInfo(); // Reload dữ liệu gốc
        });

        // 6. Bottom Navigation
        setupBottomNavigation();
    }

    /**
     * Load thông tin user từ Firebase
     */
    private void loadUserInfo() {
        FirebaseUser firebaseUser = authHelper.getCurrentUser();
        if (firebaseUser == null) return;

        // Hiển thị email từ Firebase Auth
        binding.etEmail.setText(firebaseUser.getEmail());

        // Load thêm thông tin từ Realtime Database
        dbHelper.getUser(firebaseUser.getUid(), new DatabaseHelper.UserCallback() {
            @Override
            public void onSuccess(TaiKhoan user) {
                if (user.getHoTen() != null && !user.getHoTen().isEmpty()) {
                    binding.etName.setText(user.getHoTen());
                    binding.tvAvatar.setText(user.getHoTen().substring(0, 1).toUpperCase());
                }
                if (user.getPhone() != null && !user.getPhone().isEmpty()) {
                    binding.etPhone.setText(user.getPhone());
                }
                if (user.getDob() != null && !user.getDob().isEmpty()) {
                    binding.etDob.setText(user.getDob());
                }
                if (user.getGender() != null && !user.getGender().isEmpty()) {
                    binding.etGender.setText(user.getGender());
                }
            }

            @Override
            public void onFailure(String error) {
                // Nếu không load được, hiển thị tên từ Firebase Auth
                if (firebaseUser.getDisplayName() != null && !firebaseUser.getDisplayName().isEmpty()) {
                    binding.etName.setText(firebaseUser.getDisplayName());
                    binding.tvAvatar.setText(firebaseUser.getDisplayName().substring(0, 1).toUpperCase());
                }
            }
        });
    }

    /**
     * Lưu thông tin user lên Firebase
     */
    private void saveUserInfo() {
        FirebaseUser firebaseUser = authHelper.getCurrentUser();
        if (firebaseUser == null) return;

        String name = binding.etName.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String dob = binding.etDob.getText().toString().trim();
        String gender = binding.etGender.getText().toString().trim();

        Map<String, Object> updates = new HashMap<>();
        updates.put("hoTen", name);
        updates.put("phone", phone);
        updates.put("dob", dob);
        updates.put("gender", gender);

        dbHelper.updateUser(firebaseUser.getUid(), updates, new DatabaseHelper.SimpleCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(UserInfoActivity.this, "Đã lưu thông tin thành công!", Toast.LENGTH_SHORT).show();
                setEditMode(false);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(UserInfoActivity.this, "Lưu thất bại: " + error, Toast.LENGTH_SHORT).show();
            }
        });
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

            FirebaseUser firebaseUser = authHelper.getCurrentUser();
            if (firebaseUser != null) {
                // Xóa dữ liệu trong database trước
                dbHelper.deleteUser(firebaseUser.getUid(), null);

                // Xóa tài khoản Firebase Auth
                authHelper.deleteAccount(new FirebaseAuthHelper.AuthCallback() {
                    @Override
                    public void onSuccess(FirebaseUser user) {
                        Toast.makeText(UserInfoActivity.this, "Tài khoản đã được xóa", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(UserInfoActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(UserInfoActivity.this, "Xóa thất bại. Vui lòng đăng xuất, đăng nhập lại và thử lại. Mật khẩu có thể đã cũ.", Toast.LENGTH_LONG).show();
                    }
                });
            }
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
            lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            dialog.getWindow().setAttributes(lp);
        }

        NumberPicker npDay = dialog.findViewById(R.id.npDay);
        NumberPicker npMonth = dialog.findViewById(R.id.npMonth);
        NumberPicker npYear = dialog.findViewById(R.id.npYear);

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        npYear.setMinValue(1950);
        npYear.setMaxValue(currentYear);
        npYear.setValue(2005);

        String[] months = new String[12];
        for (int i = 0; i < 12; i++) {
            months[i] = String.format("%02d", i + 1);
        }
        npMonth.setMinValue(0);
        npMonth.setMaxValue(11);
        npMonth.setDisplayedValues(months);
        npMonth.setValue(1);

        npDay.setMinValue(1);
        updateMaxDays(npDay, npMonth.getValue(), npYear.getValue());
        npDay.setValue(28);

        NumberPicker.OnValueChangeListener dateChangeListener = (picker, oldVal, newVal) -> {
            updateMaxDays(npDay, npMonth.getValue(), npYear.getValue());
        };
        npMonth.setOnValueChangedListener(dateChangeListener);
        npYear.setOnValueChangedListener(dateChangeListener);

        dialog.findViewById(R.id.ivClose).setOnClickListener(v -> dialog.dismiss());

        dialog.findViewById(R.id.btnOk).setOnClickListener(v -> {
            String dayStr = String.format("%02d", npDay.getValue());
            String monthStr = months[npMonth.getValue()];
            String yearStr = String.valueOf(npYear.getValue());

            binding.etDob.setText(dayStr + "/" + monthStr + "/" + yearStr);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateMaxDays(NumberPicker npDay, int monthIndex, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthIndex);

        int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        npDay.setMaxValue(maxDays);

        if (npDay.getValue() > maxDays) {
            npDay.setValue(maxDays);
        }
    }

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
            new android.os.Handler().postDelayed(dialog::dismiss, 300);
        });

        dialog.show();
    }

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