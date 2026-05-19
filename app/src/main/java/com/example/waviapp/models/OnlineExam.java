package com.example.waviapp.models;

import com.google.firebase.Timestamp;

public class OnlineExam {
    private String examId;
    private String title;
    private String description;
    private int totalQuestions;
    private int durationMinutes;
    private Timestamp startTime;
    private Timestamp endTime;
    private boolean active;
    private String colorTag;
    private int participantCount;

    public OnlineExam() {}

    public OnlineExam(String examId, String title, String description,
                      int totalQuestions, int durationMinutes,
                      Timestamp startTime, Timestamp endTime,
                      boolean active, String colorTag) {
        this.examId = examId;
        this.title = title;
        this.description = description;
        this.totalQuestions = totalQuestions;
        this.durationMinutes = durationMinutes;
        this.startTime = startTime;
        this.endTime = endTime;
        this.active = active;
        this.colorTag = colorTag;
        this.participantCount = 0;
    }

    public String getExamId() { return examId; }
    public void setExamId(String examId) { this.examId = examId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public Timestamp getStartTime() { return startTime; }
    public void setStartTime(Timestamp startTime) { this.startTime = startTime; }

    public Timestamp getEndTime() { return endTime; }
    public void setEndTime(Timestamp endTime) { this.endTime = endTime; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getColorTag() { return colorTag; }
    public void setColorTag(String colorTag) { this.colorTag = colorTag; }

    public int getParticipantCount() { return participantCount; }
    public void setParticipantCount(int participantCount) { this.participantCount = participantCount; }
}
