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
import com.example.waviapp.managers.UserSessionManager;
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
                    if (imageUri != null) convertImageToBase64AndSave(imageUri);
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

        binding.layoutAvatar.setOnClickListener(v -> pickImage());
        binding.ivEditAvatar.setOnClickListener(v -> pickImage());
        binding.etDob.setInputType(InputType.TYPE_NULL);
        binding.etGender.setInputType(InputType.TYPE_NULL);
        binding.ivBack.setOnClickListener(v -> finish());
        binding.etDob.setOnClickListener(v -> showDateSelectionDialog());
        binding.etGender.setOnClickListener(v -> showGenderSelectionDialog());

        setEditMode(false);
        UserSessionManager.getInstance().getUserLiveData().observe(this, this::updateUI);
        
        if (UserSessionManager.getInstance().getUserData() == null) {
            FirebaseUser firebaseUser = authHelper.getCurrentUser();
            if (firebaseUser != null) UserSessionManager.getInstance().fetchUserData(firebaseUser.getUid(), success -> { if (!success) showContent(); });
            else showContent();
        } else showContent();

        binding.btnEdit.setOnClickListener(v -> setEditMode(true));
        binding.btnChangePassword.setOnClickListener(v -> startActivity(new Intent(UserInfoActivity.this, ChangePasswordActivity.class)));
        binding.tvDeleteAccount.setPaintFlags(binding.tvDeleteAccount.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
        binding.tvDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
        binding.btnSave.setOnClickListener(v -> saveUserInfo());
        binding.tvCancel.setPaintFlags(binding.tvCancel.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
        binding.tvCancel.setOnClickListener(v -> setEditMode(false));

        setupBottomNavigation();
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) return false;
            
            Intent intent = null;
            if (id == R.id.nav_home) intent = new Intent(this, HomeActivity.class);
            else if (id == R.id.nav_exam) intent = new Intent(this, ExamActivity.class);
            else if (id == R.id.nav_premium) intent = new Intent(this, PremiumActivity.class);
            else if (id == R.id.nav_setting) intent = new Intent(this, SettingsActivity.class);

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void updateUI(TaiKhoan user) {
        if (user == null) return;
        binding.etEmail.setText(user.getEmail());
        if (user.getHoTen() != null && !user.getHoTen().isEmpty()) {
            binding.etName.setText(user.getHoTen());
            binding.tvAvatar.setText(user.getHoTen().substring(0, 1).toUpperCase());
        }
        if (user.getSdt() != null && !user.getSdt().isEmpty()) binding.etPhone.setText(user.getSdt());
        if (user.getDob() != null && !user.getDob().isEmpty()) binding.etDob.setText(user.getDob());
        if (user.getGender() != null && !user.getGender().isEmpty()) binding.etGender.setText(user.getGender());

        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(user.getAvatarUrl(), Base64.DEFAULT);
                binding.tvAvatar.setVisibility(View.GONE); binding.ivAvatar.setVisibility(View.VISIBLE);
                Glide.with(UserInfoActivity.this).asBitmap().load(decodedBytes).placeholder(R.drawable.ic_user_placeholder).error(R.drawable.ic_user_placeholder).circleCrop().into(binding.ivAvatar);
            } catch (Exception e) {
                Log.e(TAG, "Failed to decode avatar", e);
                binding.tvAvatar.setVisibility(View.VISIBLE); binding.ivAvatar.setVisibility(View.GONE);
            }
        } else {
            binding.tvAvatar.setVisibility(View.VISIBLE); binding.ivAvatar.setVisibility(View.GONE);
        }
        showContent();
    }

    private void showContent() {
        if (binding.mainContent.getVisibility() == View.VISIBLE) return;
        binding.layoutLoading.animate().alpha(0f).setDuration(300).withEndAction(() -> {
            binding.layoutLoading.setVisibility(View.GONE);
            binding.mainContent.setVisibility(View.VISIBLE);
            binding.mainContent.setAlpha(0f);
            binding.mainContent.animate().alpha(1f).setDuration(300).start();
        }).start();
    }

    private void saveUserInfo() {
        FirebaseUser firebaseUser = authHelper.getCurrentUser();
        if (firebaseUser == null) return;
        TaiKhoan user = UserSessionManager.getInstance().getUserData();
        if (user == null) return;

        String newName = binding.etName.getText().toString().trim();
        String newPhone = binding.etPhone.getText().toString().trim();
        String newDob = binding.etDob.getText().toString().trim();
        String newGender = binding.etGender.getText().toString().trim();

        Map<String, Object> updates = new HashMap<>();
        updates.put("hoTen", newName); updates.put("sdt", newPhone); updates.put("dob", newDob); updates.put("gender", newGender);

        dbHelper.updateUser(firebaseUser.getUid(), updates, new DatabaseHelper.SimpleCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(UserInfoActivity.this, "Đã lưu!", Toast.LENGTH_SHORT).show();
                user.setHoTen(newName); user.setSdt(newPhone); user.setDob(newDob); user.setGender(newGender);
                UserSessionManager.getInstance().updateUserDataLocally(user);
                setEditMode(false);
            }
            @Override
            public void onFailure(String error) { Toast.makeText(UserInfoActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show(); }
        });
    }

    private void convertImageToBase64AndSave(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) inputStream.close();
            if (bitmap != null) saveBase64ToFirestore(ImageUtils.compressAndConvertToBase64(bitmap));
        } catch (IOException e) { Log.e(TAG, "Error image", e); }
    }

    private void saveBase64ToFirestore(String base64) {
        FirebaseUser currentUser = authHelper.getCurrentUser();
        if (currentUser == null) return;
        Map<String, Object> map = new HashMap<>(); map.put("avatarUrl", base64);
        dbHelper.updateUser(currentUser.getUid(), map, new DatabaseHelper.SimpleCallback() {
            @Override
            public void onSuccess() {
                TaiKhoan user = UserSessionManager.getInstance().getUserData();
                if (user != null) { user.setAvatarUrl(base64); UserSessionManager.getInstance().updateUserDataLocally(user); }
            }
            @Override
            public void onFailure(String error) {}
        });
    }

    private void setEditMode(boolean isEdit) {
        int vVis = isEdit ? View.GONE : View.VISIBLE, eVis = isEdit ? View.VISIBLE : View.GONE;
        binding.btnEdit.setVisibility(vVis); binding.btnChangePassword.setVisibility(vVis); binding.tvDeleteAccount.setVisibility(vVis);
        binding.btnSave.setVisibility(eVis); binding.tvCancel.setVisibility(eVis);
        binding.etName.setFocusable(isEdit); binding.etName.setFocusableInTouchMode(isEdit);
        binding.etPhone.setFocusable(isEdit); binding.etPhone.setFocusableInTouchMode(isEdit);
        binding.etDob.setClickable(isEdit); binding.etGender.setClickable(isEdit);
    }

    private void showDeleteAccountDialog() {
        Dialog dialog = new Dialog(this); dialog.setContentView(R.layout.dialog_delete_account);
        if (dialog.getWindow() != null) { dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT); }
        dialog.findViewById(R.id.btnCancelDelete).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btnConfirmDelete).setOnClickListener(v -> {
            dialog.dismiss(); FirebaseUser fbUser = authHelper.getCurrentUser();
            if (fbUser != null) {
                dbHelper.deleteUser(fbUser.getUid(), null);
                authHelper.deleteAccount(new FirebaseAuthHelper.AuthCallback() {
                    @Override public void onSuccess(FirebaseUser u) { UserSessionManager.getInstance().clearSession(); startActivity(new Intent(UserInfoActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)); finish(); }
                    @Override public void onFailure(String e) {}
                });
            }
        });
        dialog.show();
    }

    private void showDateSelectionDialog() {
        Dialog dialog = new Dialog(this); dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); dialog.setContentView(R.layout.dialog_date_picker);
        if (dialog.getWindow() != null) { dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); WindowManager.LayoutParams lp = new WindowManager.LayoutParams(); lp.copyFrom(dialog.getWindow().getAttributes()); lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9); dialog.getWindow().setAttributes(lp); }
        NumberPicker npDay = dialog.findViewById(R.id.npDay), npMonth = dialog.findViewById(R.id.npMonth), npYear = dialog.findViewById(R.id.npYear);
        int curYear = Calendar.getInstance().get(Calendar.YEAR);
        npYear.setMinValue(1950); npYear.setMaxValue(curYear); npYear.setValue(2005);
        String[] mths = new String[12]; for (int i = 0; i < 12; i++) mths[i] = String.format("%02d", i + 1);
        npMonth.setMinValue(0); npMonth.setMaxValue(11); npMonth.setDisplayedValues(mths); npMonth.setValue(1);
        npDay.setMinValue(1); updateMaxDays(npDay, npMonth.getValue(), npYear.getValue()); npDay.setValue(28);
        NumberPicker.OnValueChangeListener dcl = (p, o, n) -> updateMaxDays(npDay, npMonth.getValue(), npYear.getValue());
        npMonth.setOnValueChangedListener(dcl); npYear.setOnValueChangedListener(dcl);
        dialog.findViewById(R.id.ivClose).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btnOk).setOnClickListener(v -> { binding.etDob.setText(String.format("%02d/%s/%d", npDay.getValue(), mths[npMonth.getValue()], npYear.getValue())); dialog.dismiss(); });
        dialog.show();
    }

    private void updateMaxDays(NumberPicker npDay, int mIdx, int y) {
        Calendar cal = Calendar.getInstance(); cal.set(Calendar.YEAR, y); cal.set(Calendar.MONTH, mIdx);
        int max = cal.getActualMaximum(Calendar.DAY_OF_MONTH); npDay.setMaxValue(max); if (npDay.getValue() > max) npDay.setValue(max);
    }

    private void showGenderSelectionDialog() {
        Dialog dialog = new Dialog(this); dialog.setContentView(R.layout.dialog_gender_selection);
        if (dialog.getWindow() != null) { dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT); }
        RadioGroup rg = dialog.findViewById(R.id.rgGender);
        rg.setOnCheckedChangeListener((g, id) -> { RadioButton rb = dialog.findViewById(id); if (rb != null) binding.etGender.setText(rb.getText()); dialog.dismiss(); });
        dialog.show();
    }
}