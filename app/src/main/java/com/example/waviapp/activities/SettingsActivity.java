package com.example.waviapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.example.waviapp.R;
import com.example.waviapp.databinding.ActivitySettingsBinding;
import com.example.waviapp.firebase.DatabaseHelper;
import com.example.waviapp.firebase.FirebaseAuthHelper;
import com.example.waviapp.managers.UserSessionManager;
import com.example.waviapp.models.TaiKhoan;
import com.example.waviapp.utils.LanguageManager;
import com.google.firebase.auth.FirebaseUser;
import android.net.Uri;

public class SettingsActivity extends BaseActivity {

    private ActivitySettingsBinding binding;
    private FirebaseAuthHelper authHelper;
    private final String FB_ID = "61575852834048";
    private final String MESSENGER_URL = "https://m.me/" + FB_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // A. Selection Fix: setChecked(true)
        binding.bottomNav.getMenu().findItem(R.id.nav_setting).setChecked(true);

        authHelper = new FirebaseAuthHelper();
        UserSessionManager.getInstance().getUserLiveData().observe(this, this::updateUI);

        String currentLangCode = LanguageManager.getLanguage(this);
        selectedLanguage = "en".equals(currentLangCode) ? "English" : "Tiếng Việt";
        if (binding.tvSelectedLanguage != null) binding.tvSelectedLanguage.setText(selectedLanguage);

        binding.tvLogout.setOnClickListener(v -> {
            authHelper.logout(); UserSessionManager.getInstance().clearSession();
            startActivity(new Intent(SettingsActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        });

        binding.llEditProfile.setOnClickListener(v -> startActivity(new Intent(SettingsActivity.this, UserInfoActivity.class)));
        binding.llLanguage.setOnClickListener(v -> showLanguageDialog());
        binding.llCommunity.setOnClickListener(v -> {
            try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("fb://group/1672027700753499")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); }
            catch (Exception e) { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/groups/1672027700753499"))); }
        });
        binding.llShare.setOnClickListener(v -> shareApp());
        binding.llReminder.setOnClickListener(v -> new StudyReminderBottomSheet().show(getSupportFragmentManager(), "StudyReminderBottomSheet"));
        binding.llFeedback.setOnClickListener(v -> {
            try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("fb-messenger://user/" + FB_ID)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); }
            catch (Exception e) { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(MESSENGER_URL))); }
        });

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            // B. Listener Logic: Check Current
            if (id == R.id.nav_setting) return false;
            
            Intent intent = null;
            if (id == R.id.nav_home) intent = new Intent(this, HomeActivity.class);
            else if (id == R.id.nav_exam) intent = new Intent(this, ExamActivity.class);
            else if (id == R.id.nav_premium) intent = new Intent(this, PremiumActivity.class);
            else if (id == R.id.nav_profile) intent = new Intent(this, UserInfoActivity.class);

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
        if (user.getHoTen() != null && !user.getHoTen().isEmpty()) {
            binding.tvUserName.setText(user.getHoTen());
            binding.tvAvatarText.setText(user.getHoTen().substring(0, 1).toUpperCase());
        }
        if (user.isPremium()) { binding.tvPremiumStatus.setText(getString(R.string.premium_member)); binding.tvPremiumStatus.setTextColor(android.graphics.Color.parseColor("#FFD700")); }
        else { binding.tvPremiumStatus.setText(getString(R.string.free_member)); binding.tvPremiumStatus.setTextColor(android.graphics.Color.parseColor("#888888")); }
    }

    private void shareApp() {
        Intent si = new Intent(Intent.ACTION_SEND); si.setType("text/plain");
        si.putExtra(Intent.EXTRA_TEXT, "Học tiếng Anh cực dễ cùng WaviApp! 🚀\nhttps://github.com/xxxmvppmn/NT118");
        startActivity(Intent.createChooser(si, "Chia sẻ:"));
    }

    private String selectedLanguage = "Tiếng Việt";
    private void showLanguageDialog() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_language_selection);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        android.widget.LinearLayout ll = dialog.findViewById(R.id.llLanguageContainer);
        String[] langs = {"Tiếng Việt", "English"};
        for (String lang : langs) {
            View v = getLayoutInflater().inflate(R.layout.item_language, ll, false);
            ((android.widget.TextView) v.findViewById(R.id.tvLangName)).setText(lang);
            ((android.widget.ImageView) v.findViewById(R.id.ivRadio)).setImageResource(lang.equals(selectedLanguage) ? R.drawable.ic_radio_selected : R.drawable.ic_radio_unselected);
            v.setOnClickListener(v1 -> {
                selectedLanguage = lang; LanguageManager.setLanguage(this, lang.equals("English") ? "en" : "vi");
                dialog.dismiss(); startActivity(new Intent(this, HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)); finish();
            });
            ll.addView(v);
        }
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }
}