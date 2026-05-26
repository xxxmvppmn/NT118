package com.example.waviapp.models;

import com.google.firebase.Timestamp;

public class ChatMessage {
    private String noiDung;
    private String nguoiGui; // 'user' hoặc 'bot'
    private Timestamp thoiGian;

    // Constructor không tham số bắt buộc để Firestore mapping dữ liệu
    public ChatMessage() {
    }

    // Constructor có tham số
    public ChatMessage(String noiDung, String nguoiGui, Timestamp thoiGian) {
        this.noiDung = noiDung;
        this.nguoiGui = nguoiGui;
        this.thoiGian = thoiGian;
    }

    // Getter và Setter
    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public String getNguoiGui() {
        return nguoiGui;
    }

    public void setNguoiGui(String nguoiGui) {
        this.nguoiGui = nguoiGui;
    }

    public Timestamp getThoiGian() {
        return thoiGian;
    }

    public void setThoiGian(Timestamp thoiGian) {
        this.thoiGian = thoiGian;
    }
}
