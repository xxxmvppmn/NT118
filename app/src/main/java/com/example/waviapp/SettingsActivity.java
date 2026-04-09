package com.example.waviapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.example.waviapp.databinding.ActivitySettingsBinding;
import com.example.waviapp.firebase.DatabaseHelper;
import com.example.waviapp.firebase.FirebaseAuthHelper;
import com.example.waviapp.models.TaiKhoan;
import com.google.firebase.auth.FirebaseUser;
import android.net.Uri;

public class SettingsActivity extends BaseActivity {

    private ActivitySettingsBinding binding;
    private FirebaseAuthHelper authHelper;
    private DatabaseHelper dbHelper;
    private final String FB_ID = "61575852834048";
    private final String MESSENGER_URL = "https://m.me/" + FB_ID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authHelper = new FirebaseAuthHelper();
        dbHelper = new DatabaseHelper();

        // 1. Đồng bộ lại ngôn ngữ hiện tại của máy vào UI Settings
        String currentLangCode = com.example.waviapp.utils.LanguageManager.getLanguage(this);
        selectedLanguage = "en".equals(currentLangCode) ? "English" : "Tiếng Việt";
        // Cập nhật nhãn ngôn ngữ ngay khi mở Cài đặt nếu TextView này tồn tại
        android.widget.TextView tvSelectedLanguage = findViewById(R.id.tvSelectedLanguage);
        if (tvSelectedLanguage != null) {
            tvSelectedLanguage.setText(selectedLanguage);
        }

