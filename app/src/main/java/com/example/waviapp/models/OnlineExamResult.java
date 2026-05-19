package com.example.waviapp.models;

import com.google.firebase.Timestamp;

public class OnlineExamResult {
    private String userId;
    private String displayName;
    private String examId;
    private int score;         // số câu đúng
    private int totalQuestions;
    private int durationSeconds; // thời gian làm bài (giây)
    private Timestamp submittedAt;
    private int rank;           // thứ hạng (tính sau khi query)

    public OnlineExamResult() {}

    public OnlineExamResult(String userId, String displayName, String examId,
                             int score, int totalQuestions, int durationSeconds,
                             Timestamp submittedAt) {
        this.userId = userId;
        this.displayName = displayName;
        this.examId = examId;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.durationSeconds = durationSeconds;
        this.submittedAt = submittedAt;
    }

    // Tính phần trăm điểm
    public double getScorePercent() {
        if (totalQuestions == 0) return 0;
        return (score * 100.0) / totalQuestions;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getExamId() { return examId; }
    public void setExamId(String examId) { this.examId = examId; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public int getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(int durationSeconds) { this.durationSeconds = durationSeconds; }

    public Timestamp getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Timestamp submittedAt) { this.submittedAt = submittedAt; }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }
}
