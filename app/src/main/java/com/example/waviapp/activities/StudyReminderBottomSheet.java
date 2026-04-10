package com.example.waviapp.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import com.example.waviapp.R;
import com.example.waviapp.utils.AlarmReceiver;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Calendar;
import java.util.Locale;

public class StudyReminderBottomSheet extends BottomSheetDialogFragment {

    private TextView tvTimeCurrent;
    private Switch switchReminder;
    private TimePicker timePicker;
    private Button btnSave;

    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_study_reminder, container, false);

        tvTimeCurrent = view.findViewById(R.id.tvTimeCurrent);
        switchReminder = view.findViewById(R.id.switchReminder);
        timePicker = view.findViewById(R.id.timePicker);
        btnSave = view.findViewById(R.id.btnSave);

        timePicker.setIs24HourView(true);

        sharedPreferences = requireContext().getSharedPreferences("StudyReminderPrefs", Context.MODE_PRIVATE);
        boolean isEnabled = sharedPreferences.getBoolean("isReminderEnabled", false);
        
        Calendar now = Calendar.getInstance();
        int defaultHour = now.get(Calendar.HOUR_OF_DAY);
        int defaultMinute = now.get(Calendar.MINUTE);
        
        int savedHour = sharedPreferences.getInt("reminderHour", defaultHour);
        int savedMinute = sharedPreferences.getInt("reminderMinute", defaultMinute);

        switchReminder.setChecked(isEnabled);
        timePicker.setEnabled(isEnabled); // Thiết lập trạng thái enable dựa trên switch
        
        timePicker.setHour(savedHour);
        timePicker.setMinute(savedMinute);
        updateTimeText(savedHour, savedMinute);

        // Lắng nghe sự thay đổi của switch để bật/tắt timePicker và xin quyền thông báo
        switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            timePicker.setEnabled(isChecked);
            if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
                }
            }
        });

        timePicker.setOnTimeChangedListener((view1, hourOfDay, minute) -> {
            updateTimeText(hourOfDay, minute);
        });

        btnSave.setOnClickListener(v -> {
            boolean enabled = switchReminder.isChecked();
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isReminderEnabled", enabled);
            editor.putInt("reminderHour", hour);
            editor.putInt("reminderMinute", minute);
            editor.apply();

            if (enabled) {
                setAlarm(hour, minute);
                String msg = getString(R.string.reminder_toast_saved, String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            } else {
                cancelAlarm();
                Toast.makeText(getContext(), R.string.reminder_toast_disabled, Toast.LENGTH_SHORT).show();
            }

            dismiss();
        });

        return view;
    }

    private void updateTimeText(int hour, int minute) {
        String timeStr = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
        tvTimeCurrent.setText(timeStr);
    }

    private void setAlarm(int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(requireContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                100,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1);
        }

        if (alarmManager != null) {
            try {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            } catch (SecurityException e) {
                // Yêu cầu quyền ở API >= 31 SCHEDULE_EXACT_ALARM nhưng fallback về set
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        }
    }

    private void cancelAlarm() {
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(requireContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                100,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}
