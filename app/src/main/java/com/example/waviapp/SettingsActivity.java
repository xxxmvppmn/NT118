package com.example.waviapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.content.res.Configuration;
import com.example.waviapp.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Logout handler
        binding.tvLogout.setOnClickListener(v -> {
            Toast.makeText(this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Click handlers for setting items
        View.OnClickListener itemClickListener = v -> {
            Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
        };
        binding.llEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, UserInfoActivity.class);
            startActivity(intent);
        });
        binding.llLanguage.setOnClickListener(v -> showLanguageDialog());
        binding.llDisplay.setOnClickListener(itemClickListener);
        binding.llCommunity.setOnClickListener(itemClickListener);
        binding.llShare.setOnClickListener(itemClickListener);
        binding.llDownloads.setOnClickListener(itemClickListener);
        binding.llReminder.setOnClickListener(itemClickListener);
        binding.llFeedback.setOnClickListener(itemClickListener);

        // Bottom Navigation
        binding.bottomNav.setSelectedItemId(R.id.nav_setting);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent intent = new Intent(SettingsActivity.this, HomeActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_exam) {
                Toast.makeText(this, "Chuyển sang màn hình ", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_premium) {
                Intent intent = new Intent(SettingsActivity.this, PremiumActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            } else if (id == R.id.nav_profile) {
                Toast.makeText(this, "Đang chuyển sang Profile...", Toast.LENGTH_SHORT).show();
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

    private String selectedLanguage = "Tiếng Việt";

    private void showLanguageDialog() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_language_selection);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        
        android.widget.LinearLayout llContainer = dialog.findViewById(R.id.llLanguageContainer);
        String[] languages = {"English", "Tiếng Việt", "繁體中文", "简体中文", "한국어", "日本語", "Français", "Español", "Indonesian", "ภาษาไทย", "Português", "Deutsche"};
        
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
                dialog.dismiss();
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
