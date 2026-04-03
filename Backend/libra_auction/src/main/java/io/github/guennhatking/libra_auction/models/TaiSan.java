package io.github.guennhatking.libra_auction.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Entity
public class TaiSan {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne(mappedBy = "taiSan")
    private ThongTinPhienDauGia thongTinPhienDauGia;

    @ManyToOne
    private DanhMuc danhMuc;

    private String tenTaiSan;
    private int soLuong;
    private String moTa;

    // CONSTRUCTOR
    public TaiSan() {
    }

    public TaiSan(String tenTaiSan, int soLuong, String moTa, DanhMuc danhMuc) {
        this.tenTaiSan = tenTaiSan;
        this.soLuong = soLuong;
        this.moTa = moTa;
        this.danhMuc = danhMuc;
    }

    // GETTER
    public String getId() {
        return id;
    }

    public ThongTinPhienDauGia getThongTinPhienDauGia() {
        return thongTinPhienDauGia;
    }

    public DanhMuc getDanhMuc() {
        return danhMuc;
    }

    public String getTenTaiSan() {
        return tenTaiSan;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public String getMoTa() {
        return moTa;
    }

    // SETTER
    public void setId(String id) {
        this.id = id;
    }

    public void setThongTinPhienDauGia(ThongTinPhienDauGia thongTinPhienDauGia) {
        this.thongTinPhienDauGia = thongTinPhienDauGia;
    }

    public void setDanhMuc(DanhMuc danhMuc) {
        this.danhMuc = danhMuc;
    }

    public void setTenTaiSan(String tenTaiSan) {
        this.tenTaiSan = tenTaiSan;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }
}
