package com.example.waviapp.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waviapp.activities.GrammarDetailActivity;
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

        // Hiển thị số thứ tự (ví dụ: 1, 2, 3...)
        holder.tvOrder.setText(String.valueOf(position + 1));
        // Hiển thị tiêu đề bài học (ví dụ: Present Simple)
        holder.tvGrammarTitle.setText(item.getTenBai());

        // Bắt sự kiện click để mở trang chi tiết
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), GrammarDetailActivity.class);

            intent.putExtra("grammar_title", item.getTenBai());
            intent.putExtra("grammar_content", item.getNoiDungLyThuyet());
            intent.putExtra("grammar_example", item.getViDu());

            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return (grammarList != null) ? grammarList.size() : 0;
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
