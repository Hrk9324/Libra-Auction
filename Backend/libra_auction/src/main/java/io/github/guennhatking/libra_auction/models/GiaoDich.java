package io.github.guennhatking.libra_auction.models;

import io.github.guennhatking.libra_auction.enums.Enums;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

import java.time.LocalDateTime;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class GiaoDich {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    private Enums.LoaiGiaoDich loaiGiaoDich;

    private long soTien;
    private LocalDateTime ngayTao;

    // CONSTRUCTOR
    protected GiaoDich() {
    }

    public GiaoDich(Enums.LoaiGiaoDich loaiGiaoDich, long soTien) {
        this.loaiGiaoDich = loaiGiaoDich;
        this.soTien = soTien;
        this.ngayTao = LocalDateTime.now();
    }

    // GETTER
    public String getId() {
        return id;
    }

    public Enums.LoaiGiaoDich getLoaiGiaoDich() {
        return loaiGiaoDich;
    }

    public long getSoTien() {
        return soTien;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    // SETTER
    public void setId(String id) {
        this.id = id;
    }

    public void setLoaiGiaoDich(Enums.LoaiGiaoDich loaiGiaoDich) {
        this.loaiGiaoDich = loaiGiaoDich;
    }

    public void setSoTien(long soTien) {
        this.soTien = soTien;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }
}