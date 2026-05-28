package com.example.waviapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waviapp.R;
import com.example.waviapp.models.TaiKhoan;

import java.util.ArrayList;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {

    private List<TaiKhoan> users = new ArrayList<>();
    private OnUserActionListener listener;

    public interface OnUserActionListener {
        void onLock(TaiKhoan user, int position);
        void onDelete(TaiKhoan user, int position);
    }

    public AdminUserAdapter(OnUserActionListener listener) {
        this.listener = listener;
    }

    public void setUsers(List<TaiKhoan> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < users.size()) {
            users.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, users.size() - position);
        }
    }

    public void updateItem(TaiKhoan user, int position) {
        if (position >= 0 && position < users.size()) {
            users.set(position, user);
            notifyItemChanged(position);
        }
    }

    public List<TaiKhoan> getUsers() {
        return users;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        TaiKhoan user = users.get(position);
        holder.bind(user, position);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        TextView txtUserName, txtUserEmail, txtUserStatus, txtPremium;
        ImageView btnLock, btnDelete;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtUserEmail = itemView.findViewById(R.id.txtUserEmail);
            txtUserStatus = itemView.findViewById(R.id.txtUserStatus);
            txtPremium = itemView.findViewById(R.id.txtPremium);
            btnLock = itemView.findViewById(R.id.btnLock);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(TaiKhoan user, int position) {
            txtUserName.setText(user.getHoTen() != null ? user.getHoTen() : "Chưa đặt tên");
            txtUserEmail.setText(user.getEmail() != null ? user.getEmail() : "");

            // Premium badge
            if (user.isPremium()) {
                txtPremium.setVisibility(View.VISIBLE);
            } else {
                txtPremium.setVisibility(View.GONE);
            }

            // Status
            if (user.isLocked()) {
                txtUserStatus.setText(R.string.admin_status_locked);
                txtUserStatus.setBackgroundTintList(
                        itemView.getContext().getColorStateList(R.color.admin_locked_bg));
                txtUserStatus.setTextColor(
                        itemView.getContext().getColor(R.color.admin_locked_text));
                btnLock.setImageResource(android.R.drawable.ic_lock_idle_lock);
            } else {
                txtUserStatus.setText(R.string.admin_status_active);
                txtUserStatus.setBackgroundTintList(
                        itemView.getContext().getColorStateList(R.color.admin_active_bg));
                txtUserStatus.setTextColor(
                        itemView.getContext().getColor(R.color.admin_active_text));
                btnLock.setImageResource(android.R.drawable.ic_lock_lock);
            }

            btnLock.setOnClickListener(v -> {
                if (listener != null) listener.onLock(user, position);
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(user, position);
            });
        }
    }
}
