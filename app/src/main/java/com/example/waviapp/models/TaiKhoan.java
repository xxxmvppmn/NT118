package com.example.waviapp.models;

public class TaiKhoan {
    private String id;
    private String hoTen;
    private String email;
    private String avatar;
    private String vaiTro;
    private String ngayTao;
    private int capDo;
    private int tongXP;
    private int chuoiNgayHoc;
    private int mucTieuHangNgay;
    private boolean isPremium;

    // Fields cho UI cũ để không vỡ app (phone, dob, gender)
    private String phone;
    private String dob;
    private String gender;

    public TaiKhoan() {
    }

    public TaiKhoan(String id, String hoTen, String email) {
        this.id = id;
        this.hoTen = hoTen;
        this.email = email;
        this.avatar = "";
        this.vaiTro = "User";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault());
        this.ngayTao = sdf.format(new java.util.Date());
        this.capDo = 1;
        this.tongXP = 0;
        this.chuoiNgayHoc = 0;
        this.mucTieuHangNgay = 20;
        this.isPremium = false; // Mặc định là tài khoản thường
        this.phone = "";
        this.dob = "";
        this.gender = "";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getVaiTro() { return vaiTro; }
    public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }
    public String getNgayTao() { return ngayTao; }
    public void setNgayTao(String ngayTao) { this.ngayTao = ngayTao; }
    public int getCapDo() { return capDo; }
    public void setCapDo(int capDo) { this.capDo = capDo; }
    public int getTongXP() { return tongXP; }
    public void setTongXP(int tongXP) { this.tongXP = tongXP; }
    public int getChuoiNgayHoc() { return chuoiNgayHoc; }
    public void setChuoiNgayHoc(int chuoiNgayHoc) { this.chuoiNgayHoc = chuoiNgayHoc; }
    public int getMucTieuHangNgay() { return mucTieuHangNgay; }
    public void setMucTieuHangNgay(int mucTieuHangNgay) { this.mucTieuHangNgay = mucTieuHangNgay; }
    public boolean isPremium() { return isPremium; }
    public void setPremium(boolean premium) { isPremium = premium; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
}
