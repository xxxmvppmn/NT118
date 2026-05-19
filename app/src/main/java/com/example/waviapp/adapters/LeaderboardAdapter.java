package com.example.waviapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waviapp.R;
import com.example.waviapp.models.OnlineExamResult;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private final List<OnlineExamResult> results;
    private final String currentUserId;

    public LeaderboardAdapter(List<OnlineExamResult> results, String currentUserId) {
        this.results = results;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OnlineExamResult result = results.get(position);

        // Huy chương cho top 3
        int rank = result.getRank();
        switch (rank) {
            case 1: holder.tvRank.setText("🥇"); break;
            case 2: holder.tvRank.setText("🥈"); break;
            case 3: holder.tvRank.setText("🥉"); break;
            default: holder.tvRank.setText(String.valueOf(rank)); break;
        }

        holder.tvName.setText(result.getDisplayName());
        holder.tvScore.setText(result.getScore() + "/" + result.getTotalQuestions());

        // Hiển thị phần trăm
        int percent = (int) result.getScorePercent();
        holder.tvPercent.setText(percent + "%");

        // Thời gian làm bài
        int min = result.getDurationSeconds() / 60;
        int sec = result.getDurationSeconds() % 60;
        holder.tvDuration.setText(String.format("%d:%02d", min, sec));

        // Highlight user hiện tại
        if (currentUserId != null && currentUserId.equals(result.getUserId())) {
            holder.itemView.setBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.purple_100));
            holder.tvName.setText(result.getDisplayName() + " (Bạn)");
        } else {
            holder.itemView.setBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), android.R.color.transparent));
        }
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvName, tvScore, tvPercent, tvDuration;

        ViewHolder(View itemView) {
            super(itemView);
            tvRank     = itemView.findViewById(R.id.tvLeaderRank);
            tvName     = itemView.findViewById(R.id.tvLeaderName);
            tvScore    = itemView.findViewById(R.id.tvLeaderScore);
            tvPercent  = itemView.findViewById(R.id.tvLeaderPercent);
            tvDuration = itemView.findViewById(R.id.tvLeaderDuration);
        }
    }
}
