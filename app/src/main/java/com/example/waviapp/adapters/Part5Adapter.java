package com.example.waviapp.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.waviapp.R;
import com.example.waviapp.models.Part5Question;
import com.google.android.material.button.MaterialButton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Part5Adapter extends RecyclerView.Adapter<Part5Adapter.ViewHolder> {

    private List<Part5Question> questionList;
    private Context context;
    private Map<Integer, String> selectedAnswers = new HashMap<>();
    private OnAnswerSelectedListener listener;

    public interface OnAnswerSelectedListener {
        void onAnswerSelected(int position, boolean isCorrect);
    }

    public Part5Adapter(Context context, List<Part5Question> questionList, OnAnswerSelectedListener listener) {
        this.context = context;
        this.questionList = questionList;
        this.listener = listener;
    }

    // Hàm cực kỳ quan trọng để cập nhật 20 câu mới khi đổi Set
    public void setQuestions(List<Part5Question> newList) {
        this.questionList = newList;
        this.selectedAnswers.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_part5_question, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Part5Question question = questionList.get(position);

        // Hiển thị số câu theo list 20 câu
        holder.tvQuestion.setText("Question " + (position + 1) + ": " + question.getQuestion());

        holder.btnA.setText("A. " + question.getOptionA());
        holder.btnB.setText("B. " + question.getOptionB());
        holder.btnC.setText("C. " + question.getOptionC());
        holder.btnD.setText("D. " + question.getOptionD());
        holder.tvExplanation.setText(question.getExplanation());

        String selected = selectedAnswers.get(position);
        if (selected != null) {
            showFeedback(holder, question, selected);
        } else {
            resetButtons(holder);
            enableButtons(holder, true);
        }

        holder.btnA.setOnClickListener(v -> handleSelection(holder, position, "A"));
        holder.btnB.setOnClickListener(v -> handleSelection(holder, position, "B"));
        holder.btnC.setOnClickListener(v -> handleSelection(holder, position, "C"));
        holder.btnD.setOnClickListener(v -> handleSelection(holder, position, "D"));
    }

    private void handleSelection(ViewHolder holder, int position, String answer) {
        if (selectedAnswers.containsKey(position)) return;

        selectedAnswers.put(position, answer);
        Part5Question question = questionList.get(position);
        boolean isCorrect = question.getCorrectAnswer().equalsIgnoreCase(answer);

        showFeedback(holder, question, answer);
        if (listener != null) {
            listener.onAnswerSelected(position, isCorrect);
        }
    }

    private void showFeedback(ViewHolder holder, Part5Question question, String selected) {
        enableButtons(holder, false);
        String correct = question.getCorrectAnswer();

        updateButtonStyle(holder.btnA, "A", selected, correct);
        updateButtonStyle(holder.btnB, "B", selected, correct);
        updateButtonStyle(holder.btnC, "C", selected, correct);
        updateButtonStyle(holder.btnD, "D", selected, correct);

        holder.llExplanation.setVisibility(View.VISIBLE);
    }

    private void updateButtonStyle(MaterialButton btn, String option, String selected, String correct) {
        if (option.equalsIgnoreCase(correct)) {
            btn.setBackgroundColor(Color.parseColor("#4CAF50"));
            btn.setTextColor(Color.WHITE);
            btn.setStrokeColor(ColorStateList.valueOf(Color.TRANSPARENT));
        } else if (option.equalsIgnoreCase(selected)) {
            btn.setBackgroundColor(Color.parseColor("#F44336"));
            btn.setTextColor(Color.WHITE);
            btn.setStrokeColor(ColorStateList.valueOf(Color.TRANSPARENT));
        } else {
            btn.setBackgroundColor(Color.WHITE);
            btn.setTextColor(Color.parseColor("#2D3142"));
            btn.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#9370DB")));
        }
    }

    private void resetButtons(ViewHolder holder) {
        MaterialButton[] buttons = {holder.btnA, holder.btnB, holder.btnC, holder.btnD};
        for (MaterialButton btn : buttons) {
            btn.setBackgroundColor(Color.WHITE);
            btn.setTextColor(Color.parseColor("#2D3142"));
            btn.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#9370DB")));
        }
        holder.llExplanation.setVisibility(View.GONE);
    }

    private void enableButtons(ViewHolder holder, boolean enabled) {
        holder.btnA.setEnabled(enabled);
        holder.btnB.setEnabled(enabled);
        holder.btnC.setEnabled(enabled);
        holder.btnD.setEnabled(enabled);
    }

    @Override
    public int getItemCount() {
        return questionList != null ? questionList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestion, tvExplanation;
        MaterialButton btnA, btnB, btnC, btnD;
        LinearLayout llExplanation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tvQuestion);
            btnA = itemView.findViewById(R.id.btnOptionA);
            btnB = itemView.findViewById(R.id.btnOptionB);
            btnC = itemView.findViewById(R.id.btnOptionC);
            btnD = itemView.findViewById(R.id.btnOptionD);
            llExplanation = itemView.findViewById(R.id.llExplanation);
            tvExplanation = itemView.findViewById(R.id.tvExplanation);
        }
    }
}