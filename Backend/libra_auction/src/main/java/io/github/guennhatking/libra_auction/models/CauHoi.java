package io.github.guennhatking.libra_auction.models;

import java.time.LocalDateTime;

import io.github.guennhatking.libra_auction.enums.Enums;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class CauHoi {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    private PhienDauGia phienDauGia;

    @ManyToOne
    private NguoiDung nguoiHoi;

    @ManyToOne
    private NguoiDung nguoiTraLoi;

    private String noiDungHoi;
    private String noiDungTraLoi;
    private LocalDateTime thoiGianHoi;
    private LocalDateTime thoiGianTraLoi;

    @Enumerated(EnumType.STRING)
    private Enums.TinhTrangCauHoi tinhTrangCauHoi;

    // CONSTRUCTOR
    public CauHoi() {
    }

    public CauHoi(PhienDauGia phienDauGia, NguoiDung nguoiHoi, String noiDungHoi) {
        this.phienDauGia = phienDauGia;
        this.nguoiHoi = nguoiHoi;
        this.noiDungHoi = noiDungHoi;
        this.thoiGianHoi = LocalDateTime.now();
    }

    // GETTER
    public String getId() {
        return id;
    }

    public PhienDauGia getPhienDauGia() {
        return phienDauGia;
    }

    public NguoiDung getNguoiHoi() {
        return nguoiHoi;
    }

    public NguoiDung getNguoiTraLoi() {
        return nguoiTraLoi;
    }

    public String getNoiDungHoi() {
        return noiDungHoi;
    }

    public String getNoiDungTraLoi() {
        return noiDungTraLoi;
    }

    public LocalDateTime getThoiGianHoi() {
        return thoiGianHoi;
    }

    public LocalDateTime getThoiGianTraLoi() {
        return thoiGianTraLoi;
    }

    public Enums.TinhTrangCauHoi getTinhTrangCauHoi() {
        return tinhTrangCauHoi;
    }

    // SETTER
    public void setId(String id) {
        this.id = id;
    }

    public void setPhienDauGia(PhienDauGia phienDauGia) {
        this.phienDauGia = phienDauGia;
    }

    public void setNguoiHoi(NguoiDung nguoiHoi) {
        this.nguoiHoi = nguoiHoi;
    }

    public void setNguoiTraLoi(NguoiDung nguoiTraLoi) {
        this.nguoiTraLoi = nguoiTraLoi;
    }

    public void setNoiDungHoi(String noiDungHoi) {
        this.noiDungHoi = noiDungHoi;
    }

    public void setNoiDungTraLoi(String noiDungTraLoi) {
        this.noiDungTraLoi = noiDungTraLoi;
    }

    public void setThoiGianHoi(LocalDateTime thoiGianHoi) {
        this.thoiGianHoi = thoiGianHoi;
    }

    public void setThoiGianTraLoi(LocalDateTime thoiGianTraLoi) {
        this.thoiGianTraLoi = thoiGianTraLoi;
    }

    public void setTinhTrangCauHoi(Enums.TinhTrangCauHoi tinhTrangCauHoi) {
        this.tinhTrangCauHoi = tinhTrangCauHoi;
    }
}