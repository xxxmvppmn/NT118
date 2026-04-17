package com.example.waviapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.waviapp.R;
import com.example.waviapp.utils.ProgressManager;
import java.util.List;

public class Part6SetAdapter extends RecyclerView.Adapter<Part6SetAdapter.ViewHolder> {

    private List<String> setNames;
    private ProgressManager progressManager;
    private OnSetClickListener listener;

    public interface OnSetClickListener {
        void onSetClick(int setIndex);
    }

    public Part6SetAdapter(List<String> setNames, ProgressManager progressManager, OnSetClickListener listener) {
        this.setNames = setNames;
        this.progressManager = progressManager;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_part6_set, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = setNames.get(position);
        holder.tvSetName.setText(name);
        
        // Use a prefix for Part 6 progress in ProgressManager if necessary, 
        // or just different indices if ProgressManager supports it.
        // For simplicity, let's assume we use setIndex + 100 for Part 6 to avoid collision with Part 5
        int internalIndex = position + 100; 
        
        int progress = progressManager.getSetProgress(internalIndex);
        boolean isCompleted = progressManager.isSetCompleted(internalIndex);
        
        if (isCompleted) {
            holder.tvSetProgress.setText("Completed (5/5 paragraphs)");
            holder.ivStatus.setImageResource(R.drawable.ic_check_circle);
            holder.ivStatus.setColorFilter(android.graphics.Color.parseColor("#4CAF50"));
        } else {
            // progress here might represent paragraphs completed
            holder.tvSetProgress.setText(progress + "/5 paragraphs completed");
            holder.ivStatus.setImageResource(R.drawable.ic_play);
            holder.ivStatus.setColorFilter(android.graphics.Color.parseColor("#9370DB"));
        }

        holder.itemView.setOnClickListener(v -> listener.onSetClick(position));
    }

    @Override
    public int getItemCount() {
        return setNames.size();
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
