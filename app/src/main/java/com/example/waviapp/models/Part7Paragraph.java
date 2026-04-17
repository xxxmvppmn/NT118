package com.example.waviapp.models;

import java.io.Serializable;
import java.util.List;

public class Part7Paragraph implements Serializable {
    private int id;
    private String type;
    private String content;
    private List<Part7Question> questions;

    public Part7Paragraph() {}

    public int getId() { return id; }
    public String getType() { return type; }
    public String getContent() { return content; }
    public List<Part7Question> getQuestions() { return questions; }
}
