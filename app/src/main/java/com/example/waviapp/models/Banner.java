package com.example.waviapp.models;

public class Banner {
    private String title;
    private String description;
    private int imageRes;
    private String actionType; // e.g., "FACEBOOK", "PREMIUM", "ONLINE_EXAM"

    public Banner(String title, String description, int imageRes, String actionType) {
        this.title = title;
        this.description = description;
        this.imageRes = imageRes;
        this.actionType = actionType;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getImageRes() { return imageRes; }
    public String getActionType() { return actionType; }
}
