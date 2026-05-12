package com.example.waviapp.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.waviapp.R;
import java.util.Map;

public class AnswerSheetAdapter extends RecyclerView.Adapter<AnswerSheetAdapter.ViewHolder> {

    private final int totalQuestions;
    private final Map<Integer, String> userAnswers;
    private Map<Integer, String> correctAnswers;
    private boolean isSubmitted = false;

    public AnswerSheetAdapter(int totalQuestions, Map<Integer, String> userAnswers) {
        this.totalQuestions = totalQuestions;
        this.userAnswers = userAnswers;
    }

    public void setSubmitted(boolean submitted, Map<Integer, String> correctAnswers) {
        this.isSubmitted = submitted;
        this.correctAnswers = correctAnswers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_answer_question, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int questionNum = position + 1;
        holder.tvNum.setText(questionNum + ".");

        // Clear state to avoid recycling issues
        holder.rg.setOnCheckedChangeListener(null);
        holder.rg.clearCheck();

        String userAns = userAnswers.get(questionNum);
        if ("A".equals(userAns)) holder.rbA.setChecked(true);
        else if ("B".equals(userAns)) holder.rbB.setChecked(true);
        else if ("C".equals(userAns)) holder.rbC.setChecked(true);
        else if ("D".equals(userAns)) holder.rbD.setChecked(true);

        if (isSubmitted) {
            String correct = correctAnswers != null ? correctAnswers.get(questionNum) : "";
            boolean isCorrect = userAns != null && userAns.equals(correct);

            if (isCorrect) {
                holder.itemView.setBackgroundColor(Color.parseColor("#C8E6C9")); // Light Green
            } else if (userAns != null) {
                holder.itemView.setBackgroundColor(Color.parseColor("#FFCDD2")); // Light Red
            } else {
                holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            }

            holder.tvCorrect.setVisibility(View.VISIBLE);
            holder.tvCorrect.setText("Đáp án đúng: " + correct);

            // Disable interaction after submission
            for (int i = 0; i < holder.rg.getChildCount(); i++) {
                holder.rg.getChildAt(i).setEnabled(false);
            }
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            holder.tvCorrect.setVisibility(View.GONE);
            for (int i = 0; i < holder.rg.getChildCount(); i++) {
                holder.rg.getChildAt(i).setEnabled(true);
            }

            holder.rg.setOnCheckedChangeListener((group, checkedId) -> {
                String ans = "";
                if (checkedId == R.id.rbA) ans = "A";
                else if (checkedId == R.id.rbB) ans = "B";
                else if (checkedId == R.id.rbC) ans = "C";
                else if (checkedId == R.id.rbD) ans = "D";
                userAnswers.put(questionNum, ans);
            });
        }
    }

    @Override
    public int getItemCount() {
        return totalQuestions;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNum, tvCorrect;
        RadioGroup rg;
        RadioButton rbA, rbB, rbC, rbD;

        ViewHolder(View v) {
            super(v);
            tvNum = v.findViewById(R.id.tvQuestionNumber);
            tvCorrect = v.findViewById(R.id.tvCorrectAnswer);
            rg = v.findViewById(R.id.rgOptions);
            rbA = v.findViewById(R.id.rbA);
            rbB = v.findViewById(R.id.rbB);
            rbC = v.findViewById(R.id.rbC);
            rbD = v.findViewById(R.id.rbD);
        }
    }
}
