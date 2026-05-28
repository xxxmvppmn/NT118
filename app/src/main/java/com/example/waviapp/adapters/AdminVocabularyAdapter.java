package com.example.waviapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waviapp.R;
import com.example.waviapp.models.TuVung;

import java.util.ArrayList;
import java.util.List;

public class AdminVocabularyAdapter extends RecyclerView.Adapter<AdminVocabularyAdapter.VocabViewHolder> {

    private List<TuVung> words = new ArrayList<>();
    private OnVocabActionListener listener;

    public interface OnVocabActionListener {
        void onEdit(TuVung word);
        void onDelete(TuVung word, int position);
    }

    public AdminVocabularyAdapter(OnVocabActionListener listener) {
        this.listener = listener;
    }

    public void setWords(List<TuVung> words) {
        this.words = words;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < words.size()) {
            words.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, words.size() - position);
        }
    }

    public void updateItem(TuVung word, int position) {
        if (position >= 0 && position < words.size()) {
            words.set(position, word);
            notifyItemChanged(position);
        }
    }

    @NonNull
    @Override
    public VocabViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_vocabulary, parent, false);
        return new VocabViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VocabViewHolder holder, int position) {
        holder.bind(words.get(position), position);
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    class VocabViewHolder extends RecyclerView.ViewHolder {
        TextView txtWord, txtMeaning, txtType, txtTopic;
        ImageView btnEdit, btnDelete;

        VocabViewHolder(@NonNull View itemView) {
            super(itemView);
            txtWord = itemView.findViewById(R.id.txtWord);
            txtMeaning = itemView.findViewById(R.id.txtMeaning);
            txtType = itemView.findViewById(R.id.txtType);
            txtTopic = itemView.findViewById(R.id.txtTopic);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(TuVung word, int position) {
            txtWord.setText(word.getTuTiengAnh() != null ? word.getTuTiengAnh() : "");
            txtMeaning.setText(word.getNghiaTiengViet() != null ? word.getNghiaTiengViet() : "");
            txtType.setText(word.getLoaiTu() != null ? word.getLoaiTu() : "");
            txtTopic.setText(word.getMaCD() != null ? word.getMaCD() : "");

            btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(word);
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(word, position);
            });
        }
    }
}
