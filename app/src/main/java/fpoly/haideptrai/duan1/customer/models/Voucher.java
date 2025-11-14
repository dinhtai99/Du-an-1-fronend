package fpoly.haideptrai.duan1.customer.models;

public class Voucher {
    private String ngay;
    private String mucGiamGia;
    private String dieuKien;
    private int soLuong;
    private String trangThai;

    public Voucher(String ngay, String mucGiamGia, String dieuKien, int soLuong, String trangThai) {
        this.ngay = ngay;
        this.mucGiamGia = mucGiamGia;
        this.dieuKien = dieuKien;
        this.soLuong = soLuong;
        this.trangThai = trangThai;
    }

    public String getNgay() {
        return ngay;
    }

    public void setNgay(String ngay) {
        this.ngay = ngay;
    }

    public String getMucGiamGia() {
        return mucGiamGia;
    }

    public void setMucGiamGia(String mucGiamGia) {
        this.mucGiamGia = mucGiamGia;
    }

    public String getDieuKien() {
        return dieuKien;
    }

    public void setDieuKien(String dieuKien) {
        this.dieuKien = dieuKien;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}

