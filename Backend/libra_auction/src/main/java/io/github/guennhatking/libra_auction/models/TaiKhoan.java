package io.github.guennhatking.libra_auction.models;

import io.github.guennhatking.libra_auction.enums.Enums;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToOne;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class TaiKhoan {
    @Id
    protected String id;

    protected String username;

    @Enumerated(EnumType.STRING)
    protected Enums.TrangThaiTaiKhoan trangThai;

    @ManyToOne
    protected NguoiDung nguoiDung;

    // CONSTRUCTOR
    protected TaiKhoan() {
    }

    public TaiKhoan(String id, String username, Enums.TrangThaiTaiKhoan trangThai) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID không được để trống.");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username không được để trống.");
        }
        this.id = id;
        this.username = username;
        this.trangThai = trangThai != null ? trangThai : Enums.TrangThaiTaiKhoan.CHO_XAC_NHAN;
    }

    // GETTER
    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Enums.TrangThaiTaiKhoan getTrangThai() {
        return trangThai;
    }

    public NguoiDung getNguoiDung() {
        return nguoiDung;
    }

    // SETTER
    public void setId(String id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setTrangThai(Enums.TrangThaiTaiKhoan trangThai) {
        this.trangThai = trangThai;
    }

    public void setNguoiDung(NguoiDung nguoiDung) {
        this.nguoiDung = nguoiDung;
    }
}