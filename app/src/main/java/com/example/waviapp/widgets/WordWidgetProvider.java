package com.example.waviapp.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.waviapp.R;
import com.example.waviapp.activities.HomeActivity;
import com.example.waviapp.models.TuVung;
import com.example.waviapp.utils.VocabLoader;

/**
 * Provider chính cho Wavi Vocabulary Widget.
 * Đã sửa lỗi Action mismatch và tối ưu hóa RemoteViews.
 */
public class WordWidgetProvider extends AppWidgetProvider {

    private static final String TAG = "WordWidgetProvider";
    // ĐÃ ĐỒNG BỘ: Action phải khớp hoàn toàn với AndroidManifest.xml
    public static final String ACTION_WIDGET_REFRESH = "com.example.waviapp.ACTION_WIDGET_REFRESH";
    private static final int RC_OPEN_APP = 101;
    private static final int RC_REFRESH = 102;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_WIDGET_REFRESH.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisWidget = new ComponentName(context, WordWidgetProvider.class);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
            
            for (int appWidgetId : appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId);
            }
        } else {
            super.onReceive(context, intent);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        try {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            // Lấy từ vựng ngẫu nhiên
            TuVung word = VocabLoader.getInstance(context).getRandomWord();
            
            // Cập nhật text
            views.setTextViewText(R.id.widget_tv_word, word.getTuTiengAnh());
            views.setTextViewText(R.id.widget_tv_phonetic, word.getPhienAm());
            views.setTextViewText(R.id.widget_tv_meaning, word.getNghiaTiengViet());

            // Click vào nền: Mở App
            Intent openAppIntent = new Intent(context, HomeActivity.class);
            openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent openAppPendingIntent = PendingIntent.getActivity(
                    context, RC_OPEN_APP, openAppIntent, 
                    getPendingIntentFlags(PendingIntent.FLAG_UPDATE_CURRENT));
            views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent);

            // Click vào icon Refresh: Chỉ cập nhật từ (không mở App)
            Intent refreshIntent = new Intent(context, WordWidgetProvider.class);
            refreshIntent.setAction(ACTION_WIDGET_REFRESH);
            // Quan trọng: Làm cho Intent là duy nhất để tránh bị hệ thống gộp nhầm
            refreshIntent.setData(Uri.parse(refreshIntent.toUri(Intent.URI_INTENT_SCHEME)));
            
            PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(
                    context, RC_REFRESH, refreshIntent, 
                    getPendingIntentFlags(PendingIntent.FLAG_UPDATE_CURRENT));
            views.setOnClickPendingIntent(R.id.widget_btn_refresh, refreshPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi cập nhật Widget: " + e.getMessage());
        }
    }

    private static int getPendingIntentFlags(int baseFlags) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return baseFlags | PendingIntent.FLAG_IMMUTABLE;
        }
        return baseFlags;
    }
}
