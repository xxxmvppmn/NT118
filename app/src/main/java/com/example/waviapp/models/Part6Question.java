package com.example.waviapp.models;

import java.io.Serializable;
import java.util.Map;

public class Part6Question implements Serializable {
    private int number;
    private Map<String, String> options;
    private String correctAnswer;
    private String explanation;

    public Part6Question() {}

    public int getNumber() { return number; }
    public Map<String, String> getOptions() { return options; }
    public String getCorrectAnswer() { return correctAnswer; }
    public String getExplanation() { return explanation; }
}
