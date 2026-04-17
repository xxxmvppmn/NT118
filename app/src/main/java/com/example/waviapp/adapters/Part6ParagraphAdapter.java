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
import com.example.waviapp.models.Part6Paragraph;
import com.example.waviapp.models.Part6Question;
import com.google.android.material.button.MaterialButton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Part6ParagraphAdapter extends RecyclerView.Adapter<Part6ParagraphAdapter.ViewHolder> {

    private Context context;
    private List<Part6Paragraph> paragraphs;
    private OnParagraphInteractionListener listener;
    // Map to keep track of answered questions per paragraph: ParagraphPos -> (QuestionIndex -> isCorrect)
    private Map<Integer, Map<Integer, Boolean>> answeredState = new HashMap<>();

    public interface OnParagraphInteractionListener {
        void onAllQuestionsAnswered(int paragraphPos, int correctInParagraph);
    }

    public Part6ParagraphAdapter(Context context, List<Part6Paragraph> paragraphs, OnParagraphInteractionListener listener) {
        this.context = context;
        this.paragraphs = paragraphs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_part6_paragraph, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Part6Paragraph paragraph = paragraphs.get(position);
        holder.tvType.setText(paragraph.getType());
        holder.tvContent.setText(paragraph.getContent());

        // For simplicity in this Part 6 UI, we show one question at a time or all?
        // Requirement says "Phía dưới là 4 nút đáp án cho câu hỏi hiện tại".
        // To handle 4 questions per paragraph, let's manage a local index within the ViewHolder
        // OR just show the first unanswered question.
        
        setupQuestion(holder, position, 0); // Start with question 0 of the paragraph
    }

    private void setupQuestion(ViewHolder holder, int paragraphPos, int qIndex) {
        Part6Paragraph paragraph = paragraphs.get(paragraphPos);
        if (qIndex >= paragraph.getQuestions().size()) {
            // All questions in this paragraph answered
            holder.btnNextQ.setVisibility(View.GONE);
            return;
        }

        Part6Question question = paragraph.getQuestions().get(qIndex);
        holder.tvQuestionNumber.setText("Câu hỏi " + question.getNumber() + " (" + (qIndex + 1) + "/4)");
        
        Map<String, String> opts = question.getOptions();
        holder.btnA.setText("A. " + opts.get("A"));
        holder.btnB.setText("B. " + opts.get("B"));
        holder.btnC.setText("C. " + opts.get("C"));
        holder.btnD.setText("D. " + opts.get("D"));

        resetButtons(holder);

        View.OnClickListener clickListener = v -> {
            MaterialButton clicked = (MaterialButton) v;
            String selected = clicked.getId() == R.id.btnOptionA ? "A" :
                             clicked.getId() == R.id.btnOptionB ? "B" :
                             clicked.getId() == R.id.btnOptionC ? "C" : "D";

            boolean isCorrect = selected.equals(question.getCorrectAnswer());
            
            // UI Feedback
            if (isCorrect) {
                clicked.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                clicked.setTextColor(Color.WHITE);
            } else {
                clicked.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F44336")));
                clicked.setTextColor(Color.WHITE);
                showCorrectAnswer(holder, question.getCorrectAnswer());
            }

            disableButtons(holder);
            holder.llExplanation.setVisibility(View.VISIBLE);
            holder.tvExplanation.setText(question.getExplanation());

            // Save state
            Map<Integer, Boolean> pState = answeredState.computeIfAbsent(paragraphPos, k -> new HashMap<>());
            pState.put(qIndex, isCorrect);

            // Logic to move to next question or notify activity
            holder.itemView.postDelayed(() -> {
                if (qIndex < 3) {
                    setupQuestion(holder, paragraphPos, qIndex + 1);
                    holder.llExplanation.setVisibility(View.GONE);
                } else {
                    // Finished paragraph
                    int correctCount = 0;
                    for (boolean b : pState.values()) if (b) correctCount++;
                    listener.onAllQuestionsAnswered(paragraphPos, correctCount);
                }
            }, 1500);
        };

        holder.btnA.setOnClickListener(clickListener);
        holder.btnB.setOnClickListener(clickListener);
        holder.btnC.setOnClickListener(clickListener);
        holder.btnD.setOnClickListener(clickListener);
    }

    private void showCorrectAnswer(ViewHolder holder, String correct) {
        if ("A".equals(correct)) holder.btnA.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        if ("B".equals(correct)) holder.btnB.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        if ("C".equals(correct)) holder.btnC.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        if ("D".equals(correct)) holder.btnD.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
    }

    private void resetButtons(ViewHolder holder) {
        MaterialButton[] btns = {holder.btnA, holder.btnB, holder.btnC, holder.btnD};
        for (MaterialButton b : btns) {
            b.setEnabled(true);
            b.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            b.setTextColor(Color.parseColor("#2D3142"));
        }
    }

    private void disableButtons(ViewHolder holder) {
        holder.btnA.setEnabled(false);
        holder.btnB.setEnabled(false);
        holder.btnC.setEnabled(false);
        holder.btnD.setEnabled(false);
    }

    @Override
    public int getItemCount() {
        return paragraphs.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvContent, tvQuestionNumber, tvExplanation;
        MaterialButton btnA, btnB, btnC, btnD, btnNextQ;
        LinearLayout llExplanation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvParagraphType);
            tvContent = itemView.findViewById(R.id.tvParagraphContent);
            tvQuestionNumber = itemView.findViewById(R.id.tvQuestionNumber);
            tvExplanation = itemView.findViewById(R.id.tvExplanation);
            btnA = itemView.findViewById(R.id.btnOptionA);
            btnB = itemView.findViewById(R.id.btnOptionB);
            btnC = itemView.findViewById(R.id.btnOptionC);
            btnD = itemView.findViewById(R.id.btnOptionD);
            llExplanation = itemView.findViewById(R.id.llExplanation);
        }
    }
}
