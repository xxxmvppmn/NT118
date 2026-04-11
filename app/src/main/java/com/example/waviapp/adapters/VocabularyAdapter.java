package com.example.waviapp.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waviapp.R;
import com.example.waviapp.models.TuVung;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class VocabularyAdapter extends RecyclerView.Adapter<VocabularyAdapter.VocabularyViewHolder> {

    private List<TuVung> vocabularyList;
    private OnVocabularyClickListener listener;
    private FirebaseFirestore db;

    public VocabularyAdapter(List<TuVung> vocabularyList, OnVocabularyClickListener listener) {
        this.vocabularyList = vocabularyList;
        this.listener = listener;
        this.db = FirebaseFirestore.getInstance();
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

        // Prevent trigger loops
        holder.cbFavorite.setOnCheckedChangeListener(null);
        holder.cbFavorite.setChecked(word.isFavorite());

        holder.cbFavorite.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Only update if the value has changed
            if (word.isFavorite() != isChecked) {
                word.setFavorite(isChecked);
                // Update Firestore
                if (word.getMaTV() != null) {
                    Log.d("FIREBASE_DEBUG", "Updating " + word.getMaTV() + " to " + isChecked);
                    db.collection("TuVung").document(word.getMaTV())
                            .update("favorite", isChecked)
                            .addOnFailureListener(e -> Log.e("FIREBASE_ERROR", "Failed to update favorite: " + e.getMessage()));
                } else {
                    Log.e("FIREBASE_ERROR", "maTV is null for word: " + word.getTuTiengAnh());
                }
            }
        });

        holder.layoutSpeaker.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSpeakClick(word.getTuTiengAnh());
            }
        });
    }

    @Override
    public int getItemCount() {
        return vocabularyList != null ? vocabularyList.size() : 0;
    }

    public interface OnVocabularyClickListener {
        void onSpeakClick(String text);
    }

    public static class VocabularyViewHolder extends RecyclerView.ViewHolder {
        TextView tvWord, tvPhonetic, tvMeaning;
        CheckBox cbFavorite;
        FrameLayout layoutSpeaker;

        public VocabularyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWord = itemView.findViewById(R.id.tvWord);
            tvPhonetic = itemView.findViewById(R.id.tvPhonetic);
            tvMeaning = itemView.findViewById(R.id.tvMeaning);
            cbFavorite = itemView.findViewById(R.id.cbFavorite);
            layoutSpeaker = itemView.findViewById(R.id.layoutSpeaker);
        }
    }
}
