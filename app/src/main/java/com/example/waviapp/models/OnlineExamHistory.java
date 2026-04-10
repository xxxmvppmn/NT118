package com.example.waviapp.models;

public class OnlineExamHistory {
    private String title;
    private int questions;
    private int minutes;
    private int colorResId;

    public OnlineExamHistory(String title, int questions, int minutes, int colorResId) {
        this.title = title;
        this.questions = questions;
        this.minutes = minutes;
        this.colorResId = colorResId;
    }

    public String getTitle() { return title; }
    public int getQuestions() { return questions; }
    public int getMinutes() { return minutes; }
    public int getColorResId() { return colorResId; }
}
