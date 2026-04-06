package com.example.waviapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waviapp.R;
import com.example.waviapp.models.NguPhap;

import java.util.List;

public class GrammarAdapter extends RecyclerView.Adapter<GrammarAdapter.GrammarViewHolder> {

    private List<NguPhap> grammarList;

    public GrammarAdapter(List<NguPhap> grammarList) {
        this.grammarList = grammarList;
    }

    @NonNull
    @Override
    public GrammarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grammar, parent, false);
        return new GrammarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GrammarViewHolder holder, int position) {
        NguPhap item = grammarList.get(position);
        holder.tvOrder.setText(String.valueOf(position + 1));
        holder.tvGrammarTitle.setText(item.getTenBai());
    }

    @Override
    public int getItemCount() {
        return grammarList.size();
    }

    public static class GrammarViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrder, tvGrammarTitle;

        public GrammarViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrder = itemView.findViewById(R.id.tvOrder);
            tvGrammarTitle = itemView.findViewById(R.id.tvGrammarTitle);
        }
    }
}
