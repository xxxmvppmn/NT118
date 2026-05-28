package com.example.waviapp.activities;

import android.os.Bundle;
import android.widget.Toast;

import com.example.waviapp.managers.UserSessionManager;
import com.example.waviapp.models.TaiKhoan;

public abstract class BaseAdminActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkAdminAccess();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAdminAccess();
    }

    private void checkAdminAccess() {
        TaiKhoan user = UserSessionManager.getInstance().getUserData();
        if (user == null || !"Admin".equals(user.getVaiTro()) || user.isLocked()) {
            Toast.makeText(this, "Truy cập bị từ chối. Bạn không có quyền Admin!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
