package com.example.waviapp.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.waviapp.Word;
import com.example.waviapp.databinding.ItemVocabularyBinding; // Tự động sinh ra từ file item_vocabulary.xml
import java.util.List;

public class VocabularyAdapter extends RecyclerView.Adapter<VocabularyAdapter.ViewHolder> {

    private List<Word> wordList;

    public VocabularyAdapter(List<Word> wordList) {
        this.wordList = wordList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng Binding cho từng item để code sạch và nhanh hơn
        ItemVocabularyBinding binding = ItemVocabularyBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Word word = wordList.get(position);

        // Đổ dữ liệu vào các ID bạn đã đặt trong item_vocabulary.xml
        holder.binding.tvWord.setText(word.getText());
        holder.binding.tvPhonetic.setText(word.getPhonetic());
        holder.binding.tvMeaning.setText(word.getMeaning());
        holder.binding.cbFavorite.setChecked(word.isFavorite());

        // Xử lý khi bấm vào loa (Demo Toast hoặc Log)
        holder.binding.ivSpeaker.setOnClickListener(v -> {
            // Sau này bạn sẽ viết code phát âm thanh ở đây
        });

        // Xử lý khi bấm vào trái tim
        holder.binding.cbFavorite.setOnCheckedChangeListener((buttonView, isChecked) -> {
            word.setFavorite(isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return wordList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemVocabularyBinding binding;

        public ViewHolder(ItemVocabularyBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
