package com.example.waviapp;

public class Grammar {
    private int order;
    private String title;

    public Grammar(int order, String title) {
        this.order = order;
        this.title = title;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
