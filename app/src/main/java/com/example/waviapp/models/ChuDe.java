package com.example.waviapp.models;

import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.Timestamp;

public class ChuDe {
    private String maCD;
    private String tenCD;
    private String loaiChuDe;
    private String moTa;
    private String hinhAnh;

    @ServerTimestamp
    private Timestamp ngayTao;

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
    public Timestamp getNgayTao() { return ngayTao; }
    public void setNgayTao(Timestamp ngayTao) { this.ngayTao = ngayTao; }
}
