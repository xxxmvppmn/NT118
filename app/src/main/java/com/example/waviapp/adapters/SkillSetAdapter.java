package com.example.waviapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.waviapp.R;
import java.util.List;

/**
 * Generic adapter dùng chung cho SpeakSetActivity và WriteSetActivity.
 * Hiển thị danh sách các phần luyện tập (Part 1, Part 2, ...).
 */
public class SkillSetAdapter extends RecyclerView.Adapter<SkillSetAdapter.ViewHolder> {

    public interface OnPartClickListener {
        void onPartClick(int partIndex);
    }

    private final List<String> partNames;
    private final List<String> partDescriptions;
    private final OnPartClickListener listener;

    public SkillSetAdapter(List<String> partNames, List<String> partDescriptions,
                           OnPartClickListener listener) {
        this.partNames = partNames;
        this.partDescriptions = partDescriptions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_part5_set, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvSetName.setText(partNames.get(position));
        holder.tvSetProgress.setText(partDescriptions.get(position));
        // Always show play (not locked)
        holder.ivStatus.setImageResource(R.drawable.ic_play);
        holder.ivStatus.setColorFilter(android.graphics.Color.parseColor("#9370DB"));
        holder.itemView.setOnClickListener(v -> listener.onPartClick(position));
    }

    @Override
    public int getItemCount() {
        return partNames.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSetName, tvSetProgress;
        ImageView ivStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSetName = itemView.findViewById(R.id.tvSetName);
            tvSetProgress = itemView.findViewById(R.id.tvSetProgress);
            ivStatus = itemView.findViewById(R.id.ivStatus);
        }
    }
}
