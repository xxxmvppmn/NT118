package com.example.waviapp.activities;

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
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import com.example.waviapp.R;
import com.example.waviapp.databinding.ActivityUserInfoBinding;
import com.example.waviapp.firebase.DatabaseHelper;
import com.example.waviapp.firebase.FirebaseAuthHelper;
import com.example.waviapp.models.TaiKhoan;
import com.example.waviapp.utils.ImageUtils;
import com.google.firebase.auth.FirebaseUser;

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

        binding.ivEditAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        });

        binding.etDob.setInputType(InputType.TYPE_NULL);
        binding.etGender.setInputType(InputType.TYPE_NULL);
        binding.ivBack.setOnClickListener(v -> finish());
        binding.etDob.setOnClickListener(v -> showDateSelectionDialog());
        binding.etGender.setOnClickListener(v -> showGenderSelectionDialog());

        setEditMode(false);
        loadUserInfo();

        binding.btnEdit.setOnClickListener(v -> setEditMode(true));
        binding.btnChangePassword.setOnClickListener(v -> {
            startActivity(new Intent(UserInfoActivity.this, ChangePasswordActivity.class));
        });

        binding.tvDeleteAccount.setPaintFlags(binding.tvDeleteAccount.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
        binding.tvDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());

        binding.btnSave.setOnClickListener(v -> saveUserInfo());
        binding.tvCancel.setPaintFlags(binding.tvCancel.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
        binding.tvCancel.setOnClickListener(v -> {
            setEditMode(false);
            loadUserInfo();
        });

        setupBottomNavigation();
    }

    private void loadUserInfo() {
        FirebaseUser firebaseUser = authHelper.getCurrentUser();
        if (firebaseUser == null) return;

        binding.etEmail.setText(firebaseUser.getEmail());

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

                if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                    try {
                        byte[] decodedBytes = Base64.decode(user.getAvatarUrl(), Base64.DEFAULT);
                        binding.tvAvatar.setVisibility(View.GONE);
                        binding.ivAvatar.setVisibility(View.VISIBLE);

                        Glide.with(UserInfoActivity.this)
                                .asBitmap()
                                .load(decodedBytes)
                                .placeholder(R.drawable.ic_user_placeholder)
                                .error(R.drawable.ic_user_placeholder)
                                .circleCrop()
                                .into(binding.ivAvatar);
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to decode avatar", e);
                        binding.tvAvatar.setVisibility(View.VISIBLE);
                        binding.ivAvatar.setVisibility(View.GONE);
                    }
                } else {
                    binding.tvAvatar.setVisibility(View.VISIBLE);
                    binding.ivAvatar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(String error) {
                binding.tvAvatar.setVisibility(View.VISIBLE);
                binding.ivAvatar.setVisibility(View.GONE);
            }
        });
    }

    private void saveUserInfo() {
        FirebaseUser firebaseUser = authHelper.getCurrentUser();
        if (firebaseUser == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("hoTen", binding.etName.getText().toString().trim());
        updates.put("sdt", binding.etPhone.getText().toString().trim());
        updates.put("dob", binding.etDob.getText().toString().trim());
        updates.put("gender", binding.etGender.getText().toString().trim());

        dbHelper.updateUser(firebaseUser.getUid(), updates, new DatabaseHelper.SimpleCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(UserInfoActivity.this, "Đã lưu thông tin!", Toast.LENGTH_SHORT).show();
                setEditMode(false);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(UserInfoActivity.this, "Lưu thất bại: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void convertImageToBase64AndSave(Uri uri) {
        FirebaseUser currentUser = authHelper.getCurrentUser();
        if (currentUser == null || uri == null) return;

        Toast.makeText(this, "Đang xử lý ảnh đại diện...", Toast.LENGTH_SHORT).show();

        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) inputStream.close();

            if (originalBitmap != null) {
                binding.tvAvatar.setVisibility(View.GONE);
                binding.ivAvatar.setVisibility(View.VISIBLE);
                binding.ivAvatar.setImageBitmap(originalBitmap);
                String base64String = ImageUtils.compressAndConvertToBase64(originalBitmap);
                saveBase64ToFirestore(base64String);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error processing image", e);
        }
    }

    private void saveBase64ToFirestore(String base64) {
        FirebaseUser currentUser = authHelper.getCurrentUser();
        if (currentUser == null) return;

        Map<String, Object> map = new HashMap<>();
        map.put("avatarUrl", base64);

        dbHelper.updateUser(currentUser.getUid(), map, new DatabaseHelper.SimpleCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(UserInfoActivity.this, "Cập nhật ảnh thành công!", Toast.LENGTH_SHORT).show();
                /*try {
                    byte[] decodedBytes = Base64.decode(base64, Base64.DEFAULT);
                    binding.tvAvatar.setVisibility(View.GONE);
                    binding.ivAvatar.setVisibility(View.VISIBLE);
                    Glide.with(UserInfoActivity.this)
                            .asBitmap()
                            .load(decodedBytes)
                            .circleCrop()
                            .into(binding.ivAvatar);
                } catch (Exception e) {
                    Log.e(TAG, "Error displaying new avatar", e);
                } */
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(UserInfoActivity.this, "Lưu ảnh thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setEditMode(boolean isEdit) {
        int viewVisibility = isEdit ? View.GONE : View.VISIBLE;
        int editVisibility = isEdit ? View.VISIBLE : View.GONE;

        binding.btnEdit.setVisibility(viewVisibility);
        binding.btnChangePassword.setVisibility(viewVisibility);
        binding.tvDeleteAccount.setVisibility(viewVisibility);
        binding.btnSave.setVisibility(editVisibility);
        binding.tvCancel.setVisibility(editVisibility);

        binding.etName.setFocusable(isEdit);
        binding.etName.setFocusableInTouchMode(isEdit);
        binding.etPhone.setFocusable(isEdit);
        binding.etPhone.setFocusableInTouchMode(isEdit);
        binding.etDob.setClickable(isEdit);
        binding.etGender.setClickable(isEdit);

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
                dbHelper.deleteUser(firebaseUser.getUid(), null);
                authHelper.deleteAccount(new FirebaseAuthHelper.AuthCallback() {
                    @Override
                    public void onSuccess(FirebaseUser user) {
                        startActivity(new Intent(UserInfoActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        finish();
                    }
                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(UserInfoActivity.this, "Lỗi khi xóa tài khoản", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        dialog.show();
    }

    private void showDateSelectionDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_date_picker);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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
        for (int i = 0; i < 12; i++) months[i] = String.format("%02d", i + 1);
        npMonth.setMinValue(0); npMonth.setMaxValue(11);
        npMonth.setDisplayedValues(months); npMonth.setValue(1);

        npDay.setMinValue(1);
        updateMaxDays(npDay, npMonth.getValue(), npYear.getValue());
        npDay.setValue(28);

        NumberPicker.OnValueChangeListener dateChangeListener = (picker, oldVal, newVal) -> updateMaxDays(npDay, npMonth.getValue(), npYear.getValue());
        npMonth.setOnValueChangedListener(dateChangeListener);
        npYear.setOnValueChangedListener(dateChangeListener);

        dialog.findViewById(R.id.ivClose).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btnOk).setOnClickListener(v -> {
            binding.etDob.setText(String.format("%02d/%s/%d", npDay.getValue(), months[npMonth.getValue()], npYear.getValue()));
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
        if (npDay.getValue() > maxDays) npDay.setValue(maxDays);
    }

    private void showGenderSelectionDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_gender_selection);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }
        RadioGroup rgGender = dialog.findViewById(R.id.rgGender);
        if (binding.etGender.getText().toString().equals("Nam")) ((RadioButton) dialog.findViewById(R.id.rbMale)).setChecked(true);
        else if (binding.etGender.getText().toString().equals("Nữ")) ((RadioButton) dialog.findViewById(R.id.rbFemale)).setChecked(true);

        rgGender.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton rb = dialog.findViewById(checkedId);
            if (rb != null) binding.etGender.setText(rb.getText());
            dialog.dismiss();
        });
        dialog.show();
    }

    private void setupBottomNavigation() {
        binding.bottomNav.setSelectedItemId(R.id.nav_profile);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) startActivity(new Intent(this, HomeActivity.class));
            else if (id == R.id.nav_exam) startActivity(new Intent(this, ExamActivity.class));
            else if (id == R.id.nav_premium) startActivity(new Intent(this, PremiumActivity.class));
            else if (id == R.id.nav_setting) startActivity(new Intent(this, SettingsActivity.class));
            else if (id == R.id.nav_profile) return true;
            finish();
            return true;
        });
    }
}