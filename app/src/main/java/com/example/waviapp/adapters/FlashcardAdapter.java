package com.example.waviapp.adapters;

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
        holder.tvExample.setText(word.getCauViDu() != null ? word.getCauViDu() : "");

        // --- QUAN TRỌNG: Reset trạng thái khi scroll ---
        // Đảm bảo card mới luôn hiện mặt trước và chữ XUÔI CHIỀU (0 độ)
        holder.frontCard.setVisibility(View.VISIBLE);
        holder.backCard.setVisibility(View.GONE);
        holder.frontCard.setRotationY(0f);
        holder.backCard.setRotationY(0f);
        holder.itemView.setCameraDistance(8000 * holder.itemView.getContext().getResources().getDisplayMetrics().density);

        // Sự kiện lật thẻ
        holder.itemView.setOnClickListener(v -> flipCard(holder));

        // Sự kiện loa phát âm
        holder.ivSpeaker.setOnClickListener(v -> {
            if (tts != null && word.getTuTiengAnh() != null) {
                tts.speak(word.getTuTiengAnh(), TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });
    }

    private void flipCard(FlashcardViewHolder holder) {
        View front = holder.frontCard;
        View back = holder.backCard;

        if (front.getVisibility() == View.VISIBLE) {
            // LẬT TRƯỚC -> SAU
            // Bước 1: Xoay mặt trước đi 90 độ (đứng lại)
            front.animate().rotationY(90).setDuration(200).withEndAction(() -> {
                front.setVisibility(View.GONE);
                back.setVisibility(View.VISIBLE);
                // Bước 2: Ép mặt sau chờ ở góc -90 độ rồi xoay về 0 (để chữ xuôi)
                back.setRotationY(-90);
                back.animate().rotationY(0).setDuration(200).start();
            }).start();
        } else {
            // LẬT SAU -> TRƯỚC (Fix lỗi soi gương)
            // Bước 1: Xoay mặt sau đi 90 độ
            back.animate().rotationY(90).setDuration(200).withEndAction(() -> {
                back.setVisibility(View.GONE);
                front.setVisibility(View.VISIBLE);
                // Bước 2: Ép mặt trước chờ ở góc -90 rồi xoay về 0 => CHỮ LUÔN XUÔI
                front.setRotationY(-90);
                front.animate().rotationY(0).setDuration(200).start();
            }).start();
        }
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