package com.example.waviapp.models;

public class Part1Model {
    private int id;
    private String audioName;
    private String imageName;
    private String correctAnswer;
    private String script;
    private String explanation;

    public Part1Model() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getAudioName() { return audioName; }
    public void setAudioName(String audioName) { this.audioName = audioName; }

    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public String getScript() { return script; }
    public void setScript(String script) { this.script = script; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}
