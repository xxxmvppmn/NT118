package com.example.waviapp;

public class Word {
    private String level;
    private String maCD;
    private String maTV;
    private String tuTiengAnh;
    private String phienAm;
    private String nghiaTiengViet;
    private String loaiTu;
    private String cauViDu;
    private String hinhAnh;
    private String audio;
    private boolean favorite;

    // Constructor không đối số
    public Word() {}

    // Getters và Setters (Để GSON có thể ghi dữ liệu vào các biến)
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getMaCD() { return maCD; }
    public void setMaCD(String maCD) { this.maCD = maCD; }

    public String getMaTV() { return maTV; }
    public void setMaTV(String maTV) { this.maTV = maTV; }

    public String getTuTiengAnh() { return tuTiengAnh; }
    public void setTuTiengAnh(String tuTiengAnh) { this.tuTiengAnh = tuTiengAnh; }

    public String getPhienAm() { return phienAm; }
    public void setPhienAm(String phienAm) { this.phienAm = phienAm; }

    public String getNghiaTiengViet() { return nghiaTiengViet; }
    public void setNghiaTiengViet(String nghiaTiengViet) { this.nghiaTiengViet = nghiaTiengViet; }

    public String getLoaiTu() { return loaiTu; }
    public void setLoaiTu(String loaiTu) { this.loaiTu = loaiTu; }

    public String getCauViDu() { return cauViDu; }
    public void setCauViDu(String cauViDu) { this.cauViDu = cauViDu; }

    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }

    public String getAudio() { return audio; }
    public void setAudio(String audio) { this.audio = audio; }

    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }
}