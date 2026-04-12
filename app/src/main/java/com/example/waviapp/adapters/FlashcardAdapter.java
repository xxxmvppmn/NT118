package com.example.waviapp.adapters;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.waviapp.R;
import com.example.waviapp.models.TuVung;
import java.util.List;

public class FlashcardAdapter extends RecyclerView.Adapter<FlashcardAdapter.FlashcardViewHolder> {

    private List<TuVung> wordList;
    private TextToSpeech tts;
    private boolean isFront = true;

    public FlashcardAdapter(List<TuVung> wordList, TextToSpeech tts) {
        this.wordList = wordList;
        this.tts = tts;
    }

    @NonNull
    @Override
    public FlashcardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flashcard, parent, false);
        return new FlashcardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlashcardViewHolder holder, int position) {
        TuVung word = wordList.get(position);
        holder.tvWord.setText(word.getTuTiengAnh());
        holder.tvPhonetic.setText(word.getPhienAm());
        holder.tvMeaning.setText(word.getNghiaTiengViet());
        holder.tvExample.setText(word.getCauViDu());

        // Reset to front
        holder.frontCard.setVisibility(View.VISIBLE);
        holder.backCard.setVisibility(View.GONE);
        isFront = true;

        // Flip on card click
        holder.itemView.setOnClickListener(v -> flipCard(holder));

        // Speaker click
        holder.ivSpeaker.setOnClickListener(v -> {
            if (tts != null && word.getTuTiengAnh() != null) {
                tts.speak(word.getTuTiengAnh(), TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });
    }

    private void flipCard(FlashcardViewHolder holder) {
        AnimatorSet outAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(holder.itemView.getContext(),
                isFront ? R.animator.card_flip_left_out : R.animator.card_flip_left_in);
        AnimatorSet inAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(holder.itemView.getContext(),
                isFront ? R.animator.card_flip_left_in : R.animator.card_flip_left_out);

        outAnimator.setTarget(isFront ? holder.frontCard : holder.backCard);
        inAnimator.setTarget(isFront ? holder.backCard : holder.frontCard);

        outAnimator.start();
        inAnimator.start();

        outAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                holder.frontCard.setVisibility(isFront ? View.GONE : View.VISIBLE);
                holder.backCard.setVisibility(isFront ? View.VISIBLE : View.GONE);
                isFront = !isFront;
            }
        });
    }

    @Override
    public int getItemCount() {
        return wordList.size();
    }

    public static class FlashcardViewHolder extends RecyclerView.ViewHolder {
        LinearLayout frontCard, backCard;
        TextView tvWord, tvPhonetic, tvMeaning, tvExample;
        ImageView ivSpeaker;

        public FlashcardViewHolder(@NonNull View itemView) {
            super(itemView);
            frontCard = itemView.findViewById(R.id.frontCard);
            backCard = itemView.findViewById(R.id.backCard);
            tvWord = itemView.findViewById(R.id.tvWord);
            tvPhonetic = itemView.findViewById(R.id.tvPhonetic);
            tvMeaning = itemView.findViewById(R.id.tvMeaning);
            tvExample = itemView.findViewById(R.id.tvExample);
            ivSpeaker = itemView.findViewById(R.id.ivSpeaker);
        }
    }
}
