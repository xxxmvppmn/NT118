package com.example.waviapp.models;

import java.io.Serializable;
import java.util.Map;

public class Part7Question implements Serializable {
    private String question;
    private Map<String, String> options;
    private String correctAnswer;
    private String explanation;

    public Part7Question() {}

    public String getQuestion() { return question; }
    public Map<String, String> getOptions() { return options; }
    public String getCorrectAnswer() { return correctAnswer; }
    public String getExplanation() { return explanation; }
}
