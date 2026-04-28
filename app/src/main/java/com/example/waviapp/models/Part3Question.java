package com.example.waviapp.models;

import java.util.List;

public class Part3Question {
    private int questionId;
    private String questionText;
    private List<String> options;
    private String correctAnswer;
    private String explanation;

    public Part3Question() {}

    public int getQuestionId() { return questionId; }
    public String getQuestionText() { return questionText; }
    public List<String> getOptions() { return options; }
    public String getCorrectAnswer() { return correctAnswer; }
    public String getExplanation() { return explanation; }
}
