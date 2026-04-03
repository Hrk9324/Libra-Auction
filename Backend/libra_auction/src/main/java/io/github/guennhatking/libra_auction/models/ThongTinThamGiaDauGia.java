package io.github.guennhatking.libra_auction.models;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class ThongTinThamGiaDauGia {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    private NguoiDung nguoiThamGia;

    @ManyToOne
    private PhienDauGia phienDauGia;

    private LocalDateTime thoiGianDangKy;

    // CONSTRUCTOR
    public ThongTinThamGiaDauGia() {
    }

    public ThongTinThamGiaDauGia(NguoiDung nguoiThamGia, PhienDauGia phienDauGia) {
        this.nguoiThamGia = nguoiThamGia;
        this.phienDauGia = phienDauGia;
        this.thoiGianDangKy = LocalDateTime.now();
    }

    // GETTER
    public String getId() {
        return id;
    }

    public NguoiDung getNguoiThamGia() {
        return nguoiThamGia;
    }

    public PhienDauGia getPhienDauGia() {
        return phienDauGia;
    }

    public LocalDateTime getThoiGianDangKy() {
        return thoiGianDangKy;
    }

    // SETTER
    public void setId(String id) {
        this.id = id;
    }

    public void setNguoiThamGia(NguoiDung nguoiThamGia) {
        this.nguoiThamGia = nguoiThamGia;
    }

    public void setPhienDauGia(PhienDauGia phienDauGia) {
        this.phienDauGia = phienDauGia;
    }

    public void setThoiGianDangKy(LocalDateTime thoiGianDangKy) {
        this.thoiGianDangKy = thoiGianDangKy;
    }
}
