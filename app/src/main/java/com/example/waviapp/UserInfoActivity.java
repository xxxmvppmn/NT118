package com.example.waviapp;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.waviapp.databinding.ActivityUserInfoBinding;
import com.example.waviapp.firebase.DatabaseHelper;
import com.example.waviapp.firebase.FirebaseAuthHelper;
import com.example.waviapp.models.TaiKhoan;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class UserInfoActivity extends BaseActivity {

    private static final String TAG = "UserInfoActivity";
    private ActivityUserInfoBinding binding;
    private FirebaseAuthHelper authHelper;
    private DatabaseHelper dbHelper;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        // Convert to Base64 and save
                        convertImageToBase64AndSave(imageUri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authHelper = new FirebaseAuthHelper();
        dbHelper = new DatabaseHelper();
        binding.layoutAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        });

        // Ngân cũng có thể cho click vào nút camera nhỏ (ivEditAvatar)
        binding.ivEditAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        });
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
     * Load thông tin user từ Firebase và hiển thị avatar nếu có
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
                if (user.getSdt() != null && !user.getSdt().isEmpty()) {
                    binding.etPhone.setText(user.getSdt());
                }
                if (user.getDob() != null && !user.getDob().isEmpty()) {
                    binding.etDob.setText(user.getDob());
                }
                if (user.getGender() != null && !user.getGender().isEmpty()) {
                    binding.etGender.setText(user.getGender());
                }

                // Load avatar Base64 if available
                if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                    Log.d(TAG, "Loading avatar Base64");
                    binding.tvAvatar.setVisibility(android.view.View.GONE);
                    binding.ivAvatar.setVisibility(android.view.View.VISIBLE);
                    // Decode Base64 to Bitmap and display
                    try {
                        byte[] decodedBytes = Base64.decode(user.getAvatarUrl(), Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                        Glide.with(UserInfoActivity.this)
                                .load(bitmap)
                                .circleCrop()
                                .into(binding.ivAvatar);
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to decode Base64 avatar", e);
                        // Fallback to initials
                        binding.tvAvatar.setVisibility(android.view.View.VISIBLE);
                        binding.ivAvatar.setVisibility(android.view.View.GONE);
                    }
                } else {
                    // Show default initials
                    binding.tvAvatar.setVisibility(android.view.View.VISIBLE);
                    binding.ivAvatar.setVisibility(android.view.View.GONE);
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to load user info: " + error);
                // Nếu không load được, hiển thị tên từ Firebase Auth
                if (firebaseUser.getDisplayName() != null && !firebaseUser.getDisplayName().isEmpty()) {
                    binding.etName.setText(firebaseUser.getDisplayName());
                    binding.tvAvatar.setText(firebaseUser.getDisplayName().substring(0, 1).toUpperCase());
                }
                binding.tvAvatar.setVisibility(android.view.View.VISIBLE);
                binding.ivAvatar.setVisibility(android.view.View.GONE);
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
        updates.put("sdt", phone);
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

    /**
     * Convert image to Base64 and save to Firestore
     */
    private void convertImageToBase64AndSave(Uri uri) {
        FirebaseUser currentUser = authHelper.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "Current user is null, cannot save image");
            Toast.makeText(this, "Lỗi: Người dùng không xác định", Toast.LENGTH_SHORT).show();
            return;
        }

        if (uri == null) {
            Log.e(TAG, "Image URI is null");
            Toast.makeText(this, "Lỗi: Không thể tìm thấy ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Converting image to Base64 for user: " + currentUser.getUid());

        // Show progress toast
        Toast.makeText(this, "Đang xử lý ảnh đại diện...", Toast.LENGTH_SHORT).show();

        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                Toast.makeText(this, "Lỗi: Không thể đọc ảnh", Toast.LENGTH_SHORT).show();
                return;
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();

            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            String base64String = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            Log.d(TAG, "Image converted to Base64, length: " + base64String.length());

            // Save Base64 to Firestore
            saveBase64ToFirestore(base64String);

        } catch (IOException e) {
            Log.e(TAG, "Failed to convert image to Base64", e);
            Toast.makeText(this, "Lỗi xử lý ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Save Base64 string to Firestore/Realtime Database
     */
    private void saveBase64ToFirestore(String base64) {
        FirebaseUser currentUser = authHelper.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "Cannot save Base64: Current user is null");
            return;
        }

        String uid = currentUser.getUid();
        Map<String, Object> map = new HashMap<>();
        map.put("avatarUrl", base64);

        Log.d(TAG, "Saving Base64 avatar to database for user: " + uid);

        dbHelper.updateUser(uid, map, new DatabaseHelper.SimpleCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Base64 avatar saved successfully");
                Toast.makeText(UserInfoActivity.this, "Đã cập nhật ảnh đại diện!", Toast.LENGTH_SHORT).show();
                // Decode and display immediately
                try {
                    byte[] decodedBytes = Base64.decode(base64, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    binding.tvAvatar.setVisibility(android.view.View.GONE);
                    binding.ivAvatar.setVisibility(android.view.View.VISIBLE);
                    Glide.with(UserInfoActivity.this)
                            .load(bitmap)
                            .circleCrop()
                            .into(binding.ivAvatar);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to decode Base64 after save", e);
                }
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to save Base64 avatar: " + error);
                Toast.makeText(UserInfoActivity.this, "Lỗi lưu ảnh: " + error, Toast.LENGTH_SHORT).show();
            }
        });
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
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        RadioGroup rgGender = dialog.findViewById(R.id.rgGender);
        String currentGender = binding.etGender.getText().toString();
        if (currentGender.equals("Nam")) {
            ((RadioButton) dialog.findViewById(R.id.rbMale)).setChecked(true);
        } else if (currentGender.equals("Nữ")) {
            ((RadioButton) dialog.findViewById(R.id.rbFemale)).setChecked(true);
        }

        rgGender.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton rb = dialog.findViewById(checkedId);
            if (rb != null) {
                binding.etGender.setText(rb.getText());
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private void setupBottomNavigation() {
        binding.bottomNav.setSelectedItemId(R.id.nav_profile);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(UserInfoActivity.this, HomeActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_exam) {
                startActivity(new Intent(UserInfoActivity.this, ExamActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_premium) {
                startActivity(new Intent(UserInfoActivity.this, PremiumActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_setting) {
                startActivity(new Intent(UserInfoActivity.this, SettingsActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }
}
