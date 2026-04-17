package com.example.waviapp.models;

import java.io.Serializable;
import java.util.List;

public class Part6Paragraph implements Serializable {
    private int id;
    private String type;
    private String content;
    private List<Part6Question> questions;

    public Part6Paragraph() {}

    public int getId() { return id; }
    public String getType() { return type; }
    public String getContent() { return content; }
    public List<Part6Question> getQuestions() { return questions; }
}
