package fpoly.haideptrai.duan1.api;

import fpoly.haideptrai.duan1.api.models.UserInfo;
import fpoly.haideptrai.duan1.database.entities.NhanVien;

public class ApiHelper {
    /**
     * Convert UserInfo từ API thành NhanVien entity để tương thích với code hiện tại
     */
    public static NhanVien convertToNhanVien(UserInfo user) {
        NhanVien nhanVien = new NhanVien();
        nhanVien.hoTen = user.getFullName() != null ? user.getFullName() : "";
        nhanVien.gioiTinh = "";
        nhanVien.ngaySinh = "";
        nhanVien.soDienThoai = user.getPhone() != null ? user.getPhone() : "";
        nhanVien.tenDangNhap = user.getUsername();
        nhanVien.vaiTro = user.getRole() != null ? user.getRole() : "nhan_vien";
        nhanVien.anhDaiDien = user.getAvatar() != null ? user.getAvatar() : "";
        return nhanVien;
    }
}

