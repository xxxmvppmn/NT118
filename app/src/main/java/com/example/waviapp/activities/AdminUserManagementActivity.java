package com.example.waviapp.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.waviapp.R;
import com.example.waviapp.adapters.AdminUserAdapter;
import com.example.waviapp.firebase.DatabaseHelper;
import com.example.waviapp.models.TaiKhoan;

import java.util.ArrayList;
import java.util.List;

public class AdminUserManagementActivity extends BaseAdminActivity implements AdminUserAdapter.OnUserActionListener {

    private RecyclerView rvUsers;
    private EditText edtSearchUser;
    private TextView txtUserCount, txtEmpty;
    private SwipeRefreshLayout swipeRefresh;
    private AdminUserAdapter adapter;
    private DatabaseHelper dbHelper;
    private List<TaiKhoan> allUsers = new ArrayList<>();

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_management);

        dbHelper = new DatabaseHelper();
        initViews();
        setupRecyclerView();
        setupSearch();
        loadUsers();
    }

    private void initViews() {
        rvUsers = findViewById(R.id.rvUsers);
        edtSearchUser = findViewById(R.id.edtSearchUser);
        txtUserCount = findViewById(R.id.txtUserCount);
        txtEmpty = findViewById(R.id.txtEmpty);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        swipeRefresh.setColorSchemeResources(R.color.admin_primary);
        swipeRefresh.setOnRefreshListener(this::loadUsers);
    }

    private void setupRecyclerView() {
        adapter = new AdminUserAdapter(this);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(adapter);
    }

    private void setupSearch() {
        edtSearchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchHandler.removeCallbacks(searchRunnable);
                searchRunnable = () -> {
                    if (!(isFinishing() || isDestroyed())) {
                        filterUsers(s.toString());
                    }
                };
                searchHandler.postDelayed(searchRunnable, 300); // 300ms Debounce
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadUsers() {
        swipeRefresh.setRefreshing(true);
        dbHelper.getAllUsers(new DatabaseHelper.UsersListCallback() {
            @Override
            public void onSuccess(List<TaiKhoan> users) {
                if (isFinishing() || isDestroyed()) return;
                swipeRefresh.setRefreshing(false);
                allUsers = users;
                adapter.setUsers(users);
                txtUserCount.setText(users.size() + " người dùng");
                txtEmpty.setVisibility(users.isEmpty() ? View.VISIBLE : View.GONE);
                rvUsers.setVisibility(users.isEmpty() ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onFailure(String error) {
                if (isFinishing() || isDestroyed()) return;
                swipeRefresh.setRefreshing(false);
                Toast.makeText(AdminUserManagementActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterUsers(String query) {
        if (query.isEmpty()) {
            adapter.setUsers(allUsers);
            txtUserCount.setText(allUsers.size() + " người dùng");
            return;
        }
        String lower = query.toLowerCase();
        List<TaiKhoan> filtered = new ArrayList<>();
        for (TaiKhoan u : allUsers) {
            if ((u.getHoTen() != null && u.getHoTen().toLowerCase().contains(lower)) ||
                    (u.getEmail() != null && u.getEmail().toLowerCase().contains(lower))) {
                filtered.add(u);
            }
        }
        adapter.setUsers(filtered);
        txtUserCount.setText(filtered.size() + " người dùng");
    }

    @Override
    public void onLock(TaiKhoan user, int position) {
        boolean newState = !user.isLocked();
        String message = newState
                ? getString(R.string.admin_confirm_lock_user)
                : getString(R.string.admin_confirm_unlock_user);

        new android.app.AlertDialog.Builder(this)
                .setTitle(newState ? getString(R.string.admin_lock_account) : getString(R.string.admin_unlock_account))
                .setMessage(message)
                .setPositiveButton(R.string.admin_confirm, (dialog, which) -> {
                    swipeRefresh.setRefreshing(true);
                    dbHelper.lockUser(user.getId(), newState, new DatabaseHelper.SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            if (isFinishing() || isDestroyed()) return;
                            swipeRefresh.setRefreshing(false);
                            user.setLocked(newState);
                            adapter.updateItem(user, position);
                            Toast.makeText(AdminUserManagementActivity.this,
                                    newState ? "Đã khóa tài khoản" : "Đã mở khóa", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onFailure(String error) {
                            if (isFinishing() || isDestroyed()) return;
                            swipeRefresh.setRefreshing(false);
                            Toast.makeText(AdminUserManagementActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(R.string.admin_cancel, null)
                .show();
    }

    @Override
    public void onDelete(TaiKhoan user, int position) {
        new android.app.AlertDialog.Builder(this)
                .setTitle(R.string.admin_delete_account)
                .setMessage(R.string.admin_confirm_delete_user)
                .setPositiveButton(R.string.admin_delete, (dialog, which) -> {
                    swipeRefresh.setRefreshing(true);
                    dbHelper.deleteUser(user.getId(), new DatabaseHelper.SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            if (isFinishing() || isDestroyed()) return;
                            swipeRefresh.setRefreshing(false);
                            allUsers.remove(user);
                            adapter.removeItem(position);
                            txtUserCount.setText(allUsers.size() + " người dùng");
                            Toast.makeText(AdminUserManagementActivity.this, "Đã xóa tài khoản", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onFailure(String error) {
                            if (isFinishing() || isDestroyed()) return;
                            swipeRefresh.setRefreshing(false);
                            Toast.makeText(AdminUserManagementActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(R.string.admin_cancel, null)
                .show();
    }

    @Override
    protected void onDestroy() {
        searchHandler.removeCallbacks(searchRunnable);
        super.onDestroy();
    }
}
