package io.github.guennhatking.libra_auction.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class QuanTriVien {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne
    private TaiKhoanPassword taiKhoan;

    // CONSTRUCTOR
    protected QuanTriVien() {
    }

    public QuanTriVien(TaiKhoanPassword taiKhoan) {
        this.taiKhoan = taiKhoan;
    }

    // GETTER
    public String getId() {
        return id;
    }

    public TaiKhoanPassword getTaiKhoan() {
        return taiKhoan;
    }

    // SETTER
    public void setId(String id) {
        this.id = id;
    }

    public void setTaiKhoan(TaiKhoanPassword taiKhoan) {
        this.taiKhoan = taiKhoan;
    }
}