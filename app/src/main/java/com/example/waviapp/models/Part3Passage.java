package com.example.waviapp.models;

import java.util.List;

public class Part3Passage {
    private int passageId;
    private String audioName;
    private String transcript;
    private List<Part3Question> questions;

    public Part3Passage() {}

    public int getPassageId() { return passageId; }
    public String getAudioName() { return audioName; }
    public String getTranscript() { return transcript; }
    public List<Part3Question> getQuestions() { return questions; }
}
