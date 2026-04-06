package com.example.waviapp.models;

public class NguPhap {
    private String maNP;
    private String maCD;
    private String tenBai;
    private String noiDungLyThuyet;
    private String viDu;
    private int order; // Cho UI sắp xếp

    public NguPhap() {}

    public NguPhap(String maNP, String maCD, String tenBai, String noiDungLyThuyet, String viDu, int order) {
        this.maNP = maNP;
        this.maCD = maCD;
        this.tenBai = tenBai;
        this.noiDungLyThuyet = noiDungLyThuyet;
        this.viDu = viDu;
        this.order = order;
    }

    public String getMaNP() { return maNP; }
    public void setMaNP(String maNP) { this.maNP = maNP; }
    public String getMaCD() { return maCD; }
    public void setMaCD(String maCD) { this.maCD = maCD; }
    public String getTenBai() { return tenBai; }
    public void setTenBai(String tenBai) { this.tenBai = tenBai; }
    public String getNoiDungLyThuyet() { return noiDungLyThuyet; }
    public void setNoiDungLyThuyet(String noiDungLyThuyet) { this.noiDungLyThuyet = noiDungLyThuyet; }
    public String getViDu() { return viDu; }
    public void setViDu(String viDu) { this.viDu = viDu; }
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
}
