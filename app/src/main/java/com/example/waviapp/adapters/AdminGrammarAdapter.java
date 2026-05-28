package com.example.waviapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waviapp.R;
import com.example.waviapp.models.NguPhap;

import java.util.ArrayList;
import java.util.List;

public class AdminGrammarAdapter extends RecyclerView.Adapter<AdminGrammarAdapter.GrammarViewHolder> {

    private List<NguPhap> grammarList = new ArrayList<>();
    private OnGrammarActionListener listener;

    public interface OnGrammarActionListener {
        void onEdit(NguPhap grammar);
        void onDelete(NguPhap grammar, int position);
    }

    public AdminGrammarAdapter(OnGrammarActionListener listener) {
        this.listener = listener;
    }

    public void setGrammarList(List<NguPhap> grammarList) {
        this.grammarList = grammarList;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < grammarList.size()) {
            grammarList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, grammarList.size() - position);
        }
    }

    public void updateItem(NguPhap grammar, int position) {
        if (position >= 0 && position < grammarList.size()) {
            grammarList.set(position, grammar);
            notifyItemChanged(position);
        }
    }

    @NonNull
    @Override
    public GrammarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_grammar, parent, false);
        return new GrammarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GrammarViewHolder holder, int position) {
        holder.bind(grammarList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return grammarList.size();
    }

    class GrammarViewHolder extends RecyclerView.ViewHolder {
        TextView txtGrammarName, txtGrammarTopic, txtGrammarOrder;
        ImageView btnEdit, btnDelete;

        GrammarViewHolder(@NonNull View itemView) {
            super(itemView);
            txtGrammarName = itemView.findViewById(R.id.txtGrammarName);
            txtGrammarTopic = itemView.findViewById(R.id.txtGrammarTopic);
            txtGrammarOrder = itemView.findViewById(R.id.txtGrammarOrder);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(NguPhap grammar, int position) {
            txtGrammarName.setText(grammar.getTenBai() != null ? grammar.getTenBai() : "");
            txtGrammarTopic.setText("Chủ đề: " + (grammar.getMaCD() != null ? grammar.getMaCD() : ""));
            txtGrammarOrder.setText("Thứ tự: " + grammar.getOrder());

            btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(grammar);
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(grammar, position);
            });
        }
    }
}
