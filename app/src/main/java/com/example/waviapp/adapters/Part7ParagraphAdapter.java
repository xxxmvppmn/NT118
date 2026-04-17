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
import com.example.waviapp.models.Part7Paragraph;
import com.example.waviapp.models.Part7Question;
import com.google.android.material.button.MaterialButton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Part7ParagraphAdapter extends RecyclerView.Adapter<Part7ParagraphAdapter.ViewHolder> {

    private Context context;
    private List<Part7Paragraph> paragraphs;
    private OnPart7InteractionListener listener;
    
    // Track current question index for each paragraph position
    private Map<Integer, Integer> currentQuestionIndices = new HashMap<>();
    // Track which questions are answered to prevent re-answering and enable Next button
    private Map<Integer, Map<Integer, Boolean>> answeredState = new HashMap<>();

    public interface OnPart7InteractionListener {
        void onQuestionAnswered(boolean isCorrect);
        void onParagraphCompleted(int paragraphPos, int totalCorrect);
    }

    public Part7ParagraphAdapter(Context context, List<Part7Paragraph> paragraphs, OnPart7InteractionListener listener) {
        this.context = context;
        this.paragraphs = paragraphs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_part7_paragraph, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Part7Paragraph paragraph = paragraphs.get(position);
        holder.tvType.setText(paragraph.getType());
        holder.tvContent.setText(paragraph.getContent());

        int qIndex = currentQuestionIndices.getOrDefault(position, 0);
        displayQuestion(holder, position, qIndex);
    }

    public void displayQuestion(ViewHolder holder, int paragraphPos, int qIndex) {
        Part7Paragraph paragraph = paragraphs.get(paragraphPos);
        if (qIndex >= paragraph.getQuestions().size()) return;

        Part7Question question = paragraph.getQuestions().get(qIndex);
        holder.tvQuestionText.setText("Câu hỏi " + (qIndex + 1) + "/" + paragraph.getQuestions().size() + ": " + question.getQuestion());
        
        Map<String, String> opts = question.getOptions();
        holder.btnA.setText("A. " + opts.get("A"));
        holder.btnB.setText("B. " + opts.get("B"));
        holder.btnC.setText("C. " + opts.get("C"));
        holder.btnD.setText("D. " + opts.get("D"));

        resetButtons(holder);
        holder.llExplanation.setVisibility(View.GONE);

        View.OnClickListener clickListener = v -> {
            MaterialButton clicked = (MaterialButton) v;
            String selected = "";
            if (clicked.getId() == R.id.btnOptionA) selected = "A";
            else if (clicked.getId() == R.id.btnOptionB) selected = "B";
            else if (clicked.getId() == R.id.btnOptionC) selected = "C";
            else if (clicked.getId() == R.id.btnOptionD) selected = "D";

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

            // Mark as answered
            Map<Integer, Boolean> pState = answeredState.computeIfAbsent(paragraphPos, k -> new HashMap<>());
            pState.put(qIndex, isCorrect);

            listener.onQuestionAnswered(isCorrect);
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

    /**
     * Tries to move to the next question in the current paragraph.
     * @return true if there was a next question, false if the paragraph is finished.
     */
    public boolean goToNextQuestion(int position, ViewHolder holder) {
        int currentIndex = currentQuestionIndices.getOrDefault(position, 0);
        int nextIndex = currentIndex + 1;
        
        if (nextIndex < paragraphs.get(position).getQuestions().size()) {
            currentQuestionIndices.put(position, nextIndex);
            displayQuestion(holder, position, nextIndex);
            return true;
        } else {
            // Paragraph finished
            int correctCount = 0;
            Map<Integer, Boolean> pState = answeredState.get(position);
            if (pState != null) {
                for (boolean b : pState.values()) if (b) correctCount++;
            }
            listener.onParagraphCompleted(position, correctCount);
            return false;
        }
    }

    @Override
    public int getItemCount() {
        return paragraphs.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvContent, tvQuestionText, tvExplanation;
        MaterialButton btnA, btnB, btnC, btnD;
        LinearLayout llExplanation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvParagraphType);
            tvContent = itemView.findViewById(R.id.tvParagraphContent);
            tvQuestionText = itemView.findViewById(R.id.tvQuestionText);
            tvExplanation = itemView.findViewById(R.id.tvExplanation);
            btnA = itemView.findViewById(R.id.btnOptionA);
            btnB = itemView.findViewById(R.id.btnOptionB);
            btnC = itemView.findViewById(R.id.btnOptionC);
            btnD = itemView.findViewById(R.id.btnOptionD);
            llExplanation = itemView.findViewById(R.id.llExplanation);
        }
    }
}
