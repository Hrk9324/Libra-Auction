package io.github.guennhatking.libra_auction.models;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class BanGhiBoCuocDauGia {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    private NguoiDung nguoiBoCuoc;

    @ManyToOne
    private PhienDauGia phienDauGia;

    private LocalDateTime thoiGian;
    private String lyDo;

    // CONSTRUCTOR
    public BanGhiBoCuocDauGia() {
    }

    public BanGhiBoCuocDauGia(NguoiDung nguoiBoCuoc, PhienDauGia phienDauGia, String lyDo) {
        this.nguoiBoCuoc = nguoiBoCuoc;
        this.phienDauGia = phienDauGia;
        this.thoiGian = LocalDateTime.now();
        this.lyDo = lyDo;
    }

    // GETTER
    public String getId() {
        return id;
    }

    public NguoiDung getNguoiBoCuoc() {
        return nguoiBoCuoc;
    }

    public PhienDauGia getPhienDauGia() {
        return phienDauGia;
    }

    public LocalDateTime getThoiGian() {
        return thoiGian;
    }

    public String getLyDo() {
        return lyDo;
    }

    // SETTER
    public void setId(String id) {
        this.id = id;
    }

    public void setNguoiBoCuoc(NguoiDung nguoiBoCuoc) {
        this.nguoiBoCuoc = nguoiBoCuoc;
    }

    public void setPhienDauGia(PhienDauGia phienDauGia) {
        this.phienDauGia = phienDauGia;
    }

    public void setThoiGian(LocalDateTime thoiGian) {
        this.thoiGian = thoiGian;
    }

    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
    }
}
