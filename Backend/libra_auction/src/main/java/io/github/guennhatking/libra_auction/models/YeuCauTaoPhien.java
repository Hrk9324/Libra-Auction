package io.github.guennhatking.libra_auction.models;

import io.github.guennhatking.libra_auction.enums.Enums;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class YeuCauTaoPhien {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    private PhienDauGia phienDauGia;

    @Enumerated(EnumType.STRING)
    private Enums.TrangThaiYeuCau trangThai;

    private String liDoTuChoi;

    // CONSTRUCTOR
    public YeuCauTaoPhien() {
    }

    public YeuCauTaoPhien(PhienDauGia phienDauGia) {
        this.phienDauGia = phienDauGia;
        this.trangThai = Enums.TrangThaiYeuCau.KHOI_TAO;
    }

    // GETTER
    public String getId() {
        return id;
    }

    public PhienDauGia getPhienDauGia() {
        return phienDauGia;
    }

    public Enums.TrangThaiYeuCau getTrangThai() {
        return trangThai;
    }

    public String getLiDoTuChoi() {
        return liDoTuChoi;
    }

    // SETTER
    public void setId(String id) {
        this.id = id;
    }

    public void setPhienDauGia(PhienDauGia phienDauGia) {
        this.phienDauGia = phienDauGia;
    }

    public void setTrangThai(Enums.TrangThaiYeuCau trangThai) {
        this.trangThai = trangThai;
    }

    public void setLiDoTuChoi(String liDoTuChoi) {
        this.liDoTuChoi = liDoTuChoi;
    }
}
