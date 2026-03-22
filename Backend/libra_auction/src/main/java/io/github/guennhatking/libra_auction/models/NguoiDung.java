package io.github.guennhatking.libra_auction.models;

import java.time.LocalDateTime;
import java.util.List;

import io.github.guennhatking.libra_auction.enums.Enums;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import jakarta.persistence.InheritanceType;


@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class NguoiDung {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String Id;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "nguoiDung")
    protected List<TaiKhoan> taiKhoanLienKet;

    protected String hoVaTen;
    protected String soDienThoai;
    protected String CCCD;
    protected String anhDaiDien;

    @Enumerated(EnumType.STRING)
    protected Enums.TrangThaiEmail trangThaiEmail;

    @Enumerated(EnumType.STRING)
    protected Enums.TrangThaiTaiKhoan trangThaiTaiKhoan;

    protected LocalDateTime thoiGianTao;

    protected NguoiDung() {
        // Constructor mặc định cho JPA
    }

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

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        this.Id = id;
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

    // cập nhật mật khẩu mới cho người dùng
    public void capNhatMatKhau(String matKhauMoiHash, byte[] salt) {
        for (TaiKhoan taiKhoan : taiKhoanLienKet) {
            if (taiKhoan instanceof TaiKhoanPassword taiKhoanPassword) {
                taiKhoanPassword.doiMatKhau(matKhauMoiHash, salt);
                return;
            }
        }
        throw new IllegalStateException("Người dùng không có phương thức đăng nhập bằng mật khẩu.");
    }
}
