package com.example.waviapp.models;

import java.io.Serializable;

public class TuVung implements Serializable {
    private String maTV;
    private String maCD;
    private String tuTiengAnh;
    private String nghiaTiengViet;
    private String loaiTu;
    private String phienAm;
    private String audio;
    private String hinhAnh;
    private String cauViDu;
    private boolean isFavorite; // Cho tính năng đánh dấu
    private String level;

    public TuVung() {}

    public TuVung(String maTV, String maCD, String tuTiengAnh, String nghiaTiengViet, String loaiTu, String phienAm, String level) {
        this.maTV = maTV;
        this.maCD = maCD;
        this.tuTiengAnh = tuTiengAnh;
        this.nghiaTiengViet = nghiaTiengViet;
        this.loaiTu = loaiTu;
        this.phienAm = phienAm;
        this.isFavorite = false;
        this.audio = "";
        this.hinhAnh = "";
        this.cauViDu = "";
        this.level = level;
    }

    public String getMaTV() { return maTV; }
    public void setMaTV(String maTV) { this.maTV = maTV; }
    public String getMaCD() { return maCD; }
    public void setMaCD(String maCD) { this.maCD = maCD; }
    public String getTuTiengAnh() { return tuTiengAnh; }
    public void setTuTiengAnh(String tuTiengAnh) { this.tuTiengAnh = tuTiengAnh; }
    public String getNghiaTiengViet() { return nghiaTiengViet; }
    public void setNghiaTiengViet(String nghiaTiengViet) { this.nghiaTiengViet = nghiaTiengViet; }
    public String getLoaiTu() { return loaiTu; }
    public void setLoaiTu(String loaiTu) { this.loaiTu = loaiTu; }
    public String getPhienAm() { return phienAm; }
    public void setPhienAm(String phienAm) { this.phienAm = phienAm; }
    public String getAudio() { return audio; }
    public void setAudio(String audio) { this.audio = audio; }
    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }
    public String getCauViDu() { return cauViDu; }
    public void setCauViDu(String cauViDu) { this.cauViDu = cauViDu; }
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
}
