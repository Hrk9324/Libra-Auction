package io.github.guennhatking.libra_auction.models;

import java.util.ArrayList;
import java.util.List;

import io.github.guennhatking.libra_auction.enums.Enums;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;

@Entity
public class QuanTriVien {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "tai_khoan_id", referencedColumnName = "id")
    private TaiKhoanPassword taiKhoan;

    @Transient
    private List<NguoiDung> danhSachNguoiDung;

    @Transient
    private List<PhienDauGia> danhSachPhienDauGia;

    protected QuanTriVien() {
        // Constructor mặc định cho JPA
        this.danhSachNguoiDung = new ArrayList<>();
        this.danhSachPhienDauGia = new ArrayList<>();
    }

    public QuanTriVien(TaiKhoanPassword taiKhoan) {
        this.taiKhoan = taiKhoan;
        this.danhSachNguoiDung = new ArrayList<>();
        this.danhSachPhienDauGia = new ArrayList<>();
    }

    public void duyetNguoiDung(NguoiDung nguoiDung) {
        // Logic to approve user
    }

    public void duyetPhienDauGia(PhienDauGia phienDauGia) {
        // Logic to manage auction session
    }

    public void khoaTaiKhoan(NguoiDung nguoiDung) {
        // Logic to lock account

    }

    public void moKhoaTaiKhoan(NguoiDung nguoiDung) {
        // Logic to unlock account

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<NguoiDung> getDanhSachNguoiDung() {
        return danhSachNguoiDung;
    }

    public List<PhienDauGia> getDanhSachPhienDauGia() {
        return danhSachPhienDauGia;
    }

    public TaiKhoanPassword getTaiKhoan() {
        return this.taiKhoan;
    }

    public TaiKhoanPassword setTaiKhoan(TaiKhoanPassword taiKhoan) {
        this.taiKhoan = taiKhoan;
        return this.taiKhoan;
    }

}