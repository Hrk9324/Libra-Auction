package io.github.guennhatking.libra_auction.models;

import io.github.guennhatking.libra_auction.enums.Enums;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class NguoiDung {
    protected List<TaiKhoan> danhSachPhuongThucDangNhap;
    protected TaiKhoan taiKhoanHienTai;
    protected String hoVaTen;
    protected String soDienThoai;
    protected String CCCD;
    protected String anhDaiDien;
    protected Enums.TrangThaiEmail trangThaiEmail;
    protected Enums.TrangThaiTaiKhoan trangThaiTaiKhoan;
    protected LocalDateTime thoiGianTao;

    public NguoiDung(String hoVaTen, String soDienThoai, String CCCD) {
        if (hoVaTen == null || hoVaTen.isBlank()) {
            throw new IllegalArgumentException("Họ và tên không được để trống.");
        }
        if (soDienThoai == null || soDienThoai.isBlank()) {
            throw new IllegalArgumentException("Số điện thoại không được để trống.");
        }
        if (CCCD == null || CCCD.isBlank()) {
            throw new IllegalArgumentException("CCCD không được để trống.");
        }

        this.hoVaTen = hoVaTen;
        this.soDienThoai = soDienThoai;
        this.CCCD = CCCD;
        this.trangThaiEmail = Enums.TrangThaiEmail.CHO_XAC_THUC;
        this.trangThaiTaiKhoan = Enums.TrangThaiTaiKhoan.CHO_XAC_NHAN;
        this.thoiGianTao = LocalDateTime.now();
    }

    public String getHoVaTen() {
        return hoVaTen;
    }

    public void setHoVaTen(String hoVaTen) {
        this.hoVaTen = hoVaTen;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getCCCD() {
        return CCCD;
    }

    public void setCCCD(String CCCD) {
        this.CCCD = CCCD;
    }

    public String getAnhDaiDien() {
        return anhDaiDien;
    }

    public void setAnhDaiDien(String anhDaiDien) {
        this.anhDaiDien = anhDaiDien;
    }

    public Enums.TrangThaiTaiKhoan getTrangThaiTaiKhoan() {
        return trangThaiTaiKhoan;
    }

    public void setTrangThaiTaiKhoan(Enums.TrangThaiTaiKhoan trangThaiTaiKhoan) {
        this.trangThaiTaiKhoan = trangThaiTaiKhoan;
    }

    public Enums.TrangThaiEmail getTrangThaiEmail() {
        return trangThaiEmail;
    }

    public void setTrangThaiEmail(Enums.TrangThaiEmail trangThaiEmail) {
        this.trangThaiEmail = trangThaiEmail;
    }

    public void datTrangThaiXacThucEmail(Enums.TrangThaiEmail trangThaiEmail) {
        this.trangThaiEmail = trangThaiEmail;
    }

    public void datTrangThaiTaiKhoan(Enums.TrangThaiTaiKhoan trangThaiTaiKhoan) {
        this.trangThaiTaiKhoan = trangThaiTaiKhoan;
    }

    public TaiKhoan getTaiKhoanHienTai() {
        return taiKhoanHienTai;
    }

    public void setTaiKhoanHienTai(TaiKhoan taiKhoanHienTai) {
        this.taiKhoanHienTai = taiKhoanHienTai;
    }

    // cập nhật mật khẩu mới cho người dùng
    public void capNhatMatKhau(String matKhauMoiHash, byte[] salt) {
        for (TaiKhoan taiKhoan : danhSachPhuongThucDangNhap) {
            if (taiKhoan instanceof TaiKhoanPassword taiKhoanPassword) {
                taiKhoanPassword.doiMatKhau(matKhauMoiHash, salt);
                return;
            }
        }
        throw new IllegalStateException("Người dùng không có phương thức đăng nhập bằng mật khẩu.");
    }
}
