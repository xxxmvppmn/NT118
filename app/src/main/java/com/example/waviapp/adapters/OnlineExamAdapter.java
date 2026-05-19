package com.example.waviapp.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waviapp.R;
import com.example.waviapp.models.OnlineExam;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OnlineExamAdapter extends RecyclerView.Adapter<OnlineExamAdapter.ViewHolder> {

    public interface OnExamClickListener {
        void onExamClick(OnlineExam exam);
    }

    private final List<OnlineExam> examList;
    private final OnExamClickListener listener;

    public OnlineExamAdapter(List<OnlineExam> examList, OnExamClickListener listener) {
        this.examList = examList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_online_exam_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OnlineExam exam = examList.get(position);

        holder.tvTitle.setText(exam.getTitle());
        holder.tvDescription.setText(exam.getDescription());
        holder.tvMeta.setText(exam.getTotalQuestions() + " câu  •  " + exam.getDurationMinutes() + " phút");
        holder.tvParticipants.setText("👥 " + exam.getParticipantCount() + " người thi");

        // Màu nền card theo colorTag
        try {
            holder.cardView.setCardBackgroundColor(Color.parseColor(exam.getColorTag() + "22")); // alpha 13%
            holder.vColorBar.setBackgroundColor(Color.parseColor(exam.getColorTag()));
            holder.tvTitle.setTextColor(Color.parseColor(exam.getColorTag()));
        } catch (Exception ignored) {}

        // Trạng thái: Sắp diễn ra / Đang diễn ra / Đã kết thúc
        long now = System.currentTimeMillis() / 1000;
        if (exam.getStartTime() != null && exam.getEndTime() != null) {
            long start = exam.getStartTime().getSeconds();
            long end = exam.getEndTime().getSeconds();
            if (now < start) {
                holder.tvStatus.setText("🕐 Sắp diễn ra");
                holder.tvStatus.setTextColor(Color.parseColor("#FF6F00"));
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
                holder.tvDeadline.setText("Bắt đầu: " + sdf.format(new Date(start * 1000)));
            } else if (now <= end) {
                holder.tvStatus.setText("🟢 Đang diễn ra");
                holder.tvStatus.setTextColor(Color.parseColor("#2E7D32"));
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
                holder.tvDeadline.setText("Kết thúc: " + sdf.format(new Date(end * 1000)));
            } else {
                holder.tvStatus.setText("🔴 Đã kết thúc");
                holder.tvStatus.setTextColor(Color.parseColor("#B71C1C"));
                holder.tvDeadline.setText("Xem kết quả →");
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onExamClick(exam);
        });
    }

    @Override
    public int getItemCount() {
        return examList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        View vColorBar;
        TextView tvTitle, tvDescription, tvMeta, tvParticipants, tvStatus, tvDeadline;

        ViewHolder(View itemView) {
            super(itemView);
            cardView        = itemView.findViewById(R.id.cardExam);
            vColorBar       = itemView.findViewById(R.id.vColorBar);
            tvTitle         = itemView.findViewById(R.id.tvExamTitle);
            tvDescription   = itemView.findViewById(R.id.tvExamDescription);
            tvMeta          = itemView.findViewById(R.id.tvExamMeta);
            tvParticipants  = itemView.findViewById(R.id.tvParticipants);
            tvStatus        = itemView.findViewById(R.id.tvExamStatus);
            tvDeadline      = itemView.findViewById(R.id.tvExamDeadline);
        }
    }
}
