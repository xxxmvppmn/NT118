package com.example.waviapp.models;

public class ChuDe {
    private String maCD;
    private String tenCD;
    private String loaiChuDe;
    private String moTa;
    private String hinhAnh;
    private long ngayTao;

    public ChuDe() {}

    public String getMaCD() { return maCD; }
    public void setMaCD(String maCD) { this.maCD = maCD; }
    public String getTenCD() { return tenCD; }
    public void setTenCD(String tenCD) { this.tenCD = tenCD; }
    public String getLoaiChuDe() { return loaiChuDe; }
    public void setLoaiChuDe(String loaiChuDe) { this.loaiChuDe = loaiChuDe; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }
    public long getNgayTao() { return ngayTao; }
    public void setNgayTao(long ngayTao) { this.ngayTao = ngayTao; }
}
