package com.example.waviapp.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waviapp.R;
import com.example.waviapp.models.Part3Question;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Part3Adapter extends RecyclerView.Adapter<Part3Adapter.QuestionViewHolder> {

    private final Context context;
    private final List<Part3Question> questions;
    private final Map<Integer, String> userAnswers = new HashMap<>();
    private boolean isFinished = false;
    private OnAllQuestionsAnsweredListener listener;

    public interface OnAllQuestionsAnsweredListener {
        void onAnswered(int position, boolean isCorrect);
        void onAllAnswered();
    }

    public Part3Adapter(Context context, List<Part3Question> questions, OnAllQuestionsAnsweredListener listener) {
        this.context = context;
        this.questions = questions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_part3_question, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        Part3Question question = questions.get(position);
        holder.tvQuestionText.setText((position + 1) + ". " + question.getQuestionText());
        
        List<String> options = question.getOptions();
        holder.btnA.setText(options.get(0));
        holder.btnB.setText(options.get(1));
        holder.btnC.setText(options.get(2));
        holder.btnD.setText(options.get(3));

        resetButtonStyles(holder, position);

        holder.btnA.setOnClickListener(v -> handleAnswer(holder, position, "A"));
        holder.btnB.setOnClickListener(v -> handleAnswer(holder, position, "B"));
        holder.btnC.setOnClickListener(v -> handleAnswer(holder, position, "C"));
        holder.btnD.setOnClickListener(v -> handleAnswer(holder, position, "D"));

        if (isFinished) {
            showResult(holder, position);
        } else {
            holder.tvExplanation.setVisibility(View.GONE);
        }
    }

    private void handleAnswer(QuestionViewHolder holder, int position, String selected) {
        if (isFinished || userAnswers.containsKey(position)) return;

        userAnswers.put(position, selected);
        String correct = questions.get(position).getCorrectAnswer();
        boolean isCorrect = selected.equals(correct);

        updateButtonStyle(holder, selected, isCorrect, correct);
        
        if (listener != null) {
            listener.onAnswered(position, isCorrect);
            if (userAnswers.size() == questions.size()) {
                listener.onAllAnswered();
            }
        }
    }

    private void updateButtonStyle(QuestionViewHolder holder, String selected, boolean isCorrect, String correct) {
        Button[] buttons = {holder.btnA, holder.btnB, holder.btnC, holder.btnD};
        String[] codes = {"A", "B", "C", "D"};

        for (int i = 0; i < 4; i++) {
            if (codes[i].equals(selected)) {
                buttons[i].setBackgroundTintList(ColorStateList.valueOf(isCorrect ? Color.parseColor("#4CAF50") : Color.parseColor("#F44336")));
                buttons[i].setTextColor(Color.WHITE);
            }
            if (!isCorrect && codes[i].equals(correct)) {
                buttons[i].setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                buttons[i].setTextColor(Color.WHITE);
            }
            buttons[i].setEnabled(false);
        }
    }

    private void showResult(QuestionViewHolder holder, int position) {
        holder.tvExplanation.setText(questions.get(position).getExplanation());
        holder.tvExplanation.setVisibility(View.VISIBLE);

        // Nếu đã trả lời rồi thì updateButtonStyle đã lo phần màu sắc
        // Nếu chưa trả lời mà đã bấm xem đáp án, ta highlight câu đúng
        if (!userAnswers.containsKey(position)) {
            String correct = questions.get(position).getCorrectAnswer();
            Button[] buttons = {holder.btnA, holder.btnB, holder.btnC, holder.btnD};
            String[] codes = {"A", "B", "C", "D"};
            for (int i = 0; i < 4; i++) {
                if (codes[i].equals(correct)) {
                    buttons[i].setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                    buttons[i].setTextColor(Color.WHITE);
                }
                buttons[i].setEnabled(false);
            }
        } else {
            // Re-apply selection styles to ensure consistency after scroll/rebind
            String selected = userAnswers.get(position);
            String correct = questions.get(position).getCorrectAnswer();
            updateButtonStyle(holder, selected, selected.equals(correct), correct);
        }
    }

    private void resetButtonStyles(QuestionViewHolder holder, int position) {
        Button[] buttons = {holder.btnA, holder.btnB, holder.btnC, holder.btnD};
        int purpleMain = Color.parseColor("#9370DB");

        for (Button btn : buttons) {
            btn.setBackgroundTintList(ColorStateList.valueOf(purpleMain));
            btn.setTextColor(Color.WHITE);
            btn.setEnabled(true);
        }

        // Nếu đã trả lời trước đó (khi cuộn RecyclerView lại)
        if (userAnswers.containsKey(position)) {
            String selected = userAnswers.get(position);
            String correct = questions.get(position).getCorrectAnswer();
            updateButtonStyle(holder, selected, selected.equals(correct), correct);
        }
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    static class QuestionViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestionText, tvExplanation;
        Button btnA, btnB, btnC, btnD;

        public QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestionText = itemView.findViewById(R.id.tvQuestionText);
            tvExplanation = itemView.findViewById(R.id.tvExplanation);
            btnA = itemView.findViewById(R.id.btnOptionA);
            btnB = itemView.findViewById(R.id.btnOptionB);
            btnC = itemView.findViewById(R.id.btnOptionC);
            btnD = itemView.findViewById(R.id.btnOptionD);
        }
    }
}
