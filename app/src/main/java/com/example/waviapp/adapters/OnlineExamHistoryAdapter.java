package com.example.waviapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waviapp.OnlineExamHistory;
import com.example.waviapp.R;

import java.util.List;

public class OnlineExamHistoryAdapter extends RecyclerView.Adapter<OnlineExamHistoryAdapter.ViewHolder> {

    private List<OnlineExamHistory> historyList;

    public OnlineExamHistoryAdapter(List<OnlineExamHistory> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_online_exam_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OnlineExamHistory exam = historyList.get(position);
        holder.tvTitle.setText(exam.getTitle());
        holder.tvMeta.setText(exam.getQuestions() + " câu hỏi  -  " + exam.getMinutes() + " phút");
        holder.vThumbnailBg.setBackgroundResource(exam.getColorResId());
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMeta;
        View vThumbnailBg;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvHistoryTitle);
            tvMeta = itemView.findViewById(R.id.tvHistoryMeta);
            vThumbnailBg = itemView.findViewById(R.id.vThumbnailBg);
        }
    }
}
