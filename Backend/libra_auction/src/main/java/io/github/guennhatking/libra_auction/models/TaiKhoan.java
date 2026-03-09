package io.github.guennhatking.libra_auction.models;

import io.github.guennhatking.libra_auction.enums.Enums;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

// abstract class
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class TaiKhoan {
    @Id
    protected String id;
    protected String username;
    protected Enums.TrangThaiTaiKhoan trangThai;

    public TaiKhoan(String id, Enums.TrangThaiTaiKhoan trangThai, String username) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID không được để trống.");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username không được để trống.");
        }
        this.id = id;
        this.username = username;
        if (trangThai == null) {
            this.trangThai = Enums.TrangThaiTaiKhoan.CHO_XAC_NHAN;
        }
        else {
            this.trangThai = trangThai;
        }
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }
}