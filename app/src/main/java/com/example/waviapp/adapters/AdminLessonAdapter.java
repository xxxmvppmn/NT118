package com.example.waviapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waviapp.R;
import com.example.waviapp.models.ChuDe;

import java.util.ArrayList;
import java.util.List;

public class AdminLessonAdapter extends RecyclerView.Adapter<AdminLessonAdapter.LessonViewHolder> {

    private List<ChuDe> lessons = new ArrayList<>();
    private OnLessonActionListener listener;

    public interface OnLessonActionListener {
        void onEdit(ChuDe lesson);
        void onDelete(ChuDe lesson, int position);
    }

    public AdminLessonAdapter(OnLessonActionListener listener) {
        this.listener = listener;
    }

    public void setLessons(List<ChuDe> lessons) {
        this.lessons = lessons;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < lessons.size()) {
            lessons.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, lessons.size() - position);
        }
    }

    public void updateItem(ChuDe lesson, int position) {
        if (position >= 0 && position < lessons.size()) {
            lessons.set(position, lesson);
            notifyItemChanged(position);
        }
    }

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_lesson, parent, false);
        return new LessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
        holder.bind(lessons.get(position), position);
    }

    @Override
    public int getItemCount() {
        return lessons.size();
    }

    class LessonViewHolder extends RecyclerView.ViewHolder {
        TextView txtLessonName, txtLessonType, txtLessonDesc;
        ImageView btnEdit, btnDelete;

        LessonViewHolder(@NonNull View itemView) {
            super(itemView);
            txtLessonName = itemView.findViewById(R.id.txtLessonName);
            txtLessonType = itemView.findViewById(R.id.txtLessonType);
            txtLessonDesc = itemView.findViewById(R.id.txtLessonDesc);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(ChuDe lesson, int position) {
            txtLessonName.setText(lesson.getTenCD() != null ? lesson.getTenCD() : lesson.getMaCD());
            txtLessonType.setText(lesson.getLoaiChuDe() != null ? lesson.getLoaiChuDe() : "");
            txtLessonDesc.setText(lesson.getMoTa() != null ? lesson.getMoTa() : "");

            btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(lesson);
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(lesson, position);
            });
        }
    }
}
