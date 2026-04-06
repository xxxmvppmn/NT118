package com.example.waviapp.models;

public class BaiKiemTra {
    private String maBKT;
    private String maCD;
    private String tenBKT;
    private String loaiKiemTra; // fulltest, minitest, speaking
    private int tongSoCau;
    private int thoiGianLamBai;
    private boolean isLocked; // Từ requirement UI

    public BaiKiemTra() {}

    public BaiKiemTra(String maBKT, String maCD, String tenBKT, String loaiKiemTra, int tongSoCau, int thoiGianLamBai, boolean isLocked) {
        this.maBKT = maBKT;
        this.maCD = maCD;
        this.tenBKT = tenBKT;
        this.loaiKiemTra = loaiKiemTra;
        this.tongSoCau = tongSoCau;
        this.thoiGianLamBai = thoiGianLamBai;
        this.isLocked = isLocked;
    }

    public String getMaBKT() { return maBKT; }
    public void setMaBKT(String maBKT) { this.maBKT = maBKT; }
    public String getMaCD() { return maCD; }
    public void setMaCD(String maCD) { this.maCD = maCD; }
    public String getTenBKT() { return tenBKT; }
    public void setTenBKT(String tenBKT) { this.tenBKT = tenBKT; }
    public String getLoaiKiemTra() { return loaiKiemTra; }
    public void setLoaiKiemTra(String loaiKiemTra) { this.loaiKiemTra = loaiKiemTra; }
    public int getTongSoCau() { return tongSoCau; }
    public void setTongSoCau(int tongSoCau) { this.tongSoCau = tongSoCau; }
    public int getThoiGianLamBai() { return thoiGianLamBai; }
    public void setThoiGianLamBai(int thoiGianLamBai) { this.thoiGianLamBai = thoiGianLamBai; }
    public boolean isLocked() { return isLocked; }
    public void setLocked(boolean locked) { isLocked = locked; }
}
