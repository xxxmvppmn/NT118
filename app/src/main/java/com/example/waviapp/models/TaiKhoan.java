package com.example.waviapp.models;

import com.google.firebase.Timestamp;

public class TaiKhoan {
    private String id;
    private String hoTen;
    private String email;
    private String sdt;
    private String avatar;
    private String vaiTro;
    private Timestamp ngayTao;
    private Timestamp ngaySinh;
    private String dob;
    private String gender;
    private int capDo;
    private int tongXP;
    private int chuoiNgayHoc;
    private int mucTieuHangNgay;
    private Timestamp lastLogin;
    private boolean premium;
    private String avatarUrl;
    private String phone;
    // 1. Constructor rỗng
    public TaiKhoan() {
    }

    // 2. Constructor 3 tham số dùng khi Đăng ký mới
    public TaiKhoan(String id, String hoTen, String email) {
        this.id = id;
        this.hoTen = hoTen;
        this.email = email;
        this.sdt = "";
        this.avatar = "";
        this.vaiTro = "User";
        this.lastLogin = Timestamp.now();
        this.ngayTao = Timestamp.now();
        this.ngaySinh = Timestamp.now();
        this.dob = "";
        this.gender = "";
        this.capDo = 1;
        this.tongXP = 0;
        this.chuoiNgayHoc = 1;
        this.mucTieuHangNgay = 0;
        this.premium = false;
    }

    // --- GETTER VÀ SETTER ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public Timestamp getNgayTao() { return ngayTao; }
    public void setNgayTao(Timestamp ngayTao) { this.ngayTao = ngayTao; }

    public Timestamp getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(Timestamp ngaySinh) { this.ngaySinh = ngaySinh; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public boolean isPremium() { return premium; }
    public void setPremium(boolean premium) { this.premium = premium; }

    public int getTongXP() { return tongXP; }
    public void setTongXP(int tongXP) { this.tongXP = tongXP; }

    public int getChuoiNgayHoc() { return chuoiNgayHoc; }
    public void setChuoiNgayHoc(int chuoiNgayHoc) { this.chuoiNgayHoc = chuoiNgayHoc; }

    public int getCapDo() { return capDo; }
    public void setCapDo(int capDo) { this.capDo = capDo; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getVaiTro() { return vaiTro; }
    public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }

    public int getMucTieuHangNgay() { return mucTieuHangNgay; }
    public void setMucTieuHangNgay(int mucTieuHangNgay) { this.mucTieuHangNgay = mucTieuHangNgay; }
    public Timestamp getLastLogin() { return lastLogin; }
    public void setLastLogin(Timestamp lastLogin) { this.lastLogin = lastLogin; }
    public String getAvatarUrl() {
        return avatarUrl;
    }
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
}
