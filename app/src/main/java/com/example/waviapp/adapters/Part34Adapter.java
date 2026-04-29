package com.example.waviapp.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waviapp.R;
import com.example.waviapp.models.Part3Question;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Part34Adapter extends RecyclerView.Adapter<Part34Adapter.ViewHolder> {

    private Context context;
    private List<Part3Question> questions;
    private Map<Integer, String> selectedAnswers = new HashMap<>();
    private boolean revealResults = false;
    private OnAllAnsweredListener listener;

    public interface OnAllAnsweredListener {
        void onAllAnswered();
    }

    public Part34Adapter(Context context, List<Part3Question> questions, OnAllAnsweredListener listener) {
        this.context = context;
        this.questions = questions;
        this.listener = listener;
    }

    public void setRevealResults(boolean revealResults) {
        this.revealResults = revealResults;
        notifyDataSetChanged();
    }

    public int getCorrectAnswersCount() {
        int count = 0;
        for (int i = 0; i < questions.size(); i++) {
            if (selectedAnswers.containsKey(i) && selectedAnswers.get(i).equals(questions.get(i).getCorrectAnswer())) {
                count++;
            }
        }
        return count;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_part3_question, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Part3Question question = questions.get(position);
        holder.tvQuestion.setText((position + 1) + ". " + question.getQuestionText());
        
        List<String> options = question.getOptions();
        holder.btnA.setText(options.get(0));
        holder.btnB.setText(options.get(1));
        holder.btnC.setText(options.get(2));
        holder.btnD.setText(options.get(3));

        resetButton(holder.btnA, position, "A");
        resetButton(holder.btnB, position, "B");
        resetButton(holder.btnC, position, "C");
        resetButton(holder.btnD, position, "D");

        if (revealResults) {
            revealResult(holder.btnA, position, "A", question.getCorrectAnswer());
            revealResult(holder.btnB, position, "B", question.getCorrectAnswer());
            revealResult(holder.btnC, position, "C", question.getCorrectAnswer());
            revealResult(holder.btnD, position, "D", question.getCorrectAnswer());
            
            holder.tvExplanation.setVisibility(View.VISIBLE);
            holder.tvExplanation.setText(question.getExplanation());
        } else {
            holder.tvExplanation.setVisibility(View.GONE);
            
            holder.btnA.setOnClickListener(v -> select(position, "A"));
            holder.btnB.setOnClickListener(v -> select(position, "B"));
            holder.btnC.setOnClickListener(v -> select(position, "C"));
            holder.btnD.setOnClickListener(v -> select(position, "D"));
        }
    }

    private void resetButton(MaterialButton btn, int pos, String code) {
        btn.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#E0E0E0")));
        btn.setTextColor(Color.parseColor("#333333"));
        btn.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        
        if (selectedAnswers.containsKey(pos) && selectedAnswers.get(pos).equals(code)) {
            btn.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#9370DB")));
            btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F3E5F5")));
        }
    }

    private void revealResult(MaterialButton btn, int pos, String code, String correct) {
        btn.setOnClickListener(null);
        String selected = selectedAnswers.get(pos);
        
        if (code.equals(correct)) {
            btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
            btn.setTextColor(Color.WHITE);
            btn.setStrokeColor(null);
        } else if (code.equals(selected)) {
            btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F44336")));
            btn.setTextColor(Color.WHITE);
            btn.setStrokeColor(null);
        }
    }

    private void select(int pos, String code) {
        selectedAnswers.put(pos, code);
        notifyDataSetChanged();
        if (selectedAnswers.size() == questions.size() && listener != null) {
            listener.onAllAnswered();
        }
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestion, tvExplanation;
        MaterialButton btnA, btnB, btnC, btnD;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tvQuestionText);
            tvExplanation = itemView.findViewById(R.id.tvExplanation);
            btnA = itemView.findViewById(R.id.btnOptionA);
            btnB = itemView.findViewById(R.id.btnOptionB);
            btnC = itemView.findViewById(R.id.btnOptionC);
            btnD = itemView.findViewById(R.id.btnOptionD);
        }
    }
}
