package com.example.waviapp;

public class Word {
    private String text;      // Ví dụ: "New"
    private String phonetic;  // Ví dụ: "/njuː/"
    private String meaning;   // Ví dụ: "Mới"
    private boolean isFavorite;

    // Hàm khởi tạo (Constructor)
    public Word(String text, String phonetic, String meaning) {
        this.text = text;
        this.phonetic = phonetic;
        this.meaning = meaning;
        this.isFavorite = false;
    }

    // Các hàm để lấy dữ liệu (Getters)
    public String getText() { return text; }
    public String getPhonetic() { return phonetic; }
    public String getMeaning() { return meaning; }
    public boolean isFavorite() { return isFavorite; }

    // Hàm để thay đổi trạng thái trái tim
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
}