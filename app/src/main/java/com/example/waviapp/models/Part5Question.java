package com.example.waviapp.models;

import java.io.Serializable;

public class Part5Question implements Serializable {
    private int id;
    private String question;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctAnswer;
    private String explanation;

    public Part5Question() {}

    public int getId() { return id; }
    public String getQuestion() { return question; }
    public String getOptionA() { return optionA; }
    public String getOptionB() { return optionB; }
    public String getOptionC() { return optionC; }
    public String getOptionD() { return optionD; }
    public String getCorrectAnswer() { return correctAnswer; }
    public String getExplanation() { return explanation; }
}
