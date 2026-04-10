package com.example.waviapp.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.waviapp.R;

public class SkillPracticeActivity extends BaseActivity {

    public static final String EXTRA_SKILL_CATEGORY = "extra_skill_category";
    public static final String CAT_LISTEN = "LISTEN";
    public static final String CAT_READ = "READ";
    public static final String CAT_SPEAK = "SPEAK";
    public static final String CAT_WRITE = "WRITE";

    private LinearLayout llPartsContainer;
    private TextView tvToolbarTitle;
    private ImageView ivBackSkill;
    private ImageView ivHeaderIcon;
    private TextView tvHeaderStat1;
    private TextView tvHeaderStat2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skill_practice);

        tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        llPartsContainer = findViewById(R.id.llPartsContainer);
        ivBackSkill = findViewById(R.id.ivBackSkill);
        ivHeaderIcon = findViewById(R.id.ivHeaderIcon);
        tvHeaderStat1 = findViewById(R.id.tvHeaderStat1);
        tvHeaderStat2 = findViewById(R.id.tvHeaderStat2);

        ivBackSkill.setOnClickListener(v -> finish());

        String category = getIntent().getStringExtra(EXTRA_SKILL_CATEGORY);
        if (category == null) {
            category = CAT_SPEAK;
        }

        setupUI(category);
    }

    private void setupUI(String category) {
        String[] parts;
        boolean hasDoubleStats = false;

        if (CAT_SPEAK.equals(category)) {
            tvToolbarTitle.setText(getString(R.string.skill_speaking));
            ivHeaderIcon.setImageResource(R.drawable.ic_speak);
            parts = new String[]{
                getString(R.string.speak_part1),
                getString(R.string.speak_part2),
                getString(R.string.speak_part3),
                getString(R.string.speak_part4),
                getString(R.string.speak_part5),
                getString(R.string.speak_part6)
            };
        } else if (CAT_WRITE.equals(category)) {
            tvToolbarTitle.setText(getString(R.string.skill_writing));
            ivHeaderIcon.setImageResource(R.drawable.ic_write);
            parts = new String[]{
                getString(R.string.write_part1),
                getString(R.string.write_part2),
                getString(R.string.write_part3)
            };
        } else if (CAT_LISTEN.equals(category)) {
            tvToolbarTitle.setText(getString(R.string.skill_listening));
            ivHeaderIcon.setImageResource(R.drawable.ic_listen);
            parts = new String[]{
                getString(R.string.listen_part1),
                getString(R.string.listen_part2),
                getString(R.string.listen_part3),
                getString(R.string.listen_part4)
            };
            hasDoubleStats = true;
        } else { // CAT_READ
            tvToolbarTitle.setText(getString(R.string.skill_reading));
            ivHeaderIcon.setImageResource(R.drawable.ic_read);
            parts = new String[]{
                getString(R.string.read_part1),
                getString(R.string.read_part2),
                getString(R.string.read_part3)
            };
            hasDoubleStats = true;
        }

        if (hasDoubleStats) {
            tvHeaderStat2.setVisibility(View.VISIBLE);
        } else {
            tvHeaderStat2.setVisibility(View.GONE);
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < parts.length; i++) {
            View itemView = inflater.inflate(R.layout.item_skill_part, llPartsContainer, false);

            TextView tvPartTitle = itemView.findViewById(R.id.tvPartTitle);
            TextView tvPartStat = itemView.findViewById(R.id.tvPartStat);
            ImageView ivPartLock = itemView.findViewById(R.id.ivPartLock);

            tvPartTitle.setText(parts[i]);

            if (hasDoubleStats) {
                tvPartStat.setText(getString(R.string.stat_correct));
            } else {
                tvPartStat.setText(getString(R.string.stat_done));
            }

            ivPartLock.setVisibility(View.GONE);
            llPartsContainer.addView(itemView);
        }
    }
}

