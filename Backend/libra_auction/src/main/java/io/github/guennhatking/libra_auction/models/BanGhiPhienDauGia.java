package io.github.guennhatking.libra_auction.models;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class BanGhiPhienDauGia {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    private PhienDauGia phienDauGia;

    @ManyToOne
    private NguoiDung nguoiDatGia;

    private LocalDateTime thoiGian;
    private long mucGia;

    // CONSTRUCTOR
    public BanGhiPhienDauGia() {
    }

    public BanGhiPhienDauGia(PhienDauGia phienDauGia, NguoiDung nguoiDatGia, long mucGia) {
        this.phienDauGia = phienDauGia;
        this.nguoiDatGia = nguoiDatGia;
        this.mucGia = mucGia;
        this.thoiGian = LocalDateTime.now();
    }

    // GETTER
    public String getId() {
        return id;
    }

    public PhienDauGia getPhienDauGia() {
        return phienDauGia;
    }

    public NguoiDung getNguoiDatGia() {
        return nguoiDatGia;
    }

    public LocalDateTime getThoiGian() {
        return thoiGian;
    }

    public long getMucGia() {
        return mucGia;
    }

    // SETTER
    public void setId(String id) {
        this.id = id;
    }

    public void setPhienDauGia(PhienDauGia phienDauGia) {
        this.phienDauGia = phienDauGia;
    }

    public void setNguoiDatGia(NguoiDung nguoiDatGia) {
        this.nguoiDatGia = nguoiDatGia;
    }

    public void setThoiGian(LocalDateTime thoiGian) {
        this.thoiGian = thoiGian;
    }

    public void setMucGia(long mucGia) {
        this.mucGia = mucGia;
    }
}