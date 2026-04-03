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
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class NguoiDung {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "nguoiDung")
    protected List<TaiKhoan> taiKhoanLienKet;

    protected String hoVaTen;
    protected String soDienThoai;
    protected String cccd;
    protected String anhDaiDien;
    protected String email;

    @ManyToMany
    private List<Role> roles;

    @Enumerated(EnumType.STRING)
    protected Enums.TrangThaiEmail trangThaiEmail;

    @Enumerated(EnumType.STRING)
    protected Enums.TrangThaiTaiKhoan trangThaiTaiKhoan;

    protected LocalDateTime thoiGianTao;

    // CONSTRUCTOR
    public NguoiDung() {
    }

    public NguoiDung(String hoVaTen, String email) {
        this.hoVaTen = hoVaTen;
        this.email = email;
        this.thoiGianTao = LocalDateTime.now();
    }

    // GETTER
    public String getId() {
        return id;
    }

    public List<TaiKhoan> getTaiKhoanLienKet() {
        return taiKhoanLienKet;
    }

    public String getHoVaTen() {
        return hoVaTen;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public String getCccd() {
        return cccd;
    }

    public String getAnhDaiDien() {
        return anhDaiDien;
    }

    public String getEmail() {
        return email;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public Enums.TrangThaiEmail getTrangThaiEmail() {
        return trangThaiEmail;
    }

    public Enums.TrangThaiTaiKhoan getTrangThaiTaiKhoan() {
        return trangThaiTaiKhoan;
    }

    public LocalDateTime getThoiGianTao() {
        return thoiGianTao;
    }

    // SETTER
    public void setId(String id) {
        this.id = id;
    }

    public void setTaiKhoanLienKet(List<TaiKhoan> taiKhoanLienKet) {
        this.taiKhoanLienKet = taiKhoanLienKet;
    }

    public void setHoVaTen(String hoVaTen) {
        this.hoVaTen = hoVaTen;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public void setAnhDaiDien(String anhDaiDien) {
        this.anhDaiDien = anhDaiDien;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public void setTrangThaiEmail(Enums.TrangThaiEmail trangThaiEmail) {
        this.trangThaiEmail = trangThaiEmail;
    }

    public void setTrangThaiTaiKhoan(Enums.TrangThaiTaiKhoan trangThaiTaiKhoan) {
        this.trangThaiTaiKhoan = trangThaiTaiKhoan;
    }

    public void setThoiGianTao(LocalDateTime thoiGianTao) {
        this.thoiGianTao = thoiGianTao;
    }
}
