package com.example.waviapp;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

public class WaviApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Đăng ký bộ theo dõi toàn bộ Activity trong App
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                // Cứ Activity nào vừa sinh ra là tắt hiệu ứng trượt ngay lập tức
                activity.overridePendingTransition(0, 0);
            }

            @Override
            public void onActivityStarted(Activity activity) {}
            @Override
            public void onActivityResumed(Activity activity) {
                // Tắt hiệu ứng một lần nữa khi quay lại Activity (cho chắc chắn)
                activity.overridePendingTransition(0, 0);
            }

            @Override
            public void onActivityPaused(Activity activity) {
                // Khi đóng hoặc chuyển Activity cũng không cho trượt
                activity.overridePendingTransition(0, 0);
            }

            @Override
            public void onActivityStopped(Activity activity) {}
            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
            @Override
            public void onActivityDestroyed(Activity activity) {}
        });
    }
}