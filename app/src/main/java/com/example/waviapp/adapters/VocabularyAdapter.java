package com.example.waviapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waviapp.R;
import com.example.waviapp.models.TuVung;

import java.util.List;

public class VocabularyAdapter extends RecyclerView.Adapter<VocabularyAdapter.VocabularyViewHolder> {

    private List<TuVung> vocabularyList;

    public VocabularyAdapter(List<TuVung> vocabularyList) {
        this.vocabularyList = vocabularyList;
    }

    @NonNull
    @Override
    public VocabularyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vocabulary, parent, false);
        return new VocabularyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VocabularyViewHolder holder, int position) {
        TuVung word = vocabularyList.get(position);
        holder.tvWord.setText(word.getTuTiengAnh());
        holder.tvPhonetic.setText(word.getPhienAm());
        holder.tvMeaning.setText(word.getNghiaTiengViet());

        holder.cbFavorite.setChecked(word.isFavorite());

        holder.cbFavorite.setOnCheckedChangeListener((buttonView, isChecked) -> {
            word.setFavorite(isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return vocabularyList.size();
    }

    public static class VocabularyViewHolder extends RecyclerView.ViewHolder {
        TextView tvWord, tvPhonetic, tvMeaning;
        ImageView ivSpeaker;
        CheckBox cbFavorite;

        public VocabularyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWord = itemView.findViewById(R.id.tvWord);
            tvPhonetic = itemView.findViewById(R.id.tvPhonetic);
            tvMeaning = itemView.findViewById(R.id.tvMeaning);
            ivSpeaker = itemView.findViewById(R.id.ivSpeaker);
            cbFavorite = itemView.findViewById(R.id.cbFavorite);
        }
    }
}
