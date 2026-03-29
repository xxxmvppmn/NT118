package com.example.waviapp.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.waviapp.Grammar;
import com.example.waviapp.databinding.ItemGrammarBinding;
import java.util.List;

public class GrammarAdapter extends RecyclerView.Adapter<GrammarAdapter.ViewHolder> {

    private List<Grammar> grammarList;

    public GrammarAdapter(List<Grammar> grammarList) {
        this.grammarList = grammarList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGrammarBinding binding = ItemGrammarBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Grammar grammar = grammarList.get(position);

        holder.binding.tvOrder.setText(String.valueOf(grammar.getOrder()));
        holder.binding.tvGrammarTitle.setText(grammar.getTitle());

        // Handle click events if needed
        holder.itemView.setOnClickListener(v -> {
            // Handle item click
        });
    }

    @Override
    public int getItemCount() {
        return grammarList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemGrammarBinding binding;

        public ViewHolder(ItemGrammarBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