        // Logout handler - sử dụng Firebase Auth
        binding.tvLogout.setOnClickListener(v -> {
            authHelper.logout();
            Toast.makeText(this, getString(R.string.logout_success), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        final String FB_GROUP_ID = "1672027700753499";        // Click handlers for setting items
        View.OnClickListener itemClickListener = v -> {
            Toast.makeText(this, getString(R.string.feature_dev), Toast.LENGTH_SHORT).show();
        };
        binding.llEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, UserInfoActivity.class);
            startActivity(intent);
        });
        binding.llLanguage.setOnClickListener(v -> showLanguageDialog());
        binding.llDisplay.setOnClickListener(itemClickListener);
        binding.llCommunity.setOnClickListener(v -> {
            Intent intent;
            try {
                // Mở bằng App Facebook
                intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("fb://group/" + FB_GROUP_ID));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Exception e) {
                // Nếu không có app Facebook, mở bằng trình duyệt
                String webUrl = "https://www.facebook.com/groups/" + FB_GROUP_ID;
                intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(webUrl));
                startActivity(intent);
            }
        });
        binding.llShare.setOnClickListener(v -> shareApp());
        binding.llDownloads.setOnClickListener(itemClickListener);
        binding.llReminder.setOnClickListener(v -> {
            StudyReminderBottomSheet bottomSheet = new StudyReminderBottomSheet();
            bottomSheet.show(getSupportFragmentManager(), "StudyReminderBottomSheet");
        });
        binding.llFeedback.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb-messenger://user/" + FB_ID));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            } catch (Exception e) {
                // 2. Nếu máy không có app Messenger, mở bằng trình duyệt web
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MESSENGER_URL));
                startActivity(intent);
            }
        });

        // Bottom Navigation
        binding.bottomNav.setSelectedItemId(R.id.nav_setting);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent intent = new Intent(SettingsActivity.this, HomeActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_exam) {
                Intent intent = new Intent(SettingsActivity.this, ExamActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_premium) {
                Intent intent = new Intent(SettingsActivity.this, PremiumActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(SettingsActivity.this, UserInfoActivity.class);
                startActivity(intent);

            }else if (id == R.id.nav_setting) {
                Intent intent = new Intent(this, SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
    }
    private void shareApp() {
        try {
            // Đây là nội dung văn bản sẽ được gửi đi
            String shareMessage = "Học tiếng Anh cực dễ cùng WaviApp! 🚀\n" +
                    "Tải ngay bản thử nghiệm tại đây: \n" + "https://github.com/xxxmvppmn/NT118";

            // 1. Tạo một Intent với hành động là SEND (Gửi)
            Intent shareIntent = new Intent(Intent.ACTION_SEND);

            // 2. Định nghĩa kiểu dữ liệu là văn bản thuần túy
            shareIntent.setType("text/plain");

            // 3. Đưa nội dung tin nhắn vào Intent
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);

            // 4. QUAN TRỌNG: Tạo một "Chooser" (Bảng chọn)
            // Dòng này sẽ bắt Android hiện lên danh sách Zalo, Messenger, Gmail...
            Intent chooser = Intent.createChooser(shareIntent, "Chia sẻ WaviApp qua:");

            startActivity(chooser);

        } catch (Exception e) {
            Toast.makeText(this, "Không tìm thấy ứng dụng để chia sẻ", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserProfile() {
        FirebaseUser fUser = authHelper.getCurrentUser();
        if (fUser != null) {
            dbHelper.getUser(fUser.getUid(), new DatabaseHelper.UserCallback() {
                @Override
                public void onSuccess(TaiKhoan user) {
                    if (user.getHoTen() != null && !user.getHoTen().isEmpty()) {
                        binding.tvUserName.setText(user.getHoTen());
                        binding.tvAvatarText.setText(user.getHoTen().substring(0, 1).toUpperCase());
                    } else if (fUser.getDisplayName() != null) {
                        binding.tvUserName.setText(fUser.getDisplayName());
                        binding.tvAvatarText.setText(fUser.getDisplayName().substring(0, 1).toUpperCase());
                    }

                    if (user.isPremium()) {
                        binding.tvPremiumStatus.setText(getString(R.string.premium_member));
                        binding.tvPremiumStatus.setTextColor(android.graphics.Color.parseColor("#FFD700"));
                    } else {
                        binding.tvPremiumStatus.setText(getString(R.string.free_member));
                        binding.tvPremiumStatus.setTextColor(android.graphics.Color.parseColor("#888888"));
                    }
                }

                @Override
                public void onFailure(String error) {
                    if (fUser.getDisplayName() != null) {
                        binding.tvUserName.setText(fUser.getDisplayName());
                        binding.tvAvatarText.setText(fUser.getDisplayName().substring(0, 1).toUpperCase());
                    }
                }
            });
        }
    }

    private String selectedLanguage = "Tiếng Việt";

    private void showLanguageDialog() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_language_selection);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        
        android.widget.LinearLayout llContainer = dialog.findViewById(R.id.llLanguageContainer);
        String[] languages = {"Tiếng Việt", "English"};
        
        android.view.LayoutInflater inflater = android.view.LayoutInflater.from(this);
        android.widget.TextView tvSelectedLanguage = findViewById(R.id.tvSelectedLanguage);

        for (String lang : languages) {
            android.view.View itemView = inflater.inflate(R.layout.item_language, llContainer, false);
            android.widget.TextView tvLangName = itemView.findViewById(R.id.tvLangName);
            android.widget.ImageView ivRadio = itemView.findViewById(R.id.ivRadio);
            
            tvLangName.setText(lang);
            
            if (lang.equals(selectedLanguage)) {
                ivRadio.setImageResource(R.drawable.ic_radio_selected);
            } else {
                ivRadio.setImageResource(R.drawable.ic_radio_unselected);
            }
            
            itemView.setOnClickListener(v -> {
                selectedLanguage = lang;
                if (tvSelectedLanguage != null) {
                    tvSelectedLanguage.setText(lang);
                }
                
                String langCode = lang.equals("English") ? "en" : "vi";
                com.example.waviapp.utils.LanguageManager.setLanguage(SettingsActivity.this, langCode);
                
                dialog.dismiss();

                // Restart toàn bộ app về HomeActivity để tất cả màn hình đều cập nhật ngôn ngữ mới
                Intent restartIntent = new Intent(SettingsActivity.this, HomeActivity.class);
                restartIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(restartIntent);
                finish();
            });
            
            llContainer.addView(itemView);
        }
        
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, 
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
}
